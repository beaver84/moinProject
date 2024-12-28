package com.example.moinproject.controller;

import com.example.moinproject.domain.dto.transfer.QuoteRequest;
import com.example.moinproject.domain.dto.transfer.QuoteResponse;
import com.example.moinproject.service.QuoteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/transfer")
public class TransferController {
    private final QuoteService quoteService;

    public TransferController(QuoteService quoteService) {
        this.quoteService = quoteService;
    }

    @PostMapping("/quote")
    public ResponseEntity<QuoteResponse> getQuote(@RequestBody QuoteRequest request) {
        try {
            QuoteResponse response = quoteService.createQuote(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            QuoteResponse errorResponse = new QuoteResponse();
            errorResponse.setResultCode(400);
            errorResponse.setResultMsg(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}