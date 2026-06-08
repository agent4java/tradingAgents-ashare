package com.example.tradingagents.api;

import com.agent4j.api.Agent;
import com.agent4j.api.RunEvent;
import com.agent4j.core.AgentDefinition;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TradingStreamEventTest {

    @Test
    void mapsAgentNamesToFrontendStages() {
        assertStage("market-analyst", "market_analysis", "市场分析");
        assertStage("sentiment-analyst", "social_sentiment", "社交情绪分析");
        assertStage("news-analyst", "news_analysis", "新闻分析");
        assertStage("fundamentals-analyst", "fundamentals_analysis", "基本面分析");
        assertStage("bull-researcher", "invest_debate", "投资辩论");
        assertStage("risk-judge", "risk_debate", "风险辩论");
        assertStage("portfolio-manager", "portfolio_manager", "经理决策");
    }

    @Test
    void keepsOnlyGraphRunCompletedAsGlobalCompletion() {
        Agent graphAgent = new AgentDefinition().setName("trading-graph").build();
        Agent marketAgent = new AgentDefinition().setName("market-analyst").build();

        TradingStreamEvent graphCompleted = TradingStreamEvent.fromRunEvent(
                RunEvent.of(RunEvent.Type.RUN_COMPLETED, graphAgent, "trading-graph", null, 0));
        TradingStreamEvent agentCompleted = TradingStreamEvent.fromRunEvent(
                RunEvent.of(RunEvent.Type.RUN_COMPLETED, marketAgent, "run", null, 1));

        assertThat(graphCompleted.getType()).isEqualTo("run_completed");
        assertThat(graphCompleted.getScope()).isEqualTo("graph");
        assertThat(agentCompleted.getType()).isEqualTo("agent_run_completed");
        assertThat(agentCompleted.getScope()).isEqualTo("agent");
    }

    private static void assertStage(String agentName, String stage, String stageTitle) {
        Agent agent = new AgentDefinition().setName(agentName).build();
        TradingStreamEvent event = TradingStreamEvent.fromRunEvent(
                RunEvent.of(RunEvent.Type.MODEL_DELTA, agent, "model", "delta", 1));

        assertThat(event.getAgentName()).isEqualTo(agentName);
        assertThat(event.getStage()).isEqualTo(stage);
        assertThat(event.getStageTitle()).isEqualTo(stageTitle);
    }
}
