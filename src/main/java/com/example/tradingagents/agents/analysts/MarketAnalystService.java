package com.example.tradingagents.agents.analysts;

import com.agent4j.api.Agent;
import com.agent4j.api.AgentRunner;
import com.agent4j.api.RunRequest;
import com.agent4j.api.RunResult;
import com.agent4j.api.Tool;
import com.agent4j.core.AgentDefinition;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class MarketAnalystService {

    private static final String SYSTEM_PROMPT = "你是 A 股市场技术分析师。请使用 get_stock_daily、get_indicators 等工具获取行情与技术指标后，用中文撰写简洁的行情/技术分析报告，包括趋势、支撑阻力、技术指标解读（如 MACD、RSI）。";

    private final AgentRunner agentRunner;
    private final List<Tool> tushareTools;

    public MarketAnalystService(AgentRunner agentRunner, List<Tool> tushareTools) {
        this.agentRunner = agentRunner;
        this.tushareTools = tushareTools;
    }

    public String produceReport(String symbol, LocalDate tradeDate) {
        String userMessage = "标的: " + symbol + ", 交易日期: " + tradeDate
                + "\n\n请先调用 get_stock_daily（建议 start_date 取交易日前约 60 天）、get_indicators 获取数据，再撰写分析报告。";

        Agent agent = new AgentDefinition()
                .setName("market-analyst")
                .setInstructions(SYSTEM_PROMPT)
                .setTools(tushareTools)
                .build();
        RunRequest request = RunRequest.builder()
                .input(userMessage)
                .maxTurns(10)
                .build();
        RunResult result = agentRunner.run(agent, request);
        Object output = result != null ? result.getFinalOutput() : null;
        return output != null ? output.toString() : "";
    }
}
