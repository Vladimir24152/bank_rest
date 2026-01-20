package com.example.bankcards.controller;

import com.example.bankcards.dto.request.CardTransferRequest;
import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.request.UpdateCardRequest;
import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.dto.response.CardTransferResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

import static com.example.bankcards.constant.ApiConstant.BALANCE;
import static com.example.bankcards.constant.ApiConstant.BLOCKED;
import static com.example.bankcards.constant.ApiConstant.CARD_BASE_URL;
import static com.example.bankcards.constant.ApiConstant.CARD_ID;
import static com.example.bankcards.constant.ApiConstant.CARD_NUMBER;
import static com.example.bankcards.constant.ApiConstant.CREATE;
import static com.example.bankcards.constant.ApiConstant.GET_ALL;
import static com.example.bankcards.constant.ApiConstant.TRANSFER;
import static com.example.bankcards.constant.ApiConstant.UPDATE;
import static com.example.bankcards.constant.ApiConstant.USER_ID;

@RequestMapping(CARD_BASE_URL)
public interface CardController {

    @PostMapping(CREATE)
    @Operation(summary = "Создание карты")
    ResponseEntity<CardResponse> createCard(@Valid @RequestBody CreateCardRequest createCardRequest);

    @GetMapping(CARD_ID)
    @Operation(summary = "Получение карты по Id")
    ResponseEntity<CardResponse> getCard(@PathVariable Long cardId);

    @GetMapping(GET_ALL)
    @Operation(summary = "Получение всех карт")
    Page<CardResponse> getAllCards(
            @RequestParam(defaultValue = "0") @Min(0) Integer  page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size
    );

    @GetMapping(GET_ALL + USER_ID)
    @Operation(summary = "Получение всех карт по id пользователя")
    Page<CardResponse> getAllCardsByUserId(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") @Min(0) Integer  page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size
            );

    @GetMapping(GET_ALL + CARD_NUMBER)
    @Operation(summary = "Получение всех карт по последним 4 цифрам")
    Page<CardResponse> getAllCardsByCardNumber(
            @PathVariable String cardNumber,
            @RequestParam(defaultValue = "0") @Min(0) Integer  page,
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) Integer size
    );

    @PostMapping(UPDATE + CARD_ID)
    @Operation(summary = "Обновление карты (изменение статуса или баланса карты)")
    ResponseEntity<CardResponse> updateCard(
            @PathVariable Long cardId,
            @Valid @RequestBody UpdateCardRequest updateCardRequest
    );

    @PostMapping(BLOCKED + CARD_ID)
    @Operation(summary = "Запрос на блокировку карты")
    ResponseEntity<CardResponse>  blockedCard(@PathVariable Long cardId);

    @GetMapping(BALANCE + CARD_ID)
    @Operation(summary = "Получение баланса карты по Id")
    ResponseEntity<BigDecimal> getCardBalance(@PathVariable Long cardId);

    @PostMapping(TRANSFER)
    @Operation(summary = "Перевод средств между картами")
    ResponseEntity<CardTransferResponse> transferBetweenCards(@Valid @RequestBody CardTransferRequest transferRequest);
}
