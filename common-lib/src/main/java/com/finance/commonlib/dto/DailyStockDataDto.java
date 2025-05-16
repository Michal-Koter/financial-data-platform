package com.finance.commonlib.dto;


import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDate;

@Builder
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
