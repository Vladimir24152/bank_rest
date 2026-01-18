package com.example.bankcards.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class CreateCardRequest {

    @NotNull(message = "Номер карты обязателен")
    @Pattern(regexp = "^[0-9]{16}$", message = "Номер карты должен содержать 16 цифр")
    private String cardNumber;

    @NotNull(message = "Идентификатор пользователя обязателен")
    @Positive(message = "Идентификатор должен быть положительным числом")
    private Long clientId;

    @NotNull(message = "Время окончания действия карты обязательно")
    private LocalDate expirationDate;

    @NotNull(message = "Баланс обязателен")
    @DecimalMin(value = "0.0", message = "Баланс не может быть отрицательным")
    private BigDecimal balance;
}
