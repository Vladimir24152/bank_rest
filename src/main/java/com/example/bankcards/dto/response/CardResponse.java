package com.example.bankcards.dto.response;

import com.example.bankcards.entity.enums.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
public class CardResponse {

    @Schema(description = "Уникальный идентификатор карты")
    private Long id;

    @Schema(description = "Замаскированный номер карты")
    private String encryptedCardNumber;

    @Schema(description = "Уникальный идентификатор пользователя карты")
    private Long clientId;

    @Schema(description = "Дата истечения срока действия")
    private LocalDate expirationDate;

    @Schema(description = "Статус карты(активный, блокированный, истекший")
    private CardStatus status;

    @Schema(description = "Баланс карты")
    private BigDecimal balance;
}
