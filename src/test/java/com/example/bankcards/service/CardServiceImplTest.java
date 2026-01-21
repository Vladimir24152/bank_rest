package com.example.bankcards.service;

import com.example.bankcards.dto.request.CardTransferRequest;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.request.UpdateCardRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.CardTransferResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.exception.EntityAlreadyExistsException;
import com.example.bankcards.mapper.CardMapper;
import com.example.bankcards.mapper.TransferMapper;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.TransferRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.User;
import com.example.bankcards.service.impl.CardServiceImpl;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static com.example.bankcards.entity.enums.CardStatus.ACTIVE;
import static com.example.bankcards.entity.enums.CardStatus.EXPIRED;
import static com.example.bankcards.entity.enums.CardStatus.REQUEST_FOR_BLOCKING;
import static com.example.bankcards.entity.enums.Role.ROLE_ADMIN;
import static com.example.bankcards.entity.enums.Role.ROLE_USER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private CardMapper cardMapper;

    @Mock
    private TransferMapper transferMapper;

    @Mock
    private CardEncryptionService cardEncryptionService;

    @Mock
    private UserService userService;

    @InjectMocks
    private CardServiceImpl cardService;

    private Card testCard;
    private User testUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        testCard = Card.builder()
                .id(1L)
                .encryptedCardNumber("encrypted1234567890123456")
                .lastFourDigits("3456")
                .clientId(1L)
                .expirationDate(LocalDate.now().plusYears(2))
                .status(ACTIVE)
                .balance(new BigDecimal("1000.00"))
                .build();

        testUser = User.builder()
                .id(1L)
                .username("testUser")
                .password("password")
                .role(ROLE_USER)
                .build();

        adminUser = User.builder()
                .id(2L)
                .username("admin")
                .password("password")
                .role(ROLE_ADMIN)
                .build();
    }

    @Test
    @DisplayName("Создание карты для существующего пользователя - успешное создание")
    void whenCreateCardForExistingUserThenSuccess() {
        CreateCardRequest request = CreateCardRequest.builder()
                .cardNumber("1234567890123456")
                .clientId(1L)
                .expirationDate(LocalDate.now().plusYears(2))
                .balance(new BigDecimal("500.00"))
                .build();

        CardResponse expectedResponse = CardResponse.builder()
                .id(1L)
                .clientId(1L)
                .status(ACTIVE)
                .balance(new BigDecimal("500.00"))
                .build();

        when(cardEncryptionService.encrypt("1234567890123456")).thenReturn("encrypted1234567890123456");
        when(cardRepository.existsByEncryptedCardNumber("encrypted1234567890123456")).thenReturn(false);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(cardRepository.save(any(Card.class))).thenReturn(testCard);
        when(cardMapper.mapToResponseDTO(testCard)).thenReturn(expectedResponse);

        CardResponse result = cardService.createCard(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(cardRepository).existsByEncryptedCardNumber("encrypted1234567890123456");
        verify(userRepository).existsById(1L);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("Создание карты с существующим номером - выбрасывается исключение")
    void whenCreateCardWithExistingNumberThenThrowException() {
        CreateCardRequest request = CreateCardRequest.builder()
                .cardNumber("1234567890123456")
                .clientId(1L)
                .expirationDate(LocalDate.now().plusYears(2))
                .balance(new BigDecimal("500.00"))
                .build();

        when(cardEncryptionService.encrypt("1234567890123456")).thenReturn("encrypted1234567890123456");
        when(cardRepository.existsByEncryptedCardNumber("encrypted1234567890123456")).thenReturn(true);

        EntityAlreadyExistsException exception = assertThrows(
                EntityAlreadyExistsException.class,
                () -> cardService.createCard(request)
        );

        assertTrue(exception.getMessage().contains("уже существует"));
        verify(cardRepository).existsByEncryptedCardNumber("encrypted1234567890123456");
        verify(userRepository, never()).existsById(any());
    }

    @Test
    @DisplayName("Создание карты для несуществующего пользователя - выбрасывается исключение")
    void whenCreateCardForNonExistingUserThenThrowException() {
        CreateCardRequest request = CreateCardRequest.builder()
                .cardNumber("1234567890123456")
                .clientId(999L)
                .expirationDate(LocalDate.now().plusYears(2))
                .balance(new BigDecimal("500.00"))
                .build();

        when(cardEncryptionService.encrypt("1234567890123456")).thenReturn("encrypted1234567890123456");
        when(cardRepository.existsByEncryptedCardNumber("encrypted1234567890123456")).thenReturn(false);
        when(userRepository.existsById(999L)).thenReturn(false);

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> cardService.createCard(request)
        );

        assertTrue(exception.getMessage().contains("не существует"));
        verify(userRepository).existsById(999L);
        verify(cardRepository, never()).save(any());
    }

    @Test
    @DisplayName("Получение карты текущим пользователем - успешное возвращение")
    void whenGetCardByCardOwnerThenSuccess() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(userRepository.existsById(1L)).thenReturn(true);

        CardResponse expectedResponse = CardResponse.builder()
                .id(1L)
                .clientId(1L)
                .status(ACTIVE)
                .build();

        when(cardMapper.mapToResponseDTO(testCard)).thenReturn(expectedResponse);

        CardResponse result = cardService.getCard(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getClientId());
        verify(cardRepository).findById(1L);
        verify(userService).getCurrentUser();
    }

    @Test
    @DisplayName("Получение карты администратором - успешное возвращение")
    void whenGetCardByAdminThenSuccess() {
        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(userService.getCurrentUser()).thenReturn(adminUser);
        when(userRepository.existsById(1L)).thenReturn(true);

        CardResponse expectedResponse = CardResponse.builder()
                .id(1L)
                .clientId(1L)
                .status(ACTIVE)
                .build();

        when(cardMapper.mapToResponseDTO(testCard)).thenReturn(expectedResponse);

        CardResponse result = cardService.getCard(1L);

        assertNotNull(result);
        verify(userService).getCurrentUser();
    }

    @Test
    @DisplayName("Получение несуществующей карты - выбрасывается исключение")
    void whenGetNonExistingCardThenThrowException() {
        when(cardRepository.findById(999L)).thenReturn(Optional.empty());

        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> cardService.getCard(999L)
        );

        assertTrue(exception.getMessage().contains("отсутствует"));
        verify(cardRepository).findById(999L);
        verify(userService, never()).getCurrentUser();
    }

    @Test
    @DisplayName("Перевод средств между картами - успешное выполнение")
    void whenTransferBetweenActiveCardsThenSuccess() {
        Card fromCard = Card.builder()
                .id(1L)
                .clientId(1L)
                .status(ACTIVE)
                .balance(new BigDecimal("1000.00"))
                .build();

        Card toCard = Card.builder()
                .id(2L)
                .clientId(1L)
                .status(ACTIVE)
                .balance(new BigDecimal("500.00"))
                .build();

        CardTransferRequest request = CardTransferRequest.builder()
                .fromCardId(1L)
                .toCardId(2L)
                .amount(new BigDecimal("200.00"))
                .description("Тестовый перевод")
                .build();

        Transfer transfer = Transfer.builder()
                .id(1L)
                .fromCardId(1L)
                .toCardId(2L)
                .amount(new BigDecimal("200.00"))
                .build();

        CardTransferResponse expectedResponse = CardTransferResponse.builder()
                .transactionId(1L)
                .fromCardId(1L)
                .toCardId(2L)
                .amount(new BigDecimal("200.00"))
                .build();

        when(cardRepository.findByIdWithLock(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdWithLock(2L)).thenReturn(Optional.of(toCard));
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(transferRepository.save(any(Transfer.class))).thenReturn(transfer);
        when(transferMapper.toCardTransferResponse(transfer)).thenReturn(expectedResponse);

        CardTransferResponse result = cardService.transferBetweenCards(request);

        assertNotNull(result);
        assertEquals(1L, result.getTransactionId());
        assertEquals(1L, result.getFromCardId());
        assertEquals(2L, result.getToCardId());
        verify(cardRepository).findByIdWithLock(1L);
        verify(cardRepository).findByIdWithLock(2L);
        verify(transferRepository).save(any(Transfer.class));
    }

    @Test
    @DisplayName("Перевод средств с недостаточным балансом - выбрасывается исключение")
    void whenTransferWithInsufficientBalanceThenThrowException() {
        Card fromCard = Card.builder()
                .id(1L)
                .clientId(1L)
                .status(ACTIVE)
                .balance(new BigDecimal("100.00"))
                .build();

        Card toCard = Card.builder()
                .id(2L)
                .clientId(1L)
                .status(ACTIVE)
                .balance(new BigDecimal("500.00"))
                .build();

        CardTransferRequest request = CardTransferRequest.builder()
                .fromCardId(1L)
                .toCardId(2L)
                .amount(new BigDecimal("200.00"))
                .description("Тестовый перевод")
                .build();

        when(cardRepository.findByIdWithLock(1L)).thenReturn(Optional.of(fromCard));
        when(cardRepository.findByIdWithLock(2L)).thenReturn(Optional.of(toCard));
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(userRepository.existsById(1L)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cardService.transferBetweenCards(request)
        );

        assertTrue(exception.getMessage().contains("недостаточно средств"));
    }

    @Test
    @DisplayName("Обновление баланса карты администратором - успешное обновление")
    void whenUpdateCardBalanceByAdminThenSuccess() {
        UpdateCardRequest request = UpdateCardRequest.builder()
                .balance(new BigDecimal("1500.00"))
                .build();

        Card updatedCard = Card.builder()
                .id(1L)
                .clientId(1L)
                .balance(new BigDecimal("1500.00"))
                .build();

        CardResponse expectedResponse = CardResponse.builder()
                .id(1L)
                .balance(new BigDecimal("1500.00"))
                .build();

        when(cardRepository.findById(1L)).thenReturn(Optional.of(testCard));
        when(cardRepository.save(any(Card.class))).thenReturn(updatedCard);
        when(cardMapper.mapToResponseDTO(updatedCard)).thenReturn(expectedResponse);

        CardResponse result = cardService.updateCard(1L, request);

        assertNotNull(result);
        assertEquals(new BigDecimal("1500.00"), result.getBalance());
        verify(cardRepository).findById(1L);
        verify(cardRepository).save(any(Card.class));
    }

    @Test
    @DisplayName("Получение всех карт пользователя - успешное возвращение")
    void whenGetAllCardsByUserIdThenReturnPaginatedResults() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Card> cardPage = new PageImpl<>(List.of(testCard));
        Page<CardResponse> responsePage = new PageImpl<>(List.of(
                CardResponse.builder().id(1L).build()
        ));

        when(userService.getCurrentUser()).thenReturn(testUser);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(cardRepository.findAllByClientId(1L, pageRequest)).thenReturn(cardPage);
        when(cardMapper.mapToResponseDTO(testCard)).thenReturn(CardResponse.builder().id(1L).build());

        Page<CardResponse> result = cardService.getAllCardsByUserId(1L, pageRequest);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(cardRepository).findAllByClientId(1L, pageRequest);
    }

    @Test
    @DisplayName("Запрос на блокировку активной карты - успешное выполнение")
    void whenBlockActiveCardThenSuccess() {
        Card activeCard = Card.builder()
                .id(1L)
                .clientId(1L)
                .status(ACTIVE)
                .build();

        Card blockedCard = Card.builder()
                .id(1L)
                .clientId(1L)
                .status(REQUEST_FOR_BLOCKING)
                .build();

        CardResponse expectedResponse = CardResponse.builder()
                .id(1L)
                .status(REQUEST_FOR_BLOCKING)
                .build();

        when(cardRepository.findById(1L)).thenReturn(Optional.of(activeCard));
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(userRepository.existsById(1L)).thenReturn(true);
        when(cardRepository.save(any(Card.class))).thenReturn(blockedCard);
        when(cardMapper.mapToResponseDTO(blockedCard)).thenReturn(expectedResponse);

        CardResponse result = cardService.blockedCard(1L);

        assertNotNull(result);
        assertEquals(REQUEST_FOR_BLOCKING, result.getStatus());
        verify(cardRepository).save(argThat(card ->
                card.getStatus() == REQUEST_FOR_BLOCKING
        ));
    }

    @Test
    @DisplayName("Запрос на блокировку неактивной карты - выбрасывается исключение")
    void whenBlockNonActiveCardThenThrowException() {
        Card expiredCard = Card.builder()
                .id(1L)
                .clientId(1L)
                .status(EXPIRED)
                .build();

        when(cardRepository.findById(1L)).thenReturn(Optional.of(expiredCard));
        when(userService.getCurrentUser()).thenReturn(testUser);
        when(userRepository.existsById(1L)).thenReturn(true);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cardService.blockedCard(1L)
        );

        assertTrue(exception.getMessage().contains("активной карте"));
    }
}