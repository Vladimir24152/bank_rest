package com.example.bankcards.dto.request;

import com.example.bankcards.entity.enums.CardStatus;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class UpdateCardRequest {

    private CardStatus status;

    @DecimalMin(value = "0.0", message = "Баланс не может быть отрицательным")
    private BigDecimal balance;
}
