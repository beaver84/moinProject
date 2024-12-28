package com.example.moinproject.service;

import com.example.moinproject.config.JwtTokenProvider;
import com.example.moinproject.config.exception.DailyLimitExceededException;
import com.example.moinproject.config.exception.QuoteExpiredException;
import com.example.moinproject.domain.dto.transfer.TransferRequest;
import com.example.moinproject.domain.dto.transfer.TransferResponse;
import com.example.moinproject.domain.entity.Quote;
import com.example.moinproject.domain.dto.transfer.QuoteRequest;
import com.example.moinproject.domain.dto.transfer.QuoteResponse;
import com.example.moinproject.domain.entity.User;
import com.example.moinproject.repository.QuoteRepository;
import com.example.moinproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.List;
import java.util.Optional;

@Slf4j
@Transactional
@Service
@RequiredArgsConstructor
public class TransferService {
    private final ExchangeRateService exchangeRateService;
    private final QuoteRepository quoteRepository;
    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;

    public QuoteResponse createQuote(QuoteRequest request, String jwt) {
        if (request.getAmount() <= 0) {
            throw new IllegalArgumentException("NEGATIVE_NUMBER");
        }

        log.debug(jwt);

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
        quote.setUser(jwtTokenProvider.getUserFromJwt(jwt));
        Quote savedQuote = quoteRepository.save(quote);

        QuoteResponse response = new QuoteResponse();
        response.setResultCode(200);
        response.setResultMsg("OK");
        QuoteResponse.Quote quoteDTO = new QuoteResponse.Quote();
        quoteDTO.setQuoteId(savedQuote.getQuoteId());
        quoteDTO.setExchangeRate(savedQuote.getExchangeRate());
        quoteDTO.setExpireTime(savedQuote.getExpireTime());
        quoteDTO.setTargetAmount(savedQuote.getTargetAmount());
        response.setQuote(quoteDTO);

        return response;
    }

    public TransferResponse processTransfer(TransferRequest request, String jwt) {
        Quote quote = quoteRepository.findById(request.getQuoteId())
                .orElseThrow(() -> new QuoteExpiredException("Quote not found"));

        if (isQuoteExpired(quote)) {
            throw new QuoteExpiredException("Quote has expired");
        }

        User user = jwtTokenProvider.getUserFromJwt(jwt);
        BigDecimal dailyTransferAmount = userService.getDailyTransferAmount(user);
        BigDecimal newDailyTotal = dailyTransferAmount.add(quote.getTargetAmount());

        if (user.getIdType().equals("REG_NO") && newDailyTotal.compareTo(new BigDecimal("1000")) > 0) {
            throw new DailyLimitExceededException("Personal account daily limit exceeded");
        }

        if (user.getIdType().equals("BUSINESS_NO") && newDailyTotal.compareTo(new BigDecimal("5000")) > 0) {
            throw new DailyLimitExceededException("Business account daily limit exceeded");
        }

        //TODO 여기에 실제 송금 처리 로직 구현

        TransferResponse response = new TransferResponse();
        response.setResultCode(200);
        response.setResultMsg("OK");
        return response;
    }

    private boolean isQuoteExpired(Quote quote) {
        LocalDateTime expireTime = LocalDateTime.parse(quote.getExpireTime(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return LocalDateTime.now().isAfter(expireTime);
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

