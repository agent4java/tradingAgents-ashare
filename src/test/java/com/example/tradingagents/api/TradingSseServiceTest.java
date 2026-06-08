package com.example.tradingagents.api;

import com.agent4j.api.Agent;
import com.agent4j.api.RunConfig;
import com.agent4j.api.RunEvent;
import com.agent4j.core.AgentDefinition;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TradingSseServiceTest {

    @Test
    void createStreamConfigEnablesModelStreaming() {
        TradingSseService service = new TradingSseService(null);

        RunConfig config = service.createStreamConfig(event -> {
        });

        assertThat(config.isStreamModel()).isTrue();
        assertThat(config.getEventConsumer()).isNotNull();
    }

    @Test
    void createStreamConfigMapsRunEventsToTradingStreamEvents() {
        TradingSseService service = new TradingSseService(null);
        List<TradingStreamEvent> events = new ArrayList<>();
        Agent agent = new AgentDefinition().setName("market-analyst").build();

        RunConfig config = service.createStreamConfig(events::add);
        config.getEventConsumer().accept(RunEvent.of(RunEvent.Type.MODEL_DELTA, agent, "model", "hi", 1));
        config.getEventConsumer().accept(RunEvent.of(RunEvent.Type.RUN_FAILED, agent, "run", new IllegalStateException("boom"), 1));

        assertThat(events).hasSize(2);
        assertThat(events.get(0).getType()).isEqualTo("model_delta");
        assertThat(events.get(0).getDelta()).isEqualTo("hi");
        assertThat(events.get(0).getAgentName()).isEqualTo("market-analyst");
        assertThat(events.get(0).getStage()).isEqualTo("market_analysis");
        assertThat(events.get(0).getStageTitle()).isEqualTo("市场分析");
        assertThat(events.get(0).getScope()).isEqualTo("agent");
        assertThat(events.get(1).getType()).isEqualTo("agent_run_failed");
        assertThat(events.get(1).getError()).isEqualTo("boom");
        assertThat(events.get(1).getStage()).isEqualTo("market_analysis");
    }

    @Test
    void createStreamConfigDoesNotExposeAgentRunCompletedAsGraphCompletion() {
        TradingSseService service = new TradingSseService(null);
        List<TradingStreamEvent> events = new ArrayList<>();
        Agent agent = new AgentDefinition().setName("market-analyst").build();

        RunConfig config = service.createStreamConfig(events::add);
        config.getEventConsumer().accept(RunEvent.of(RunEvent.Type.RUN_COMPLETED, agent, "run", null, 1));

        assertThat(events).hasSize(1);
        assertThat(events.get(0).getType()).isEqualTo("agent_run_completed");
        assertThat(events.get(0).getScope()).isEqualTo("agent");
        assertThat(events.get(0).getStage()).isEqualTo("market_analysis");
    }
}
