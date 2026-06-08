package com.example.tradingagents.api;

import com.agent4j.api.Agent;
import com.agent4j.api.AgentStreamEvent;
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
    void createStreamConfigMapsRunEventsToAgentStreamEvents() {
        TradingSseService service = new TradingSseService(null);
        List<AgentStreamEvent> events = new ArrayList<>();
        Agent agent = new AgentDefinition().setName("assistant").build();

        RunConfig config = service.createStreamConfig(events::add);
        config.getEventConsumer().accept(RunEvent.of(RunEvent.Type.MODEL_DELTA, agent, "model", "hi", 1));
        config.getEventConsumer().accept(RunEvent.of(RunEvent.Type.RUN_FAILED, agent, "run", new IllegalStateException("boom"), 1));

        assertThat(events).hasSize(2);
        assertThat(events.get(0).getType()).isEqualTo("model_delta");
        assertThat(events.get(0).getDelta()).isEqualTo("hi");
        assertThat(events.get(1).getType()).isEqualTo("run_failed");
        assertThat(events.get(1).getError()).isEqualTo("boom");
    }
}
