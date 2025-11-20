# 后端 AI 代码功能分析报告

## 项目概述

这是一个基于 **Micronaut** 框架的多 Agent 编排系统，使用 **LangChain4j** 和 **LangGraph4j** 实现 AI Agent 的构建、编排和执行。系统支持 RAG（检索增强生成）、知识图谱、MCP（Model Context Protocol）等高级功能。

## 核心技术栈

- **框架**: Micronaut 4.10.1
- **AI 框架**: LangChain4j 1.5.0
- **图执行引擎**: LangGraph4j 1.7.3
- **数据库**: PostgreSQL (支持 pgvector 向量扩展)
- **图数据库**: Neo4j
- **LLM**: OpenAI API (GPT-4o)
- **向量模型**: text-embedding-3-small

## 核心功能模块

### 1. Agent 编排系统 (Orchestrator)

**核心类**: `Orchestrator.java`

**功能**:
- **主管-工作者模式**: 采用 Supervisor-Worker 架构
  - Supervisor（主管）: 负责生成任务计划（TaskPlan）
  - Worker（工作者）: 执行具体任务
- **任务规划**: Supervisor 根据用户请求和子 Agent 能力生成任务计划
- **图执行**: 根据 TaskPlan 构建有向无环图（DAG）并执行

**执行流程**:
```
用户请求 → Supervisor 生成计划 → 构建执行图 → 异步执行任务 → 流式返回结果
```

**关键方法**:
- `plan()`: 主管生成任务计划
- `buildGraph()`: 构建执行图
- `execute()`: 执行完整流程

### 2. AI 服务层 (AiService)

**核心接口**: `AiService.java`

**功能**:
- 提供统一的 AI 聊天接口
- 支持流式输出（TokenStream）
- 支持自定义系统提示词

**实现特点**:
- 基于 LangChain4j 的 `AiServices` 构建
- 支持 RAG 内容检索
- 支持 MCP 工具调用

### 3. Agent 节点构建 (AgentNode)

**核心类**: `AgentNode.java`

**功能**:
- **动态构建 AiService**: 根据配置的 ChatModel、RAG、MCP 构建 AI 服务
- **ServiceNode 管理**: 创建和管理 Agent 服务节点
- **多 RAG 支持**: 支持为 Agent 配置多个 RAG 知识库
- **MCP 集成**: 支持多个 MCP 客户端，提供工具调用能力

**构建流程**:
```
Agent配置 → ChatModel → RAG检索器 → MCP工具 → SystemPrompt → AiService
```

### 4. RAG 服务 (RagService)

**核心类**: `RagService.java`

**功能**:

#### 4.1 文档向量化与检索
- **文档添加**: 将文档文本转换为向量并存储到 PostgreSQL（pgvector）
- **相似度搜索**: 基于向量相似度检索相关文档
- **知识图谱搜索**: 结合向量匹配和 Neo4j 图查询

#### 4.2 知识图谱构建
- **实体抽取**: 使用 LLM 从文档中抽取实体和关系
- **实体存储**: 实体存储到 PostgreSQL，包含向量嵌入
- **关系存储**: 关系存储到 Neo4j 图数据库
- **图谱查询**: 支持按实体查询相关关系和节点

**关键方法**:
- `addDocument()`: 添加文档到 RAG
- `search()`: 向量相似度搜索
- `searchKnowledgeGraph()`: 知识图谱搜索
- `importDocument()`: 导入文档并构建知识图谱

### 5. 任务执行系统

#### 5.1 任务计划 (TaskPlan)

**核心类**: `TaskPlan.java`

**结构**:
- `summary`: 计划摘要
- `totalTasks`: 总任务数
- `tasks`: 任务列表

#### 5.2 任务 (Task)

**核心类**: `Task.java`

**属性**:
- `id`: 任务ID
- `title`: 任务标题
- `description`: 任务描述
- `agentId`: 负责的 Agent ID
- `inputs`: 输入配置（可来自用户或前置任务）
- `outputs`: 输出字段列表
- `executionStrategy`: 执行策略（SILENT/STREAMING）

#### 5.3 执行器 (Executor)

**核心类**: 
- `BaseTaskExecutor.java`: 基础执行器抽象类
- `StreamingExecutor.java`: 流式执行器
- `SilentExecutor.java`: 静默执行器
- `ExecutorFactory.java`: 执行器工厂

**执行策略**:
- **SILENT（静默执行）**:
  - 只将结果保存到 state 中
  - 不向用户输出内容，但发送任务状态事件
  - 适用于：数据预处理、中间计算、后台任务
  
- **STREAMING（流式输出）**:
  - 将结果保存到 state
  - 同时通过 TokenStream 向用户实时输出
  - 适用于：生成报告、总结、说明等需要用户看到的内容

**功能**:
- **任务输入处理**: 
  - 支持从用户输入获取
  - 支持从前置任务输出获取
  - 自动注入对话历史记忆（仅对特定 Agent）
- **任务结果保存**: 将结果保存到 WorkspaceState 的 scratchpad
- **记忆集成**: 自动保存对话历史到 InMemory（仅 StreamingExecutor）
- **事件推送**: 发送任务状态事件（开始、完成、失败）

### 6. 图构建与执行 (GraphBuilder)

**核心类**: `GraphBuilder.java`

**功能**:
- **DAG 构建**: 根据 TaskPlan 构建有向无环图
- **节点依赖**: 处理任务之间的依赖关系
- **异步执行**: 使用线程池异步执行节点
- **状态管理**: 管理 WorkspaceState 的传递和更新

**图结构**:
```
START → [根节点] → [中间节点] → [叶子节点] → END
```

### 7. 记忆系统 (InMemory)

**核心类**: `InMemory.java`

**功能**:
- **对话历史管理**: 存储用户和助手的对话历史
- **会话隔离**: 基于 userId + sessionId 隔离不同会话
- **历史格式化**: 将历史对话格式化为 prompt 可用的格式
- **记忆注入**: 为特定 Agent（Supervisor、Researcher、Summarizer）注入历史记忆

**存储结构**:
```
Map<userId_sessionId, List<ConversationMessage>>
```

### 8. 流式事件系统 (UserContext)

**核心类**: `UserContext.java`

**功能**:
- **SSE 事件流**: 通过 Server-Sent Events 实时推送执行状态
- **事件类型**:
  - `PLANNING_START`: 计划开始
  - `PLAN_READY`: 计划就绪
  - `TASK_START`: 任务开始
  - `STREAMING`: 流式输出
  - `TASK_COMPLETE`: 任务完成
  - `TASK_FAILED`: 任务失败

### 9. 数据存储

#### 9.1 PostgreSQL 表结构
- `orchestrator`: 编排器配置
- `agent`: Agent 配置
- `model`: 模型配置
- `rag`: RAG 知识库配置
- `vector_document`: 向量文档存储（pgvector）
- `kg_entity`: 知识图谱实体（带向量嵌入）
- `mcp`: MCP 配置
- `agent_memory`: Agent 共享记忆

#### 9.2 Neo4j 图数据库
- **节点**: 知识图谱实体
- **边**: 实体之间的关系
- **查询**: 支持深度查询和关系遍历

## 工作流程

### 完整执行流程

#### Armory 构建流程（责任链模式）

**核心节点**:
1. **RootNode**: 并行加载所有配置数据
   - Orchestrator 配置
   - Agent 列表
   - Model 映射
   - RAG 映射
   - MCP 映射
   
2. **McpNode**: 构建 MCP 客户端
   - 初始化 MCP 连接
   - 注册到 BeanUtil
   
3. **RagNode**: 构建 RAG 组件
   - 创建 EmbeddingStore（向量存储）
   - 创建 EmbeddingModel（向量模型）
   - 注册到 BeanUtil
   
4. **ChatModelNode**: 构建聊天模型
   - 创建 StreamingChatModel
   - 注册到 BeanUtil
   
5. **AgentNode**: 构建 Agent 服务
   - 为每个 Agent 构建 AiService
   - 集成 RAG、MCP、SystemPrompt
   - 创建 ServiceNode
   - 注册到 BeanUtil
   
6. **OrchestratorNode**: 构建编排器
   - 分离 Supervisor 和 Worker 节点
   - 创建 Orchestrator 实例
   - 注册到 BeanUtil

**构建流程**:
```
RootNode → McpNode → RagNode → ChatModelNode → AgentNode → OrchestratorNode
```

#### 任务执行流程

```
1. 用户请求
   ↓
2. 调用 /armory 构建 Agent 节点（如上）
   ↓
3. 调用 /task/execute 执行任务
   ↓
4. Orchestrator.plan()
   - Supervisor 生成任务计划（TaskPlan）
   - 考虑历史对话记忆
   - 分析子 Agent 能力
   ↓
5. Orchestrator.buildGraph()
   - 根据 TaskPlan 构建执行图（DAG）
   - 处理任务依赖关系
   - 创建执行器节点
   ↓
6. 图执行
   - 异步执行各个任务节点
   - 每个节点调用对应的 Agent
   - Agent 可以使用 RAG 检索、MCP 工具
   - 任务结果保存到 WorkspaceState
   ↓
7. 流式输出
   - 通过 SSE 实时推送执行状态
   - 保存对话历史到 InMemory
   ↓
8. 返回最终结果
```

## 关键特性

### 1. 多 Agent 协作
- 支持多个专业化的 Agent 协同工作
- 每个 Agent 可以配置不同的能力（RAG、MCP、SystemPrompt）

### 2. 任务编排
- 支持复杂的任务依赖关系
- 自动构建执行图
- 支持并行和串行执行

### 3. RAG 增强
- 文档向量化存储
- 相似度检索
- 知识图谱构建和查询

### 4. MCP 工具集成
- 支持多个 MCP 客户端
- 动态工具调用
- 扩展 Agent 能力

### 5. 流式输出
- 实时推送执行状态
- 支持流式文本输出
- 良好的用户体验

### 6. 记忆管理
- 对话历史存储
- 上下文注入
- 会话隔离

## API 端点

### 1. `/task/execute` (POST)
**功能**: 执行任务
**请求体**:
```json
{
  "orchestratorId": 1,
  "message": "用户请求",
  "userId": "user123",
  "sessionId": "session456"
}
```
**响应**: SSE 事件流

### 2. `/rag/search` (POST)
**功能**: RAG 文档搜索

### 3. `/rag/addDocument` (POST)
**功能**: 添加文档到 RAG

### 4. `/rag/importDocument` (POST)
**功能**: 导入文档并构建知识图谱

## 配置

### OpenAI 配置 (`application.yml`)
```yaml
openai:
  api:
    url: https://api.bianxie.ai/v1
    key: sk-xxx
    model: gpt-4o
```

### 数据库配置
- PostgreSQL: 存储结构化数据和向量
- Neo4j: 存储知识图谱关系

## 状态管理

### WorkspaceState

**核心类**: `WorkspaceState.java`

**状态结构**:
- `userMessage`: 用户原始输入
- `historyContext`: 历史对话上下文
- `scratchpad`: 任务执行结果存储（Map<String, Object>）
  - Key: `task_{taskId}`
  - Value: 任务执行结果字符串

**状态流转**:
```
初始状态 → 任务1执行 → 更新scratchpad → 任务2执行（读取任务1结果） → 更新scratchpad → ... → 最终状态
```

## 设计模式

1. **策略模式**: 不同的执行策略（Streaming/Silent）
2. **工厂模式**: ExecutorFactory 创建执行器
3. **责任链模式**: Armory 构建流程（RootNode → McpNode → RagNode → ChatModelNode → AgentNode → OrchestratorNode）
4. **观察者模式**: 事件流推送（SSE）
5. **单例模式**: InMemory 记忆管理
6. **模板方法模式**: BaseTaskExecutor 定义执行模板

## 技术亮点

1. **异步执行**: 使用 CompletableFuture 和线程池实现真正的异步
2. **流式处理**: 支持 TokenStream 流式输出
3. **向量检索**: 使用 pgvector 实现高效的向量相似度搜索
4. **图数据库**: 结合 Neo4j 实现知识图谱查询
5. **动态构建**: 运行时动态构建 Agent 和 AiService
6. **事件驱动**: 基于 SSE 的实时事件推送

## 总结

这是一个功能完整的多 Agent 编排系统，具备以下核心能力：

1. ✅ **多 Agent 协作**: Supervisor-Worker 架构
2. ✅ **任务编排**: 基于 DAG 的任务执行
3. ✅ **RAG 增强**: 文档检索和知识图谱
4. ✅ **工具集成**: MCP 协议支持
5. ✅ **流式输出**: 实时状态推送
6. ✅ **记忆管理**: 对话历史存储和注入

系统设计良好，模块化程度高，易于扩展和维护。

