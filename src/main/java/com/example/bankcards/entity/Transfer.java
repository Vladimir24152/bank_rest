package com.example.bankcards.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "transfers")
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Comment("Уникальный идентификатор перевода средств")
    private Long id;

    @Comment("ID изначальной карты")
    @Column(name = "from_card_id", nullable = false)
    private Long fromCardId;

    @Comment("ID целевой для перевода карты")
    @Column(name = "to_card_id", nullable = false)
    private Long toCardId;

    @Comment("Сумма перевода")
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Comment("Новый баланс изначальной карты")
    @Column(name = "from_card_new_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal fromCardNewBalance;

    @Comment("Новый баланс целевой карты")
    @Column(name = "to_card_new_balance", nullable = false, precision = 15, scale = 2)
    private BigDecimal toCardNewBalance;

    @Comment("Сообщение при переводе")
    @Column(name = "description")
    private String description;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
