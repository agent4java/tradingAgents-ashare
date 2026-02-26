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
public class NewsAnalystService {

    private static final String SYSTEM_PROMPT = "你是 A 股新闻分析师。根据给定的公司新闻与宏观资讯，用中文撰写简洁的新闻影响报告，包括对股价的潜在影响、重要事件解读。";

    private final NewsService newsService;
    private final AgentRunner agentRunner;

    public NewsAnalystService(NewsService newsService, AgentRunner agentRunner) {
        this.newsService = newsService;
        this.agentRunner = agentRunner;
    }

    public String produceReport(String symbol, LocalDate tradeDate) {
        List<Map<String, Object>> news = newsService.getNews(symbol, tradeDate, 20);
        List<Map<String, Object>> global = newsService.getGlobalNews(tradeDate, 10);
        String newsContext = news.stream()
                .map(m -> String.valueOf(m.get("title")) + ": " + String.valueOf(m.get("content")))
                .collect(Collectors.joining("\n"));
        String globalContext = global.stream()
                .map(m -> String.valueOf(m.get("title")) + ": " + String.valueOf(m.get("content")))
                .collect(Collectors.joining("\n"));
        String dataContext = "公司新闻:\n" + (newsContext.isEmpty() ? "无" : newsContext) + "\n\n宏观/市场新闻:\n" + (globalContext.isEmpty() ? "无" : globalContext);
        String userMessage = "标的: " + symbol + ", 交易日期: " + tradeDate + "\n\n" + dataContext;

        Agent agent = new AgentDefinition()
                .setName("news-analyst")
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
