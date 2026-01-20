package com.example.bankcards.controller.impl;

import com.example.bankcards.controller.CardController;
import com.example.bankcards.dto.request.CardTransferRequest;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.request.UpdateCardRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.CardTransferResponse;
import com.example.bankcards.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;


@RestController

@RequiredArgsConstructor
public class CardControllerImpl implements CardController {

    private final CardService cardService;

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> createCard(CreateCardRequest createCardRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cardService.createCard(createCardRequest));
    }

    @Override
    public ResponseEntity<CardResponse> getCard(Long cardId) {
        return ResponseEntity.ok(cardService.getCard(cardId));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<CardResponse> getAllCards(Integer page, Integer size) {
        return cardService.getAllCards(PageRequest.of(page, size));
    }

    @Override
    public Page<CardResponse> getAllCardsByUserId(Long userId, Integer page, Integer size) {
        return cardService.getAllCardsByUserId(userId, PageRequest.of(page, size));
    }

    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public Page<CardResponse> getAllCardsByCardNumber(String cardNumber, Integer page, Integer size) {
        return cardService.getAllCardsByLastFourDigits(cardNumber, PageRequest.of(page, size));
    }


    @Override
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CardResponse> updateCard(Long cardId, UpdateCardRequest updateCardRequest) {
        return ResponseEntity.ok(cardService.updateCard(cardId,updateCardRequest));
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CardResponse> blockedCard(Long cardId) {
        return ResponseEntity.ok(cardService.blockedCard(cardId));
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<BigDecimal> getCardBalance(Long cardId) {
        return ResponseEntity.ok(cardService.getCardBalance(cardId));
    }

    @Override
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<CardTransferResponse> transferBetweenCards(CardTransferRequest transferRequest) {
        return ResponseEntity.ok(cardService.transferBetweenCards(transferRequest));
    }
}
