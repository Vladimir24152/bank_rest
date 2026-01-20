package com.example.bankcards.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CreateCardRequest {

    @Schema(description = "Последние 4 цифры карты", example = "1111222233334444")
    @NotNull(message = "Номер карты обязателен")
    @Pattern(regexp = "^[0-9]{16}$", message = "Номер карты должен содержать 16 цифр")
    private String cardNumber;

    @Schema(description = "Уникальный идентификатор карты", example = "12")
    @NotNull(message = "Идентификатор пользователя обязателен")
    @Positive(message = "Идентификатор должен быть положительным числом")
    private Long clientId;

    @Schema(description = "Уникальный идентификатор карты", example = "2026-01-20")
    @NotNull(message = "Время окончания действия карты обязательно")
    private LocalDate expirationDate;

    @Schema(description = "Баланс карты", example = "100.00")
    @NotNull(message = "Баланс обязателен")
    @DecimalMin(value = "0.0", message = "Баланс не может быть отрицательным")
    private BigDecimal balance;
}
