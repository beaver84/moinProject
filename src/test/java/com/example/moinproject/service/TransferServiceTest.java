package com.example.moinproject.service;

import com.example.moinproject.config.JwtTokenProvider;
import com.example.moinproject.domain.dto.transfer.QuoteRequest;
import com.example.moinproject.domain.dto.transfer.QuoteResponse;
import com.example.moinproject.domain.dto.transfer.TransferRequest;
import com.example.moinproject.domain.dto.transfer.TransferResponse;
import com.example.moinproject.domain.dto.user.SignUpRequest;
import com.example.moinproject.domain.entity.Quote;
import com.example.moinproject.domain.entity.User;
import com.example.moinproject.repository.QuoteRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class TransferServiceTest {

    @Autowired
    private QuoteRepository quoteRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private TransferService transferService;

    @Transactional
    @Test
    @DisplayName("송금견적서를 갖고 오는 요청에 성공한다.")
    void successCreateQuote() {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUserId("sample@gmail.com");
        signUpRequest.setPassword("Qq09iu!@1238798");
        signUpRequest.setIdType("REG_NO");
        signUpRequest.setIdValue("001123-3111111");
        signUpRequest.setName("테스트");
        userService.signup(signUpRequest);

        User user = userService.authenticateUser("sample@gmail.com", "Qq09iu!@1238798");
        String token = jwtTokenProvider.generateToken(user);

        QuoteRequest request = new QuoteRequest();
        request.setAmount(10000);
        request.setTargetCurrency("JPY");

        QuoteResponse result = transferService.createQuote(request, token);
        assertThat(result.getQuote().getQuoteId()).isGreaterThanOrEqualTo(1L);
    }

    @Transactional
    @Test
    @DisplayName("송금 접수 요청에 성공한다.")
    void successProcessTransfer(){
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUserId("sample@gmail.com");
        signUpRequest.setPassword("Qq09iu!@1238798");
        signUpRequest.setIdType("REG_NO");
        signUpRequest.setIdValue("001123-3111111");
        signUpRequest.setName("테스트");
        userService.signup(signUpRequest);

        User user = userService.authenticateUser("sample@gmail.com", "Qq09iu!@1238798");
        String token = jwtTokenProvider.generateToken(user);

        Quote quote = new Quote();
        quote.setExchangeRate(BigDecimal.valueOf(9.33));
        quote.setExpireTime(LocalDateTime.now().plusMinutes(10).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        quote.setTargetAmount(BigDecimal.valueOf(1000.0));
        quote.setIsTransfered(false);
        quote.setAmount(10000);
        quote.setFee(BigDecimal.valueOf(3050.00));
        quote.setUsdExchangeRate(BigDecimal.valueOf(933.30));
        quote.setTargetCurrency("JPY");
        quote.setUser(user);
        Quote savedQuote = quoteRepository.save(quote);

        TransferRequest request = new TransferRequest();
        request.setQuoteId(savedQuote.getQuoteId());

        TransferResponse response = transferService.processTransfer(request, token);
        assertThat(response.getResultCode()).isEqualTo(200);

    }
}