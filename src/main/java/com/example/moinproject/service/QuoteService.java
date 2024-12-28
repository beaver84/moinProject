package com.example.moinproject.service;

import com.example.moinproject.domain.entity.Quote;
import com.example.moinproject.domain.dto.transfer.QuoteRequest;
import com.example.moinproject.domain.dto.transfer.QuoteResponse;
import com.example.moinproject.repository.QuoteRepository;
import com.example.moinproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Currency;

@Transactional
@Service
@RequiredArgsConstructor
public class QuoteService {
    private final ExchangeRateService exchangeRateService;
    private final QuoteRepository quoteRepository;

    public QuoteResponse createQuote(QuoteRequest request) {
        if (request.getAmount() <= 0) {
            throw new IllegalArgumentException("NEGATIVE_NUMBER");
        }

        BigDecimal basePrice = exchangeRateService.getBasePrice(request.getTargetCurrency()).block();
        Integer currencyUnit = exchangeRateService.getCurrencyUnit(request.getTargetCurrency()).block();
        BigDecimal fee = calculateFee(request.getTargetCurrency(), BigDecimal.valueOf(request.getAmount()));
        BigDecimal exchangeRateResponse = basePrice.divide(BigDecimal.valueOf(currencyUnit), 3, RoundingMode.HALF_UP);
        BigDecimal targetAmount = BigDecimal.valueOf(request.getAmount())
                .subtract(fee)
                .divide(exchangeRateResponse, 2, RoundingMode.HALF_UP);

        if (targetAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("NEGATIVE_NUMBER");
        }

        Currency targetCurrency = Currency.getInstance(request.getTargetCurrency());
        int fractionDigits = targetCurrency.getDefaultFractionDigits();
        targetAmount = targetAmount.setScale(fractionDigits, RoundingMode.HALF_UP);

        Quote quote = new Quote();
        quote.setExchangeRate(exchangeRateResponse);
        quote.setExpireTime(LocalDateTime.now().plusMinutes(10).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        quote.setTargetAmount(targetAmount);
        quoteRepository.save(quote);

        QuoteResponse response = new QuoteResponse();
        response.setResultCode(200);
        response.setResultMsg("OK");
        response.setQuote(quote);

        return response;
    }

    public static BigDecimal calculateFee(String currency, BigDecimal amount) {
        if ("USD".equals(currency)) {
            if (amount.compareTo(BigDecimal.valueOf(1000000)) <= 0) {
                return amount.multiply(BigDecimal.valueOf(0.002)).add(BigDecimal.valueOf(1000));
            } else {
                return amount.multiply(BigDecimal.valueOf(0.001)).add(BigDecimal.valueOf(3000));
            }
        } else if ("JPY".equals(currency)) {
            return amount.multiply(BigDecimal.valueOf(0.005)).add(BigDecimal.valueOf(3000));
        }
        throw new IllegalArgumentException("Unsupported currency");
    }
}

