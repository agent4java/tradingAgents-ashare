package com.example.tradingagents.agents.analysts;

import com.finagent.api.Agent;
import com.finagent.api.AgentRunner;
import com.finagent.api.RunRequest;
import com.finagent.api.RunResult;
import com.finagent.api.Tool;
import com.finagent.core.AgentDefinition;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class NewsAnalystService {

    private static final String SYSTEM_PROMPT = "你是 A 股新闻分析师。请使用 get_news、get_global_news 工具获取公司新闻与宏观资讯后，用中文撰写简洁的新闻影响报告，包括对股价的潜在影响、重要事件解读。";

    private final AgentRunner agentRunner;
    private final List<Tool> tushareTools;

    public NewsAnalystService(AgentRunner agentRunner, List<Tool> tushareTools) {
        this.agentRunner = agentRunner;
        this.tushareTools = tushareTools;
    }

    public String produceReport(String symbol, LocalDate tradeDate) {
        String userMessage = "标的: " + symbol + ", 交易日期: " + tradeDate
                + "\n\n请先调用 get_news（标的相关）、get_global_news（宏观/市场）获取新闻数据，再撰写分析报告。";

        Agent agent = new AgentDefinition()
                .setName("news-analyst")
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
