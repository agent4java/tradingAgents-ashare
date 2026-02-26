package com.example.tradingagents.data.api;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

@Service
@ConditionalOnProperty(name = "tradingagents.data.vendor", havingValue = "stub", matchIfMissing = false)
public class StubIndicatorsService implements IndicatorsService {

    @Override
    public Map<String, Object> getIndicators(String symbol, LocalDate tradeDate) {
        return Collections.singletonMap("note", "stub: no data");
    }
}
