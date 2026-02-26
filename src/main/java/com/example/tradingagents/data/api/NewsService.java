package com.example.tradingagents.data.api;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * News / sentiment data for A-share (company news, macro, etc.).
 */
public interface NewsService {

    /**
     * Get news items for symbol around tradeDate. Returns list of { title, content/summary, date, source }.
     */
    List<Map<String, Object>> getNews(String symbol, LocalDate tradeDate, int limit);

    /**
     * Get broader market or macro news if supported.
     */
    List<Map<String, Object>> getGlobalNews(LocalDate tradeDate, int limit);
}
