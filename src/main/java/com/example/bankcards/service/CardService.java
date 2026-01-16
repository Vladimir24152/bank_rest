package com.example.bankcards.service;

import com.example.bankcards.dto.request.CreateCardRequest;
import com.example.bankcards.dto.response.CardResponse;

public interface CardService{

    CardResponse createCard(CreateCardRequest createCardRequest);
}
