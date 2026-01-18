package com.example.bankcards.controller;

import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.response.CardResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import static com.example.bankcards.constant.ApiConstant.CARD_BASE_URL;
import static com.example.bankcards.constant.ApiConstant.CREATE;

public interface CardController {

    @Operation(summary = "Создание карты")
    ResponseEntity<CardResponse> createCard(@Valid @RequestBody CreateCardRequest createCardRequest);
}
