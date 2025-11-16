-- ========================================
-- Database Schema Initialization Script
-- Version: 2.0
-- Description: Agent / Model / MCP / RAG / Client Schema
-- Author: zzz
-- ========================================

BEGIN;

-- ========================
-- 扩展
-- ========================
CREATE EXTENSION IF NOT EXISTS vector;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ========================
-- 1. Orchestrator 表（原 Agent 表）
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

COMMENT ON TABLE public.orchestrator IS 'Orchestrator表（编排器）';
COMMENT ON COLUMN public.orchestrator.id IS '主键ID';
COMMENT ON COLUMN public.orchestrator.name IS 'Orchestrator名称';
COMMENT ON COLUMN public.orchestrator.description IS 'Orchestrator描述';
COMMENT ON COLUMN public.orchestrator.status IS '状态：ACTIVE, INACTIVE';
COMMENT ON COLUMN public.orchestrator.created_at IS '创建时间（时间戳，秒）';
COMMENT ON COLUMN public.orchestrator.updated_at IS '更新时间（时间戳，秒）';

ALTER TABLE public.orchestrator OWNER TO postgres;

-- 索引
CREATE INDEX IF NOT EXISTS idx_orchestrator_status ON public.orchestrator(status);
CREATE INDEX IF NOT EXISTS idx_orchestrator_created_at ON public.orchestrator(created_at);

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

COMMENT ON TABLE public.model IS 'Model表';
COMMENT ON COLUMN public.model.provider IS '提供商：OPENAI, ANTHROPIC, OLLAMA等';
COMMENT ON COLUMN public.model.api_key IS 'API密钥（加密存储）';
COMMENT ON COLUMN public.model.max_tokens IS '最大token数';
COMMENT ON COLUMN public.model.temperature IS '温度参数';

ALTER TABLE public.model OWNER TO postgres;

-- 索引
CREATE INDEX IF NOT EXISTS idx_model_provider ON public.model(provider);
CREATE INDEX IF NOT EXISTS idx_model_status ON public.model(status);

-- ========================
-- 3. MCP 表
-- ========================
CREATE TABLE IF NOT EXISTS public.mcp
(
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    type       VARCHAR(100) NOT NULL,
    endpoint   VARCHAR(500),
    config     JSONB,
    status     VARCHAR(50) DEFAULT 'ACTIVE',
    created_at BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT,
    updated_at BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT
);

COMMENT ON TABLE public.mcp IS 'MCP表（Model Context Protocol）';
COMMENT ON COLUMN public.mcp.config IS 'MCP配置信息（JSON格式）';

ALTER TABLE public.mcp OWNER TO postgres;

-- 索引
CREATE INDEX IF NOT EXISTS idx_mcp_type ON public.mcp(type);
CREATE INDEX IF NOT EXISTS idx_mcp_status ON public.mcp(status);

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

COMMENT ON TABLE public.rag IS 'RAG表（Retrieval-Augmented Generation）';
COMMENT ON COLUMN public.rag.config IS 'RAG配置信息（JSON格式）';

ALTER TABLE public.rag OWNER TO postgres;

-- 索引
CREATE INDEX IF NOT EXISTS idx_rag_status ON public.rag(status);
CREATE INDEX IF NOT EXISTS idx_rag_vector_store_type ON public.rag(vector_store_type);

-- ========================
-- 5. Agent 表（原 Client 表）
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

COMMENT ON TABLE public.agent IS 'Agent表（智能体）';
COMMENT ON COLUMN public.agent.system_prompt IS '系统提示词（System Prompt）';

ALTER TABLE public.agent OWNER TO postgres;

-- 如果表已存在但system_prompt是VARCHAR，则修改为TEXT
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'agent' 
        AND column_name = 'system_prompt'
        AND data_type = 'character varying'
    ) THEN
        ALTER TABLE public.agent ALTER COLUMN system_prompt TYPE TEXT;
    END IF;
END $$;

-- 索引
CREATE INDEX IF NOT EXISTS idx_agent_status ON public.agent(status);

-- ========================
-- 6. 关联表 Orchestrator-Agent
-- ========================
CREATE TABLE IF NOT EXISTS public.orchestrator_agent
(
    orchestrator_id BIGINT NOT NULL,
    agent_id        BIGINT NOT NULL,
    seq             INTEGER,
    role            BIGINT,
    CONSTRAINT pk_orchestrator_agent PRIMARY KEY (orchestrator_id, agent_id),
    CONSTRAINT fk_orchestrator_agent_orchestrator FOREIGN KEY (orchestrator_id) REFERENCES public.orchestrator(id) ON DELETE CASCADE,
    CONSTRAINT fk_orchestrator_agent_agent FOREIGN KEY (agent_id) REFERENCES public.agent(id) ON DELETE CASCADE
);

COMMENT ON TABLE public.orchestrator_agent IS 'Orchestrator与Agent的关联表';
COMMENT ON COLUMN public.orchestrator_agent.seq IS 'Agent在Orchestrator中的执行顺序';
COMMENT ON COLUMN public.orchestrator_agent.role IS 'Agent角色代码：1=supervisor, 2=worker';

ALTER TABLE public.orchestrator_agent OWNER TO postgres;

-- 如果表已存在但role字段不存在，则添加role字段；如果存在但类型不对，则修改类型
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.tables 
        WHERE table_schema = 'public' 
        AND table_name = 'orchestrator_agent'
    ) THEN
        IF NOT EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_schema = 'public' 
            AND table_name = 'orchestrator_agent' 
            AND column_name = 'role'
        ) THEN
            ALTER TABLE public.orchestrator_agent ADD COLUMN role BIGINT;
            COMMENT ON COLUMN public.orchestrator_agent.role IS 'Agent角色代码：1=supervisor, 2=worker';
        ELSIF EXISTS (
            SELECT 1 FROM information_schema.columns 
            WHERE table_schema = 'public' 
            AND table_name = 'orchestrator_agent' 
            AND column_name = 'role'
            AND data_type != 'bigint'
        ) THEN
            ALTER TABLE public.orchestrator_agent ALTER COLUMN role TYPE BIGINT USING CASE 
                WHEN role = 'supervisor' THEN 1
                WHEN role = 'worker' THEN 2
                ELSE NULL
            END;
            COMMENT ON COLUMN public.orchestrator_agent.role IS 'Agent角色代码：1=supervisor, 2=worker';
        END IF;
    END IF;
END $$;

-- 索引
CREATE INDEX IF NOT EXISTS idx_orchestrator_agent_orchestrator_id ON public.orchestrator_agent(orchestrator_id);
CREATE INDEX IF NOT EXISTS idx_orchestrator_agent_agent_id ON public.orchestrator_agent(agent_id);
CREATE INDEX IF NOT EXISTS idx_orchestrator_agent_seq ON public.orchestrator_agent(orchestrator_id, seq);

-- ========================
-- 7. 关联表 Agent-Model
-- ========================
CREATE TABLE IF NOT EXISTS public.agent_model
(
    agent_id BIGINT NOT NULL,
    model_id  BIGINT NOT NULL,
    CONSTRAINT pk_agent_model PRIMARY KEY (agent_id, model_id),
    CONSTRAINT fk_agent_model_agent FOREIGN KEY (agent_id) REFERENCES public.agent(id) ON DELETE CASCADE,
    CONSTRAINT fk_agent_model_model FOREIGN KEY (model_id) REFERENCES public.model(id) ON DELETE CASCADE
);

COMMENT ON TABLE public.agent_model IS 'Agent与Model的关联表';

ALTER TABLE public.agent_model OWNER TO postgres;

-- 索引
CREATE INDEX IF NOT EXISTS idx_agent_model_agent_id ON public.agent_model(agent_id);
CREATE INDEX IF NOT EXISTS idx_agent_model_model_id ON public.agent_model(model_id);

-- ========================
-- 8. 关联表 Agent-RAG
-- ========================
CREATE TABLE IF NOT EXISTS public.agent_rag
(
    agent_id BIGINT NOT NULL,
    rag_id    BIGINT NOT NULL,
    CONSTRAINT pk_agent_rag PRIMARY KEY (agent_id, rag_id),
    CONSTRAINT fk_agent_rag_agent FOREIGN KEY (agent_id) REFERENCES public.agent(id) ON DELETE CASCADE,
    CONSTRAINT fk_agent_rag_rag FOREIGN KEY (rag_id) REFERENCES public.rag(id) ON DELETE CASCADE
);

COMMENT ON TABLE public.agent_rag IS 'Agent与RAG的关联表';

ALTER TABLE public.agent_rag OWNER TO postgres;

-- 索引
CREATE INDEX IF NOT EXISTS idx_agent_rag_agent_id ON public.agent_rag(agent_id);
CREATE INDEX IF NOT EXISTS idx_agent_rag_rag_id ON public.agent_rag(rag_id);

-- ========================
-- 9. 关联表 Agent-MCP
-- ========================
CREATE TABLE IF NOT EXISTS public.agent_mcp
(
    agent_id BIGINT NOT NULL,
    mcp_id    BIGINT NOT NULL,
    CONSTRAINT pk_agent_mcp PRIMARY KEY (agent_id, mcp_id),
    CONSTRAINT fk_agent_mcp_agent FOREIGN KEY (agent_id) REFERENCES public.agent(id) ON DELETE CASCADE,
    CONSTRAINT fk_agent_mcp_mcp FOREIGN KEY (mcp_id) REFERENCES public.mcp(id) ON DELETE CASCADE
);

COMMENT ON TABLE public.agent_mcp IS 'Agent与MCP的关联表';

ALTER TABLE public.agent_mcp OWNER TO postgres;

-- 索引
CREATE INDEX IF NOT EXISTS idx_agent_mcp_agent_id ON public.agent_mcp(agent_id);
CREATE INDEX IF NOT EXISTS idx_agent_mcp_mcp_id ON public.agent_mcp(mcp_id);

-- ========================
-- 10. 向量存储表
-- ========================
CREATE TABLE IF NOT EXISTS public.vector_document
(
    embedding_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    embedding    VECTOR(1536),
    text         TEXT,
    metadata     JSONB
);

ALTER TABLE public.vector_document OWNER TO postgres;

COMMENT ON TABLE public.vector_document IS '存储向量嵌入数据的表';
COMMENT ON COLUMN public.vector_document.embedding_id IS '向量文档ID（UUID）';
COMMENT ON COLUMN public.vector_document.embedding IS '向量嵌入数据（1536维）';
COMMENT ON COLUMN public.vector_document.text IS '原始文本内容';
COMMENT ON COLUMN public.vector_document.metadata IS '元信息（JSON格式）';

-- 向量索引（使用IVFFlat算法，适合大规模数据）
-- 注意：如果表已有数据，需要先创建索引
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_indexes 
        WHERE schemaname = 'public' 
        AND tablename = 'vector_document' 
        AND indexname = 'vector_document_ivfflat_index'
    ) THEN
        CREATE INDEX vector_document_ivfflat_index
            ON public.vector_document 
            USING ivfflat (embedding vector_cosine_ops)
            WITH (lists = 100);
    END IF;
END $$;

-- 文本索引（用于全文搜索）
CREATE INDEX IF NOT EXISTS idx_vector_document_text 
    ON public.vector_document 
    USING gin(to_tsvector('english', text))
    WHERE text IS NOT NULL;

-- 元数据索引（用于JSONB查询）
CREATE INDEX IF NOT EXISTS idx_vector_document_metadata
    ON public.vector_document
    USING gin(metadata)
    WHERE metadata IS NOT NULL;

-- ========================
-- 11. Orchestrator执行会话表 (Orchestrator Run)
-- ========================
CREATE TABLE IF NOT EXISTS public.orchestrator_run
(
    id          BIGSERIAL PRIMARY KEY,
    orchestrator_id    BIGINT NOT NULL,
    session_id  VARCHAR(255),  -- 用户会话ID，可为空用于匿名会话
    user_id     VARCHAR(255),  -- 用户标识
    goal        TEXT,          -- 执行目标
    status      VARCHAR(50) DEFAULT 'RUNNING', -- RUNNING, COMPLETED, FAILED
    start_time  BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT,
    end_time    BIGINT,
    total_tasks INTEGER DEFAULT 0,
    completed_tasks INTEGER DEFAULT 0,
    execution_mode VARCHAR(50), -- PARALLEL, SERIAL, HYBRID
    metadata    JSONB,         -- 扩展元数据
    created_at  BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT,
    updated_at  BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT,
    CONSTRAINT fk_orchestrator_run_orchestrator FOREIGN KEY (orchestrator_id) REFERENCES public.orchestrator(id) ON DELETE CASCADE
);

COMMENT ON TABLE public.orchestrator_run IS 'Orchestrator执行会话表，记录每次Orchestrator执行的完整信息';
COMMENT ON COLUMN public.orchestrator_run.session_id IS '用户会话ID，用于跨会话记忆';
COMMENT ON COLUMN public.orchestrator_run.user_id IS '用户标识';
COMMENT ON COLUMN public.orchestrator_run.goal IS '执行目标/用户查询';
COMMENT ON COLUMN public.orchestrator_run.status IS '执行状态';
COMMENT ON COLUMN public.orchestrator_run.execution_mode IS '执行模式：PARALLEL/SERIAL/HYBRID';
COMMENT ON COLUMN public.orchestrator_run.metadata IS '扩展元数据（JSON格式）';

ALTER TABLE public.orchestrator_run OWNER TO postgres;

-- 索引
CREATE INDEX IF NOT EXISTS idx_orchestrator_run_orchestrator_id ON public.orchestrator_run(orchestrator_id);
CREATE INDEX IF NOT EXISTS idx_orchestrator_run_session_id ON public.orchestrator_run(session_id);
CREATE INDEX IF NOT EXISTS idx_orchestrator_run_user_id ON public.orchestrator_run(user_id);
CREATE INDEX IF NOT EXISTS idx_orchestrator_run_status ON public.orchestrator_run(status);
CREATE INDEX IF NOT EXISTS idx_orchestrator_run_start_time ON public.orchestrator_run(start_time);

-- ========================
-- 12. Orchestrator消息记录表 (Orchestrator Message)
-- ========================
CREATE TABLE IF NOT EXISTS public.orchestrator_message
(
    id          BIGSERIAL PRIMARY KEY,
    run_id      BIGINT NOT NULL,   -- 关联的执行会话
    task_id     VARCHAR(255),      -- 任务ID (如 t1, t2)
    agent_role  VARCHAR(100) NOT NULL, -- SUPERVISOR, RESEARCHER, EXECUTOR, CRITIC
    message_type VARCHAR(50) NOT NULL, -- REQUEST, RESPONSE, PLAN, TASK_RESULT, ERROR, INFO
    content     TEXT,              -- 消息内容
    metadata    JSONB,             -- 消息元数据（tokens, cost, duration等）
    timestamp   BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT,
    sequence    INTEGER,           -- 消息序列号，用于排序
    CONSTRAINT fk_orchestrator_message_run FOREIGN KEY (run_id) REFERENCES public.orchestrator_run(id) ON DELETE CASCADE
);

COMMENT ON TABLE public.orchestrator_message IS 'Orchestrator消息记录表，记录所有Agent间的交互信息';
COMMENT ON COLUMN public.orchestrator_message.run_id IS '关联的执行会话ID';
COMMENT ON COLUMN public.orchestrator_message.task_id IS '任务ID';
COMMENT ON COLUMN public.orchestrator_message.agent_role IS 'Agent角色';
COMMENT ON COLUMN public.orchestrator_message.message_type IS '消息类型';
COMMENT ON COLUMN public.orchestrator_message.content IS '消息内容';
COMMENT ON COLUMN public.orchestrator_message.metadata IS '消息元数据（tokens消耗、成本、耗时等）';
COMMENT ON COLUMN public.orchestrator_message.sequence IS '消息序列号';

ALTER TABLE public.orchestrator_message OWNER TO postgres;

-- 索引
CREATE INDEX IF NOT EXISTS idx_orchestrator_message_run_id ON public.orchestrator_message(run_id);
CREATE INDEX IF NOT EXISTS idx_orchestrator_message_task_id ON public.orchestrator_message(task_id);
CREATE INDEX IF NOT EXISTS idx_orchestrator_message_agent_role ON public.orchestrator_message(agent_role);
CREATE INDEX IF NOT EXISTS idx_orchestrator_message_type ON public.orchestrator_message(message_type);
CREATE INDEX IF NOT EXISTS idx_orchestrator_message_timestamp ON public.orchestrator_message(timestamp);
CREATE INDEX IF NOT EXISTS idx_orchestrator_message_sequence ON public.orchestrator_message(run_id, sequence);

-- ========================
-- 13. Agent共享记忆表 (Agent Memory)
-- ========================
CREATE TABLE IF NOT EXISTS public.agent_memory
(
    id          BIGSERIAL PRIMARY KEY,
    session_id  VARCHAR(255),      -- 会话ID，为空表示全局记忆
    agent_id    BIGINT,            -- Agent ID，为空表示所有Agent共享
    memory_key  VARCHAR(255) NOT NULL, -- 记忆键
    memory_value TEXT,             -- 记忆值
    memory_type VARCHAR(50) DEFAULT 'KV', -- KV(键值), CONTEXT(上下文), FACT(事实), PREFERENCE(偏好)
    tags        TEXT[],            -- 标签数组，用于分类和搜索
    expires_at  BIGINT,            -- 过期时间戳
    metadata    JSONB,             -- 扩展元数据
    created_at  BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT,
    updated_at  BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT,
    CONSTRAINT fk_agent_memory_agent FOREIGN KEY (agent_id) REFERENCES public.agent(id) ON DELETE CASCADE
);

COMMENT ON TABLE public.agent_memory IS 'Agent共享记忆表，支持会话级和全局级别的记忆存储';
COMMENT ON COLUMN public.agent_memory.session_id IS '会话ID，为空表示全局记忆';
COMMENT ON COLUMN public.agent_memory.agent_id IS 'Agent ID，为空表示所有Agent共享';
COMMENT ON COLUMN public.agent_memory.memory_key IS '记忆键';
COMMENT ON COLUMN public.agent_memory.memory_value IS '记忆值';
COMMENT ON COLUMN public.agent_memory.memory_type IS '记忆类型';
COMMENT ON COLUMN public.agent_memory.tags IS '标签数组';
COMMENT ON COLUMN public.agent_memory.expires_at IS '过期时间戳';

ALTER TABLE public.agent_memory OWNER TO postgres;

-- 索引
CREATE INDEX IF NOT EXISTS idx_agent_memory_session_id ON public.agent_memory(session_id);
CREATE INDEX IF NOT EXISTS idx_agent_memory_agent_id ON public.agent_memory(agent_id);
CREATE INDEX IF NOT EXISTS idx_agent_memory_key ON public.agent_memory(memory_key);
CREATE INDEX IF NOT EXISTS idx_agent_memory_type ON public.agent_memory(memory_type);
CREATE INDEX IF NOT EXISTS idx_agent_memory_tags ON public.agent_memory USING gin(tags);
CREATE INDEX IF NOT EXISTS idx_agent_memory_expires_at ON public.agent_memory(expires_at);

-- ========================
-- 11. 触发器：自动更新updated_at
-- ========================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT;
    RETURN NEW;
END;
$$ language 'plpgsql';

-- 删除已存在的触发器（如果存在）
DROP TRIGGER IF EXISTS update_orchestrator_updated_at ON public.orchestrator;
DROP TRIGGER IF EXISTS update_model_updated_at ON public.model;
DROP TRIGGER IF EXISTS update_mcp_updated_at ON public.mcp;
DROP TRIGGER IF EXISTS update_rag_updated_at ON public.rag;
DROP TRIGGER IF EXISTS update_agent_updated_at ON public.agent;
DROP TRIGGER IF EXISTS update_orchestrator_run_updated_at ON public.orchestrator_run;
DROP TRIGGER IF EXISTS update_agent_memory_updated_at ON public.agent_memory;

-- 为所有表添加updated_at触发器
CREATE TRIGGER update_orchestrator_updated_at
    BEFORE UPDATE ON public.orchestrator
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_model_updated_at
    BEFORE UPDATE ON public.model
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_mcp_updated_at
    BEFORE UPDATE ON public.mcp
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_rag_updated_at
    BEFORE UPDATE ON public.rag
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_agent_updated_at
    BEFORE UPDATE ON public.agent
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_orchestrator_run_updated_at
    BEFORE UPDATE ON public.orchestrator_run
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_agent_memory_updated_at
    BEFORE UPDATE ON public.agent_memory
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ========================
-- 12. 初始数据
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
        '默认Orchestrator配置，包含Supervisor、Researcher、Executor、Critic四个角色的多agent系统',
        'ACTIVE',
        EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT,
        EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT
    ),
    (
        2,
        'Research Orchestrator',
        '专注于研究任务的Orchestrator，包含Supervisor和多个Researcher',
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
    '你是Supervisor，负责任务规划。根据用户需求，将复杂任务拆解为多个子任务，并分配给合适的Agent执行。

你必须返回一个有效的JSON格式的TaskPlan，结构如下：

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
        "fromTask": "依赖的任务ID（字符串，如果依赖其他任务）",
        "fields": ["字段名列表"]
      },
      "outputs": ["输出字段名列表"],
      "executionStrategy": "SILENT|STREAMING|BATCH（执行策略）",
      "children": []（子任务列表，可选，结构与tasks相同）
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
    - fromTask: 如果任务依赖其他任务的输出，填写依赖任务的id
    - fields: 需要的字段名列表
  - outputs: 任务产生的输出字段名列表
  - executionStrategy: 执行策略
    - SILENT: 静默执行，只保存到state，不输出给用户（用于中间步骤）
    - STREAMING: 流式输出，实时向用户输出（用于需要实时反馈的任务）
    - BATCH: 批量输出，执行完成后一次性返回（用于最终结果）
  - children: 子任务列表（可选），用于嵌套任务结构

注意事项：
1. 确保所有任务的agentId都在subAgents中存在
2. 如果任务有依赖关系，使用inputs.fromTask指定依赖的任务id
3. 第一个任务通常fromUser为true
4. 根据任务性质选择合适的executionStrategy
5. 返回的JSON必须是有效的，可以直接解析',
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
    '你是Researcher，负责信息检索和整合。基于RAG返回准确、简洁的答案。回答要精炼，控制在500字以内，避免冗长描述。',
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

-- Executor Agent (id=3, 执行器)
INSERT INTO public.agent (id, name, description, system_prompt, status, created_at, updated_at)
VALUES (
    3,
    'Executor Agent',
    '执行器角色，负责执行动作（MCP/API/DB/脚本等），cap:exec',
    '你是Executor，负责执行具体操作。按要求完成任务，返回执行结果。',
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

-- Critic Agent (id=4, 评审员)
INSERT INTO public.agent (id, name, description, system_prompt, status, created_at, updated_at)
VALUES (
    4,
    'Critic Agent',
    '评审员角色，负责质量评审与停机判定',
    '你是Critic，负责质量评审。返回JSON：{"score":0-1,"pass":boolean,"issues":[],"suggestions":[]}。',
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

-- Writer Agent (id=5, 写作者)
INSERT INTO public.agent (id, name, description, system_prompt, status, created_at, updated_at)
VALUES (
    5,
    'Writer Agent',
    '写作者角色，负责生成自然语言内容（报告、总结、说明），需要tokenstream返回用户',
    '你是Writer，负责生成自然语言内容。根据提供的信息，撰写清晰、结构化的报告、总结或说明文档。',
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

-- Reviewer Agent (id=6, 审查员)
INSERT INTO public.agent (id, name, description, system_prompt, status, created_at, updated_at)
VALUES (
    6,
    'Reviewer Agent',
    '审查员角色，检查整体结果、给出修改建议，需要tokenstream返回用户',
    '你是Reviewer，负责检查整体结果并给出修改建议。仔细审查内容的质量、准确性和完整性，提供建设性的反馈。',
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

-- Orchestrator-Agent 关联（按执行顺序）

-- Orchestrator 1: Default Orchestrator
INSERT INTO public.orchestrator_agent (orchestrator_id, agent_id, seq, role)
VALUES 
    (1, 1, 1, 1),  -- Supervisor (seq=1, role=1)
    (1, 2, 2, 2),  -- Researcher (seq=2, role=2)
    (1, 3, 3, 2),  -- Executor (seq=3, role=2)
    (1, 4, 4, 2)   -- Critic (seq=4, role=2)
ON CONFLICT (orchestrator_id, agent_id) DO UPDATE SET seq = EXCLUDED.seq, role = EXCLUDED.role;

-- Orchestrator 2: Research Orchestrator
INSERT INTO public.orchestrator_agent (orchestrator_id, agent_id, seq, role)
VALUES 
    (2, 1, 1, 1),  -- Supervisor (seq=1, role=1)
    (2, 2, 2, 2),  -- Researcher (seq=2, role=2)
    (2, 5, 3, 2),  -- Writer (seq=3, role=2)
    (2, 6, 4, 2)   -- Reviewer (seq=4, role=2)
ON CONFLICT (orchestrator_id, agent_id) DO UPDATE SET seq = EXCLUDED.seq, role = EXCLUDED.role;

-- Agent-Model 关联

INSERT INTO public.agent_model (agent_id, model_id)
VALUES 
    (1, 1),  -- Supervisor使用GPT-4o
    (2, 1),  -- Researcher使用GPT-4o
    (3, 1),  -- Executor使用GPT-4o
    (4, 1),  -- Critic使用GPT-4o
    (5, 1),  -- Writer使用GPT-4o
    (6, 2)   -- Reviewer使用GPT-3.5-turbo
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

COMMIT;

-- ========================================
-- End of File
-- ========================================
