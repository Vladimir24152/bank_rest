package com.example.bankcards.service;

import com.example.bankcards.dto.request.CardTransferRequest;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.request.UpdateCardRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.CardTransferResponse;
import com.example.bankcards.entity.Card;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;

public interface CardService{

    CardResponse createCard(CreateCardRequest createCardRequest);

    Page<Card> getAllCards(PageRequest pageRequest);

    Page<Card> getAllCardsByUserId(Long userId, PageRequest pageRequest);

    Page<Card> getAllCardsByLastFourDigits(String cardNumber, PageRequest pageRequest);

    CardResponse updateCard(Long cardId, UpdateCardRequest request);

    BigDecimal getCardBalance(Long cardId);

    Card getCard(Long cardId);

    CardTransferResponse transferBetweenCards(CardTransferRequest transferRequest);

    Card blockedCard(Long cardId);
}
