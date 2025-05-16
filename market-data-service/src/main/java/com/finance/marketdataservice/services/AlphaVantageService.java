package com.finance.marketdataservice.services;

import com.crazzyghost.alphavantage.AlphaVantage;
import com.crazzyghost.alphavantage.parameters.DataType;
import com.crazzyghost.alphavantage.parameters.Interval;
import com.crazzyghost.alphavantage.parameters.OutputSize;
import com.crazzyghost.alphavantage.technicalindicator.response.PeriodicSeriesResponse;
import com.crazzyghost.alphavantage.technicalindicator.response.SimpleTechnicalIndicatorUnit;
import com.crazzyghost.alphavantage.timeseries.response.MetaData;
import com.crazzyghost.alphavantage.timeseries.response.QuoteResponse;
import com.crazzyghost.alphavantage.timeseries.response.StockUnit;
import com.crazzyghost.alphavantage.timeseries.response.TimeSeriesResponse;
import com.finance.commonlib.dto.DailyStockDataDto;
import com.finance.commonlib.dto.StockQuoteDto;
import com.finance.commonlib.dto.TechnicalIndicatorDto;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AlphaVantageService {
    private final AlphaVantage alphaVantage;

    @Value("${alpha-vantage.rate-limit}")
    private int rateLimit;

    //TODO: Implement rate limiting logic
    //TODO: Implement breakout logic

    public void getStockQuote(String symbol) {
        alphaVantage.timeSeries()
                .quote()
                .forSymbol(symbol)
                .onSuccess(this::stockQuoteData)
//                .onFailure(e -> {})
                .fetch();
    }

    private void stockQuoteData(Object response) {
        if (response instanceof QuoteResponse quote) {
            BigDecimal price = getBigDecimal(quote.getPrice());
            BigDecimal change = getBigDecimal(quote.getChange());
            BigDecimal changePercent = getBigDecimal(quote.getChangePercent());
            LocalDateTime lastTradingDay = LocalDateTime.parse(quote.getLatestTradingDay());
            var stockQuote = new StockQuoteDto(quote.getSymbol(), price, change, changePercent, lastTradingDay);
            //TODO
        }
        throw new IllegalArgumentException("Invalid response type");
    }

    private BigDecimal getBigDecimal(Number number) {
        if (number == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(String.valueOf(number));
    }

    public void getDailyTimeSeries(String symbol) {
        alphaVantage.timeSeries()
                .daily()
                .forSymbol(symbol)
                .outputSize(OutputSize.FULL) //possibly FULL or COMPACT
                .dataType(DataType.JSON)
                .onSuccess(this::dailyStockData)
//                .onFailure()
                .fetch();
    }

    private void dailyStockData(Object response) {
        if (response instanceof TimeSeriesResponse quote) {
            MetaData metaData = quote.getMetaData();
            List<StockUnit> stockUnits = quote.getStockUnits();

            List<DailyStockDataDto> dailyStockDataDtos = new ArrayList<>();
            for (var stockUnit : stockUnits) {
                var dailyStockData = DailyStockDataDto.builder()
                        .symbol(metaData.getSymbol())
                        .date(LocalDate.parse(stockUnit.getDate()))
                        .open(getBigDecimal(stockUnit.getOpen()))
                        .high(getBigDecimal(stockUnit.getHigh()))
                        .low(getBigDecimal(stockUnit.getLow()))
                        .close(getBigDecimal(stockUnit.getClose()))
                        .volume(stockUnit.getVolume())
                        .build();
                dailyStockDataDtos.add(dailyStockData);
            }
            //TODO
        }
    }

    public void getSimpleMovingAverage(String symbol, String interval, int timePeriod) {
        alphaVantage.technicalIndicator()
                .sma()
                .forSymbol(symbol)
                .interval(Interval.valueOf(interval))
                .timePeriod(timePeriod)
                .dataType(DataType.JSON)
                .onSuccess(this::periodicSeriesData)
//                .onFailure(error -> {})
                .fetch();
    }

    //TODO: Technical indicators
    public void getRelativeStrengthIndex(String symbol, String interval, int timePeriod) {
        alphaVantage.technicalIndicator()
                .rsi()
                .forSymbol(symbol)
                .interval(Interval.valueOf(interval))
                .timePeriod(timePeriod)
                .dataType(DataType.JSON)
                .onSuccess(this::periodicSeriesData)
//                .onFailure(error -> {})
                .fetch();
    }

    private void periodicSeriesData(Object response) {
        if (response instanceof PeriodicSeriesResponse periodicSeriesResponse) {
            PeriodicSeriesResponse.MetaData metaData = periodicSeriesResponse.getMetaData();
            List<SimpleTechnicalIndicatorUnit> technicalIndicatorUnits = periodicSeriesResponse.getIndicatorUnits();

            List<TechnicalIndicatorDto> technicalIndicatorDtos = new ArrayList<>();
            for (var indicator : technicalIndicatorUnits) {
                var technicalIndicatorDto = new TechnicalIndicatorDto(metaData.getSymbol(), LocalDate.parse(indicator.getDate()),
                        getBigDecimal(indicator.getValue()), metaData.getIndicator());
                technicalIndicatorDtos.add(technicalIndicatorDto);
            }
            //TODO
        }
    }
}
