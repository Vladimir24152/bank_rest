package com.example.bankcards.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardTransferResponse {

    private Long transactionId;
    private Long fromCardId;
    private Long toCardId;
    private BigDecimal amount;
    private BigDecimal fromCardNewBalance;
    private BigDecimal toCardNewBalance;
    private String description;
    private LocalDateTime timestamp;
}
