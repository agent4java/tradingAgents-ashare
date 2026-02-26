package com.example.tradingagents.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({TradingAgentsProperties.class, TushareProperties.class})
public class TradingAgentsConfig {
}
