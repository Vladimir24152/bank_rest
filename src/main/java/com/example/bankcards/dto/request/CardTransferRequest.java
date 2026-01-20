package com.example.bankcards.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CardTransferRequest {

    @Schema(description = "Уникальный идентификатор карты отправителя", example = "12")
    @Positive(message = "Идентификатор должен быть положительным числом")
    @NotNull(message = "ID карты отправителя обязателен")
    private Long fromCardId;

    @Schema(description = "Уникальный идентификатор карты получателя", example = "12")
    @Positive(message = "Идентификатор должен быть положительным числом")
    @NotNull(message = "ID карты получателя обязателен")
    private Long toCardId;

    @Schema(description = "Сумма перевода", example = "100.00")
    @NotNull(message = "Сумма перевода обязательна")
    @DecimalMin(value = "0.01", message = "Сумма перевода должна быть больше 0")
    private BigDecimal amount;

    private String description;
}
