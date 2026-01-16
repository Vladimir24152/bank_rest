package com.example.bankcards.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
public class CreateCardRequest {

    @NotNull(message = "Номер карты обязателен")
    @Size(max = 16, message = "Длинна не более 16 символов")
    private String cardNumber;

    @NotNull(message = "Идентификатор пользователя обязателен")
    @Positive(message = "Идентификатор должен быть положительным числом")
    private Long clientId;

    @NotNull(message = "Время окончания действия карты обязательно")
    private LocalDate expirationDate;

    private BigDecimal balance;
}
