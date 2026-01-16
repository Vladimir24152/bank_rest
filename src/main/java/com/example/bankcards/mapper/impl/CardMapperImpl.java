package com.example.bankcards.mapper.impl;

import com.example.bankcards.dto.response.CardResponse;
import com.example.bankcards.entity.Card;
import com.example.bankcards.mapper.CardMapper;
import org.springframework.stereotype.Service;

@Service
public class CardMapperImpl implements CardMapper {
    @Override
    public CardResponse mapToResponseDTO(Card savedCard) {
        return CardResponse.builder()
                .id(savedCard.getId())
                .encryptedCardNumber("**** **** **** " + savedCard.getLastFourDigits())
                .clientId(savedCard.getClientId())
                .status(savedCard.getStatus())
                .balance(savedCard.getBalance())
                .build();
    }
}
