package com.example.tradingagents.data.api;

import java.time.LocalDate;
import java.util.Map;

/**
 * Technical indicators (e.g. MACD, RSI) for A-share. Can be computed from daily data or from vendor API.
 */
public interface IndicatorsService {

    /**
     * Get technical indicators for symbol as of tradeDate (or latest available).
     * Returns a map of indicator name -> value (or description string for LLM).
     */
    Map<String, Object> getIndicators(String symbol, LocalDate tradeDate);
}
