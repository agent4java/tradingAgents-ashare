package com.example.tradingagents.graph;

/**
 * Stages of the trading agents pipeline, used for streaming progress.
 */
public enum ThinkingStage {
    ANALYST_MARKET,
    ANALYST_SENTIMENT,
    ANALYST_NEWS,
    ANALYST_FUNDAMENTALS,
    INVEST_DEBATE,
    TRADER,
    RISK_DEBATE,
    PORTFOLIO,
    FINAL_DECISION
}

