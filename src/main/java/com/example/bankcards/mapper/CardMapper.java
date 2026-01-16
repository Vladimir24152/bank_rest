package com.example.bankcards.mapper;


import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.entity.Card;

public interface CardMapper {
    CardResponse mapToResponseDTO(Card savedCard);
}
