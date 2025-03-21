package com.example.moinproject.controller;

import com.example.moinproject.config.exception.BaseCustomException;
import com.example.moinproject.config.exception.DailyLimitExceededException;
import com.example.moinproject.config.exception.QuoteExpiredException;
import com.example.moinproject.domain.dto.transfer.QuoteRequest;
import com.example.moinproject.domain.dto.transfer.QuoteResponse;
import com.example.moinproject.domain.dto.transfer.TransferHistoryResponse;
import com.example.moinproject.domain.dto.transfer.TransferRequest;
import com.example.moinproject.domain.dto.transfer.TransferResponse;
import com.example.moinproject.service.TransferService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/transfer")
public class TransferController {
    private final TransferService transferService;

    @PostMapping("/quote")
    public ResponseEntity<QuoteResponse> getQuote(@RequestBody QuoteRequest request, @RequestHeader("Authorization") String jwt) {
        try {
            QuoteResponse response = transferService.createQuote(request, jwt);
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
        } catch (BaseCustomException e) {
            log.error("An error occurred: " + e.getMessage());
            if (e instanceof DailyLimitExceededException) {
                return createErrorResponse("QUOTE_EXPIRED", HttpStatus.BAD_REQUEST);
            } else if (e instanceof QuoteExpiredException) {
                return createErrorResponse("LIMIT_EXCESS", HttpStatus.BAD_REQUEST);
            } else {
                return createErrorResponse("UNKNOWN_ERROR", HttpStatus.INTERNAL_SERVER_ERROR);
            }
        }
    }

    @GetMapping("/list")
    public ResponseEntity<TransferHistoryResponse> getTransferHistory(@RequestHeader("Authorization") String token) {
        String jwt = token.replace("Bearer ", "");
        TransferHistoryResponse response = transferService.getTransferHistory(jwt);
        return ResponseEntity.ok(response);
    }

    private ResponseEntity<TransferResponse> createErrorResponse(String errorMsg, HttpStatus status) {
        TransferResponse errorResponse = new TransferResponse();
        errorResponse.setResultCode(status.value());
        errorResponse.setResultMsg(errorMsg);
        return ResponseEntity.status(status).body(errorResponse);
    }
}

