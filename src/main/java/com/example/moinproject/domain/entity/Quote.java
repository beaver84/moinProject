package com.example.moinproject.domain.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Quote {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long quoteId;

    @Column(nullable = false)
    private BigDecimal exchangeRate;

    @Column(nullable = false)
    private String expireTime;

    @Column(nullable = false)
    private BigDecimal targetAmount;

    @Column(nullable = false)
    private Boolean isTransfered;

    @Column(nullable = false)
    private int amount;

    @Column(nullable = false)
    private BigDecimal fee;

    @Column(nullable = false)
    private BigDecimal usdExchangeRate;

    @Column(nullable = false)
    private String targetCurrency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Builder
    public Quote(BigDecimal exchangeRate, String expireTime, BigDecimal targetAmount, int amount, BigDecimal fee, BigDecimal usdExchangeRate, String targetCurrency, User user) {
        this.exchangeRate = exchangeRate;
        this.expireTime = expireTime;
        this.targetAmount = targetAmount;
        this.amount = amount;
        this.fee = fee;
        this.usdExchangeRate = usdExchangeRate;
        this.targetCurrency = targetCurrency;
        this.user = user;
        this.isTransfered = false;
    }
}