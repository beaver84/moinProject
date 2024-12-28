package com.example.moinproject.domain.dto.transfer;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ExchangeRateResponse {
    private String code;
    private String currencyCode;
    private BigDecimal basePrice;
    private int currencyUnit;
}
