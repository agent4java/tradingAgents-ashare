package com.example.tradingagents.data.api;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "tradingagents.data.vendor", havingValue = "stub", matchIfMissing = false)
public class StubFundamentalsService implements FundamentalsService {

    @Override
    public Map<String, Object> getFundamentals(String symbol, LocalDate tradeDate) {
        return Collections.singletonMap("note", "stub: no data");
    }

    @Override
    public List<Map<String, Object>> getBalanceSheet(String symbol, LocalDate tradeDate, int limit) {
        return Collections.emptyList();
    }

    @Override
    public List<Map<String, Object>> getCashflow(String symbol, LocalDate tradeDate, int limit) {
        return Collections.emptyList();
    }

    @Override
    public List<Map<String, Object>> getIncomeStatement(String symbol, LocalDate tradeDate, int limit) {
        return Collections.emptyList();
    }
}
