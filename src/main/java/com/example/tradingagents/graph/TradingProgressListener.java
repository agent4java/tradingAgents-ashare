package com.example.tradingagents.graph;

import com.example.tradingagents.domain.AgentState;

/**
 * Listener for streaming trading pipeline progress.
 */
public interface TradingProgressListener {

    void onStage(ThinkingStage stage, AgentState state);
}

