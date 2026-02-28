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
public class FundamentalsAnalystService {

    private static final String SYSTEM_PROMPT = "你是 A 股基本面分析师。请使用 get_fundamentals 工具获取财务指标后，用中文撰写简洁的基本面分析报告，包括估值、盈利能力、偿债能力、风险点。";

    private final AgentRunner agentRunner;
    private final List<Tool> tushareTools;

    public FundamentalsAnalystService(AgentRunner agentRunner, List<Tool> tushareTools) {
        this.agentRunner = agentRunner;
        this.tushareTools = tushareTools;
    }

    public String produceReport(String symbol, LocalDate tradeDate) {
        String userMessage = "标的: " + symbol + ", 交易日期: " + tradeDate
                + "\n\n请先调用 get_fundamentals 获取基本面数据，再撰写分析报告。";

        Agent agent = new AgentDefinition()
                .setName("fundamentals-analyst")
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
