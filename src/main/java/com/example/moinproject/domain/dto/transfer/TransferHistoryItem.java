package com.example.moinproject.domain.dto.transfer;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class TransferHistoryItem {
    private int sourceAmount;
    private BigDecimal fee;
    private BigDecimal usdExchangeRate;
    private BigDecimal usdAmount;
    private String targetCurrency;
    private BigDecimal exchangeRate;
    private BigDecimal targetAmount;
    private String requestedDate;
}