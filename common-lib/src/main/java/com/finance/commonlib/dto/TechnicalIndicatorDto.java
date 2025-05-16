package com.finance.commonlib.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TechnicalIndicatorDto(
        String symbol,
        LocalDate date,
        BigDecimal value,
        String indicator
) {
}
