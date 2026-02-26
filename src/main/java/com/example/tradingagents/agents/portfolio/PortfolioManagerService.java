package com.example.tradingagents.agents.portfolio;

import com.example.tradingagents.domain.AgentState;
import com.finagent.api.Agent;
import com.finagent.api.AgentRunner;
import com.finagent.api.RunRequest;
import com.finagent.api.RunResult;
import com.finagent.core.AgentDefinition;
import org.springframework.stereotype.Service;

/**
 * Portfolio manager: final approval and trade decision (simulated; no live order).
 */
@Service
public class PortfolioManagerService {

    private static final String SYSTEM_PROMPT = "你是组合经理。根据交易员投资计划与风控结论，做出最终交易决策：批准买入/卖出/观望，并简要说明理由与执行要点。用中文输出，且明确写出「买入」「卖出」或「观望」之一。";

    private final AgentRunner agentRunner;

    public PortfolioManagerService(AgentRunner agentRunner) {
        this.agentRunner = agentRunner;
    }

    public void produceFinalDecision(AgentState state) {
        String context = "标的: " + state.getCompanyOfInterest() + ", 日期: " + state.getTradeDate() + "\n" +
                "交易员计划: " + nullToEmpty(state.getTraderInvestmentPlan()) + "\n" +
                "风控结论: " + nullToEmpty(state.getRiskDebateState().getJudgeDecision());

        Agent agent = new AgentDefinition()
                .setName("portfolio-manager")
                .setInstructions(SYSTEM_PROMPT)
                .build();
        RunRequest request = RunRequest.builder()
                .input(context)
                .maxTurns(10)
                .build();
        RunResult result = agentRunner.run(agent, request);
        Object output = result != null ? result.getFinalOutput() : null;
        String decision = output != null ? output.toString() : "";
        state.setFinalTradeDecision(decision);
        state.setInvestmentPlan(decision);
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
