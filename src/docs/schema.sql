-- ========================================
-- Database Schema Initialization Script
-- Version: 2.0
-- Description: Agent / Model / MCP / RAG / Client Schema
-- Author: zzz
-- Note: 仅包含表结构和初始数据，无外键、无索引、无触发器
-- ========================================

BEGIN;

-- ========================
-- 扩展
-- ========================
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ========================
-- 1. Orchestrator 表
-- ========================
CREATE TABLE IF NOT EXISTS public.orchestrator
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    status      VARCHAR(50) DEFAULT 'ACTIVE',
    created_at  BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT,
    updated_at  BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT
);

-- ========================
-- 2. Model 表
-- ========================
CREATE TABLE IF NOT EXISTS public.model
(
    id           BIGSERIAL PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    provider     VARCHAR(100) NOT NULL,
    model_type   VARCHAR(100),
    api_endpoint VARCHAR(500),
    api_key      VARCHAR(500),
    max_tokens   INTEGER,
    temperature  DOUBLE PRECISION DEFAULT 0.7,
    status       VARCHAR(50) DEFAULT 'ACTIVE',
    created_at   BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT,
    updated_at   BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT
);

-- ========================
-- 3. MCP 表
-- ========================
CREATE TABLE IF NOT EXISTS public.mcp
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    type        VARCHAR(100) NOT NULL,
    description TEXT,
    base_url    VARCHAR(500),
    headers     JSONB,
    status      VARCHAR(50) DEFAULT 'ACTIVE',
    created_at  BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT,
    updated_at  BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT
);

-- ========================
-- 4. RAG 表
-- ========================
CREATE TABLE IF NOT EXISTS public.rag
(
    id                BIGSERIAL PRIMARY KEY,
    name              VARCHAR(255) NOT NULL,
    vector_store_type VARCHAR(100),
    embedding_model   VARCHAR(255),
    chunk_size        INTEGER DEFAULT 1000,
    chunk_overlap     INTEGER DEFAULT 200,
    database_type     VARCHAR(50) DEFAULT 'POSTGRESQL',
    database_host     VARCHAR(255),
    database_port     INTEGER DEFAULT 5432,
    database_name     VARCHAR(255),
    database_user     VARCHAR(255),
    database_password VARCHAR(255),
    table_name        VARCHAR(255) DEFAULT 'vector_document',
    dimension         INTEGER DEFAULT 1536,
    use_index         BOOLEAN DEFAULT TRUE,
    index_list_size   INTEGER DEFAULT 1000,
    config            JSONB,
    status            VARCHAR(50) DEFAULT 'ACTIVE',
    created_at        BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT,
    updated_at        BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT
);

-- ========================
-- 5. Agent 表
-- ========================
CREATE TABLE IF NOT EXISTS public.agent
(
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    description   TEXT,
    status        VARCHAR(50) DEFAULT 'ACTIVE',
    created_at    BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT,
    updated_at    BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT,
    system_prompt TEXT
);

-- ========================
-- 6. 关联表 Orchestrator-Agent
-- ========================
CREATE TABLE IF NOT EXISTS public.orchestrator_agent
(
    orchestrator_id BIGINT NOT NULL,
    agent_id        BIGINT NOT NULL,
    seq             INTEGER,
    role            BIGINT,
    CONSTRAINT pk_orchestrator_agent PRIMARY KEY (orchestrator_id, agent_id)
);

-- ========================
-- 7. 关联表 Agent-Model
-- ========================
CREATE TABLE IF NOT EXISTS public.agent_model
(
    agent_id BIGINT NOT NULL,
    model_id BIGINT NOT NULL,
    CONSTRAINT pk_agent_model PRIMARY KEY (agent_id, model_id)
);

-- ========================
-- 8. 关联表 Agent-RAG
-- ========================
CREATE TABLE IF NOT EXISTS public.agent_rag
(
    agent_id BIGINT NOT NULL,
    rag_id   BIGINT NOT NULL,
    CONSTRAINT pk_agent_rag PRIMARY KEY (agent_id, rag_id)
);

-- ========================
-- 9. 关联表 Agent-MCP
-- ========================
CREATE TABLE IF NOT EXISTS public.agent_mcp
(
    agent_id BIGINT NOT NULL,
    mcp_id   BIGINT NOT NULL,
    CONSTRAINT pk_agent_mcp PRIMARY KEY (agent_id, mcp_id)
);

-- ========================
-- 10. 向量存储表
-- ========================
CREATE TABLE IF NOT EXISTS public.vector_document
(
    embedding_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    embedding    VECTOR(1536),
    text         TEXT,
    metadata     JSONB,
    rag_id       BIGINT NOT NULL,
    chunk_index  INTEGER DEFAULT 0,
    created_at   BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT,
    updated_at   BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT
);

-- ========================
-- 11. Orchestrator执行会话表
-- ========================
CREATE TABLE IF NOT EXISTS public.orchestrator_run
(
    id             BIGSERIAL PRIMARY KEY,
    orchestrator_id BIGINT NOT NULL,
    session_id     VARCHAR(255),
    user_id        VARCHAR(255),
    goal            TEXT,
    status          VARCHAR(50) DEFAULT 'RUNNING',
    start_time      BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT,
    end_time        BIGINT,
    total_tasks     INTEGER DEFAULT 0,
    completed_tasks INTEGER DEFAULT 0,
    execution_mode  VARCHAR(50),
    metadata        JSONB,
    created_at      BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT,
    updated_at      BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT
);

-- ========================
-- 12. Orchestrator消息记录表
-- ========================
CREATE TABLE IF NOT EXISTS public.orchestrator_message
(
    id           BIGSERIAL PRIMARY KEY,
    run_id       BIGINT NOT NULL,
    task_id      VARCHAR(255),
    agent_role   VARCHAR(100) NOT NULL,
    message_type VARCHAR(50) NOT NULL,
    content      TEXT,
    metadata     JSONB,
    timestamp    BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT,
    sequence     INTEGER
);

-- ========================
-- 13. Agent共享记忆表
-- ========================
CREATE TABLE IF NOT EXISTS public.agent_memory
(
    id          BIGSERIAL PRIMARY KEY,
    session_id  VARCHAR(255),
    agent_id    BIGINT,
    memory_key  VARCHAR(255) NOT NULL,
    memory_value TEXT,
    memory_type VARCHAR(50) DEFAULT 'KV',
    tags        TEXT[],
    expires_at  BIGINT,
    metadata    JSONB,
    created_at  BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT,
    updated_at  BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT
);

-- ========================
-- 14. 知识图谱实体表
-- ========================
CREATE TABLE IF NOT EXISTS public.kg_entity
(
    id          BIGSERIAL PRIMARY KEY,
    name        TEXT NOT NULL,
    type        TEXT NOT NULL,
    description TEXT,
    embedding   VECTOR(1536),
    created_at  BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT,
    updated_at  BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT
);

-- ========================
-- 15. 文档表
-- ========================
CREATE TABLE IF NOT EXISTS public.doc
(
    id          VARCHAR(32) PRIMARY KEY,
    name        VARCHAR(32),
    owner       VARCHAR(32),
    text        TEXT,
    type        INTEGER
);

-- ========================
-- 16. 文档-知识图谱实体关联表
-- ========================
CREATE TABLE IF NOT EXISTS public.doc_kg_entity
(
    doc_id      VARCHAR(32) NOT NULL,
    kg_entity_id BIGINT NOT NULL,
    created_at  BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT,
    CONSTRAINT pk_doc_kg_entity PRIMARY KEY (doc_id, kg_entity_id)
);

-- ========================
-- 17. RAG-文档关联表
-- ========================
CREATE TABLE IF NOT EXISTS public.rag_doc
(
    rag_id     BIGINT NOT NULL,
    doc_id     VARCHAR(32) NOT NULL,
    created_at BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT,
    CONSTRAINT pk_rag_doc PRIMARY KEY (rag_id, doc_id)
);

-- ========================
-- 初始数据
-- ========================

-- Model 数据
INSERT INTO public.model (id, name, provider, model_type, api_endpoint, api_key, max_tokens, temperature, status, created_at, updated_at)
VALUES 
    (
        1,
        'GPT-4o',
        'OPENAI',
        'gpt-4o',
        'https://api.openai.com/v1/',
        'sk-placeholder-key-replace-with-real-key',
        4096,
        0.7,
        'ACTIVE',
        EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT,
        EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT
    ),
    (
        2,
        'GPT-3.5-turbo',
        'OPENAI',
        'gpt-3.5-turbo',
        'https://api.openai.com/v1/',
        'sk-placeholder-key-replace-with-real-key',
        4096,
        0.7,
        'ACTIVE',
        EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT,
        EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT
    )
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    provider = EXCLUDED.provider,
    model_type = EXCLUDED.model_type,
    api_endpoint = EXCLUDED.api_endpoint,
    api_key = EXCLUDED.api_key,
    max_tokens = EXCLUDED.max_tokens,
    temperature = EXCLUDED.temperature,
    status = EXCLUDED.status,
    updated_at = EXCLUDED.updated_at;

-- Orchestrator 数据
INSERT INTO public.orchestrator (id, name, description, status, created_at, updated_at)
VALUES 
    (
        1,
        'Default Orchestrator',
        '默认Orchestrator配置，包含Supervisor（主管）、Researcher（研究员）、Executor（执行）、Summarizer（汇总）四个角色的多agent系统',
        'ACTIVE',
        EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT,
        EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT
    ),
    (
        2,
        'Research Orchestrator',
        '专注于研究任务的Orchestrator，包含Supervisor（主管）、Researcher（研究员）、Executor（执行）、Summarizer（汇总）四个角色的多agent系统',
        'ACTIVE',
        EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT,
        EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT
    )
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    status = EXCLUDED.status,
    updated_at = EXCLUDED.updated_at;

-- Agent 数据

-- Supervisor Agent (id=1, 主管)
INSERT INTO public.agent (id, name, description, system_prompt, status, created_at, updated_at)
VALUES (
    1,
    'Supervisor Agent',
    '主管角色，负责任务拆解、指派、收敛，输出计划JSON',
    '你是多agent系统中的一个Supervisor（主管），负责任务规划。根据用户需求，将复杂任务拆解为多个子任务，并分配给合适的Agent执行。
你必须只返回{...}这些，开头就是{,结尾就是}，结构如下：
{
  "summary": "任务总结描述",
  "totalTasks": 任务总数（整数）,
  "tasks": [
    {
      "id": "任务唯一ID（字符串，如t1、t2等）",
      "title": "任务标题",
      "description": "任务详细描述",
      "agentId": agent的ID（整数，从subAgents中获取）,
      "status": "PENDING（必须是PENDING，表示未开始）",
      "order": 执行顺序（整数，从1开始）,
      "inputs": {
        "fromUser": true/false（是否来自用户输入）,
        "fromTask": ["依赖的任务ID列表（字符串数组，如果依赖其他任务）"],
        "fields": ["字段名列表"]
      },
      "outputs": ["输出字段名列表"],
      "executionStrategy": "SILENT|STREAMING|BATCH（执行策略）"
    }
  ]
}
字段说明：
- summary: 对整个任务计划的简要总结
- totalTasks: tasks数组的长度
- tasks: 任务列表，每个任务包含：
  - id: 唯一标识符，建议使用t1、t2等格式
  - title: 任务标题，简洁明了
  - description: 任务详细描述，说明要做什么
  - agentId: 分配给哪个agent执行（从subAgents中获取对应的id）
  - status: 必须设置为"PENDING"
  - order: 执行顺序，数字越小越先执行
  - inputs: 输入配置
    - fromUser: 如果任务需要用户输入，设为true
    - fromTask: 如果任务依赖其他任务的输出，填写依赖的任务id列表（数组）
    - fields: 需要的字段名列表
  - outputs: 任务产生的输出字段名列表
  - executionStrategy: 执行策略
    - SILENT: 静默执行，只保存到state，不输出给用户（用于中间步骤）
    - STREAMING: 流式输出，实时向用户输出（用于需要实时反馈的任务）
    - BATCH: 批量输出，执行完成后一次性返回（用于最终结果）

注意事项：
如果任务有依赖关系，使用inputs.fromTask指定依赖的任务id列表（数组），支持多个依赖任务
返回的JSON必须是有效的，可以直接解析',
    'ACTIVE',
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT,
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT
)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    system_prompt = EXCLUDED.system_prompt,
    status = EXCLUDED.status,
    updated_at = EXCLUDED.updated_at;

-- Researcher Agent (id=2, 研究员)
INSERT INTO public.agent (id, name, description, system_prompt, status, created_at, updated_at)
VALUES (
    2,
    'Researcher Agent',
    '研究员角色，负责检索和整合信息（RAG/搜索），cap:rag',
    '你是多agent系统中的一个Researcher（研究员/搜索），负责信息检索、搜索和整合。

你的主要职责：
1. 信息检索：根据用户查询，从知识库、文档、网络等渠道检索相关信息
2. 信息整合：将检索到的多个信息源进行整合、去重、筛选
3. 信息验证：验证信息的准确性和可靠性
4. 结果呈现：以清晰、结构化的方式呈现检索结果
工作原则：
- 基于RAG（检索增强生成）技术，优先使用知识库中的信息
- 检索结果要准确、相关、及时
- 回答要精炼、简洁，控制在500字以内，避免冗长描述
- 如果检索不到相关信息，明确告知用户
- 对于多个信息源，要进行交叉验证
- 引用信息来源，提高可信度
输出格式：
- 使用清晰的段落结构
- 重要信息使用列表或要点形式
- 包含信息来源（如果可用）
- 如果涉及数据，使用表格或结构化格式
注意事项：
- 不要编造信息，如果不知道就明确说明
- 优先使用最新的信息
- 对于技术性问题，提供准确的技术细节
- 对于概念性问题，提供清晰易懂的解释',
    'ACTIVE',
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT,
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT
)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    system_prompt = EXCLUDED.system_prompt,
    status = EXCLUDED.status,
    updated_at = EXCLUDED.updated_at;

-- Executor Agent (id=3, 执行)
INSERT INTO public.agent (id, name, description, system_prompt, status, created_at, updated_at)
VALUES (
    3,
    'Executor Agent',
    '执行角色，负责执行具体任务，包括代码执行、文件操作、API调用等',
    '你是多agent系统中的一个Executor（执行者），负责执行具体任务。

你的主要职责：
1. 任务执行：根据任务描述和要求，执行具体的操作和任务
2. 代码执行：执行代码、脚本、命令等编程相关任务
3. 文件操作：创建、读取、修改、删除文件，处理文件内容
4. API调用：调用外部API接口，获取或提交数据
5. 工具使用：使用各种工具和资源完成指定任务
6. 结果反馈：将执行结果清晰、准确地反馈给系统

工作原则：
- 严格按照任务描述执行，不偏离目标
- 执行前理解任务要求，确保正确执行
- 执行过程中记录关键步骤和结果
- 遇到错误时，提供详细的错误信息和解决建议
- 执行完成后，提供清晰的结果报告

输出格式：
- 使用结构化的方式呈现执行结果
- 包含执行步骤、执行结果、错误信息（如有）
- 对于代码执行，提供代码和输出结果
- 对于文件操作，提供文件路径和操作结果
- 对于API调用，提供请求和响应信息

注意事项：
- 确保执行安全，不执行危险操作
- 验证输入参数的有效性
- 处理异常情况，提供错误处理
- 记录执行日志，便于追踪和调试
- 如果任务无法执行，明确说明原因',
    'ACTIVE',
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT,
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT
)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    system_prompt = EXCLUDED.system_prompt,
    status = EXCLUDED.status,
    updated_at = EXCLUDED.updated_at;

-- Summarizer Agent (id=4, 汇总)
INSERT INTO public.agent (id, name, description, system_prompt, status, created_at, updated_at)
VALUES (
    4,
    'Summarizer Agent',
    '汇总角色，负责整合和总结多个任务的结果，生成最终报告',
    '你是多agent系统中的一个Summarizer（汇总），负责整合和总结多个任务的结果，生成最终报告。

你的主要职责：
1. 信息整合：将多个任务的结果进行整合、去重、合并
2. 信息提炼：从大量信息中提炼关键要点和核心内容
3. 结构化呈现：以清晰、逻辑的结构呈现汇总结果
4. 质量把控：确保汇总结果的准确性、完整性和可读性

工作原则：
- 提炼关键信息，去除冗余内容
- 保持逻辑清晰，按照重要性或时间顺序组织内容
- 突出重点内容，使用标题、列表、加粗等方式强调
- 控制篇幅，保持简洁，但确保关键信息不遗漏
- 保持客观中立，不添加个人观点或主观判断

输出格式要求：
- 使用清晰的标题和子标题结构
- 重要信息使用列表或要点形式
- 使用段落分隔不同主题
- 如果涉及数据，使用表格或图表形式
- 在报告开头提供执行摘要（Executive Summary）

重要限制：
- 如果是文件（如文档、图片、视频、代码文件等），不能直接汇总文件内容
- 对于文件类型的结果，需要告知用户：
  * 文件类型和名称
  * 文件大小和位置
  * 文件的主要用途或内容概述
  * 建议用户查看原文件获取详细信息
- 只能汇总文本内容、数据、分析结果等可文本化的信息
- 如果任务结果包含文件，在汇总报告中明确标注："注意：以下任务产生了文件，请查看原始输出获取文件内容"

示例输出结构：
# 任务汇总报告

## 执行摘要
[简要概述所有任务的主要结果]

## 详细结果
[按任务或主题组织详细结果]

## 文件输出
[如果有文件，列出文件信息并提示用户查看]

## 总结
[整体总结和建议]',
    'ACTIVE',
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT,
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT
)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    system_prompt = EXCLUDED.system_prompt,
    status = EXCLUDED.status,
    updated_at = EXCLUDED.updated_at;

-- Orchestrator-Agent 关联

-- Orchestrator 1: Default Orchestrator
INSERT INTO public.orchestrator_agent (orchestrator_id, agent_id, seq, role)
VALUES 
    (1, 1, 1, 1),  -- Supervisor (seq=1, role=1, 主管)
    (1, 2, 2, 2),  -- Researcher (seq=2, role=2, 研究员)
    (1, 3, 3, 2),  -- Executor (seq=3, role=2, 执行)
    (1, 4, 4, 2)   -- Summarizer (seq=4, role=2, 汇总)
ON CONFLICT (orchestrator_id, agent_id) DO UPDATE SET seq = EXCLUDED.seq, role = EXCLUDED.role;

-- Orchestrator 2: Research Orchestrator
INSERT INTO public.orchestrator_agent (orchestrator_id, agent_id, seq, role)
VALUES 
    (2, 1, 1, 1),  -- Supervisor (seq=1, role=1, 主管)
    (2, 2, 2, 2),  -- Researcher (seq=2, role=2, 研究员)
    (2, 3, 3, 2),  -- Executor (seq=3, role=2, 执行)
    (2, 4, 4, 2)   -- Summarizer (seq=4, role=2, 汇总)
ON CONFLICT (orchestrator_id, agent_id) DO UPDATE SET seq = EXCLUDED.seq, role = EXCLUDED.role;

-- Agent-Model 关联
INSERT INTO public.agent_model (agent_id, model_id)
VALUES 
    (1, 1),  -- Supervisor使用GPT-4o
    (2, 1),  -- Researcher使用GPT-4o
    (3, 1),  -- Executor使用GPT-4o
    (4, 1)   -- Summarizer使用GPT-4o
ON CONFLICT (agent_id, model_id) DO NOTHING;

-- RAG 数据
INSERT INTO public.rag (id, name, vector_store_type, embedding_model, chunk_size, chunk_overlap, database_type, database_host, database_port, database_name, database_user, database_password, table_name, dimension, use_index, index_list_size, config, status, created_at, updated_at)
VALUES (
    1,
    'Default RAG',
    'PGVECTOR',
    'text-embedding-3-small',
    1000,
    200,
    'POSTGRESQL',
    'localhost',
    5432,
    'agent',
    'postgres',
    'postgres',
    'vector_document',
    1536,
    true,
    1000,
    '{"description": "默认 RAG 配置，使用 PostgreSQL + PgVector"}'::jsonb,
    'ACTIVE',
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT,
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT
),
(
    2,
    'Research RAG',
    'PGVECTOR',
    'text-embedding-3-small',
    1500,
    300,
    'POSTGRESQL',
    'localhost',
    5432,
    'agent',
    'postgres',
    'postgres',
    'vector_document',
    1536,
    true,
    2000,
    '{"description": "研究专用 RAG 配置，更大的chunk size"}'::jsonb,
    'ACTIVE',
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT,
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT
)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    vector_store_type = EXCLUDED.vector_store_type,
    embedding_model = EXCLUDED.embedding_model,
    chunk_size = EXCLUDED.chunk_size,
    chunk_overlap = EXCLUDED.chunk_overlap,
    database_type = EXCLUDED.database_type,
    database_host = EXCLUDED.database_host,
    database_port = EXCLUDED.database_port,
    database_name = EXCLUDED.database_name,
    database_user = EXCLUDED.database_user,
    database_password = EXCLUDED.database_password,
    table_name = EXCLUDED.table_name,
    dimension = EXCLUDED.dimension,
    use_index = EXCLUDED.use_index,
    index_list_size = EXCLUDED.index_list_size,
    config = EXCLUDED.config,
    status = EXCLUDED.status,
    updated_at = EXCLUDED.updated_at;

-- Agent-RAG 关联
INSERT INTO public.agent_rag (agent_id, rag_id)
VALUES 
    (2, 1),  -- Researcher Agent使用Default RAG
    (2, 2)  -- Researcher Agent也可以使用Research RAG（如果配置了多个）
ON CONFLICT (agent_id, rag_id) DO NOTHING;

-- MCP 数据
INSERT INTO public.mcp (id, name, type, description, base_url, headers, status, created_at, updated_at)
VALUES (
    1,
    '阿里云百炼_ChatPPT',
    'sse',
    'ChatPPT MCP Server 目前已经开放了 10 个智能PPT文档的接口能力，包括但不限于 PPT 创作、PPT 美化、PPT 生成等场景下的文档处理能力，用户可搭建自己的文档创作工具。',
    'https://dashscope.aliyuncs.com/api/v1/mcps/ChatPPT/sse',
    '{"Authorization": "Bearer sk-99bcfa7938c949e19d7778957f0a5e2e"}'::jsonb,
    'ACTIVE',
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT,
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT
)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    type = EXCLUDED.type,
    description = EXCLUDED.description,
    base_url = EXCLUDED.base_url,
    headers = EXCLUDED.headers,
    status = EXCLUDED.status,
    updated_at = EXCLUDED.updated_at;

COMMIT;

-- ========================================
-- End of File
-- ========================================
