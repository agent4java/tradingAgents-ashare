package com.example.tradingagents.agents.analysts;

import com.example.tradingagents.data.api.FundamentalsService;
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
public class FundamentalsAnalystService {

    private static final String SYSTEM_PROMPT = "你是 A 股基本面分析师。根据给定的财务指标、资产负债表、现金流、利润表，用中文撰写简洁的基本面分析报告，包括估值、盈利能力、偿债能力、风险点。";

    private final FundamentalsService fundamentalsService;
    private final AgentRunner agentRunner;

    public FundamentalsAnalystService(FundamentalsService fundamentalsService, AgentRunner agentRunner) {
        this.fundamentalsService = fundamentalsService;
        this.agentRunner = agentRunner;
    }

    public String produceReport(String symbol, LocalDate tradeDate) {
        Map<String, Object> fund = fundamentalsService.getFundamentals(symbol, tradeDate);
        List<Map<String, Object>> bs = fundamentalsService.getBalanceSheet(symbol, tradeDate, 3);
        List<Map<String, Object>> cf = fundamentalsService.getCashflow(symbol, tradeDate, 3);
        List<Map<String, Object>> inc = fundamentalsService.getIncomeStatement(symbol, tradeDate, 3);
        String dataContext = "财务指标: " + fund + "\n资产负债表(近几期): " + bs + "\n现金流: " + cf + "\n利润表: " + inc;
        String userMessage = "标的: " + symbol + ", 交易日期: " + tradeDate + "\n\n" + dataContext;

        Agent agent = new AgentDefinition()
                .setName("fundamentals-analyst")
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
}
