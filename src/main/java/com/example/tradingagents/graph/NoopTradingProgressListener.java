package com.example.tradingagents.graph;

import com.example.tradingagents.domain.AgentState;

/**
 * Default no-op implementation for non-streaming use cases.
 */
public class NoopTradingProgressListener implements TradingProgressListener {

    @Override
    public void onStage(ThinkingStage stage, AgentState state) {
        // no-op
    }
}

