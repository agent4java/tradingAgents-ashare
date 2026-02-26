package com.example.tradingagents.agents.analysts;

import com.example.tradingagents.data.api.IndicatorsService;
import com.example.tradingagents.data.api.StockDataService;
import com.finagent.api.Agent;
import com.finagent.api.AgentRunner;
import com.finagent.api.RunRequest;
import com.finagent.api.RunResult;
import com.finagent.core.AgentDefinition;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class MarketAnalystService {

    private static final String SYSTEM_PROMPT = "你是 A 股市场技术分析师。根据给定的行情与技术指标，用中文撰写简洁的行情/技术分析报告，包括趋势、支撑阻力、技术指标解读（如 MACD、RSI）。";

    private final StockDataService stockDataService;
    private final IndicatorsService indicatorsService;
    private final AgentRunner agentRunner;

    public MarketAnalystService(StockDataService stockDataService,
                                IndicatorsService indicatorsService,
                                AgentRunner agentRunner) {
        this.stockDataService = stockDataService;
        this.indicatorsService = indicatorsService;
        this.agentRunner = agentRunner;
    }

    public String produceReport(String symbol, LocalDate tradeDate) {
        LocalDate start = tradeDate.minusDays(60);
        List<Map<String, Object>> daily = stockDataService.getDaily(symbol, start, tradeDate);
        Map<String, Object> indicators = indicatorsService.getIndicators(symbol, tradeDate);
        String dataContext = formatData(daily, indicators);
        String userMessage = "标的: " + symbol + ", 交易日期: " + tradeDate + "\n\n行情与指标数据:\n" + dataContext;

        Agent agent = new AgentDefinition()
                .setName("market-analyst")
                .setInstructions(SYSTEM_PROMPT)
                .build();
        RunRequest request = RunRequest.builder()
                .input(userMessage)
                .maxTurns(10)
                .build();
        RunResult result = agentRunner.run(agent, request);
        Object output = result != null ? result.getFinalOutput() : null;
        return output != null ? output.toString() : "";
    }

    private String formatData(List<Map<String, Object>> daily, Map<String, Object> indicators) {
        StringBuilder sb = new StringBuilder();
        if (!daily.isEmpty()) {
            sb.append("近期日线(最近几条):\n");
            daily.stream().skip(Math.max(0, daily.size() - 10)).forEach(m -> sb.append(m.toString()).append("\n"));
        }
        sb.append("技术指标: ").append(indicators.toString());
        return sb.toString();
    }
}
