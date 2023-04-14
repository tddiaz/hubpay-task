package com.github.tddiaz.wallet.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.math.BigDecimal;

@Configuration
public class TransactionLimitsConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "limits.withdraw")
    public WithdrawalLimit withdrawalLimit() {
        return new WithdrawalLimit();
    }

    @Bean
    @ConfigurationProperties(prefix = "limits.deposit")
    public DepositLimit depositLimit() {
        return new DepositLimit();
    }

    private sealed interface TransactionLimit permits WithdrawalLimit, DepositLimit {
        BigDecimal getMax();

        BigDecimal getMin();

        default Boolean isWithinLimit(BigDecimal value) {
            return value.compareTo(getMin()) >= 0 && value.compareTo(getMax()) <= 0;
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class WithdrawalLimit implements TransactionLimit {
        private BigDecimal max;
        private BigDecimal min;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static final class DepositLimit implements TransactionLimit {
        private BigDecimal max;
        private BigDecimal min;
    }
}
