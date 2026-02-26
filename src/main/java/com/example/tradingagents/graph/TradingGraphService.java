package com.example.tradingagents.graph;

import com.example.tradingagents.agents.analysts.FundamentalsAnalystService;
import com.example.tradingagents.agents.analysts.MarketAnalystService;
import com.example.tradingagents.agents.analysts.NewsAnalystService;
import com.example.tradingagents.agents.analysts.SentimentAnalystService;
import com.example.tradingagents.agents.portfolio.PortfolioManagerService;
import com.example.tradingagents.agents.researchers.InvestDebateService;
import com.example.tradingagents.agents.risk.RiskDebateService;
import com.example.tradingagents.agents.trader.TraderService;
import com.example.tradingagents.domain.AgentState;
import com.example.tradingagents.domain.TradeDecision;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

/**
 * Main entry: propagate(symbol, tradeDate) runs the full pipeline and returns (state, processed decision).
 */
@Service
public class TradingGraphService {

    private final MarketAnalystService marketAnalyst;
    private final SentimentAnalystService sentimentAnalyst;
    private final NewsAnalystService newsAnalyst;
    private final FundamentalsAnalystService fundamentalsAnalyst;
    private final InvestDebateService investDebateService;
    private final TraderService traderService;
    private final RiskDebateService riskDebateService;
    private final PortfolioManagerService portfolioManager;
    private final SignalProcessor signalProcessor;

    public TradingGraphService(MarketAnalystService marketAnalyst,
                               SentimentAnalystService sentimentAnalyst,
                               NewsAnalystService newsAnalyst,
                               FundamentalsAnalystService fundamentalsAnalyst,
                               InvestDebateService investDebateService,
                               TraderService traderService,
                               RiskDebateService riskDebateService,
                               PortfolioManagerService portfolioManager,
                               SignalProcessor signalProcessor) {
        this.marketAnalyst = marketAnalyst;
        this.sentimentAnalyst = sentimentAnalyst;
        this.newsAnalyst = newsAnalyst;
        this.fundamentalsAnalyst = fundamentalsAnalyst;
        this.investDebateService = investDebateService;
        this.traderService = traderService;
        this.riskDebateService = riskDebateService;
        this.portfolioManager = portfolioManager;
        this.signalProcessor = signalProcessor;
    }

    /**
     * Run trading agents pipeline for symbol on tradeDate. selectedAnalysts: "market", "social", "news", "fundamentals".
     */
    public PropagateResult propagate(String symbol, String tradeDateStr, List<String> selectedAnalysts) {
        LocalDate tradeDate = LocalDate.parse(tradeDateStr);
        AgentState state = new AgentState();
        state.setCompanyOfInterest(symbol);
        state.setTradeDate(tradeDateStr);

        List<String> analysts = selectedAnalysts != null && !selectedAnalysts.isEmpty()
                ? selectedAnalysts
                : List.of("market", "social", "news", "fundamentals");

        if (analysts.contains("market")) {
            state.setMarketReport(marketAnalyst.produceReport(symbol, tradeDate));
        }
        if (analysts.contains("social")) {
            state.setSentimentReport(sentimentAnalyst.produceReport(symbol, tradeDate));
        }
        if (analysts.contains("news")) {
            state.setNewsReport(newsAnalyst.produceReport(symbol, tradeDate));
        }
        if (analysts.contains("fundamentals")) {
            state.setFundamentalsReport(fundamentalsAnalyst.produceReport(symbol, tradeDate));
        }

        investDebateService.runDebate(state);
        traderService.producePlan(state);
        riskDebateService.runDebate(state);
        portfolioManager.produceFinalDecision(state);

        TradeDecision decision = signalProcessor.processSignal(state.getFinalTradeDecision());
        state.setProcessedDecision(decision);

        return new PropagateResult(state, decision);
    }

    public PropagateResult propagate(String symbol, String tradeDate) {
        return propagate(symbol, tradeDate, null);
    }

    public static class PropagateResult {
        private final AgentState state;
        private final TradeDecision decision;

        public PropagateResult(AgentState state, TradeDecision decision) {
            this.state = state;
            this.decision = decision;
        }

        public AgentState getState() {
            return state;
        }

        public TradeDecision getDecision() {
            return decision;
        }
    }
}
