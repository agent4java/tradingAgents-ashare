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
public class SentimentAnalystService {

    private static final String SYSTEM_PROMPT = "你是 A 股情绪分析师。请使用 get_news、get_global_news 工具获取新闻与舆情信息后，用中文撰写简洁的市场情绪报告，包括多空情绪、热点、短期情绪倾向。";

    private final AgentRunner agentRunner;
    private final List<Tool> tushareTools;

    public SentimentAnalystService(AgentRunner agentRunner, List<Tool> tushareTools) {
        this.agentRunner = agentRunner;
        this.tushareTools = tushareTools;
    }

    public String produceReport(String symbol, LocalDate tradeDate) {
        String userMessage = "标的: " + symbol + ", 交易日期: " + tradeDate
                + "\n\n请先调用 get_news、get_global_news 获取新闻与舆情数据，再撰写情绪分析报告。";

        Agent agent = new AgentDefinition()
                .setName("sentiment-analyst")
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
