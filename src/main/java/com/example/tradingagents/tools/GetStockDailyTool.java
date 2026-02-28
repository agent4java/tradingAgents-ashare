package com.example.tradingagents.tools;

import com.example.tradingagents.data.api.StockDataService;
import com.agent4j.api.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GetStockDailyTool implements Tool {

    private static final Map<String, Object> PARAM_SCHEMA = Map.of(
            "type", "object",
            "properties", Map.of(
                    "symbol", Map.of("type", "string", "description", "A股代码，如 600519.SH"),
                    "start_date", Map.of("type", "string", "description", "开始日期 YYYY-MM-DD"),
                    "end_date", Map.of("type", "string", "description", "结束日期 YYYY-MM-DD")
            ),
            "required", List.of("symbol", "start_date", "end_date")
    );

    private final StockDataService stockDataService;

    public GetStockDailyTool(StockDataService stockDataService) {
        this.stockDataService = stockDataService;
    }

    @Override
    public String getName() {
        return "get_stock_daily";
    }

    @Override
    public String getDescription() {
        return "获取 A 股日线行情（OHLCV）。需要 symbol、start_date、end_date，日期格式 YYYY-MM-DD。";
    }

    @Override
    public Map<String, Object> getParameterSchema() {
        return PARAM_SCHEMA;
    }

    @Override
    public Object invoke(Tool.ToolContext context) {
        Map<String, Object> args = ToolArgHelper.parseArgs(context.getArgumentsJson());
        String symbol = ToolArgHelper.getString(args, "symbol");
        LocalDate start = ToolArgHelper.parseDate(args, "start_date");
        LocalDate end = ToolArgHelper.parseDate(args, "end_date");
        if (symbol == null || symbol.isEmpty()) {
            return "错误: 缺少参数 symbol。";
        }
        if (start == null) {
            return ToolArgHelper.errorInvalidDate("start_date");
        }
        if (end == null) {
            return ToolArgHelper.errorInvalidDate("end_date");
        }
        try {
            List<Map<String, Object>> daily = stockDataService.getDaily(symbol, start, end);
            if (daily.isEmpty()) {
                return "该区间内无日线数据。";
            }
            return daily.stream()
                    .map(Map::toString)
                    .collect(Collectors.joining("\n"));
        } catch (Exception e) {
            return "获取日线失败: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        }
    }
}
