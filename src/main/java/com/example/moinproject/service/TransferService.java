package com.example.moinproject.service;

import com.example.moinproject.config.JwtTokenProvider;
import com.example.moinproject.config.exception.DailyLimitExceededException;
import com.example.moinproject.config.exception.QuoteExpiredException;
import com.example.moinproject.domain.dto.transfer.TransferHistoryItem;
import com.example.moinproject.domain.dto.transfer.TransferHistoryResponse;
import com.example.moinproject.domain.dto.transfer.TransferRequest;
import com.example.moinproject.domain.dto.transfer.TransferResponse;
import com.example.moinproject.domain.entity.Quote;
import com.example.moinproject.domain.dto.transfer.QuoteRequest;
import com.example.moinproject.domain.dto.transfer.QuoteResponse;
import com.example.moinproject.domain.entity.Transfer;
import com.example.moinproject.domain.entity.User;
import com.example.moinproject.repository.QuoteRepository;
import com.example.moinproject.repository.TransferRepository;
import com.example.moinproject.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Currency;
import java.util.List;
import java.util.stream.Collectors;

import static java.math.BigDecimal.*;

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
    private final TransferRepository transferRepository;

    public QuoteResponse createQuote(QuoteRequest request, String jwt) {
        User user = getUser(jwt);

        if (request.getAmount() <= 0) {
            throw new IllegalArgumentException("보내는 금액이 음수가 될 수 없습니다.");
        }

        ExchangeRateService.ExchangeRateInfo exchangeRateInfo = exchangeRateService.getExchangeRateInfo("USD").block();
        BigDecimal basePrice = exchangeRateInfo != null ? exchangeRateInfo.getBasePrice() : valueOf(0);
        int currencyUnit = exchangeRateInfo != null ? exchangeRateInfo.getCurrencyUnit() : 0;

        BigDecimal exchangeRateResponse = basePrice.divide(valueOf(currencyUnit), 3, RoundingMode.HALF_UP);
        BigDecimal fee = calculateFee(request.getTargetCurrency(), valueOf(request.getAmount()));
        BigDecimal targetAmount = valueOf(request.getAmount())
                .subtract(fee)
                .divide(exchangeRateResponse, 2, RoundingMode.HALF_UP);
        if (targetAmount.compareTo(ZERO) <= 0) {
            throw new IllegalArgumentException("받는 금액이 음수가 될 수 없습니다.");
        }

        Currency targetCurrency = Currency.getInstance(request.getTargetCurrency());
        int fractionDigits = targetCurrency.getDefaultFractionDigits();
        targetAmount = targetAmount.setScale(fractionDigits, RoundingMode.HALF_UP);

        Quote quote = Quote.builder()
                .exchangeRate(exchangeRateResponse)
                .expireTime(LocalDateTime.now().plusMinutes(10).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .targetAmount(targetAmount)
                .amount(request.getAmount())
                .fee(fee)
                .targetCurrency(request.getTargetCurrency())
                .usdExchangeRate(basePrice)
                .user(user)
                .build();
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

    private User getUser(String jwt) {
        String userId = jwtTokenProvider.getUserFromJwt(jwt).getUserId();
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("유저가 존재하지 않습니다."));
        return user;
    }

    public TransferResponse processTransfer(TransferRequest request, String jwt) {
        Quote quote = quoteRepository.findById(request.getQuoteId())
                .orElseThrow(() -> new QuoteExpiredException("Quote not found"));

        if (isQuoteExpired(quote)) {
            throw new QuoteExpiredException("견적서가 만료되었습니다.");
        }

        User user = getUser(jwt);
        BigDecimal dailyTransferAmount = userService.getDailyTransferAmount(user);
        BigDecimal newDailyTotal = dailyTransferAmount.add(quote.getTargetAmount());

        if (user.getIdType().equals("REG_NO") && newDailyTotal.compareTo(new BigDecimal("1000")) > 0) {
            throw new DailyLimitExceededException("개인 회원 한도가 초과되었습니다.");
        }

        if (user.getIdType().equals("BUSINESS_NO") && newDailyTotal.compareTo(new BigDecimal("5000")) > 0) {
            throw new DailyLimitExceededException("기업 회원 한도가 초과되었습니다.");
        }

        quote.setIsTransfered(true);

        Transfer transfer = Transfer.builder()
                .sourceAmount(quote.getAmount())
                .fee(quote.getFee())
                .usdExchangeRate(quote.getUsdExchangeRate())
                .usdAmount(quote.getAmount())
                .targetCurrency(quote.getTargetCurrency())
                .exchangeRate(quote.getExchangeRate())
                .targetAmount(quote.getTargetAmount())
                .requestedDate(LocalDateTime.now())
                .user(user)
                .build();
        transferRepository.save(transfer);

        TransferResponse response = new TransferResponse();
        response.setResultCode(200);
        response.setResultMsg("OK");
        return response;
    }

    public TransferHistoryResponse getTransferHistory(String jwt) {
        User user = getUser(jwt);

        List<Transfer> transfers = transferRepository.findByUserOrderByRequestedDateDesc(user);

        LocalDate today = LocalDate.now();
        long todayTransferCount = transfers.stream()
                .filter(t -> t.getRequestedDate().toLocalDate().isEqual(today))
                .count();

        double todayTransferUsdAmount = transfers.stream()
                .filter(t -> t.getRequestedDate().toLocalDate().isEqual(today))
                .mapToDouble(Transfer::getUsdAmount)
                .sum();

        List<TransferHistoryItem> history = transfers.stream()
                .map(this::mapToTransferHistoryItem)
                .collect(Collectors.toList());

        return TransferHistoryResponse.builder()
                .resultCode(200)
                .resultMsg("OK")
                .userId(user.getUserId())
                .name(user.getName())
                .todayTransferCount((int) todayTransferCount)
                .todayTransferUsdAmount(roundToTwoDecimals(todayTransferUsdAmount))
                .history(history)
                .build();
    }

    private TransferHistoryItem mapToTransferHistoryItem(Transfer transfer) {
        return TransferHistoryItem.builder()
                .sourceAmount(transfer.getSourceAmount())
                .fee(transfer.getFee())
                .usdExchangeRate(transfer.getUsdExchangeRate())
                .usdAmount(valueOf(roundToTwoDecimals(transfer.getUsdAmount())))
                .targetCurrency(transfer.getTargetCurrency())
                .exchangeRate(transfer.getExchangeRate())
                .targetAmount(transfer.getTargetAmount())
                .requestedDate(transfer.getRequestedDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private boolean isQuoteExpired(Quote quote) {
        LocalDateTime expireTime = LocalDateTime.parse(quote.getExpireTime(),
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return LocalDateTime.now().isAfter(expireTime);
    }

    public static BigDecimal calculateFee(String currency, BigDecimal amount) {
        if ("USD".equals(currency)) {
            if (amount.compareTo(valueOf(1000000)) <= 0) {
                return amount.multiply(valueOf(0.002)).add(valueOf(1000));
            } else {
                return amount.multiply(valueOf(0.001)).add(valueOf(3000));
            }
        } else if ("JPY".equals(currency)) {
            return amount.multiply(valueOf(0.005)).add(valueOf(3000));
        }
        throw new IllegalArgumentException("USD 또는 JPY를 입력해 주세요.");
    }
}

