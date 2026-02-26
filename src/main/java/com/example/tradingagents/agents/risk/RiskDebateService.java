package com.example.tradingagents.agents.risk;

import com.example.tradingagents.config.TradingAgentsProperties;
import com.example.tradingagents.domain.AgentState;
import com.example.tradingagents.domain.RiskDebateState;
import com.finagent.api.Agent;
import com.finagent.api.AgentRunner;
import com.finagent.api.RunRequest;
import com.finagent.api.RunResult;
import com.finagent.core.AgentDefinition;
import org.springframework.stereotype.Service;

/**
 * Risk management debate: aggressive vs conservative (and optional neutral), then judge.
 */
@Service
public class RiskDebateService {

    private static final String AGGRESSIVE_PROMPT = "你是风控辩论中的激进方。根据交易员的投资计划，从可接受更高风险角度论证，用中文简洁陈述。";
    private static final String CONSERVATIVE_PROMPT = "你是风控辩论中的保守方。根据交易员的投资计划，从控制风险、减仓或观望角度论证，用中文简洁陈述。";
    private static final String JUDGE_PROMPT = "你是风控辩论裁判。根据激进与保守方观点，给出是否批准该交易计划、仓位建议或风控措施，用中文简洁输出。";

    private final AgentRunner agentRunner;
    private final TradingAgentsProperties properties;

    public RiskDebateService(AgentRunner agentRunner, TradingAgentsProperties properties) {
        this.agentRunner = agentRunner;
        this.properties = properties;
    }

    public void runDebate(AgentState state) {
        String context = "标的: " + state.getCompanyOfInterest() + ", 日期: " + state.getTradeDate() + "\n" +
                "交易员投资计划: " + nullToEmpty(state.getTraderInvestmentPlan());
        RiskDebateState debate = state.getRiskDebateState();
        debate.getAggressiveHistory().clear();
        debate.getConservativeHistory().clear();
        debate.getHistory().clear();

        int rounds = Math.max(1, properties.getDebate().getMaxRiskDiscussRounds());
        for (int r = 0; r < rounds; r++) {
            String agg = runAgent("risk-aggressive", AGGRESSIVE_PROMPT,
                    context + "\n\n当前辩论:\n" + String.join("\n", debate.getHistory()));
            debate.getAggressiveHistory().add(agg);
            debate.getHistory().add("激进: " + agg);

            String cons = runAgent("risk-conservative", CONSERVATIVE_PROMPT,
                    context + "\n\n当前辩论:\n" + String.join("\n", debate.getHistory()));
            debate.getConservativeHistory().add(cons);
            debate.getHistory().add("保守: " + cons);
        }

        String judgeDecision = runAgent("risk-judge", JUDGE_PROMPT,
                context + "\n\n完整辩论:\n" + String.join("\n", debate.getHistory()));
        debate.setJudgeDecision(judgeDecision);
    }

    private String runAgent(String name, String prompt, String userMessage) {
        Agent agent = new AgentDefinition()
                .setName(name)
                .setInstructions(prompt)
                .build();
        RunRequest request = RunRequest.builder()
                .input(userMessage)
                .maxTurns(20)
                .build();
        RunResult result = agentRunner.run(agent, request);
        Object output = result != null ? result.getFinalOutput() : null;
        return output != null ? output.toString() : "";
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
