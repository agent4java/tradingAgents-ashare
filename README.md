# TradingAgents A-share (Spring Boot)

面向 A 股市场的多智能体交易框架，基于 Spring Boot，参考 [TauricResearch/TradingAgents](https://github.com/TauricResearch/TradingAgents)。  
不提供实盘下单，仅输出研究与模拟用决策信号。

## 要求

- JDK 17+
- Maven 3.8+

## 构建与运行

1. **安装 [agent4j](https://github.com/agent4java/agent4j) 依赖（如未发布到中央仓库）**

   若 `com.agent4j:agent4j-spring-boot-starter` 不在公共仓库，请先安装到本地或配置私服：

   ```bash
   # 在 agent4j 项目目录执行
   mvn install
   ```

   或在当前项目的 `pom.xml` 中配置贵司 Maven 仓库（如需要）。

2. **配置 Tushare Token**

   在 [Tushare Pro](https://tushare.pro) 注册并获取 token，然后：

   - 环境变量：`TUSHARE_TOKEN=你的token`
   - 或在 `application.yml` 中设置 `tushare.token`

3. **编译与启动**

   ```bash
   mvn clean package -DskipTests
   java -jar target/tradingagents-ashare-0.1.0-SNAPSHOT.jar
   ```

   或：

   ```bash
   mvn spring-boot:run
   ```

## 配置说明

| 配置项 | 说明 | 默认 |
|--------|------|------|
| `tradingagents.llm.provider` | LLM 提供商 | openai |
| `tradingagents.llm.deep-think-model` | 深度推理模型 | gpt-4 |
| `tradingagents.llm.quick-think-model` | 快速模型 | gpt-4-mini |
| `tradingagents.debate.max-debate-rounds` | 多空辩论轮数 | 1 |
| `tradingagents.debate.max-risk-discuss-rounds` | 风控辩论轮数 | 1 |
| `tradingagents.data.vendor` | 数据源 | tushare |
| `tushare.token` | Tushare Pro Token | 需配置 |
| `tushare.api-url` | Tushare API 地址 | https://api.tushare.pro |

agent4j 相关配置请按 `agent4j-spring-boot-starter` 文档在 `application.yml` 或 `Agent4jConfig` 中配置。

## API

- **POST** `/api/trading/propagate`

  请求体示例：

  ```json
  {
    "symbol": "600519.SH",
    "tradeDate": "2026-01-15",
    "selectedAnalysts": ["market", "social", "news", "fundamentals"],
    "maxDebateRounds": 1,
    "maxRiskDiscussRounds": 1
  }
  ```

  - `symbol`: A 股代码，如 `600519.SH`、`000001.SZ`
  - `tradeDate`: 交易日期，`yyyy-MM-dd`
  - `selectedAnalysts`: 可选，默认全部。**说明**：舆论情绪、公司新闻相关接口仍在对接中。
  - `maxDebateRounds` / `maxRiskDiscussRounds`: 可选，覆盖配置

  查询参数：`includeState=true` 时响应中返回完整 `state`。

  响应示例：

  ```json
  {
    "decision": {
      "action": "BUY",
      "strength": null,
      "reason": "...",
      "rawDecision": "...",
      "details": []
    },
    "rawDecision": "组合经理最终决策原文..."
  }
  ```

- **POST** `/api/trading/propagate/stream`

  以 **Server-Sent Events (SSE)** 实时流的方式返回整个多智能体链路的思考过程，载荷为 JSON，其中 `markdown` 字段可直接在前端以 markdown 渲染。

  请求体与 `/api/trading/propagate` 相同，例如：

  ```json
  {
    "symbol": "600519.SH",
    "tradeDate": "2026-01-15",
    "selectedAnalysts": ["market", "social", "news", "fundamentals"],
    "maxDebateRounds": 1,
    "maxRiskDiscussRounds": 1
  }
  ```

  响应为 `text/event-stream`，每个事件形如：

  ```text
  event: stage
  data: {"stage":"analyst_market","markdown":"## 分析师阶段：市场报告\n\n...", "symbol":"600519.SH","tradeDate":"2026-01-15","ts":"2026-02-26T12:00:00Z"}

  event: stage
  data: {"stage":"invest_debate","markdown":"## 研究投资辩论阶段\n\n- 多头观点：\n  - ...", ...}

  ...

  event: complete
  data: {"stage":"summary","symbol":"600519.SH","tradeDate":"2026-01-15","decision":{...},"ts":"2026-02-26T12:00:05Z"}
  ```

  - `stage`: 当前推送的阶段标识，例如 `analyst_market` / `invest_debate` / `trader` / `risk_debate` / `portfolio` / `final_decision` / `summary`
  - `markdown`: 当前阶段的 markdown 文本（例如“分析师阶段：市场报告”“研究投资辩论阶段”等）
  - `decision`: 仅在 `complete` 事件中出现，对应结构化 `TradeDecision`

  前端可使用原生 `EventSource`：

  ```js
  const es = new EventSource('/api/trading/propagate/stream', { withCredentials: false });
  es.onmessage = (event) => {
    const payload = JSON.parse(event.data);
    // payload.markdown -> 渲染 markdown；payload.stage -> 分组展示
  };
  ```

## 项目结构

- `config/` — 配置与属性（TradingAgents、Tushare、Agent4j 占位）
- `domain/` — 状态与决策模型（AgentState、TradeDecision 等）
- `data/api/` — 数据接口；`data/tushare/` — Tushare 实现
- `agents/analysts|researchers|trader|risk|portfolio/` — 各角色服务
- `graph/` — 编排（TradingGraphService、SignalProcessor）
- `api/` — REST 控制器

## 免责声明

本框架仅供研究与模拟使用。交易表现受模型、参数、数据质量等众多因素影响，**不构成任何投资、财务或交易建议**。请勿直接用于实盘交易决策。

## 参考

- [TradingAgents (TauricResearch)](https://github.com/TauricResearch/TradingAgents)
- [agent4j](https://github.com/agent4java/agent4j) — 轻量级多智能体框架
- [Tushare Pro 文档](https://tushare.pro/document/2)
