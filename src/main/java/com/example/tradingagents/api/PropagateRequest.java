package com.example.tradingagents.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public class PropagateRequest {

    @NotBlank(message = "symbol is required")
    @Pattern(regexp = "^[0-9]{6}\\.(SH|SZ|BJ)$", message = "symbol must be A-share code e.g. 600519.SH")
    private String symbol;

    @NotBlank(message = "tradeDate is required")
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$", message = "tradeDate must be yyyy-MM-dd")
    private String tradeDate;

    private List<String> selectedAnalysts;
    private Integer maxDebateRounds;
    private Integer maxRiskDiscussRounds;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(String tradeDate) {
        this.tradeDate = tradeDate;
    }

    public List<String> getSelectedAnalysts() {
        return selectedAnalysts;
    }

    public void setSelectedAnalysts(List<String> selectedAnalysts) {
        this.selectedAnalysts = selectedAnalysts;
    }

    public Integer getMaxDebateRounds() {
        return maxDebateRounds;
    }

    public void setMaxDebateRounds(Integer maxDebateRounds) {
        this.maxDebateRounds = maxDebateRounds;
    }

    public Integer getMaxRiskDiscussRounds() {
        return maxRiskDiscussRounds;
    }

    public void setMaxRiskDiscussRounds(Integer maxRiskDiscussRounds) {
        this.maxRiskDiscussRounds = maxRiskDiscussRounds;
    }
}
