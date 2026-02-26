package com.example.tradingagents.domain;

import java.util.ArrayList;
import java.util.List;

/** State for bull vs bear researcher debate and investment judge. */
public class InvestDebateState {

    private List<String> bullHistory = new ArrayList<>();
    private List<String> bearHistory = new ArrayList<>();
    private List<String> history = new ArrayList<>();
    private String currentResponse;
    private String judgeDecision;

    public List<String> getBullHistory() {
        return bullHistory;
    }

    public void setBullHistory(List<String> bullHistory) {
        this.bullHistory = bullHistory != null ? bullHistory : new ArrayList<>();
    }

    public List<String> getBearHistory() {
        return bearHistory;
    }

    public void setBearHistory(List<String> bearHistory) {
        this.bearHistory = bearHistory != null ? bearHistory : new ArrayList<>();
    }

    public List<String> getHistory() {
        return history;
    }

    public void setHistory(List<String> history) {
        this.history = history != null ? history : new ArrayList<>();
    }

    public String getCurrentResponse() {
        return currentResponse;
    }

    public void setCurrentResponse(String currentResponse) {
        this.currentResponse = currentResponse;
    }

    public String getJudgeDecision() {
        return judgeDecision;
    }

    public void setJudgeDecision(String judgeDecision) {
        this.judgeDecision = judgeDecision;
    }
}
