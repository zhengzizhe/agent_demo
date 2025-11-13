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
-- 1. Agent 表
-- ========================
CREATE TABLE IF NOT EXISTS public.agent
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(255) NOT NULL,
    description TEXT,
    status      VARCHAR(50) DEFAULT 'ACTIVE',
    created_at  BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT,
    updated_at  BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT
);

COMMENT ON TABLE public.agent IS 'Agent表';
COMMENT ON COLUMN public.agent.id IS '主键ID';
COMMENT ON COLUMN public.agent.name IS 'Agent名称';
COMMENT ON COLUMN public.agent.description IS 'Agent描述';
COMMENT ON COLUMN public.agent.status IS '状态：ACTIVE, INACTIVE';
COMMENT ON COLUMN public.agent.created_at IS '创建时间（时间戳，秒）';
COMMENT ON COLUMN public.agent.updated_at IS '更新时间（时间戳，秒）';

ALTER TABLE public.agent OWNER TO postgres;

-- 索引
CREATE INDEX IF NOT EXISTS idx_agent_status ON public.agent(status);
CREATE INDEX IF NOT EXISTS idx_agent_created_at ON public.agent(created_at);

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
-- 5. Client 表
-- ========================
CREATE TABLE IF NOT EXISTS public.client
(
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    description   TEXT,
    status        VARCHAR(50) DEFAULT 'ACTIVE',
    created_at    BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT,
    updated_at    BIGINT DEFAULT (EXTRACT(EPOCH FROM CURRENT_TIMESTAMP))::BIGINT,
    system_prompt TEXT
);

COMMENT ON TABLE public.client IS 'Client表';
COMMENT ON COLUMN public.client.system_prompt IS '系统提示词（System Prompt）';

ALTER TABLE public.client OWNER TO postgres;

-- 如果表已存在但system_prompt是VARCHAR，则修改为TEXT
DO $$
BEGIN
    IF EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_schema = 'public' 
        AND table_name = 'client' 
        AND column_name = 'system_prompt'
        AND data_type = 'character varying'
    ) THEN
        ALTER TABLE public.client ALTER COLUMN system_prompt TYPE TEXT;
    END IF;
END $$;

-- 索引
CREATE INDEX IF NOT EXISTS idx_client_status ON public.client(status);

-- ========================
-- 6. 关联表 Agent-Client
-- ========================
CREATE TABLE IF NOT EXISTS public.agent_client
(
    agent_id  BIGINT NOT NULL,
    client_id BIGINT NOT NULL,
    seq       INTEGER,
    CONSTRAINT pk_agent_client PRIMARY KEY (agent_id, client_id),
    CONSTRAINT fk_agent_client_agent FOREIGN KEY (agent_id) REFERENCES public.agent(id) ON DELETE CASCADE,
    CONSTRAINT fk_agent_client_client FOREIGN KEY (client_id) REFERENCES public.client(id) ON DELETE CASCADE
);

COMMENT ON TABLE public.agent_client IS 'Agent与Client的关联表';
COMMENT ON COLUMN public.agent_client.seq IS 'Client在Agent中的执行顺序';

ALTER TABLE public.agent_client OWNER TO postgres;

-- 索引
CREATE INDEX IF NOT EXISTS idx_agent_client_agent_id ON public.agent_client(agent_id);
CREATE INDEX IF NOT EXISTS idx_agent_client_client_id ON public.agent_client(client_id);
CREATE INDEX IF NOT EXISTS idx_agent_client_seq ON public.agent_client(agent_id, seq);

-- ========================
-- 7. 关联表 Client-Model
-- ========================
CREATE TABLE IF NOT EXISTS public.client_model
(
    client_id BIGINT NOT NULL,
    model_id  BIGINT NOT NULL,
    CONSTRAINT pk_client_model PRIMARY KEY (client_id, model_id),
    CONSTRAINT fk_client_model_client FOREIGN KEY (client_id) REFERENCES public.client(id) ON DELETE CASCADE,
    CONSTRAINT fk_client_model_model FOREIGN KEY (model_id) REFERENCES public.model(id) ON DELETE CASCADE
);

COMMENT ON TABLE public.client_model IS 'Client与Model的关联表';

ALTER TABLE public.client_model OWNER TO postgres;

-- 索引
CREATE INDEX IF NOT EXISTS idx_client_model_client_id ON public.client_model(client_id);
CREATE INDEX IF NOT EXISTS idx_client_model_model_id ON public.client_model(model_id);

-- ========================
-- 8. 关联表 Client-RAG
-- ========================
CREATE TABLE IF NOT EXISTS public.client_rag
(
    client_id BIGINT NOT NULL,
    rag_id    BIGINT NOT NULL,
    CONSTRAINT pk_client_rag PRIMARY KEY (client_id, rag_id),
    CONSTRAINT fk_client_rag_client FOREIGN KEY (client_id) REFERENCES public.client(id) ON DELETE CASCADE,
    CONSTRAINT fk_client_rag_rag FOREIGN KEY (rag_id) REFERENCES public.rag(id) ON DELETE CASCADE
);

COMMENT ON TABLE public.client_rag IS 'Client与RAG的关联表';

ALTER TABLE public.client_rag OWNER TO postgres;

-- 索引
CREATE INDEX IF NOT EXISTS idx_client_rag_client_id ON public.client_rag(client_id);
CREATE INDEX IF NOT EXISTS idx_client_rag_rag_id ON public.client_rag(rag_id);

-- ========================
-- 9. 关联表 Client-MCP
-- ========================
CREATE TABLE IF NOT EXISTS public.client_mcp
(
    client_id BIGINT NOT NULL,
    mcp_id    BIGINT NOT NULL,
    CONSTRAINT pk_client_mcp PRIMARY KEY (client_id, mcp_id),
    CONSTRAINT fk_client_mcp_client FOREIGN KEY (client_id) REFERENCES public.client(id) ON DELETE CASCADE,
    CONSTRAINT fk_client_mcp_mcp FOREIGN KEY (mcp_id) REFERENCES public.mcp(id) ON DELETE CASCADE
);

COMMENT ON TABLE public.client_mcp IS 'Client与MCP的关联表';

ALTER TABLE public.client_mcp OWNER TO postgres;

-- 索引
CREATE INDEX IF NOT EXISTS idx_client_mcp_client_id ON public.client_mcp(client_id);
CREATE INDEX IF NOT EXISTS idx_client_mcp_mcp_id ON public.client_mcp(mcp_id);

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
-- 11. Agent执行会话表 (Agent Run)
-- ========================
CREATE TABLE IF NOT EXISTS public.agent_run
(
    id          BIGSERIAL PRIMARY KEY,
    agent_id    BIGINT NOT NULL,
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
    CONSTRAINT fk_agent_run_agent FOREIGN KEY (agent_id) REFERENCES public.agent(id) ON DELETE CASCADE
);

COMMENT ON TABLE public.agent_run IS 'Agent执行会话表，记录每次Agent执行的完整信息';
COMMENT ON COLUMN public.agent_run.session_id IS '用户会话ID，用于跨会话记忆';
COMMENT ON COLUMN public.agent_run.user_id IS '用户标识';
COMMENT ON COLUMN public.agent_run.goal IS '执行目标/用户查询';
COMMENT ON COLUMN public.agent_run.status IS '执行状态';
COMMENT ON COLUMN public.agent_run.execution_mode IS '执行模式：PARALLEL/SERIAL/HYBRID';
COMMENT ON COLUMN public.agent_run.metadata IS '扩展元数据（JSON格式）';

ALTER TABLE public.agent_run OWNER TO postgres;

-- 索引
CREATE INDEX IF NOT EXISTS idx_agent_run_agent_id ON public.agent_run(agent_id);
CREATE INDEX IF NOT EXISTS idx_agent_run_session_id ON public.agent_run(session_id);
CREATE INDEX IF NOT EXISTS idx_agent_run_user_id ON public.agent_run(user_id);
CREATE INDEX IF NOT EXISTS idx_agent_run_status ON public.agent_run(status);
CREATE INDEX IF NOT EXISTS idx_agent_run_start_time ON public.agent_run(start_time);

-- ========================
-- 12. Agent消息记录表 (Agent Message)
-- ========================
CREATE TABLE IF NOT EXISTS public.agent_message
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
    CONSTRAINT fk_agent_message_run FOREIGN KEY (run_id) REFERENCES public.agent_run(id) ON DELETE CASCADE
);

COMMENT ON TABLE public.agent_message IS 'Agent消息记录表，记录所有Agent间的交互信息';
COMMENT ON COLUMN public.agent_message.run_id IS '关联的执行会话ID';
COMMENT ON COLUMN public.agent_message.task_id IS '任务ID';
COMMENT ON COLUMN public.agent_message.agent_role IS 'Agent角色';
COMMENT ON COLUMN public.agent_message.message_type IS '消息类型';
COMMENT ON COLUMN public.agent_message.content IS '消息内容';
COMMENT ON COLUMN public.agent_message.metadata IS '消息元数据（tokens消耗、成本、耗时等）';
COMMENT ON COLUMN public.agent_message.sequence IS '消息序列号';

ALTER TABLE public.agent_message OWNER TO postgres;

-- 索引
CREATE INDEX IF NOT EXISTS idx_agent_message_run_id ON public.agent_message(run_id);
CREATE INDEX IF NOT EXISTS idx_agent_message_task_id ON public.agent_message(task_id);
CREATE INDEX IF NOT EXISTS idx_agent_message_agent_role ON public.agent_message(agent_role);
CREATE INDEX IF NOT EXISTS idx_agent_message_type ON public.agent_message(message_type);
CREATE INDEX IF NOT EXISTS idx_agent_message_timestamp ON public.agent_message(timestamp);
CREATE INDEX IF NOT EXISTS idx_agent_message_sequence ON public.agent_message(run_id, sequence);

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
DROP TRIGGER IF EXISTS update_agent_updated_at ON public.agent;
DROP TRIGGER IF EXISTS update_model_updated_at ON public.model;
DROP TRIGGER IF EXISTS update_mcp_updated_at ON public.mcp;
DROP TRIGGER IF EXISTS update_rag_updated_at ON public.rag;
DROP TRIGGER IF EXISTS update_client_updated_at ON public.client;
DROP TRIGGER IF EXISTS update_agent_run_updated_at ON public.agent_run;
DROP TRIGGER IF EXISTS update_agent_memory_updated_at ON public.agent_memory;

-- 为所有表添加updated_at触发器
CREATE TRIGGER update_agent_updated_at
    BEFORE UPDATE ON public.agent
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

CREATE TRIGGER update_client_updated_at
    BEFORE UPDATE ON public.client
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_agent_run_updated_at
    BEFORE UPDATE ON public.agent_run
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_agent_memory_updated_at
    BEFORE UPDATE ON public.agent_memory
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

-- ========================
-- 12. 初始数据
-- ========================

-- Model 数据
INSERT INTO public.model (id, name, provider, model_type, api_endpoint, api_key, max_tokens, temperature, status, created_at, updated_at)
VALUES (
    1,
    'GPT-4o',
    'OPENAI',
    'gpt-4o',
    'https://apis.itedus.cn/v1/',
    'sk-uw6scttcKPDJZt8DDfD0135c85A04570Ae780c5f350dDf72',
    4096,
    0.7,
    'ACTIVE',
    1762738285,
    1762738285
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

-- Agent 数据
INSERT INTO public.agent (id, name, description, status, created_at, updated_at)
VALUES (
    1,
    'Default Agent',
    '默认Agent配置，包含Supervisor、Researcher、Executor、Critic四个角色',
    'ACTIVE',
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT,
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT
)
ON CONFLICT (id) DO UPDATE SET
    name = EXCLUDED.name,
    description = EXCLUDED.description,
    status = EXCLUDED.status,
    updated_at = EXCLUDED.updated_at;

-- Client 数据

-- Supervisor Client (seq=1, 主管)
INSERT INTO public.client (id, name, description, system_prompt, status, created_at, updated_at)
VALUES (
    1,
    'Supervisor Client',
    '主管角色，负责任务拆解、指派、收敛，输出计划JSON',
    '你是Supervisor，负责任务规划。返回JSON格式：{"shouldProceed":boolean,"executionMode":"PARALLEL|SERIAL","greeting":"","tasks":[{"id":"t1","type":"research|execute","assignee":"Researcher|Executor","payload":""}]} 或自然语言总结结果。',
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

-- Researcher Client (seq=2, 研究员)
INSERT INTO public.client (id, name, description, system_prompt, status, created_at, updated_at)
VALUES (
    2,
    'Researcher Client',
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

-- Executor Client (seq=3, 执行器)
INSERT INTO public.client (id, name, description, system_prompt, status, created_at, updated_at)
VALUES (
    3,
    'Executor Client',
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

-- Critic Client (seq=4, 评审员)
INSERT INTO public.client (id, name, description, system_prompt, status, created_at, updated_at)
VALUES (
    4,
    'Critic Client',
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

-- Agent-Client 关联（按执行顺序）

-- Supervisor (seq=1)
INSERT INTO public.agent_client (agent_id, client_id, seq)
VALUES (1, 1, 1)
ON CONFLICT (agent_id, client_id) DO UPDATE SET seq = EXCLUDED.seq;

-- Researcher (seq=2)
INSERT INTO public.agent_client (agent_id, client_id, seq)
VALUES (1, 2, 2)
ON CONFLICT (agent_id, client_id) DO UPDATE SET seq = EXCLUDED.seq;

-- Executor (seq=3)
INSERT INTO public.agent_client (agent_id, client_id, seq)
VALUES (1, 3, 3)
ON CONFLICT (agent_id, client_id) DO UPDATE SET seq = EXCLUDED.seq;

-- Critic (seq=4)
INSERT INTO public.agent_client (agent_id, client_id, seq)
VALUES (1, 4, 4)
ON CONFLICT (agent_id, client_id) DO UPDATE SET seq = EXCLUDED.seq;

-- Client-Model 关联（所有Client都使用GPT-4o）

INSERT INTO public.client_model (client_id, model_id)
VALUES 
    (1, 1),  -- Supervisor使用GPT-4o
    (2, 1),  -- Researcher使用GPT-4o
    (3, 1),  -- Executor使用GPT-4o
    (4, 1)   -- Critic使用GPT-4o
ON CONFLICT (client_id, model_id) DO NOTHING;

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

-- Client-RAG 关联（Researcher Client 使用默认 RAG）
INSERT INTO public.client_rag (client_id, rag_id)
VALUES (2, 1)
ON CONFLICT (client_id, rag_id) DO NOTHING;

COMMIT;

-- ========================================
-- End of File
-- ========================================
