package com.example.tradingagents.tools;

import com.finagent.api.Tool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class TushareToolsConfig {

    @Bean
    public List<Tool> tushareTools(GetStockDailyTool getStockDailyTool,
                                   GetIndicatorsTool getIndicatorsTool,
                                   GetFundamentalsTool getFundamentalsTool,
                                   GetNewsTool getNewsTool,
                                   GetGlobalNewsTool getGlobalNewsTool) {
        return List.of(
                getStockDailyTool,
                getIndicatorsTool,
                getFundamentalsTool,
                getNewsTool,
                getGlobalNewsTool
        );
    }
}
