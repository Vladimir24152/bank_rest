package com.example.bankcards.mapper.impl;

import com.example.bankcards.dto.response.CardTransferResponse;
import com.example.bankcards.entity.Transfer;
import com.example.bankcards.mapper.TransferMapper;
import org.springframework.stereotype.Service;

@Service
public class TransferMapperImpl implements TransferMapper {
    @Override
    public CardTransferResponse toCardTransferResponse(Transfer transfer) {
        return CardTransferResponse.builder()
                .transactionId(transfer.getId())
                .fromCardId(transfer.getFromCardId())
                .toCardId(transfer.getToCardId())
                .amount(transfer.getAmount())
                .fromCardNewBalance(transfer.getFromCardNewBalance())
                .toCardNewBalance(transfer.getToCardNewBalance())
                .description(transfer.getDescription())
                .timestamp(transfer.getCreatedAt())
                .build();
    }
}
