package com.example.tradingagents.api;

import com.example.tradingagents.domain.AgentState;
import com.example.tradingagents.domain.TradeDecision;
import com.example.tradingagents.graph.TradingGraphService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

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
}
