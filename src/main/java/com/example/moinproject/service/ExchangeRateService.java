package com.example.moinproject.service;

import com.example.moinproject.domain.dto.transfer.ExchangeRateResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
public class ExchangeRateService {
    private final WebClient webClient;

    public ExchangeRateService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://crix-api-cdn.upbit.com").build();
    }

    public Mono<BigDecimal> getBasePrice(String targetCurrency) {
        return webClient.get()
                .uri("/v1/forex/recent?codes=FRX.KRW" + targetCurrency)
                .retrieve()
                .bodyToMono(ExchangeRateResponse[].class)
                .map(rates -> {
                    if (rates.length > 0) {
                        return rates[0].getBasePrice();
                    }
                    throw new RuntimeException("Exchange rate not available");
                });
    }

    public Mono<Integer> getCurrencyUnit(String targetCurrency) {
        return webClient.get()
                .uri("/v1/forex/recent?codes=FRX.KRW" + targetCurrency)
                .retrieve()
                .bodyToMono(ExchangeRateResponse[].class)
                .map(rates -> {
                    if (rates.length > 0) {
                        return rates[0].getCurrencyUnit();
                    }
                    throw new RuntimeException("Exchange rate not available");
                });
    }
}