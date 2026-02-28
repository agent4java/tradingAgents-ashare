package com.example.tradingagents.graph;

import com.example.tradingagents.domain.AgentState;
import com.example.tradingagents.domain.InvestDebateState;
import com.example.tradingagents.domain.RiskDebateState;
import com.example.tradingagents.domain.TradeDecision;

import java.util.List;
import java.util.StringJoiner;

/**
 * Helper to format pipeline stages into markdown for streaming to the frontend.
 */
public final class MarkdownFormatter {

    private MarkdownFormatter() {
    }

    public static String format(ThinkingStage stage, AgentState state) {
        return switch (stage) {
            case ANALYST_MARKET -> formatMarket(state);
            case ANALYST_SENTIMENT -> formatSentiment(state);
            case ANALYST_NEWS -> formatNews(state);
            case ANALYST_FUNDAMENTALS -> formatFundamentals(state);
            case INVEST_DEBATE -> formatInvestDebate(state);
            case TRADER -> formatTrader(state);
            case RISK_DEBATE -> formatRiskDebate(state);
            case PORTFOLIO -> formatPortfolio(state);
            case FINAL_DECISION -> formatFinalDecision(state);
        };
    }

    private static String header(String title) {
        return "## " + title + "\n\n";
    }

    private static String valueOrPlaceholder(String value) {
        return value == null || value.isBlank() ? "（暂无内容）" : value;
    }

    private static String formatMarket(AgentState state) {
        return header("分析师阶段：市场报告")
                + valueOrPlaceholder(state.getMarketReport());
    }

    private static String formatSentiment(AgentState state) {
        return header("分析师阶段：情绪报告")
                + valueOrPlaceholder(state.getSentimentReport());
    }

    private static String formatNews(AgentState state) {
        return header("分析师阶段：新闻报告")
                + valueOrPlaceholder(state.getNewsReport());
    }

    private static String formatFundamentals(AgentState state) {
        return header("分析师阶段：基本面报告")
                + valueOrPlaceholder(state.getFundamentalsReport());
    }

    private static String formatInvestDebate(AgentState state) {
        InvestDebateState debate = state.getInvestmentDebateState();
        StringBuilder sb = new StringBuilder();
        sb.append(header("研究投资辩论阶段"));

        List<String> bull = debate.getBullHistory();
        List<String> bear = debate.getBearHistory();
        String judge = debate.getJudgeDecision();

        sb.append("- 多头观点：\n");
        if (bull == null || bull.isEmpty()) {
            sb.append("  - （暂无）\n");
        } else {
            for (String b : bull) {
                sb.append("  - ").append(b).append("\n");
            }
        }

        sb.append("\n- 空头观点：\n");
        if (bear == null || bear.isEmpty()) {
            sb.append("  - （暂无）\n");
        } else {
            for (String b : bear) {
                sb.append("  - ").append(b).append("\n");
            }
        }

        sb.append("\n- 裁判结论：\n");
        sb.append("  - ").append(valueOrPlaceholder(judge)).append("\n");

        return sb.toString();
    }

    private static String formatTrader(AgentState state) {
        return header("交易员阶段")
                + valueOrPlaceholder(state.getTraderInvestmentPlan());
    }

    private static String formatRiskDebate(AgentState state) {
        RiskDebateState risk = state.getRiskDebateState();
        StringBuilder sb = new StringBuilder();
        sb.append(header("风险辩论阶段"));

        appendListSection(sb, "激进方观点", risk.getAggressiveHistory());
        sb.append("\n");
        appendListSection(sb, "保守方观点", risk.getConservativeHistory());
        sb.append("\n");
        appendListSection(sb, "中立方观点", risk.getNeutralHistory());
        sb.append("\n- 风险裁判结论：\n");
        sb.append("  - ").append(valueOrPlaceholder(risk.getJudgeDecision())).append("\n");

        return sb.toString();
    }

    private static void appendListSection(StringBuilder sb, String title, List<String> items) {
        sb.append("- ").append(title).append("：\n");
        if (items == null || items.isEmpty()) {
            sb.append("  - （暂无）\n");
        } else {
            for (String s : items) {
                sb.append("  - ").append(s).append("\n");
            }
        }
    }

    private static String formatPortfolio(AgentState state) {
        return header("组合经理阶段")
                + valueOrPlaceholder(state.getInvestmentPlan());
    }

    private static String formatFinalDecision(AgentState state) {
        StringBuilder sb = new StringBuilder();
        sb.append(header("最终决策"));
        sb.append("**原始决策文本：**\n\n");
        sb.append(valueOrPlaceholder(state.getFinalTradeDecision())).append("\n\n");

        TradeDecision decision = state.getProcessedDecision();
        if (decision != null) {
            sb.append("**结构化信号：**\n\n");
            sb.append("- 动作: ").append(decision.getAction()).append("\n");
            if (decision.getStrength() != null) {
                sb.append("- 强度: ").append(decision.getStrength()).append("\n");
            }
            if (decision.getReason() != null) {
                sb.append("- 核心理由: ").append(decision.getReason()).append("\n");
            }
            List<String> details = decision.getDetails();
            if (details != null && !details.isEmpty()) {
                StringJoiner joiner = new StringJoiner("\\n  - ", "  - ", "");
                for (String d : details) {
                    joiner.add(d);
                }
                sb.append("- 详细要点:\n");
                sb.append(joiner).append("\n");
            }
        }
        return sb.toString();
    }
}

