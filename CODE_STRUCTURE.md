# 代码结构说明

## 项目架构

本项目采用 **DDD（领域驱动设计）分层架构** + **Spring Boot** + **MyBatis**。

## 包结构

```
com.example.ddd/
├── common/                    # 公共层（所有层都可以使用）
│   ├── constant/            # 常量定义
│   └── utils/               # 工具类（JSON、ID生成器、BeanUtil等）
│
├── application/              # 应用层（应用服务 + 仓储接口定义）
│   ├── agent/               # Agent 应用
│   │   ├── repository/     # 仓储接口定义（I*Repository）
│   │   └── service/         # 应用服务（RagService、TaskExecuteService等）
│   ├── doc/                 # 文档应用
│   │   └── service/         # 应用服务（DocService）
│   └── kg/                  # 知识图谱应用
│       └── service/          # 应用服务（KgEntityService）
│
├── domain/                   # 领域层（核心业务逻辑 + 仓储实现）
│   ├── agent/               # Agent 领域
│   │   ├── model/          # 领域模型
│   │   │   ├── entity/     # 实体（AgentEntity、RagEntity等）
│   │   │   └── valobj/     # 值对象（EntityDTO、RelationDTO等）
│   │   ├── repository/     # 仓储实现（实现 application 层定义的接口）
│   │   └── service/        # 领域服务
│   │       ├── armory/     # Agent 构建服务（RootNode、AgentNode等）
│   │       └── execute/    # Agent 执行服务（Orchestrator、TaskExecutor等）
│   ├── doc/                # 文档领域
│   │   └── repository/     # 仓储实现
│   └── kg/                 # 知识图谱领域
│       └── repository/      # 仓储实现
│
├── infrastructure/          # 基础设施层（技术实现）
│   ├── adapter/           # 适配器实现
│   │   └── repository/    # 辅助仓储（关联表操作，如 DocKgEntityRepository、RagDocRepository等）
│   ├── config/            # 基础设施配置
│   │   ├── MyBatisConfig.java
│   │   ├── CorsConfig.java
│   │   ├── Neo4jDriverFactory.java
│   │   ├── ObjectMapperConfig.java
│   │   └── OpenAIProperties.java
│   └── dao/               # 数据访问层
│       ├── mapper/        # MyBatis Mapper 接口
│       ├── po/            # 持久化对象（PO）
│       └── typehandler/   # MyBatis 类型处理器
│
└── interfaces/            # 接口层（API 层）
    ├── controller/        # REST 控制器
    ├── request/          # 请求 DTO
    └── response/         # 响应 DTO
```

## 分层依赖规则

```
┌─────────────┐
│ interfaces  │  ← 只能依赖 application
└──────┬──────┘
       │
┌──────▼──────────┐
│  application    │  ← 定义仓储接口，提供应用服务
└──────┬──────┘
       │
┌──────▼──────┐
│   domain    │  ← 实现仓储接口，包含领域逻辑
└──────┬──────┘
       │
┌──────▼──────────┐
│ infrastructure  │  ← 提供数据访问能力（Mapper、PO）
└─────────────────┘
       │
       │ 所有层都可以使用
       ▼
┌─────────────┐
│   common    │
└─────────────┘
```

### 依赖规则说明

1. **interfaces 层**：
   - ✅ 只能注入 `application` 层的 Service
   - ❌ 不能直接注入 Repository 或 Mapper

2. **application 层**：
   - ✅ 定义仓储接口（I*Repository）
   - ✅ 提供应用服务（协调多个领域服务）
   - ✅ 可以注入 `domain` 层的 Repository（通过接口）
   - ❌ 不能直接注入 Mapper

3. **domain 层**：
   - ✅ 实现 `application` 层定义的仓储接口
   - ✅ 可以注入 `infrastructure` 层的 Mapper（数据访问）
   - ✅ 包含领域模型和领域服务
   - ❌ 不能依赖 `application` 层

4. **infrastructure 层**：
   - ✅ 提供数据访问能力（Mapper、PO、TypeHandler）
   - ✅ 可以包含辅助仓储（关联表操作）
   - ❌ 不实现主仓储接口（主仓储在 domain 层实现）

## 命名规范

### Repository
- **接口**：`application/*/repository/I*Repository.java`
- **实现**：`domain/*/repository/*Repository.java`

### Service
- **应用服务**：`application/*/service/*Service.java`
- **领域服务**：`domain/*/service/*Service.java`

### Controller
- **REST 控制器**：`interfaces/controller/*Controller.java`

### DTO
- **请求**：`interfaces/request/*Request.java`
- **响应**：`interfaces/response/*View.java`

### Entity/PO
- **领域实体**：`domain/*/model/entity/*Entity.java`
- **持久化对象**：`infrastructure/dao/po/*PO.java`

## 代码组织原则

1. **单一职责**：每个类只负责一个功能
2. **依赖倒置**：application 层定义接口，domain 层实现
3. **分层清晰**：严格遵循分层依赖规则
4. **命名统一**：遵循统一的命名规范

## 技术栈

- **框架**：Spring Boot
- **ORM**：MyBatis
- **数据库**：PostgreSQL（支持 pgvector）
- **图数据库**：Neo4j
- **AI 框架**：LangChain4j、LangGraph4j
