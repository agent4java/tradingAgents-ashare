package com.example.tradingagents.data.api;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "tradingagents.data.vendor", havingValue = "stub", matchIfMissing = false)
public class StubNewsService implements NewsService {

    @Override
    public List<Map<String, Object>> getNews(String symbol, LocalDate tradeDate, int limit) {
        return Collections.emptyList();
    }

    @Override
    public List<Map<String, Object>> getGlobalNews(LocalDate tradeDate, int limit) {
        return Collections.emptyList();
    }
}
