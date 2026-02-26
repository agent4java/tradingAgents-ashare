package com.example.tradingagents.agents.analysts;

import com.example.tradingagents.data.api.NewsService;
import com.finagent.api.Agent;
import com.finagent.api.AgentRunner;
import com.finagent.api.RunRequest;
import com.finagent.api.RunResult;
import com.finagent.core.AgentDefinition;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SentimentAnalystService {

    private static final String SYSTEM_PROMPT = "你是 A 股情绪分析师。根据给定的新闻与舆情信息，用中文撰写简洁的市场情绪报告，包括多空情绪、热点、短期情绪倾向。";

    private final NewsService newsService;
    private final AgentRunner agentRunner;

    public SentimentAnalystService(NewsService newsService, AgentRunner agentRunner) {
        this.newsService = newsService;
        this.agentRunner = agentRunner;
    }

    public String produceReport(String symbol, LocalDate tradeDate) {
        List<Map<String, Object>> news = newsService.getNews(symbol, tradeDate, 20);
        String dataContext = news.stream()
                .map(m -> String.valueOf(m.get("title")) + ": " + String.valueOf(m.get("content")))
                .collect(Collectors.joining("\n"));
        if (dataContext.isEmpty()) {
            dataContext = "暂无新闻数据。";
        }
        String userMessage = "标的: " + symbol + ", 交易日期: " + tradeDate + "\n\n新闻与舆情:\n" + dataContext;

        Agent agent = new AgentDefinition()
                .setName("sentiment-analyst")
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
