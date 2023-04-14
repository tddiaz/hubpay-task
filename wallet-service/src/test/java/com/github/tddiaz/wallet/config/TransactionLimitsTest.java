package com.github.tddiaz.wallet.config;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransactionLimitsTest {

    @Test
    void isWithinLimit_boundaryTest() {
        var depositLimit = new TransactionLimitsConfiguration.DepositLimit(BigDecimal.TEN, BigDecimal.ONE);

        assertTrue(depositLimit.isWithinLimit(BigDecimal.TEN));
        assertTrue(depositLimit.isWithinLimit(BigDecimal.ONE));
        assertTrue(depositLimit.isWithinLimit(BigDecimal.valueOf(9L)));
        assertTrue(depositLimit.isWithinLimit(BigDecimal.valueOf(2L)));

        assertFalse(depositLimit.isWithinLimit(BigDecimal.valueOf(11L)));
        assertFalse(depositLimit.isWithinLimit(BigDecimal.ZERO));
    }

}