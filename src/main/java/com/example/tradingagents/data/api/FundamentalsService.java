package com.example.tradingagents.data.api;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * Fundamental data: financials, balance sheet, cashflow, income statement for A-share.
 */
public interface FundamentalsService {

    /**
     * Get main fundamental metrics for symbol (e.g. PE, PB, revenue, profit).
     */
    Map<String, Object> getFundamentals(String symbol, LocalDate tradeDate);

    /**
     * Get balance sheet summary (key items for LLM).
     */
    List<Map<String, Object>> getBalanceSheet(String symbol, LocalDate tradeDate, int limit);

    /**
     * Get cashflow summary.
     */
    List<Map<String, Object>> getCashflow(String symbol, LocalDate tradeDate, int limit);

    /**
     * Get income statement summary.
     */
    List<Map<String, Object>> getIncomeStatement(String symbol, LocalDate tradeDate, int limit);
}
