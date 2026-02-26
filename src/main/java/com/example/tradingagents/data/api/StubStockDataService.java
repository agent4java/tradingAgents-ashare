package com.example.tradingagents.data.api;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "tradingagents.data.vendor", havingValue = "stub", matchIfMissing = false)
public class StubStockDataService implements StockDataService {

    @Override
    public List<Map<String, Object>> getDaily(String symbol, LocalDate start, LocalDate end) {
        return Collections.emptyList();
    }

    @Override
    public Map<String, Object> getLatestDaily(String symbol, LocalDate tradeDate) {
        return Collections.emptyMap();
    }
}
