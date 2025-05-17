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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class AlphaVantageService {
    private final AlphaVantage alphaVantage;

    @Autowired
    public AlphaVantageService(AlphaVantage alphaVantage) {
        this.alphaVantage = alphaVantage;
    }

    public StockQuoteDto getStockQuote(String symbol) {
        QuoteResponse quoteResponse = alphaVantage.timeSeries()
                .quote()
                .forSymbol(symbol)
                .fetchSync();
        return stockQuoteData(quoteResponse);
    }

    private StockQuoteDto stockQuoteData(QuoteResponse quote) {
        BigDecimal price = getBigDecimal(quote.getPrice());
        BigDecimal change = getBigDecimal(quote.getChange());
        BigDecimal changePercent = getBigDecimal(quote.getChangePercent());
        LocalDateTime lastTradingDay = LocalDateTime.parse(quote.getLatestTradingDay());
        return new StockQuoteDto(quote.getSymbol(), price, change, changePercent, lastTradingDay);
    }

    private BigDecimal getBigDecimal(Number number) {
        if (number == null) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(String.valueOf(number));
    }

    public List<DailyStockDataDto> getDailyTimeSeries(String symbol) {
        TimeSeriesResponse timeSeriesResponse = alphaVantage.timeSeries()
                .daily()
                .forSymbol(symbol)
                .outputSize(OutputSize.FULL) //possibly FULL or COMPACT
                .dataType(DataType.JSON)
                .fetchSync();
        return dailyStockData(timeSeriesResponse);
    }

    private List<DailyStockDataDto> dailyStockData(TimeSeriesResponse series) {
        MetaData metaData = series.getMetaData();
        List<StockUnit> stockUnits = series.getStockUnits();

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
        return dailyStockDataDtos;
    }

    public List<TechnicalIndicatorDto> getSimpleMovingAverage(String symbol, String interval, int timePeriod) {
        PeriodicSeriesResponse periodicSeriesResponse = alphaVantage.technicalIndicator()
                .sma()
                .forSymbol(symbol)
                .interval(Interval.valueOf(interval))
                .timePeriod(timePeriod)
                .dataType(DataType.JSON)
                .fetchSync();
        return periodicSeriesData(periodicSeriesResponse);
    }

    public List<TechnicalIndicatorDto> getRelativeStrengthIndex(String symbol, String interval, int timePeriod) {
        PeriodicSeriesResponse periodicSeriesResponse = alphaVantage.technicalIndicator()
                .rsi()
                .forSymbol(symbol)
                .interval(Interval.valueOf(interval))
                .timePeriod(timePeriod)
                .dataType(DataType.JSON)
                .fetchSync();
        return periodicSeriesData(periodicSeriesResponse);
    }

    private List<TechnicalIndicatorDto> periodicSeriesData(PeriodicSeriesResponse series) {
        PeriodicSeriesResponse.MetaData metaData = series.getMetaData();
        List<SimpleTechnicalIndicatorUnit> technicalIndicatorUnits = series.getIndicatorUnits();

        List<TechnicalIndicatorDto> technicalIndicatorDtos = new ArrayList<>();
        for (var indicator : technicalIndicatorUnits) {
            var technicalIndicatorDto = new TechnicalIndicatorDto(metaData.getSymbol(), LocalDate.parse(indicator.getDate()),
                    getBigDecimal(indicator.getValue()), metaData.getIndicator());
            technicalIndicatorDtos.add(technicalIndicatorDto);
        }
        return technicalIndicatorDtos;
    }
}
