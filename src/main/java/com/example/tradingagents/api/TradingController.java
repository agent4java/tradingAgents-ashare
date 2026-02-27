package com.example.tradingagents.api;

import com.example.tradingagents.domain.AgentState;
import com.example.tradingagents.domain.TradeDecision;
import com.example.tradingagents.graph.MarkdownFormatter;
import com.example.tradingagents.graph.ThinkingStage;
import com.example.tradingagents.graph.TradingGraphService;
import com.example.tradingagents.graph.TradingProgressListener;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/trading")
public class TradingController {

    private final TradingGraphService tradingGraphService;

    public TradingController(TradingGraphService tradingGraphService) {
        this.tradingGraphService = tradingGraphService;
    }

    /**
     * Run trading agents pipeline for the given A-share symbol and date.
     * Request: { "symbol": "600519.SH", "tradeDate": "2026-01-15", optional: "selectedAnalysts", "maxDebateRounds", "maxRiskDiscussRounds" }
     * Response: decision (parsed signal) and optionally full state.
     */
    @PostMapping("/propagate")
    public ResponseEntity<Map<String, Object>> propagate(@Valid @RequestBody PropagateRequest request,
                                                         @RequestParam(required = false, defaultValue = "false") boolean includeState) {
        TradingGraphService.PropagateResult result = tradingGraphService.propagate(
                request.getSymbol(),
                request.getTradeDate(),
                request.getSelectedAnalysts()
        );

        Map<String, Object> body = new HashMap<>();
        TradeDecision d = result.getDecision();
        body.put("decision", d);
        body.put("rawDecision", result.getState().getFinalTradeDecision());
        if (includeState) {
            body.put("state", result.getState());
        }
        return ResponseEntity.ok(body);
    }

    /**
     * SSE streaming endpoint: stream the full thinking process in markdown by stages.
     */
    @PostMapping(value = "/propagate/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter propagateStream(@Valid @RequestBody PropagateRequest request) {
        // 0L = no timeout, keep connection until complete
        SseEmitter emitter = new SseEmitter(0L);

        CompletableFuture.runAsync(() -> {
            try {
                TradingProgressListener listener = (stage, state) -> {
                    try {
                        String markdown = MarkdownFormatter.format(stage, state);
                        Map<String, Object> payload = new HashMap<>();
                        payload.put("stage", stage.name().toLowerCase());
                        payload.put("markdown", markdown);
                        payload.put("symbol", state.getCompanyOfInterest());
                        payload.put("tradeDate", state.getTradeDate());
                        payload.put("ts", Instant.now().toString());

                        emitter.send(SseEmitter.event()
                                .name("stage")
                                .data(payload));
                    } catch (IOException e) {
                        emitter.completeWithError(e);
                    }
                };

                TradingGraphService.PropagateResult result = tradingGraphService.propagate(
                        request.getSymbol(),
                        request.getTradeDate(),
                        request.getSelectedAnalysts(),
                        listener
                );

                // send final summary event
                // AgentState state = result.getState();
                // TradeDecision decision = result.getDecision();
                // Map<String, Object> summary = new HashMap<>();
                // summary.put("stage", "summary");
                // summary.put("symbol", state.getCompanyOfInterest());
                // summary.put("tradeDate", state.getTradeDate());
                // summary.put("decision", decision);
                // summary.put("ts", Instant.now().toString());

                emitter.send(SseEmitter.event()
                        .name("complete")
                        .data("推演结束"));
                emitter.complete();
            } catch (Exception e) {
                emitter.completeWithError(e);
            }
        });

        return emitter;
    }

    /**
     * SSE streaming endpoint (GET) for browser EventSource / Postman testing.
     * Accepts the same fields as PropagateRequest via query parameters.
     */
    @GetMapping(value = "/propagate/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter propagateStreamGet(@RequestParam String symbol,
                                         @RequestParam String tradeDate,
                                         @RequestParam(required = false) List<String> selectedAnalysts,
                                         @RequestParam(required = false) Integer maxDebateRounds,
                                         @RequestParam(required = false) Integer maxRiskDiscussRounds) {
        PropagateRequest request = new PropagateRequest();
        request.setSymbol(symbol);
        request.setTradeDate(tradeDate);
        request.setSelectedAnalysts(selectedAnalysts);
        request.setMaxDebateRounds(maxDebateRounds);
        request.setMaxRiskDiscussRounds(maxRiskDiscussRounds);
        return propagateStream(request);
    }

}
