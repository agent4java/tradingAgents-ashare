package com.example.tradingagents.data.api;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * A-share stock data: daily bars, adjusted prices. Tushare-agnostic API.
 */
public interface StockDataService {

    /**
     * Get daily OHLCV for symbol in date range (A-share ts_code e.g. 600519.SH).
     */
    List<Map<String, Object>> getDaily(String symbol, LocalDate start, LocalDate end);

    /**
     * Get latest available daily bar before or on tradeDate.
     */
    Map<String, Object> getLatestDaily(String symbol, LocalDate tradeDate);
}
