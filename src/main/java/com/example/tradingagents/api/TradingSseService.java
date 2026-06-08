package com.example.tradingagents.api;

import com.agent4j.api.Agent;
import com.agent4j.api.AgentStreamEvent;
import com.agent4j.api.RunConfig;
import com.agent4j.api.RunEvent;
import com.agent4j.api.RunResult;
import com.agent4j.core.AgentDefinition;
import com.example.tradingagents.domain.AgentState;
import com.example.tradingagents.graph.TradingGraphService;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

@Service
public class TradingSseService {

    private static final long DEFAULT_TIMEOUT_MILLIS = 0L;

    private final TradingGraphService tradingGraphService;
    private final Agent graphAgent = new AgentDefinition().setName("trading-graph").build();

    public TradingSseService(TradingGraphService tradingGraphService) {
        this.tradingGraphService = tradingGraphService;
    }

    public SseEmitter stream(PropagateRequest request) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT_MILLIS);
        Consumer<RunEvent> eventConsumer = event -> send(emitter, AgentStreamEvent.fromRunEvent(event));
        RunConfig runConfig = createStreamConfig(event -> send(emitter, event));

        CompletableFuture.runAsync(() -> {
            try {
                eventConsumer.accept(RunEvent.of(RunEvent.Type.RUN_STARTED, graphAgent, "trading-graph", request.getSymbol(), 0));
                TradingGraphService.PropagateResult result = tradingGraphService.propagate(
                        request.getSymbol(),
                        request.getTradeDate(),
                        request.getSelectedAnalysts(),
                        runConfig
                );
                eventConsumer.accept(RunEvent.of(RunEvent.Type.RUN_COMPLETED, graphAgent, "trading-graph",
                        buildGraphRunResult(request, result), 0));
                emitter.complete();
            } catch (Exception e) {
                try {
                    eventConsumer.accept(RunEvent.of(RunEvent.Type.RUN_FAILED, graphAgent, "trading-graph", e, 0));
                } catch (Exception sendFailure) {
                    e.addSuppressed(sendFailure);
                }
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    RunConfig createStreamConfig(Consumer<AgentStreamEvent> streamConsumer) {
        return RunConfig.builder()
                .eventConsumer(event -> streamConsumer.accept(AgentStreamEvent.fromRunEvent(event)))
                .streamModel(true)
                .build();
    }

    private RunResult buildGraphRunResult(PropagateRequest request, TradingGraphService.PropagateResult result) {
        AgentState state = result.getState();
        Map<String, Object> finalOutput = new HashMap<>();
        finalOutput.put("symbol", request.getSymbol());
        finalOutput.put("tradeDate", request.getTradeDate());
        finalOutput.put("decision", result.getDecision());
        finalOutput.put("rawDecision", state.getFinalTradeDecision());

        return RunResult.builder()
                .input(request)
                .lastAgent(graphAgent)
                .rawResponses(List.of())
                .finalOutput(finalOutput)
                .currentTurn(0)
                .maxTurns(0)
                .build();
    }

    private void send(SseEmitter emitter, AgentStreamEvent event) {
        try {
            emitter.send(SseEmitter.event()
                    .name(event.getType())
                    .data(event));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to send trading SSE event", e);
        }
    }
}
