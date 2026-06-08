package com.example.tradingagents.api;

import com.example.tradingagents.graph.TradingGraphService;
import org.junit.jupiter.api.Test;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class TradingControllerTest {

    @Test
    void postStreamDelegatesRequestToTradingSseService() {
        CapturingTradingSseService sseService = new CapturingTradingSseService();
        TradingController controller = new TradingController(null, sseService);
        PropagateRequest request = request("600519.SH", "2026-01-15", List.of("market"));

        SseEmitter emitter = controller.propagateStream(request);

        assertThat(emitter).isNotNull();
        assertThat(sseService.captured.get()).isSameAs(request);
    }

    @Test
    void getStreamBuildsRequestFromQueryParameters() {
        CapturingTradingSseService sseService = new CapturingTradingSseService();
        TradingController controller = new TradingController(null, sseService);

        SseEmitter emitter = controller.propagateStreamGet("600519.SH", "2026-01-15", List.of("market"), 1, 2);

        PropagateRequest captured = sseService.captured.get();
        assertThat(emitter).isNotNull();
        assertThat(captured.getSymbol()).isEqualTo("600519.SH");
        assertThat(captured.getTradeDate()).isEqualTo("2026-01-15");
        assertThat(captured.getSelectedAnalysts()).containsExactly("market");
        assertThat(captured.getMaxDebateRounds()).isEqualTo(1);
        assertThat(captured.getMaxRiskDiscussRounds()).isEqualTo(2);
    }

    private static PropagateRequest request(String symbol, String tradeDate, List<String> selectedAnalysts) {
        PropagateRequest request = new PropagateRequest();
        request.setSymbol(symbol);
        request.setTradeDate(tradeDate);
        request.setSelectedAnalysts(selectedAnalysts);
        return request;
    }

    private static final class CapturingTradingSseService extends TradingSseService {
        private final AtomicReference<PropagateRequest> captured = new AtomicReference<>();

        private CapturingTradingSseService() {
            super((TradingGraphService) null);
        }

        @Override
        public SseEmitter stream(PropagateRequest request) {
            captured.set(request);
            return new SseEmitter();
        }
    }
}
