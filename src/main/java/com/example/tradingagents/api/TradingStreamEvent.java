package com.example.tradingagents.api;

import com.agent4j.api.AgentStreamEvent;
import com.agent4j.api.RunEvent;
import com.agent4j.api.RunResult;

public final class TradingStreamEvent {

    private final String type;
    private final String name;
    private final int turn;
    private final String delta;
    private final Object data;
    private final String error;
    private final String agentName;
    private final String stage;
    private final String stageTitle;
    private final String scope;

    private TradingStreamEvent(AgentStreamEvent event, String agentName, String type, String scope) {
        TradingStreamStage streamStage = TradingStreamStage.fromAgentName(agentName);
        this.type = type;
        this.name = event.getName();
        this.turn = event.getTurn();
        this.delta = event.getDelta();
        this.data = event.getData();
        this.error = event.getError();
        this.agentName = agentName;
        this.stage = streamStage.getCode();
        this.stageTitle = streamStage.getTitle();
        this.scope = scope;
    }

    public static TradingStreamEvent fromRunEvent(RunEvent event) {
        AgentStreamEvent streamEvent = AgentStreamEvent.fromRunEvent(event);
        String agentName = resolveAgentName(event);
        String scope = "trading-graph".equals(agentName) ? "graph" : "agent";
        String type = remapTypeForScope(streamEvent.getType(), scope);
        return new TradingStreamEvent(streamEvent, agentName, type, scope);
    }

    private static String resolveAgentName(RunEvent event) {
        if (event.getAgent() != null && event.getAgent().getName() != null) {
            return event.getAgent().getName();
        }
        if (event.getPayload() instanceof RunResult result && result.getLastAgent() != null) {
            return result.getLastAgent().getName();
        }
        return null;
    }

    private static String remapTypeForScope(String type, String scope) {
        if ("graph".equals(scope)) {
            return type;
        }
        return switch (type) {
            case "run_started" -> "agent_run_started";
            case "run_completed" -> "agent_run_completed";
            case "run_failed" -> "agent_run_failed";
            default -> type;
        };
    }

    public String getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public int getTurn() {
        return turn;
    }

    public String getDelta() {
        return delta;
    }

    public Object getData() {
        return data;
    }

    public String getError() {
        return error;
    }

    public String getAgentName() {
        return agentName;
    }

    public String getStage() {
        return stage;
    }

    public String getStageTitle() {
        return stageTitle;
    }

    public String getScope() {
        return scope;
    }
}
