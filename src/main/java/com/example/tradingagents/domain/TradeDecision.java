package com.example.tradingagents.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * Final trade decision with parsed signal (action, strength, reason).
 * Aligned with reference process_signal(final_trade_decision).
 */
public class TradeDecision {

    public enum Action {
        BUY,
        SELL,
        HOLD
    }

    private Action action = Action.HOLD;
    private String strength;   // e.g. "strong", "moderate", "weak"
    private String reason;
    private String rawDecision; // original text from portfolio manager
    private List<String> details = new ArrayList<>();

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public String getStrength() {
        return strength;
    }

    public void setStrength(String strength) {
        this.strength = strength;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRawDecision() {
        return rawDecision;
    }

    public void setRawDecision(String rawDecision) {
        this.rawDecision = rawDecision;
    }

    public List<String> getDetails() {
        return details;
    }

    public void setDetails(List<String> details) {
        this.details = details != null ? details : new ArrayList<>();
    }
}
