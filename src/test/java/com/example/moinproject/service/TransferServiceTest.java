package com.example.moinproject.service;

import com.example.moinproject.config.JwtTokenProvider;
import com.example.moinproject.domain.dto.transfer.QuoteRequest;
import com.example.moinproject.domain.dto.transfer.QuoteResponse;
import com.example.moinproject.domain.dto.user.SignUpRequest;
import com.example.moinproject.domain.dto.user.UserDto;
import com.example.moinproject.domain.entity.User;
import com.example.moinproject.repository.QuoteRepository;
import com.example.moinproject.repository.TransferRepository;
import com.example.moinproject.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class TransferServiceTest {

    @Autowired
    private ExchangeRateService exchangeRateService;
    @Autowired
    private QuoteRepository quoteRepository;
    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    @Autowired
    private TransferRepository transferRepository;
    @Autowired
    private TransferService transferService;

    @Transactional
    @Test
    void createQuote() {
        SignUpRequest signUpRequest = new SignUpRequest();
        signUpRequest.setUserId("sample@gmail.com");
        signUpRequest.setPassword("Qq09iu!@1238798");
        signUpRequest.setIdType("REG_NO");
        signUpRequest.setIdValue("001123-3111111");
        signUpRequest.setName("테스트");
        UserDto userDto = userService.signup(signUpRequest);

        User user = userService.authenticateUser("sample@gmail.com", "Qq09iu!@1238798");
        String token = jwtTokenProvider.generateToken(user);

        QuoteRequest request = new QuoteRequest();
        request.setAmount(10000);
        request.setTargetCurrency("JPY");

        QuoteResponse result = transferService.createQuote(request, token);
        assertThat(result.getQuote().getQuoteId()).isGreaterThanOrEqualTo(1L);
    }
}