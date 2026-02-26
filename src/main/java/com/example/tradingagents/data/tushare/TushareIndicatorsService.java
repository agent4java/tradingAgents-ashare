package com.example.tradingagents.data.tushare;

import com.example.tradingagents.data.api.IndicatorsService;
import com.example.tradingagents.data.api.StockDataService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.*;

@Service
@ConditionalOnProperty(name = "tradingagents.data.vendor", havingValue = "tushare")
public class TushareIndicatorsService implements IndicatorsService {

    private final StockDataService stockDataService;

    public TushareIndicatorsService(StockDataService stockDataService) {
        this.stockDataService = stockDataService;
    }

    @Override
    public Map<String, Object> getIndicators(String symbol, LocalDate tradeDate) {
        LocalDate start = tradeDate.minusDays(120);
        List<Map<String, Object>> daily = stockDataService.getDaily(symbol, start, tradeDate);
        Map<String, Object> out = new LinkedHashMap<>();
        out.put("symbol", symbol);
        out.put("trade_date", tradeDate.toString());
        if (daily.isEmpty()) {
            out.put("note", "无日线数据，无法计算技术指标");
            return out;
        }
        double[] closes = daily.stream()
                .mapToDouble(r -> ((Number) r.getOrDefault("收盘", r.get("close"))).doubleValue())
                .toArray();
        if (closes.length >= 14) {
            double rsi = computeRsi(closes, 14);
            out.put("RSI_14", String.format("%.2f", rsi));
        }
        if (closes.length >= 26) {
            double[] macd = computeMacd(closes, 12, 26, 9);
            out.put("MACD", String.format("%.4f", macd[0]));
            out.put("MACD_signal", String.format("%.4f", macd[1]));
            out.put("MACD_hist", String.format("%.4f", macd[2]));
        }
        return out;
    }

    private static double computeRsi(double[] closes, int period) {
        double gains = 0, losses = 0;
        for (int i = closes.length - period; i < closes.length - 1; i++) {
            double diff = closes[i + 1] - closes[i];
            if (diff > 0) gains += diff;
            else losses -= diff;
        }
        double avgGain = gains / period;
        double avgLoss = losses / period;
        if (avgLoss == 0) return 100;
        double rs = avgGain / avgLoss;
        return 100 - (100 / (1 + rs));
    }

    private static double[] computeMacd(double[] closes, int fast, int slow, int signal) {
        double[] emaFast = ema(closes, fast);
        double[] emaSlow = ema(closes, slow);
        double[] macdLine = new double[closes.length];
        for (int i = 0; i < closes.length; i++) {
            macdLine[i] = emaFast[i] - emaSlow[i];
        }
        double[] signalLine = ema(macdLine, signal);
        double[] hist = new double[closes.length];
        for (int i = 0; i < closes.length; i++) {
            hist[i] = macdLine[i] - signalLine[i];
        }
        int last = closes.length - 1;
        return new double[]{macdLine[last], signalLine[last], hist[last]};
    }

    private static double[] ema(double[] values, int period) {
        double[] out = new double[values.length];
        double k = 2.0 / (period + 1);
        out[0] = values[0];
        for (int i = 1; i < values.length; i++) {
            out[i] = values[i] * k + out[i - 1] * (1 - k);
        }
        return out;
    }
}
