package com.example.bankcards.mapper;

import com.example.bankcards.dto.response.CardTransferResponse;
import com.example.bankcards.entity.Transfer;

public interface TransferMapper {
    CardTransferResponse toCardTransferResponse(Transfer transfer);
}
