package com.example.bankcards.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
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

    @Schema(description = "Уникальный идентификатор транзакции")
    private Long transactionId;

    @Schema(description = "Уникальный идентификатор карты отправителя")
    private Long fromCardId;

    @Schema(description = "Уникальный идентификатор карты получателя")
    private Long toCardId;

    @Schema(description = "Сумма перевода")
    private BigDecimal amount;

    @Schema(description = "Баланс карты отправителя")
    private BigDecimal fromCardNewBalance;

    @Schema(description = "Баланс карты получателя")
    private BigDecimal toCardNewBalance;

    @Schema(description = "Сообщение при переводе")
    private String description;

    @Schema(description = "Время создания транзакции")
    private LocalDateTime timestamp;
}
