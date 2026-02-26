package com.example.tradingagents.agents.trader;

import com.example.tradingagents.domain.AgentState;
import com.finagent.api.Agent;
import com.finagent.api.AgentRunner;
import com.finagent.api.RunRequest;
import com.finagent.api.RunResult;
import com.finagent.core.AgentDefinition;
import org.springframework.stereotype.Service;

/**
 * Trader agent: composes analyst reports and debate result into an investment plan.
 */
@Service
public class TraderService {

    private static final String SYSTEM_PROMPT = "你是 A 股交易员。根据分析师报告与多空辩论结论，用中文撰写投资计划：方向（做多/做空/观望）、建议仓位或比例、理由与风险提示。";

    private final AgentRunner agentRunner;

    public TraderService(AgentRunner agentRunner) {
        this.agentRunner = agentRunner;
    }

    public void producePlan(AgentState state) {
        String context = "标的: " + state.getCompanyOfInterest() + ", 日期: " + state.getTradeDate() + "\n" +
                "行情: " + nullToEmpty(state.getMarketReport()) + "\n" +
                "情绪: " + nullToEmpty(state.getSentimentReport()) + "\n" +
                "新闻: " + nullToEmpty(state.getNewsReport()) + "\n" +
                "基本面: " + nullToEmpty(state.getFundamentalsReport()) + "\n" +
                "投资辩论结论: " + nullToEmpty(state.getInvestmentDebateState().getJudgeDecision());

        Agent agent = new AgentDefinition()
                .setName("trader")
                .setInstructions(SYSTEM_PROMPT)
                .build();
        RunRequest request = RunRequest.builder()
                .input(context)
                .maxTurns(10)
                .build();
        RunResult result = agentRunner.run(agent, request);
        Object output = result != null ? result.getFinalOutput() : null;
        state.setTraderInvestmentPlan(output != null ? output.toString() : "");
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
