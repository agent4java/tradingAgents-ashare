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
        return propagate(symbol, tradeDateStr, selectedAnalysts, new NoopTradingProgressListener());
    }

    /**
     * Run trading agents pipeline with optional progress listener for streaming.
     */
    public PropagateResult propagate(String symbol,
                                     String tradeDateStr,
                                     List<String> selectedAnalysts,
                                     TradingProgressListener listener) {
        TradingProgressListener effectiveListener = listener != null ? listener : new NoopTradingProgressListener();
        LocalDate tradeDate = LocalDate.parse(tradeDateStr);
        AgentState state = new AgentState();
        state.setCompanyOfInterest(symbol);
        state.setTradeDate(tradeDateStr);

        // 分析师阶段：行情、情绪、新闻、基本面
        List<String> analysts = selectedAnalysts != null && !selectedAnalysts.isEmpty()
                ? selectedAnalysts
                : List.of("market", "social", "news", "fundamentals");

        if (analysts.contains("market")) {
            state.setMarketReport(marketAnalyst.produceReport(symbol, tradeDate));
            effectiveListener.onStage(ThinkingStage.ANALYST_MARKET, state);
        }
        if (analysts.contains("social")) {
            state.setSentimentReport(sentimentAnalyst.produceReport(symbol, tradeDate));
            effectiveListener.onStage(ThinkingStage.ANALYST_SENTIMENT, state);
        }
        if (analysts.contains("news")) {
            state.setNewsReport(newsAnalyst.produceReport(symbol, tradeDate));
            effectiveListener.onStage(ThinkingStage.ANALYST_NEWS, state);
        }
        if (analysts.contains("fundamentals")) {
            state.setFundamentalsReport(fundamentalsAnalyst.produceReport(symbol, tradeDate));
            effectiveListener.onStage(ThinkingStage.ANALYST_FUNDAMENTALS, state);
        }

        // 研究投资辩论阶段：多头、空头、裁判
        investDebateService.runDebate(state);
        effectiveListener.onStage(ThinkingStage.INVEST_DEBATE, state);

        // 交易员阶段：投资计划
        traderService.producePlan(state);
        effectiveListener.onStage(ThinkingStage.TRADER, state);

        // 风险辩论阶段：激进、保守、裁判
        riskDebateService.runDebate(state);
        effectiveListener.onStage(ThinkingStage.RISK_DEBATE, state);

        // 组合经理阶段：最终决策
        portfolioManager.produceFinalDecision(state);
        effectiveListener.onStage(ThinkingStage.PORTFOLIO, state);

        TradeDecision decision = signalProcessor.processSignal(state.getFinalTradeDecision());
        state.setProcessedDecision(decision);
        effectiveListener.onStage(ThinkingStage.FINAL_DECISION, state);

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
