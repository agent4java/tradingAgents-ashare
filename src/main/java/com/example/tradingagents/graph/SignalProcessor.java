package com.example.tradingagents.graph;

import com.example.tradingagents.domain.TradeDecision;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

/**
 * Parses final trade decision text into structured TradeDecision (action, strength, reason).
 */
@Component
public class SignalProcessor {

    private static final Pattern BUY = Pattern.compile("买入|做多|买进|建仓");
    private static final Pattern SELL = Pattern.compile("卖出|做空|减仓|清仓");
    private static final Pattern HOLD = Pattern.compile("观望|持有|不变|中性");

    public TradeDecision processSignal(String fullSignal) {
        TradeDecision d = new TradeDecision();
        d.setRawDecision(fullSignal);
        if (fullSignal == null || fullSignal.isBlank()) {
            d.setAction(TradeDecision.Action.HOLD);
            d.setReason("无决策文本");
            return d;
        }
        String text = fullSignal.trim();
        // if (BUY.matcher(text).find()) {
        //     d.setAction(TradeDecision.Action.BUY);
        // } else if (SELL.matcher(text).find()) {
        //     d.setAction(TradeDecision.Action.SELL);
        // } else if (HOLD.matcher(text).find()) {
        //     d.setAction(TradeDecision.Action.HOLD);
        // } else {
        //     d.setAction(TradeDecision.Action.HOLD);
        //     d.setReason("无法解析明确方向，默认观望");
        // }
        d.setReason(text);
        return d;
    }
}
