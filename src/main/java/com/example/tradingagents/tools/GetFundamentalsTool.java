package com.example.tradingagents.tools;

import com.example.tradingagents.data.api.FundamentalsService;
import com.agent4j.api.Tool;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Component
public class GetFundamentalsTool implements Tool {

    private static final Map<String, Object> PARAM_SCHEMA = Map.of(
            "type", "object",
            "properties", Map.of(
                    "symbol", Map.of("type", "string", "description", "A股代码，如 600519.SH"),
                    "trade_date", Map.of("type", "string", "description", "交易日期 YYYY-MM-DD")
            ),
            "required", List.of("symbol", "trade_date")
    );

    private final FundamentalsService fundamentalsService;

    public GetFundamentalsTool(FundamentalsService fundamentalsService) {
        this.fundamentalsService = fundamentalsService;
    }

    @Override
    public String getName() {
        return "get_fundamentals";
    }

    @Override
    public String getDescription() {
        return "获取基本面主要指标（PE、PB、营收、利润等）。需要 symbol、trade_date，日期格式 YYYY-MM-DD。";
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
        if (symbol == null || symbol.isEmpty()) {
            return "错误: 缺少参数 symbol。";
        }
        if (tradeDate == null) {
            return ToolArgHelper.errorInvalidDate("trade_date");
        }
        try {
            Map<String, Object> fund = fundamentalsService.getFundamentals(symbol, tradeDate);
            return fund != null ? fund.toString() : "暂无基本面数据。";
        } catch (Exception e) {
            return "获取基本面失败: " + (e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
        }
    }
}
