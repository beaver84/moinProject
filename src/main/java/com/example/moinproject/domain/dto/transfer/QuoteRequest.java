package com.example.moinproject.domain.dto.transfer;

import lombok.Data;

@Data
public class QuoteRequest {
    private int amount;
    private String targetCurrency;
}