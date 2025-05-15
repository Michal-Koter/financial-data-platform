package com.finance.commonlib.events;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StockQuoteEvent(
        String symbol,
        BigDecimal price,
        BigDecimal change,
        BigDecimal changePercent,
        LocalDateTime timestamp,
        String eventId,
        LocalDateTime eventTimestamp
) {
}
