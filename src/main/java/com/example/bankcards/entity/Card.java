package com.example.bankcards.entity;

import com.example.bankcards.entity.enums.CardStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cards")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Уникальный идентификатор карты")
    private Long id;

    @Comment("Зашифрованный номер карты")
    @Column(name = "encrypted_card_number", nullable = false, unique = true, length = 200)
    private String encryptedCardNumber;

    @Comment("Последние 4 цифры карты")
    @Column(name = "last_four_digits", nullable = false, length = 4)
    private String lastFourDigits;

    @Comment("Идентификатор владельца карты")
    @Column(name = "user_id", nullable = false)
    private Long clientId;

    @Comment("Дата окончания действия карты")
    @Column(name = "expiration_date", nullable = false)
    private LocalDate expirationDate;

    @Comment("Статус карты")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private CardStatus status;

    @Comment("Баланс карты")
    @Column(name = "balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal balance;

    @Transient
    private transient String fullCardNumber;
}
