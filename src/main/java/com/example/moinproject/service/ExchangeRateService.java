package com.example.moinproject.service;

import com.example.moinproject.domain.dto.transfer.ExchangeRateResponse;
import lombok.Getter;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
public class ExchangeRateService {
    private final WebClient webClient;

    public ExchangeRateService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.baseUrl("https://crix-api-cdn.upbit.com").build();
    }

    public Mono<ExchangeRateInfo> getExchangeRateInfo(String targetCurrency) {
        return webClient.get()
                .uri("/v1/forex/recent?codes=FRX.KRW" + targetCurrency)
                .retrieve()
                .bodyToMono(ExchangeRateResponse[].class)
                .map(rates -> {
                    if (rates.length > 0) {
                        ExchangeRateResponse rate = rates[0];
                        return new ExchangeRateInfo(rate.getBasePrice(), rate.getCurrencyUnit());
                    }
                    throw new RuntimeException("Exchange rate not available");
                });
    }

    @Getter
    public static class ExchangeRateInfo {
        private final BigDecimal basePrice;
        private final int currencyUnit;

        public ExchangeRateInfo(BigDecimal basePrice, int currencyUnit) {
            this.basePrice = basePrice;
            this.currencyUnit = currencyUnit;
        }

    }
}