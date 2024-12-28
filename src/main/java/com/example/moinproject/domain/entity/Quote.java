package com.example.moinproject.domain.entity;

import jakarta.persistence.*;
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
}