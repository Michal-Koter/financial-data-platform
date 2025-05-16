package com.finance.marketdataservice.config;

import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AlphaVantageConfig {

    @Bean
    public AlphaVantage alphaVantage(@Value("${alpha-vantage.api-key}") String apiKey) {
        Config config = Config.builder()
                .key(apiKey)
                .timeOut(100)
                .build();
        AlphaVantage instance = AlphaVantage.api();
        instance.init(config);
        return instance;
    }
}
