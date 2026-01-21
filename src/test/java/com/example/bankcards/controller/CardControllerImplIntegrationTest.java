package com.example.bankcards.controller;

import com.example.bankcards.BaseIntegrationTest;
import com.example.bankcards.constant.ApiConstant;
import com.example.bankcards.dto.request.CardTransferRequest;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.request.UpdateCardRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.entity.enums.CardStatus;
import com.example.bankcards.repository.CardRepository;
import com.example.bankcards.repository.UserRepository;
import com.example.bankcards.security.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CardControllerImplIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private UserRepository userRepository;

    private static final String CARD_BASE_URL = ApiConstant.CARD_BASE_URL;

    private User testUser;
    private User testAdmin;

    @BeforeEach
    void setUp() {
        cardRepository.deleteAll();
        userRepository.deleteAll();

        testUser = User.builder()
                .username("testuser")
                .password("password123")
                .role(com.example.bankcards.entity.enums.Role.ROLE_USER)
                .build();
        testUser = userRepository.save(testUser);

        testAdmin = User.builder()
                .username("admin")
                .password("admin123")
                .role(com.example.bankcards.entity.enums.Role.ROLE_ADMIN)
                .build();
        testAdmin = userRepository.save(testAdmin);
    }

    @Test
    @DisplayName("Создание карты администратором - успешное создание")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void whenAdminCreatesCardThenCardIsCreated() throws Exception {
        CreateCardRequest request = CreateCardRequest.builder()
                .cardNumber("1111222233334444")
                .clientId(testUser.getId())
                .expirationDate(LocalDate.now().plusYears(2))
                .balance(BigDecimal.valueOf(1000))
                .build();

        mockMvc.perform(post(CARD_BASE_URL + ApiConstant.CREATE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.clientId").value(testUser.getId()))
                .andExpect(jsonPath("$.balance").value(1000))
                .andExpect(jsonPath("$.status").value("ACTIVE"));
    }

    @Test
    @DisplayName("Создание карты обычным пользователем - возвращает ошибку 403")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void whenUserCreatesCardThenReturnForbidden() throws Exception {
        CreateCardRequest request = CreateCardRequest.builder()
                .cardNumber("1111222233334444")
                .clientId(testUser.getId())
                .expirationDate(LocalDate.now().plusYears(2))
                .balance(BigDecimal.valueOf(1000))
                .build();

        mockMvc.perform(post(CARD_BASE_URL + ApiConstant.CREATE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Получение карты по ID владельцем - успешное получение")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void whenUserGetsOwnCardThenReturnCard() throws Exception {
        Card card = Card.builder()
                .encryptedCardNumber("encrypted123")
                .lastFourDigits("4444")
                .clientId(testUser.getId())
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1500))
                .build();
        Card savedCard = cardRepository.save(card);

        mockMvc.perform(get(CARD_BASE_URL + "/{cardId}", savedCard.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedCard.getId()))
                .andExpect(jsonPath("$.clientId").value(testUser.getId()))
                .andExpect(jsonPath("$.balance").value(1500));
    }

    @Test
    @DisplayName("Получение карты по ID администратором - успешное получение")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void whenAdminGetsAnyCardThenReturnCard() throws Exception {
        Card card = Card.builder()
                .encryptedCardNumber("encrypted123")
                .lastFourDigits("4444")
                .clientId(testUser.getId())
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1500))
                .build();
        Card savedCard = cardRepository.save(card);

        mockMvc.perform(get(CARD_BASE_URL + "/{cardId}", savedCard.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedCard.getId()));
    }

    @Test
    @DisplayName("Получение баланса своей карты - успешное получение")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void whenUserGetsOwnCardBalanceThenReturnBalance() throws Exception {
        Card card = Card.builder()
                .encryptedCardNumber("encrypted123")
                .lastFourDigits("4444")
                .clientId(testUser.getId())
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1500))
                .build();
        Card savedCard = cardRepository.save(card);

        mockMvc.perform(get(CARD_BASE_URL + ApiConstant.BALANCE + "/{cardId}", savedCard.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1500));
    }

    @Test
    @DisplayName("Получение баланса чужой карты пользователем - возвращает ошибку 403")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void whenUserGetsAnotherUserCardBalanceThenReturnForbidden() throws Exception {
        Card card = Card.builder()
                .encryptedCardNumber("encrypted123")
                .lastFourDigits("4444")
                .clientId(testAdmin.getId())
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1500))
                .build();
        Card savedCard = cardRepository.save(card);

        mockMvc.perform(get(CARD_BASE_URL + ApiConstant.BALANCE + "/{cardId}", savedCard.getId()))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Получение баланса карты администратором - успешное получение")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void whenAdminGetsAnyCardBalanceThenReturnBalance() throws Exception {
        Card card = Card.builder()
                .encryptedCardNumber("encrypted123")
                .lastFourDigits("4444")
                .clientId(testUser.getId())
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1500))
                .build();
        Card savedCard = cardRepository.save(card);

        mockMvc.perform(get(CARD_BASE_URL + ApiConstant.BALANCE + "/" + savedCard.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").value(1500));
    }

    @Test
    @DisplayName("Получение всех карт администратором - успешное получение")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void whenAdminGetsAllCardsThenReturnPage() throws Exception {
        Card card1 = Card.builder()
                .encryptedCardNumber("encrypted1111")
                .lastFourDigits("1111")
                .clientId(testUser.getId())
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000))
                .build();
        cardRepository.save(card1);

        Card card2 = Card.builder()
                .encryptedCardNumber("encrypted2222")
                .lastFourDigits("2222")
                .clientId(testAdmin.getId())
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(2000))
                .build();
        cardRepository.save(card2);

        mockMvc.perform(get(CARD_BASE_URL + ApiConstant.GET_ALL)
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].clientId").value(testUser.getId()))
                .andExpect(jsonPath("$.content[0].balance").value(1000))
                .andExpect(jsonPath("$.content[1].clientId").value(testAdmin.getId()))
                .andExpect(jsonPath("$.content[1].balance").value(2000));
    }

    @Test
    @DisplayName("Получение всех карт пользователя - успешное получение")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void whenUserGetsOwnCardsThenReturnPage() throws Exception {
        Card card1 = Card.builder()
                .encryptedCardNumber("encrypted1111")
                .lastFourDigits("1111")
                .clientId(testUser.getId())
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1000))
                .build();
        cardRepository.save(card1);

        Card card2 = Card.builder()
                .encryptedCardNumber("encrypted2222")
                .lastFourDigits("2222")
                .clientId(testUser.getId())
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(2000))
                .build();
        cardRepository.save(card2);

        mockMvc.perform(get(CARD_BASE_URL + ApiConstant.GET_ALL + "/user/{userId}", testUser.getId())
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].clientId").value(testUser.getId()))
                .andExpect(jsonPath("$.content[0].balance").value(1000))
                .andExpect(jsonPath("$.content[1].clientId").value(testUser.getId()))
                .andExpect(jsonPath("$.content[1].balance").value(2000));
    }

    @Test
    @DisplayName("Получение карт по последним 4 цифрам - успешное получение")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void whenAdminGetsCardsByLastFourDigitsThenReturnPage() throws Exception {
        Card card = Card.builder()
                .encryptedCardNumber("encrypted123")
                .lastFourDigits("4444")
                .clientId(testUser.getId())
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1500))
                .build();
        cardRepository.save(card);

        MvcResult result = mockMvc.perform(get(CARD_BASE_URL + ApiConstant.GET_ALL + "/{cardNumber}", "4444")
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].balance").value(1500))
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"))
                .andReturn();
    }

    @Test
    @DisplayName("Обновление карты администратором - успешное обновление")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void whenAdminUpdatesCardThenCardIsUpdated() throws Exception {
        Card card = Card.builder()
                .encryptedCardNumber("encrypted123")
                .lastFourDigits("4444")
                .clientId(testUser.getId())
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1500))
                .build();
        Card savedCard = cardRepository.save(card);

        UpdateCardRequest request = UpdateCardRequest.builder()
                .status(CardStatus.REQUEST_FOR_BLOCKING)
                .balance(BigDecimal.valueOf(2000))
                .build();

        mockMvc.perform(post(CARD_BASE_URL + ApiConstant.UPDATE + "/{cardId}", savedCard.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REQUEST_FOR_BLOCKING"))
                .andExpect(jsonPath("$.balance").value(2000));
    }

    @Test
    @DisplayName("Запрос на блокировку своей карты - успешный запрос")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void whenUserRequestsBlockOwnCardThenSuccess() throws Exception {
        Card card = Card.builder()
                .encryptedCardNumber("encrypted123")
                .lastFourDigits("4444")
                .clientId(testUser.getId())
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(1500))
                .build();
        Card savedCard = cardRepository.save(card);

        mockMvc.perform(post(CARD_BASE_URL + ApiConstant.BLOCKED + "/{cardId}", savedCard.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REQUEST_FOR_BLOCKING"));
    }

    @Test
    @DisplayName("Перевод между своими картами - успешный перевод")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void whenUserTransfersBetweenOwnCardsThenTransferSuccess() throws Exception {
        Card fromCard = Card.builder()
                .encryptedCardNumber("encrypted1111")
                .lastFourDigits("1111")
                .clientId(testUser.getId())
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(2000))
                .build();
        Card savedFromCard = cardRepository.save(fromCard);

        Card toCard = Card.builder()
                .encryptedCardNumber("encrypted2222")
                .lastFourDigits("2222")
                .clientId(testUser.getId())
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(500))
                .build();
        Card savedToCard = cardRepository.save(toCard);

        CardTransferRequest request = CardTransferRequest.builder()
                .fromCardId(savedFromCard.getId())
                .toCardId(savedToCard.getId())
                .amount(BigDecimal.valueOf(300))
                .description("Тестовый перевод")
                .build();

        mockMvc.perform(post(CARD_BASE_URL + ApiConstant.TRANSFER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fromCardId").value(savedFromCard.getId()))
                .andExpect(jsonPath("$.toCardId").value(savedToCard.getId()))
                .andExpect(jsonPath("$.amount").value(300))
                .andExpect(jsonPath("$.fromCardNewBalance").value(1700))
                .andExpect(jsonPath("$.toCardNewBalance").value(800));
    }

    @Test
    @DisplayName("Перевод с недостаточным балансом - возвращает ошибку")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void whenUserTransfersWithInsufficientBalanceThenReturnError() throws Exception {
        Card fromCard = Card.builder()
                .encryptedCardNumber("encrypted1111")
                .lastFourDigits("1111")
                .clientId(testUser.getId())
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(100))
                .build();
        Card savedFromCard = cardRepository.save(fromCard);

        Card toCard = Card.builder()
                .encryptedCardNumber("encrypted2222")
                .lastFourDigits("2222")
                .clientId(testUser.getId())
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(500))
                .build();
        Card savedToCard = cardRepository.save(toCard);

        CardTransferRequest request = CardTransferRequest.builder()
                .fromCardId(savedFromCard.getId())
                .toCardId(savedToCard.getId())
                .amount(BigDecimal.valueOf(200))
                .description("Перевод с недостатком")
                .build();

        mockMvc.perform(post(CARD_BASE_URL + ApiConstant.TRANSFER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Перевод с заблокированной карты - возвращает ошибку")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void whenUserTransfersFromBlockedCardThenReturnError() throws Exception {
        Card fromCard = Card.builder()
                .encryptedCardNumber("encrypted1111")
                .lastFourDigits("1111")
                .clientId(testUser.getId())
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.REQUEST_FOR_BLOCKING)
                .balance(BigDecimal.valueOf(2000))
                .build();
        Card savedFromCard = cardRepository.save(fromCard);

        Card toCard = Card.builder()
                .encryptedCardNumber("encrypted2222")
                .lastFourDigits("2222")
                .clientId(testUser.getId())
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(500))
                .build();
        Card savedToCard = cardRepository.save(toCard);

        CardTransferRequest request = CardTransferRequest.builder()
                .fromCardId(savedFromCard.getId())
                .toCardId(savedToCard.getId())
                .amount(BigDecimal.valueOf(300))
                .description("Перевод с заблокированной карты")
                .build();

        mockMvc.perform(post(CARD_BASE_URL + ApiConstant.TRANSFER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Перевод на заблокированную карту - возвращает ошибку")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void whenUserTransfersToBlockedCardThenReturnError() throws Exception {
        Card fromCard = Card.builder()
                .encryptedCardNumber("encrypted1111")
                .lastFourDigits("1111")
                .clientId(testUser.getId())
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(2000))
                .build();
        Card savedFromCard = cardRepository.save(fromCard);

        Card toCard = Card.builder()
                .encryptedCardNumber("encrypted2222")
                .lastFourDigits("2222")
                .clientId(testUser.getId())
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.REQUEST_FOR_BLOCKING)
                .balance(BigDecimal.valueOf(500))
                .build();
        Card savedToCard = cardRepository.save(toCard);

        CardTransferRequest request = CardTransferRequest.builder()
                .fromCardId(savedFromCard.getId())
                .toCardId(savedToCard.getId())
                .amount(BigDecimal.valueOf(300))
                .description("Перевод на заблокированную карту")
                .build();

        mockMvc.perform(post(CARD_BASE_URL + ApiConstant.TRANSFER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Перевод между одинаковыми картами - возвращает ошибку")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void whenUserTransfersBetweenSameCardsThenReturnError() throws Exception {
        Card card = Card.builder()
                .encryptedCardNumber("encrypted1111")
                .lastFourDigits("1111")
                .clientId(testUser.getId())
                .expirationDate(LocalDate.now().plusYears(2))
                .status(CardStatus.ACTIVE)
                .balance(BigDecimal.valueOf(2000))
                .build();
        Card savedCard = cardRepository.save(card);

        CardTransferRequest request = CardTransferRequest.builder()
                .fromCardId(savedCard.getId())
                .toCardId(savedCard.getId())
                .amount(BigDecimal.valueOf(300))
                .description("Перевод на ту же карту")
                .build();

        mockMvc.perform(post(CARD_BASE_URL + ApiConstant.TRANSFER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Создание карты с истекшим сроком действия - создается с статусом EXPIRED")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void whenAdminCreatesExpiredCardThenCardCreatedWithExpiredStatus() throws Exception {
        CreateCardRequest request = CreateCardRequest.builder()
                .cardNumber("1111222233334444")
                .clientId(testUser.getId())
                .expirationDate(LocalDate.now().minusDays(1))
                .balance(BigDecimal.valueOf(1000))
                .build();

        mockMvc.perform(post(CARD_BASE_URL + ApiConstant.CREATE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("EXPIRED"));
    }

    @Test
    @DisplayName("Создание карты с некорректными данными - возвращает ошибку валидации")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void whenAdminCreatesCardWithInvalidDataThenReturnBadRequest() throws Exception {
        CreateCardRequest request = CreateCardRequest.builder()
                .cardNumber("123") // некорректный номер
                .clientId(testUser.getId())
                .expirationDate(LocalDate.now().plusYears(2))
                .balance(BigDecimal.valueOf(-100)) // отрицательный баланс
                .build();

        mockMvc.perform(post(CARD_BASE_URL + ApiConstant.CREATE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Перевод с некорректными данными - возвращает ошибку валидации")
    @WithMockUser(username = "testuser", roles = {"USER"})
    void whenUserTransfersWithInvalidDataThenReturnBadRequest() throws Exception {
        CardTransferRequest request = CardTransferRequest.builder()
                .fromCardId(1L)
                .toCardId(2L)
                .amount(BigDecimal.valueOf(0)) // неверная сумма
                .description("Перевод с нулевой суммой")
                .build();

        mockMvc.perform(post(CARD_BASE_URL + ApiConstant.TRANSFER)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}