package com.example.bankcards.service.impl;

import com.example.bankcards.exception.CardEncryptionException;
import com.example.bankcards.service.CardEncryptionService;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
public class CardEncryptionServiceImpl implements CardEncryptionService {
    @Value("${card.encryption.algorithm}")
    private String algorithm;

    @Value("${card.encryption.transformation}")
    private String transformation;

    @Value("${card.encryption.key}")
    private String encryptionKey;

    @PostConstruct
    public void init() {
        log.info("Encryption key length: {} bytes", encryptionKey.getBytes(StandardCharsets.UTF_8).length);
        log.info("Algorithm: {}, Transformation: {}", algorithm, transformation);

        byte[] keyBytes = encryptionKey.getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length != 16 && keyBytes.length != 24 && keyBytes.length != 32) {
            throw new IllegalStateException(
                    String.format("Неверная длина ключа: %d байт. Для AES требуется 16, 24 или 32 байта", keyBytes.length)
            );
        }
    }

    @Override
    public String encrypt(String cardNumber){
        try {
            SecretKeySpec secretKey = new SecretKeySpec(
                    encryptionKey.getBytes(StandardCharsets.UTF_8),
                    algorithm
            );

            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] encryptedBytes = cipher.doFinal(cardNumber.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            throw new CardEncryptionException("Проблема с шифрованием номера карты.");
        }
    }

    @Override
    public String decrypt(String encryptedCardNumber){
        try {
            SecretKeySpec secretKey = new SecretKeySpec(
                    encryptionKey.getBytes(StandardCharsets.UTF_8),
                    algorithm
            );

            Cipher cipher = Cipher.getInstance(transformation);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            byte[] decodedBytes = Base64.getDecoder().decode(encryptedCardNumber);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CardEncryptionException("Проблема с расшифровкой номера карты.");
        }
    }
}
