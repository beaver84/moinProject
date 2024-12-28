package com.example.moinproject.controller;

import com.example.moinproject.config.exception.DailyLimitExceededException;
import com.example.moinproject.config.exception.QuoteExpiredException;
import com.example.moinproject.domain.dto.transfer.QuoteRequest;
import com.example.moinproject.domain.dto.transfer.QuoteResponse;
import com.example.moinproject.domain.dto.transfer.TransferRequest;
import com.example.moinproject.domain.dto.transfer.TransferResponse;
import com.example.moinproject.service.TransferService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transfer")
public class TransferController {
    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PostMapping("/quote")
    public ResponseEntity<QuoteResponse> getQuote(@RequestBody QuoteRequest request) {
        try {
            QuoteResponse response = transferService.createQuote(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            QuoteResponse errorResponse = new QuoteResponse();
            errorResponse.setResultCode(400);
            errorResponse.setResultMsg(e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    @PostMapping("/request")
    public ResponseEntity<TransferResponse> requestTransfer(@RequestBody TransferRequest request,
                                                            @RequestHeader("Authorization") String jwt) {
        try {
            TransferResponse response = transferService.processTransfer(request, jwt);
            return ResponseEntity.ok(response);
        } catch (QuoteExpiredException e) {
            return createErrorResponse("QUOTE_EXPIRED", HttpStatus.BAD_REQUEST);
        } catch (DailyLimitExceededException e) {
            return createErrorResponse("LIMIT_EXCESS", HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            return createErrorResponse("UNKNOWN_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private ResponseEntity<TransferResponse> createErrorResponse(String errorMsg, HttpStatus status) {
        TransferResponse errorResponse = new TransferResponse();
        errorResponse.setResultCode(status.value());
        errorResponse.setResultMsg(errorMsg);
        return ResponseEntity.status(status).body(errorResponse);
    }
}

