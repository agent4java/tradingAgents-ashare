package com.example.tradingagents.api;

import com.example.tradingagents.domain.TradeDecision;

public class PropagateResponse {

    private TradeDecision decision;
    private String rawDecision;
    private boolean includeState;

    public TradeDecision getDecision() {
        return decision;
    }

    public void setDecision(TradeDecision decision) {
        this.decision = decision;
    }

    public String getRawDecision() {
        return rawDecision;
    }

    public void setRawDecision(String rawDecision) {
        this.rawDecision = rawDecision;
    }

    public boolean isIncludeState() {
        return includeState;
    }

    public void setIncludeState(boolean includeState) {
        this.includeState = includeState;
    }
}
