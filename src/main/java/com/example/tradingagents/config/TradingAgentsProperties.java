package com.example.tradingagents.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "tradingagents")
public class TradingAgentsProperties {

    private Llm llm = new Llm();
    private Debate debate = new Debate();
    private Data data = new Data();
    private String resultsDir = "./results";

    public Llm getLlm() {
        return llm;
    }

    public void setLlm(Llm llm) {
        this.llm = llm;
    }

    public Debate getDebate() {
        return debate;
    }

    public void setDebate(Debate debate) {
        this.debate = debate;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public String getResultsDir() {
        return resultsDir;
    }

    public void setResultsDir(String resultsDir) {
        this.resultsDir = resultsDir;
    }

    public static class Llm {
        private String provider = "openai";
        private String deepThinkModel = "gpt-4";
        private String quickThinkModel = "gpt-4-mini";
        private String backendUrl = "https://api.openai.com/v1";

        public String getProvider() {
            return provider;
        }

        public void setProvider(String provider) {
            this.provider = provider;
        }

        public String getDeepThinkModel() {
            return deepThinkModel;
        }

        public void setDeepThinkModel(String deepThinkModel) {
            this.deepThinkModel = deepThinkModel;
        }

        public String getQuickThinkModel() {
            return quickThinkModel;
        }

        public void setQuickThinkModel(String quickThinkModel) {
            this.quickThinkModel = quickThinkModel;
        }

        public String getBackendUrl() {
            return backendUrl;
        }

        public void setBackendUrl(String backendUrl) {
            this.backendUrl = backendUrl;
        }
    }

    public static class Debate {
        private int maxDebateRounds = 1;
        private int maxRiskDiscussRounds = 1;

        public int getMaxDebateRounds() {
            return maxDebateRounds;
        }

        public void setMaxDebateRounds(int maxDebateRounds) {
            this.maxDebateRounds = maxDebateRounds;
        }

        public int getMaxRiskDiscussRounds() {
            return maxRiskDiscussRounds;
        }

        public void setMaxRiskDiscussRounds(int maxRiskDiscussRounds) {
            this.maxRiskDiscussRounds = maxRiskDiscussRounds;
        }
    }

    public static class Data {
        private String vendor = "tushare";

        public String getVendor() {
            return vendor;
        }

        public void setVendor(String vendor) {
            this.vendor = vendor;
        }
    }
}
