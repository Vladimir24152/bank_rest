package com.example.bankcards.service;

public interface CardEncryptionService {

    String encrypt(String cardNumber);

    String decrypt(String encryptedCardNumber);
}
