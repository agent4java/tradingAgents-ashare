package com.example.tradingagents.wechat;

import com.agent4j.api.Agent;
import com.agent4j.api.AgentRunner;
import com.agent4j.api.RunRequest;
import com.agent4j.api.RunResult;
import com.agent4j.core.AgentDefinition;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

/**
 * LLM fallback: extract (symbol, tradeDate) from free-form text.
 * Returns empty if extraction fails.
 */
public class LlmWeChatCommandExtractor {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE;

    private final AgentRunner agentRunner;
    private final ObjectMapper objectMapper;

    public LlmWeChatCommandExtractor(AgentRunner agentRunner, ObjectMapper objectMapper) {
        this.agentRunner = agentRunner;
        this.objectMapper = objectMapper;
    }

    public Optional<ParsedWeChatCommand> extract(String text, LocalDate now) {
        if (text == null || text.isBlank()) {
            return Optional.empty();
        }
        LocalDate base = now != null ? now : LocalDate.now();

        String prompt = """
                你是一个信息抽取器。请从用户消息中抽取 A 股代码(symbol)与日期(tradeDate)。
                
                约束：
                - symbol 输出为 ts_code 格式：6位数字 + .SH/.SZ/.BJ，例如 600519.SH
                - 如果用户只给了 6 位数字，请推断交易所：6xxxxxx->SH，0/3xxxxxx->SZ，8/4xxxxxx->BJ
                - tradeDate 输出为 yyyy-MM-dd；如果用户没给日期，使用 today
                - 仅输出 JSON，不要输出其它文字
                
                输出 JSON 结构：
                {"symbol":"600519.SH","tradeDate":"2026-01-15"}
                
                today=%s
                用户消息：%s
                """.formatted(base, text);

        Agent agent = new AgentDefinition()
                .setName("wechat-command-extractor")
                .setInstructions("Extract fields and output strict JSON only.")
                .build();

        RunResult result = agentRunner.run(agent, RunRequest.builder()
                .input(prompt)
                .maxTurns(5)
                .build());

        String out = result != null && result.getFinalOutput() != null ? result.getFinalOutput().toString() : "";
        if (out.isBlank()) {
            return Optional.empty();
        }

        try {
            JsonNode node = objectMapper.readTree(out);
            String symbol = node.path("symbol").asText(null);
            String dateStr = node.path("tradeDate").asText(null);
            if (symbol == null || symbol.isBlank()) {
                return Optional.empty();
            }
            LocalDate date;
            if (dateStr == null || dateStr.isBlank()) {
                date = base;
            } else {
                try {
                    date = LocalDate.parse(dateStr, ISO);
                } catch (DateTimeParseException e) {
                    date = base;
                }
            }
            return Optional.of(new ParsedWeChatCommand(symbol.trim(), date));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }
}

