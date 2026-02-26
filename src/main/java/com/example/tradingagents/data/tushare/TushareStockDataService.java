package com.example.tradingagents.data.tushare;

import com.example.tradingagents.data.api.StockDataService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "tradingagents.data.vendor", havingValue = "tushare")
public class TushareStockDataService implements StockDataService {

    private static final DateTimeFormatter TUSHARE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final TushareClient client;

    public TushareStockDataService(TushareClient client) {
        this.client = client;
    }

    @Override
    public List<Map<String, Object>> getDaily(String symbol, LocalDate start, LocalDate end) {
        Map<String, String> params = new HashMap<>();
        params.put("ts_code", symbol);
        params.put("start_date", start.format(TUSHARE_DATE));
        params.put("end_date", end.format(TUSHARE_DATE));
        String fields = "ts_code,trade_date,open,high,low,close,vol,amount";
        List<Map<String, Object>> rows = client.request("daily", params, fields);
        return rows.stream()
                .map(this::toReadableMap)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Object> getLatestDaily(String symbol, LocalDate tradeDate) {
        LocalDate start = tradeDate.minusDays(30);
        List<Map<String, Object>> list = getDaily(symbol, start, tradeDate);
        if (list.isEmpty()) {
            return Collections.emptyMap();
        }
        return list.get(list.size() - 1);
    }

    private Map<String, Object> toReadableMap(Map<String, Object> raw) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("代码", raw.get("ts_code"));
        m.put("日期", raw.get("trade_date"));
        m.put("开盘", raw.get("open"));
        m.put("最高", raw.get("high"));
        m.put("最低", raw.get("low"));
        m.put("收盘", raw.get("close"));
        m.put("成交量", raw.get("vol"));
        m.put("成交额", raw.get("amount"));
        return m;
    }
}
