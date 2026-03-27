package com.example.tradingagents.wechat;

import com.agent4j.api.AgentRunner;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WeChatBotWiringConfig {

    @Bean
    public LlmWeChatCommandExtractor llmWeChatCommandExtractor(AgentRunner agentRunner, ObjectMapper objectMapper) {
        return new LlmWeChatCommandExtractor(agentRunner, objectMapper);
    }
}

