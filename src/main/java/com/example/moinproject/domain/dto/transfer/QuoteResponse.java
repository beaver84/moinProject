package com.example.moinproject.domain.dto.transfer;

import com.example.moinproject.domain.entity.Quote;
import lombok.Data;

@Data
public class QuoteResponse {
    private int resultCode;
    private String resultMsg;
    private Quote quote;
}
