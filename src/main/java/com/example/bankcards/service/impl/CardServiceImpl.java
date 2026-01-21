package com.example.bankcards.service.impl;

import com.example.bankcards.dto.request.CardTransferRequest;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.request.UpdateCardRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.CardTransferResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.exception.EntityAlreadyExistsException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.mapper.TransferMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.User;
import com.example.bankcards.service.UserService;
import com.example.bankcards.service.CardEncryptionService;
import com.example.bankcards.service.CardService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.example.bankcards.entity.enums.CardStatus.ACTIVE;
import static com.example.bankcards.entity.enums.CardStatus.EXPIRED;
import static com.example.bankcards.entity.enums.CardStatus.REQUEST_FOR_BLOCKING;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;

    private final UserRepository userRepository;

    private final TransferRepository transferRepository;

    private final CardMapper cardMapper;

    private final TransferMapper transferMapper;

    private final CardEncryptionService cardEncryptionService;

    private final UserService userService;

    @Override
    @Transactional
    public CardResponse createCard(CreateCardRequest request) {

        log.info("Создание карты для пользователя ID: {}", request.getClientId());

        String encryptedCardNumber = cardEncryptionService.encrypt(request.getCardNumber());
        String lastFourDigits = extractLastFourDigits(request.getCardNumber());

        if (cardRepository.existsByEncryptedCardNumber(encryptedCardNumber)) {
            throw new EntityAlreadyExistsException("Карта с номером **** **** **** " + lastFourDigits + " уже существует");
        }

        if (!userRepository.existsById(request.getClientId())) {
            throw new EntityNotFoundException("Пользователя с ID " + request.getClientId() + " не существует");
        }

        CardStatus status = determineCardStatus(request.getExpirationDate());

        Card card = Card.builder()
                .encryptedCardNumber(encryptedCardNumber)
                .lastFourDigits(lastFourDigits)
                .clientId(request.getClientId())
                .expirationDate(request.getExpirationDate())
                .status(status)
                .balance(request.getBalance())
                .build();

        Card savedCard = cardRepository.save(card);

        log.info("Карта успешно создана: ID {} для пользователя ID {}", savedCard.getId(), savedCard.getClientId());

        return cardMapper.mapToResponseDTO(savedCard);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardResponse> getAllCards(PageRequest pageRequest) {
        return cardRepository.findAll(pageRequest)
                .map(cardMapper::mapToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardResponse> getAllCardsByUserId(Long userId, PageRequest pageRequest) {
        verificationAccessRights(userId);

        return cardRepository.findAllByClientId(userId, pageRequest)
                .map(cardMapper::mapToResponseDTO);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CardResponse> getAllCardsByLastFourDigits(String cardNumber, PageRequest pageRequest) {
        return cardRepository.findAllByLastFourDigits(cardNumber, pageRequest)
                .map(cardMapper::mapToResponseDTO);
    }

    @Override
    @Transactional
    public CardResponse updateCard(Long cardId, UpdateCardRequest request) {

        log.debug("Обновление карты ID: {}", cardId);

        Card card = cardRepository.findById(cardId).orElseThrow(
                () -> new EntityNotFoundException("Карта с номером Id " + cardId + " уже отсутствует"));

        if (request.getStatus() != null && !card.getStatus().equals(EXPIRED) && !request.getStatus().equals(EXPIRED)){
            card.setStatus(request.getStatus());
        }

        if (request.getBalance() != null){
            card.setBalance(request.getBalance());
        }

        Card updatedCard = cardRepository.save(card);

        log.debug("Карта с ID: {} успешно обновлена", cardId);

        return cardMapper.mapToResponseDTO(updatedCard);
    }

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCardBalance(Long cardId) {
        Card card = cardRepository.findById(cardId).orElseThrow(
                () -> new EntityNotFoundException("Карта с номером Id " + cardId + " отсутствует"));

        verificationAccessRights(card.getClientId());

        return card.getBalance();
    }

    @Override
    @Transactional
    public CardResponse getCard(Long cardId) {
        log.info("Получение карты ID: {}", cardId);
        Card card = cardRepository.findById(cardId).orElseThrow(
                () -> new EntityNotFoundException("Карта с номером Id " + cardId + " отсутствует"));

        verificationAccessRights(card.getClientId());

        return cardMapper.mapToResponseDTO(card);
    }

    @Override
    @Transactional
    public CardTransferResponse transferBetweenCards(CardTransferRequest transferRequest) {

        Card fromCard = cardRepository.findByIdWithLock(transferRequest.getFromCardId()).orElseThrow(
                () -> new EntityNotFoundException("Карта с номером Id " + transferRequest.getFromCardId() + " отсутствует"));

        Card toCard = cardRepository.findByIdWithLock(transferRequest.getToCardId()).orElseThrow(
                () -> new EntityNotFoundException("Карта с номером Id " + transferRequest.getFromCardId() + " отсутствует"));

        BigDecimal amount = transferRequest.getAmount();

        checkValidationRequest(fromCard, toCard, amount);

        BigDecimal fromCardNewBalance = fromCard.getBalance().subtract(amount);
        BigDecimal toCardNewBalance = toCard.getBalance().add(amount);

        fromCard.setBalance(fromCardNewBalance);
        toCard.setBalance(toCardNewBalance);

        cardRepository.save(fromCard);
        cardRepository.save(toCard);

        Transfer savedTransfer = transferRepository.save(Transfer.builder()
                .fromCardId(fromCard.getId())
                .toCardId(toCard.getId())
                .amount(amount)
                .fromCardNewBalance(fromCard.getBalance())
                .toCardNewBalance(toCard.getBalance())
                .description(transferRequest.getDescription())
                .build()
        );


        return transferMapper.toCardTransferResponse(savedTransfer);
    }

    @Override
    @Transactional
    public CardResponse blockedCard(Long cardId) {
        Card card = cardRepository.findById(cardId).orElseThrow(
                () -> new EntityNotFoundException("Карта с номером Id " + cardId + " отсутствует"));

        verificationAccessRights(card.getClientId());

        if (!card.getStatus().equals(ACTIVE)){
            throw new IllegalArgumentException("Создать запрос на блокировку можно только по активной карте");
        }

        card.setStatus(REQUEST_FOR_BLOCKING);

        return cardMapper.mapToResponseDTO(cardRepository.save(card));
    }

    private void checkValidationRequest(Card fromCard, Card toCard, BigDecimal amount) {
        if (fromCard.equals(toCard)){
            throw new IllegalArgumentException("Неверно указаны Id карт");
        }

        verificationAccessRights(fromCard.getClientId());
        verificationAccessRights(toCard.getClientId());

        if (!fromCard.getStatus().equals(ACTIVE) || !toCard.getStatus().equals(ACTIVE)){
            throw new IllegalArgumentException("В переводе средств могут участвовать только карты с активным статусом");
        }

        if (fromCard.getBalance().compareTo(amount) < 0){
            throw new IllegalArgumentException("На карте ID " + fromCard.getId() + " недостаточно средств для перевода");
        }
    }

    private CardStatus determineCardStatus(LocalDate expirationDate) {
        if (expirationDate.isBefore(LocalDate.now())) {
            return EXPIRED;
        }
        return ACTIVE;
    }

    public String extractLastFourDigits(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            throw new IllegalArgumentException("Номер карты должен содержать минимум 4 цифры");
        }
        return cardNumber.substring(cardNumber.length() - 4);
    }

    private void verificationAccessRights(Long userId){
        User currentUser = userService.getCurrentUser();

        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Пользователя с ID " + userId + " не существует");
        }

        boolean isAdmin = currentUser.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        boolean isSameUser = currentUser.getId().equals(userId);

        if (!isAdmin && !isSameUser) {
            log.warn("Отказ в доступе: пользователь ID {} пытается получить доступ к данным пользователя ID {}",
                    currentUser.getId(), userId);
            throw new AccessDeniedException("У вас нет прав для взаимодействия с картой другого пользователя");
        }
    }
}
