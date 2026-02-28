package com.example.tradingagents.tools;

import com.example.tradingagents.data.api.NewsService;
import com.agent4j.api.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GetNewsTool implements Tool {

    private static final int DEFAULT_LIMIT = 10;
    private static final Map<String, Object> PARAM_SCHEMA = Map.of(
            "type", "object",
            "properties", Map.of(
                    "symbol", Map.of("type", "string", "description", "A股代码，如 600519.SH"),
                    "trade_date", Map.of("type", "string", "description", "交易日期 YYYY-MM-DD"),
                    "limit", Map.of("type", "integer", "description", "返回条数，可选，默认 10")
            ),
            "required", List.of("symbol", "trade_date")
    );

    private final NewsService newsService;

    public GetNewsTool(NewsService newsService) {
        this.newsService = newsService;
    }

    @Override
    public String getName() {
        return "get_news";
    }

    @Override
    public String getDescription() {
        return "获取标的相关新闻。需要 symbol、trade_date；可选 limit（默认 10）。日期格式 YYYY-MM-DD。";
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        return PARAM_SCHEMA;
    }

    @Override
    public Object invoke(Tool.ToolContext context) {
        Map<String, Object> args = ToolArgHelper.parseArgs(context.getArgumentsJson());
        String symbol = ToolArgHelper.getString(args, "symbol");
        LocalDate tradeDate = ToolArgHelper.parseDate(args, "trade_date");
        int limit = ToolArgHelper.getInt(args, "limit", DEFAULT_LIMIT);
        if (symbol == null || symbol.isEmpty()) {
            return "错误: 缺少参数 symbol。";
        }
        if (tradeDate == null) {
            return ToolArgHelper.errorInvalidDate("trade_date");
        }
        try {
            List<Map<String, Object>> news = newsService.getNews(symbol, tradeDate, limit);
            if (news == null || news.isEmpty()) {
                return "暂无该标的新闻。";
            }
            return news.stream()
                    .map(m -> String.valueOf(m.get("title")) + ": " + String.valueOf(m.get("content")))
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "获取新闻失败: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        }
    }
}
