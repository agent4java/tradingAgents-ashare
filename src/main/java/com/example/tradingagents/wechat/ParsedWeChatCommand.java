package com.example.tradingagents.wechat;

import java.time.LocalDate;

public record ParsedWeChatCommand(String symbol, LocalDate tradeDate) {
}

