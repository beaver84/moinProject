package com.example.moinproject.domain.dto.transfer;

import lombok.Builder;
import lombok.Getter;
import java.util.List;

@Getter
@Builder
public class TransferHistoryResponse {
    private int resultCode;
    private String resultMsg;
    private String userId;
    private String name;
    private int todayTransferCount;
    private double todayTransferUsdAmount;
    private List<TransferHistoryItem> history;
}