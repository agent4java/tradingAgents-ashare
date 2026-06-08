package com.example.tradingagents.api;

import java.util.Map;

public enum TradingStreamStage {
    TRADING_GRAPH("trading_graph", "交易编排"),
    MARKET_ANALYSIS("market_analysis", "市场分析"),
    SOCIAL_SENTIMENT("social_sentiment", "社交情绪分析"),
    NEWS_ANALYSIS("news_analysis", "新闻分析"),
    FUNDAMENTALS_ANALYSIS("fundamentals_analysis", "基本面分析"),
    INVEST_DEBATE("invest_debate", "投资辩论"),
    TRADER_DECISION("trader_decision", "交易员决策"),
    RISK_DEBATE("risk_debate", "风险辩论"),
    PORTFOLIO_MANAGER("portfolio_manager", "经理决策"),
    UNKNOWN("unknown", "未知阶段");

    private static final Map<String, TradingStreamStage> AGENT_STAGE = Map.ofEntries(
            Map.entry("trading-graph", TRADING_GRAPH),
            Map.entry("market-analyst", MARKET_ANALYSIS),
            Map.entry("sentiment-analyst", SOCIAL_SENTIMENT),
            Map.entry("news-analyst", NEWS_ANALYSIS),
            Map.entry("fundamentals-analyst", FUNDAMENTALS_ANALYSIS),
            Map.entry("bull-researcher", INVEST_DEBATE),
            Map.entry("bear-researcher", INVEST_DEBATE),
            Map.entry("invest-judge", INVEST_DEBATE),
            Map.entry("trader", TRADER_DECISION),
            Map.entry("risk-aggressive", RISK_DEBATE),
            Map.entry("risk-conservative", RISK_DEBATE),
            Map.entry("risk-judge", RISK_DEBATE),
            Map.entry("portfolio-manager", PORTFOLIO_MANAGER)
    );

    private final String code;
    private final String title;

    TradingStreamStage(String code, String title) {
        this.code = code;
        this.title = title;
    }

    public static TradingStreamStage fromAgentName(String agentName) {
        if (agentName == null || agentName.isBlank()) {
            return UNKNOWN;
        }
        return AGENT_STAGE.getOrDefault(agentName, UNKNOWN);
    }

    public String getCode() {
        return code;
    }

    public String getTitle() {
        return title;
    }
}
