package com.example.tradingagents.wechat;

import com.agent4j.wechatbot.auth.LoginResult;
import com.agent4j.wechatbot.core.WeChatBotClient;
import com.agent4j.wechatbot.protocol.Model;
import com.example.tradingagents.graph.TradingGraphService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class WeChatBotRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(WeChatBotRunner.class);

    private final WeChatBotClient client;
    private final TradingGraphService tradingGraphService;
    private final WeChatCommandParser regexParser = new WeChatCommandParser();
    private final LlmWeChatCommandExtractor llmExtractor;
    private final boolean forceRelogin;

    public WeChatBotRunner(WeChatBotClient client,
                           TradingGraphService tradingGraphService,
                           LlmWeChatCommandExtractor llmExtractor,
                           @Value("${wechatbot.force-relogin:false}") boolean forceRelogin) {
        this.client = client;
        this.tradingGraphService = tradingGraphService;
        this.llmExtractor = llmExtractor;
        this.forceRelogin = forceRelogin;
    }

    @Override
    public void run(String... args) {
        client.onMessage(this::handleMessage);
        LoginResult loginResult = client.login(forceRelogin);
        String qrCodeUrl = loginResult != null ? loginResult.qrCodeUrl() : null;
        if (qrCodeUrl != null && !qrCodeUrl.isBlank()) {
            log.info("WeChat QR URL: {}", qrCodeUrl);
        } else {
            log.info("WeChat session restored, QR login not triggered. To force QR login, set wechatbot.force-relogin=true or remove %USERPROFILE%\\.wechatbot\\java-session-state.json");
        }
        client.start();
        log.info("WeChatBot started.");
    }

    private void handleMessage(Model.IncomingMessage msg) {
        if (msg == null) {
            return;
        }
        String text = msg.text();
        if (text == null || text.isBlank()) {
            return;
        }

        log.info("WeChat message from {}: {}", msg.userId(), text);
        LocalDate today = LocalDate.now();

        Optional<ParsedWeChatCommand> parsed = regexParser.parse(text, today);
        if (parsed.isEmpty()) {
            parsed = llmExtractor.extract(text, today);
        }

        if (parsed.isEmpty()) {
            client.reply(msg, "我没识别出股票代码/日期。请在消息里包含如「600519.SH 2026-01-15」或「600519 今天」。");
            return;
        }

        ParsedWeChatCommand cmd = parsed.get();
        String symbol = cmd.symbol();
        String tradeDate = cmd.tradeDate().toString();

        try {
            TradingGraphService.PropagateResult result = tradingGraphService.propagate(symbol, tradeDate);
            String decision = result != null && result.getState() != null ? result.getState().getFinalTradeDecision() : "";
            if (decision == null || decision.isBlank()) {
                decision = result != null && result.getDecision() != null ? String.valueOf(result.getDecision()) : "";
            }
            String reply = "识别参数: " + symbol + " / " + tradeDate + "\n\n" + (decision != null ? decision : "");
            client.reply(msg, reply);
        } catch (Exception e) {
            log.error("Failed to run trading pipeline", e);
            client.reply(msg, "执行失败：" + e.getMessage());
        }
    }
}

