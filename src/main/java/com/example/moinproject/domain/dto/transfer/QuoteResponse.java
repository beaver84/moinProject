package com.example.moinproject.domain.dto.transfer;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class QuoteResponse {
    private int resultCode;
    private String resultMsg;

    private Quote quote;

    @Data
    public static class Quote {
        private Long quoteId;
        private BigDecimal exchangeRate;
        private String expireTime;
        private BigDecimal targetAmount;
    }
}
