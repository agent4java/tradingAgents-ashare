package com.example.tradingagents.domain;

/**
 * Full pipeline state: company, date, analyst reports, debate states, investment plan, final decision.
 * Aligned with reference AgentState / final_state.
 */
public class AgentState {

    private String companyOfInterest;  // symbol, e.g. 600519.SH
    private String tradeDate;

    private String marketReport;
    private String sentimentReport;
    private String newsReport;
    private String fundamentalsReport;

    private InvestDebateState investmentDebateState = new InvestDebateState();
    private String traderInvestmentPlan;   // trader output
    private RiskDebateState riskDebateState = new RiskDebateState();
    private String investmentPlan;        // risk/portfolio output summary
    private String finalTradeDecision;    // portfolio manager final text

    private TradeDecision processedDecision;  // parsed signal

    public String getCompanyOfInterest() {
        return companyOfInterest;
    }

    public void setCompanyOfInterest(String companyOfInterest) {
        this.companyOfInterest = companyOfInterest;
    }

    public String getTradeDate() {
        return tradeDate;
    }

    public void setTradeDate(String tradeDate) {
        this.tradeDate = tradeDate;
    }

    public String getMarketReport() {
        return marketReport;
    }

    public void setMarketReport(String marketReport) {
        this.marketReport = marketReport;
    }

    public String getSentimentReport() {
        return sentimentReport;
    }

    public void setSentimentReport(String sentimentReport) {
        this.sentimentReport = sentimentReport;
    }

    public String getNewsReport() {
        return newsReport;
    }

    public void setNewsReport(String newsReport) {
        this.newsReport = newsReport;
    }

    public String getFundamentalsReport() {
        return fundamentalsReport;
    }

    public void setFundamentalsReport(String fundamentalsReport) {
        this.fundamentalsReport = fundamentalsReport;
    }

    public InvestDebateState getInvestmentDebateState() {
        return investmentDebateState;
    }

    public void setInvestmentDebateState(InvestDebateState investmentDebateState) {
        this.investmentDebateState = investmentDebateState != null ? investmentDebateState : new InvestDebateState();
    }

    public String getTraderInvestmentPlan() {
        return traderInvestmentPlan;
    }

    public void setTraderInvestmentPlan(String traderInvestmentPlan) {
        this.traderInvestmentPlan = traderInvestmentPlan;
    }

    public RiskDebateState getRiskDebateState() {
        return riskDebateState;
    }

    public void setRiskDebateState(RiskDebateState riskDebateState) {
        this.riskDebateState = riskDebateState != null ? riskDebateState : new RiskDebateState();
    }

    public String getInvestmentPlan() {
        return investmentPlan;
    }

    public void setInvestmentPlan(String investmentPlan) {
        this.investmentPlan = investmentPlan;
    }

    public String getFinalTradeDecision() {
        return finalTradeDecision;
    }

    public void setFinalTradeDecision(String finalTradeDecision) {
        this.finalTradeDecision = finalTradeDecision;
    }

    public TradeDecision getProcessedDecision() {
        return processedDecision;
    }

    public void setProcessedDecision(TradeDecision processedDecision) {
        this.processedDecision = processedDecision;
    }
}
