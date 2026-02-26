package com.example.tradingagents.data.tushare;

import com.example.tradingagents.data.api.NewsService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@ConditionalOnProperty(name = "tradingagents.data.vendor", havingValue = "tushare")
public class TushareNewsService implements NewsService {

    private static final DateTimeFormatter TUSHARE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final TushareClient client;

    public TushareNewsService(TushareClient client) {
        this.client = client;
    }

    @Override
    public List<Map<String, Object>> getNews(String symbol, LocalDate tradeDate, int limit) {
        Map<String, String> params = new HashMap<>();
        params.put("ts_code", symbol);
        String start = tradeDate.minusDays(7).format(TUSHARE_DATE);
        String end = tradeDate.format(TUSHARE_DATE);
        params.put("start_date", start);
        params.put("end_date", end);
        String fields = "ts_code,ann_date,title,content";
        List<Map<String, Object>> rows = client.request("ann_main", params, fields);
        if (rows.isEmpty()) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (int i = 0; i < Math.min(limit, rows.size()); i++) {
            Map<String, Object> r = rows.get(i);
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("title", r.get("title"));
            item.put("content", r.get("content"));
            item.put("date", r.get("ann_date"));
            item.put("source", "公司公告");
            result.add(item);
        }
        return result;
    }

    @Override
    public List<Map<String, Object>> getGlobalNews(LocalDate tradeDate, int limit) {
        // Tushare may have cn_news or similar; placeholder return
        return Collections.emptyList();
    }
}
