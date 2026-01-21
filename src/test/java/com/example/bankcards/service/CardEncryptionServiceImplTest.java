package com.example.bankcards.service;

import com.example.bankcards.exception.CardEncryptionException;
import com.example.bankcards.service.impl.CardEncryptionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


@ExtendWith(MockitoExtension.class)
public class CardEncryptionServiceImplTest {

    private CardEncryptionServiceImpl cardEncryptionService;

    @BeforeEach
    void setUp() {
        cardEncryptionService = new CardEncryptionServiceImpl();

        ReflectionTestUtils.setField(cardEncryptionService, "algorithm", "AES");
        ReflectionTestUtils.setField(cardEncryptionService, "transformation", "AES/ECB/PKCS5Padding");
        ReflectionTestUtils.setField(cardEncryptionService, "encryptionKey", "1234567890123456");

        cardEncryptionService.init();
    }

    @Test
    @DisplayName("Шифрование и расшифровка корректного номера карты - успешное выполнение")
    void whenEncryptAndDecryptValidCardNumberThenSuccess() {
        String originalCardNumber = "4111111111111111";

        String encrypted = cardEncryptionService.encrypt(originalCardNumber);
        String decrypted = cardEncryptionService.decrypt(encrypted);

        assertNotNull(encrypted);
        assertNotEquals(originalCardNumber, encrypted);
        assertEquals(originalCardNumber, decrypted);

        assertDoesNotThrow(() -> java.util.Base64.getDecoder().decode(encrypted));
    }

    @Test
    @DisplayName("Шифрование null значения - выбрасывается исключение")
    void whenEncryptNullCardNumberThenThrowException() {
        CardEncryptionException exception = assertThrows(
                CardEncryptionException.class,
                () -> cardEncryptionService.encrypt(null)
        );

        assertTrue(exception.getMessage().contains("шифрованием"));
    }

    @Test
    @DisplayName("Инициализация сервиса с ключом неверной длины - выбрасывается исключение")
    void whenInitWithInvalidKeyLengthThenThrowIllegalStateException() {
        ReflectionTestUtils.setField(cardEncryptionService, "encryptionKey", "короткий-ключ");

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> cardEncryptionService.init()
        );

        assertTrue(exception.getMessage().contains("длина ключа"));
        assertTrue(exception.getMessage().contains("байт"));
    }
}
