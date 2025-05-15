package com.finance.commonlib.dto;


import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyStockDataDto(
        String symbol,
        LocalDate date,
        BigDecimal open,
        BigDecimal high,
        BigDecimal low,
        BigDecimal close,
        Long volume
) {
}
