# TradingAgents A-share (Spring Boot)

面向 A 股市场的多智能体交易研究框架，基于 Spring Boot、agent4j 与 Tushare。项目仅输出研究分析与模拟决策信号，不提供实盘下单能力。

## Requirements

- JDK 17+
- Maven 3.8+
- 可用的 `com.agent4j:agent4j-spring-boot-starter:0.1.0-SNAPSHOT`

如果 agent4j 尚未发布到公共仓库，请先在本机 agent4j 项目中安装最新版本：

```bash
mvn -q -DskipTests clean install
```

## Configuration

配置 Tushare Token：

- 环境变量：`TUSHARE_TOKEN=你的 token`
- 或在 `application.yml` 中设置 `tushare.token`

主要配置项见 `src/main/resources/application.yml.example`。

## Run

```bash
mvn clean package -DskipTests
java -jar target/tradingagents-ashare-0.1.0-SNAPSHOT.jar
```

也可以直接运行：

```bash
mvn spring-boot:run
```

## API

### POST `/api/trading/propagate`

运行完整交易智能体链路并返回最终结构化决策。

请求示例：

```json
{
  "symbol": "600519.SH",
  "tradeDate": "2026-01-15",
  "selectedAnalysts": ["market", "social", "news", "fundamentals"],
  "maxDebateRounds": 1,
  "maxRiskDiscussRounds": 1
}
```

可选查询参数：

- `includeState=true`：返回完整 `AgentState`。

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

### GET/POST `/api/trading/propagate/stream`

以 agent4j 原生 Server-Sent Events 协议实时输出整条多智能体链路。响应类型为 `text/event-stream`，不再输出旧版 `stage`/`markdown`/`complete` 自定义事件。

POST 请求体与 `/api/trading/propagate` 相同。GET 版本用于浏览器 `EventSource`，通过 query 参数传入同名字段。

事件名遵循 agent4j `AgentStreamEvent`：

- `run_started`
- `agent_started`
- `model_started`
- `model_delta`
- `model_completed`
- `tool_started`
- `tool_completed`
- `handoff`
- `guardrail`
- `run_completed`
- `run_failed`

每条事件的 `data` JSON 结构兼容 agent4j 事件字段，并额外补充前端分组所需的交易阶段元数据：

```json
{
  "type": "model_delta",
  "name": "model",
  "turn": 1,
  "delta": "增量文本",
  "data": null,
  "error": null,
  "agentName": "market-analyst",
  "stage": "market_analysis",
  "stageTitle": "市场分析",
  "scope": "agent"
}
```

阶段映射：

- `market_analysis`：市场分析
- `social_sentiment`：社交情绪分析
- `news_analysis`：新闻分析
- `fundamentals_analysis`：基本面分析
- `invest_debate`：投资辩论
- `trader_decision`：交易员决策
- `risk_debate`：风险辩论
- `portfolio_manager`：经理决策

顶层交易编排也会输出 `trading-graph` 的 `run_started`、`run_completed` 或 `run_failed`。最终 `run_completed` 的 `data.finalOutput` 包含：

内部 agent 的 `run_started`、`run_completed`、`run_failed` 会分别映射为 `agent_run_started`、`agent_run_completed`、`agent_run_failed`，避免前端在市场分析等单个 agent 完成时误判整条流结束。前端只应在 `scope === "graph"` 且 `type === "run_completed"` 时关闭连接。

```json
{
  "symbol": "600519.SH",
  "tradeDate": "2026-01-15",
  "decision": { "...": "..." },
  "rawDecision": "组合经理最终决策原文..."
}
```

浏览器消费示例：

```js
const source = new EventSource("/api/trading/propagate/stream?symbol=600519.SH&tradeDate=2026-01-15");

source.addEventListener("model_delta", event => {
  const message = JSON.parse(event.data);
  appendText(message.delta);
});

source.addEventListener("run_completed", event => {
  const message = JSON.parse(event.data);
  if (message.scope !== "graph") return;
  console.log("final output", message.data?.finalOutput);
  source.close();
});

source.addEventListener("run_failed", event => {
  const message = JSON.parse(event.data);
  console.error(message.error);
  source.close();
});
```

## Project Structure

- `api/`：REST 与 SSE 接口
- `graph/`：交易智能体编排与信号处理
- `agents/`：分析师、研究员、交易员、风控、组合经理
- `tools/`：agent4j 工具封装
- `data/`：数据服务抽象与 Tushare 实现
- `domain/`：状态与决策模型
- `config/`：配置属性与自动配置入口

## Disclaimer

本项目仅供研究与模拟使用，不构成任何投资、财务或交易建议。请勿直接用于实盘交易决策。
