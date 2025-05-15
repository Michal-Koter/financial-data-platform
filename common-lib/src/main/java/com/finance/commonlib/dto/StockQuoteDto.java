package com.finance.commonlib.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StockQuoteDto(
        String symbol,
        BigDecimal price,
        BigDecimal change,
        BigDecimal changePercent,
        LocalDateTime timestamp
) {
}
