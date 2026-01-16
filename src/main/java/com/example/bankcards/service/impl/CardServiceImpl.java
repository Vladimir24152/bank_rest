package com.example.bankcards.service.impl;

import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.service.CardEncryptionService;
import com.example.bankcards.service.CardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Slf4j
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardRepository cardRepository;

    private final CardMapper cardMapper;

    private final CardEncryptionService cardEncryptionService;

    @Override
    @Transactional
    public CardResponse createCard(CreateCardRequest request) {

        String encryptedCardNumber = cardEncryptionService.encrypt(request.getCardNumber());
        String lastFourDigits = extractLastFourDigits(request.getCardNumber());

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
        return cardMapper.mapToResponseDTO(savedCard);
    }

    private CardStatus determineCardStatus(LocalDate expirationDate) {
        if (expirationDate.isBefore(LocalDate.now())) {
            return CardStatus.EXPIRED;
        }
        return CardStatus.ACTIVE;
    }

    public String extractLastFourDigits(String cardNumber) {
        if (cardNumber == null || cardNumber.length() < 4) {
            throw new IllegalArgumentException("Номер карты должен содержать минимум 4 цифры");
        }
        return cardNumber.substring(cardNumber.length() - 4);
    }
}
