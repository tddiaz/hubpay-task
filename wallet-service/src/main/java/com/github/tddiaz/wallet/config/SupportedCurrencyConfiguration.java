package com.github.tddiaz.wallet.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Set;

@Configuration
public class SupportedCurrencyConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "supported-currencies")
    public SupportedCurrencies supportedCurrencies() {
        return new SupportedCurrencies();
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SupportedCurrencies {
        private Set<String> values;

        public Boolean isSupported(String currency) {
            return values.contains(currency);
        }
    }
}
