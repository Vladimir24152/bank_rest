package com.example.bankcards.dto.request;

import com.example.bankcards.entity.enums.CardStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
public class UpdateCardRequest {

    @Schema(description = "Статус карты", example = "ACTIVE")
    private CardStatus status;

    @Schema(description = "Баланс карты", example = "100.00")
    @DecimalMin(value = "0.0", message = "Баланс не может быть отрицательным")
    private BigDecimal balance;
}
