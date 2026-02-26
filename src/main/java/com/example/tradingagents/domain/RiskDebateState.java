package com.example.tradingagents.domain;

import java.util.ArrayList;
import java.util.List;

/** State for risk management debate (aggressive / conservative / neutral) and judge. */
public class RiskDebateState {

    private List<String> aggressiveHistory = new ArrayList<>();
    private List<String> conservativeHistory = new ArrayList<>();
    private List<String> neutralHistory = new ArrayList<>();
    private List<String> history = new ArrayList<>();
    private String judgeDecision;

    public List<String> getAggressiveHistory() {
        return aggressiveHistory;
    }

    public void setAggressiveHistory(List<String> aggressiveHistory) {
        this.aggressiveHistory = aggressiveHistory != null ? aggressiveHistory : new ArrayList<>();
    }

    public List<String> getConservativeHistory() {
        return conservativeHistory;
    }

    public void setConservativeHistory(List<String> conservativeHistory) {
        this.conservativeHistory = conservativeHistory != null ? conservativeHistory : new ArrayList<>();
    }

    public List<String> getNeutralHistory() {
        return neutralHistory;
    }

    public void setNeutralHistory(List<String> neutralHistory) {
        this.neutralHistory = neutralHistory != null ? neutralHistory : new ArrayList<>();
    }

    public List<String> getHistory() {
        return history;
    }

    public void setHistory(List<String> history) {
        this.history = history != null ? history : new ArrayList<>();
    }

    public String getJudgeDecision() {
        return judgeDecision;
    }

    public void setJudgeDecision(String judgeDecision) {
        this.judgeDecision = judgeDecision;
    }
}
