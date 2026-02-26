package com.example.tradingagents.agents.researchers;

import com.example.tradingagents.config.TradingAgentsProperties;
import com.example.tradingagents.domain.AgentState;
import com.example.tradingagents.domain.InvestDebateState;
import com.finagent.api.Agent;
import com.finagent.api.AgentRunner;
import com.finagent.api.RunRequest;
import com.finagent.api.RunResult;
import com.finagent.core.AgentDefinition;
import org.springframework.stereotype.Service;

/**
 * Runs bull vs bear researcher debate and investment judge for up to maxDebateRounds.
 */
@Service
public class InvestDebateService {

    private static final String BULL_PROMPT = "你是多头研究员。根据分析师报告，从看多角度论证 A 股标的投资价值，用中文简洁陈述。";
    private static final String BEAR_PROMPT = "你是空头研究员。根据分析师报告，从看空/风险角度论证，用中文简洁陈述。";
    private static final String JUDGE_PROMPT = "你是投资辩论裁判。根据多头与空头的观点，给出综合判断结论（偏多/偏空/中性）及理由，用中文简洁输出。";

    private final AgentRunner agentRunner;
    private final TradingAgentsProperties properties;

    public InvestDebateService(AgentRunner agentRunner, TradingAgentsProperties properties) {
        this.agentRunner = agentRunner;
        this.properties = properties;
    }

    public void runDebate(AgentState state) {
        String context = buildContext(state);
        InvestDebateState debate = state.getInvestmentDebateState();
        debate.getBullHistory().clear();
        debate.getBearHistory().clear();
        debate.getHistory().clear();

        int rounds = Math.max(1, properties.getDebate().getMaxDebateRounds());
        for (int r = 0; r < rounds; r++) {
            String bullResp = runAgent("bull-researcher", BULL_PROMPT,
                    context + "\n\n当前辩论历史:\n" + String.join("\n", debate.getHistory()));
            debate.getBullHistory().add(bullResp);
            debate.getHistory().add("多头: " + bullResp);

            String bearResp = runAgent("bear-researcher", BEAR_PROMPT,
                    context + "\n\n当前辩论历史:\n" + String.join("\n", debate.getHistory()));
            debate.getBearHistory().add(bearResp);
            debate.getHistory().add("空头: " + bearResp);
        }

        String judgeDecision = runAgent("invest-judge", JUDGE_PROMPT,
                context + "\n\n完整辩论:\n" + String.join("\n", debate.getHistory()));
        debate.setCurrentResponse(judgeDecision);
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

    private String buildContext(AgentState state) {
        return "标的: " + state.getCompanyOfInterest() + ", 日期: " + state.getTradeDate() + "\n" +
                "行情报告: " + nullToEmpty(state.getMarketReport()) + "\n" +
                "情绪报告: " + nullToEmpty(state.getSentimentReport()) + "\n" +
                "新闻报告: " + nullToEmpty(state.getNewsReport()) + "\n" +
                "基本面报告: " + nullToEmpty(state.getFundamentalsReport());
    }

    private static String nullToEmpty(String s) {
        return s == null ? "" : s;
    }
}
