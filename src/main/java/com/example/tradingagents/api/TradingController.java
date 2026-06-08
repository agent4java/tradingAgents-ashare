package com.example.tradingagents.api;

import com.example.tradingagents.domain.TradeDecision;
import com.example.tradingagents.graph.TradingGraphService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/trading")
public class TradingController {

    private final TradingGraphService tradingGraphService;
    private final TradingSseService tradingSseService;

    public TradingController(TradingGraphService tradingGraphService, TradingSseService tradingSseService) {
        this.tradingGraphService = tradingGraphService;
        this.tradingSseService = tradingSseService;
    }

    /**
     * Run trading agents pipeline for the given A-share symbol and date.
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
     * Stream the full trading pipeline as agent4j-native Server-Sent Events.
     */
    @PostMapping(value = "/propagate/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter propagateStream(@Valid @RequestBody PropagateRequest request) {
        return tradingSseService.stream(request);
    }

    /**
     * GET variant for browser EventSource clients.
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
        return tradingSseService.stream(request);
    }
}
