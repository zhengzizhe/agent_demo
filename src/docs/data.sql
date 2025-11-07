-- ============================================
-- Agent Demo 测试数据
-- ============================================
-- 说明：包含任务处理Agent及其3个Client的完整测试数据

-- ============================================
-- 0. 确保client表有system_prompt字段
-- ============================================
DO $$ 
BEGIN
    IF NOT EXISTS (
        SELECT 1 
        FROM information_schema.columns 
        WHERE table_name = 'client' 
        AND column_name = 'system_prompt'
    ) THEN
        ALTER TABLE client ADD COLUMN system_prompt TEXT;
        COMMENT ON COLUMN client.system_prompt IS '系统提示词（System Prompt）';
    END IF;
END $$;

-- ============================================
-- 1. Model 测试数据
-- ============================================
INSERT INTO model (name, provider, model_type, api_endpoint, api_key, max_tokens, temperature, status, created_at, updated_at)
SELECT
    'GPT-4o',
    'OPENAI',
    'gpt-4o',
    'https://apis.itedus.cn/v1/',
    'sk-uw6scttcKPDJZt8DDfD0135c85A04570Ae780c5f350dDf72',
    4096,
    0.7,
    'ACTIVE',
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT,
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT
WHERE NOT EXISTS (SELECT 1 FROM model WHERE name = 'GPT-4o');

INSERT INTO model (name, provider, model_type, api_endpoint, api_key, max_tokens, temperature, status, created_at, updated_at)
SELECT
    'GPT-4o-mini',
    'OPENAI',
    'gpt-4o-mini',
    'https://apis.itedus.cn/v1/',
    'sk-uw6scttcKPDJZt8DDfD0135c85A04570Ae780c5f350dDf72',
    16384,
    0.7,
    'ACTIVE',
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT,
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT
WHERE NOT EXISTS (SELECT 1 FROM model WHERE name = 'GPT-4o-mini');

-- ============================================
-- 2. RAG 测试数据
-- ============================================
INSERT INTO rag (
    name, 
    vector_store_type, 
    embedding_model, 
    chunk_size, 
    chunk_overlap,
    database_type,
    database_host,
    database_port,
    database_name,
    database_user,
    database_password,
    table_name,
    dimension,
    use_index,
    index_list_size,
    status,
    created_at,
    updated_at
)
SELECT
    '本地知识库RAG',
    'PGVECTOR',
    'text-embedding-ada-002',
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
    'ACTIVE',
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT,
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT
WHERE NOT EXISTS (SELECT 1 FROM rag WHERE name = '本地知识库RAG');

-- ============================================
-- 3. Agent 测试数据
-- ============================================
INSERT INTO agent (name, description, status, created_at, updated_at)
SELECT
    '任务处理Agent',
    '由任务分析、任务执行、总结三个Client组成的完整任务处理流程Agent',
    'ACTIVE',
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT,
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT
WHERE NOT EXISTS (SELECT 1 FROM agent WHERE name = '任务处理Agent');

-- ============================================
-- 4. Client 测试数据（3个Client组成任务处理流程）
-- ============================================
INSERT INTO client (name, description, system_prompt, status, created_at, updated_at)
SELECT
    '任务分析Client',
    '负责分析用户任务，拆解任务步骤，识别任务类型和优先级',
    '任务分析师。分析任务需求，拆解执行步骤，识别类型和优先级，输出结构化结果（目标/步骤/资源/预期）。',
    'ACTIVE',
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT,
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT
WHERE NOT EXISTS (SELECT 1 FROM client WHERE name = '任务分析Client');

INSERT INTO client (name, description, system_prompt, status, created_at, updated_at)
SELECT
    '任务执行Client',
    '负责执行具体的任务步骤，调用相应的工具和资源完成任务',
    '任务执行者。按步骤执行任务，调用工具/API，处理异常，记录过程，确保准确完成。',
    'ACTIVE',
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT,
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT
WHERE NOT EXISTS (SELECT 1 FROM client WHERE name = '任务执行Client');

INSERT INTO client (name, description, system_prompt, status, created_at, updated_at)
SELECT
    '总结Client',
    '负责总结任务执行结果，生成最终报告和反馈',
    '总结分析师。汇总执行过程和结果，评估效果，识别问题，提供改进建议，生成清晰报告。',
    'ACTIVE',
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT,
    EXTRACT(EPOCH FROM CURRENT_TIMESTAMP)::BIGINT
WHERE NOT EXISTS (SELECT 1 FROM client WHERE name = '总结Client');

-- ============================================
-- 5. Agent-Client 关联关系
-- ============================================
INSERT INTO agent_client (agent_id, client_id)
SELECT a.id, c.id
FROM agent a, client c
WHERE a.name = '任务处理Agent' AND c.name IN ('任务分析Client', '任务执行Client', '总结Client')
  AND NOT EXISTS (
    SELECT 1 FROM agent_client ac
    WHERE ac.agent_id = a.id AND ac.client_id = c.id
);

-- ============================================
-- 6. Client-Model 关联关系
-- ============================================
-- 任务分析Client -> GPT-4o
INSERT INTO client_model (client_id, model_id)
SELECT c.id, m.id
FROM client c, model m
WHERE c.name = '任务分析Client' AND m.name = 'GPT-4o'
  AND NOT EXISTS (
    SELECT 1 FROM client_model cm
    WHERE cm.client_id = c.id AND cm.model_id = m.id
);

-- 任务执行Client -> GPT-4o
INSERT INTO client_model (client_id, model_id)
SELECT c.id, m.id
FROM client c, model m
WHERE c.name = '任务执行Client' AND m.name = 'GPT-4o'
  AND NOT EXISTS (
    SELECT 1 FROM client_model cm
    WHERE cm.client_id = c.id AND cm.model_id = m.id
);

-- 总结Client -> GPT-4o-mini
INSERT INTO client_model (client_id, model_id)
SELECT c.id, m.id
FROM client c, model m
WHERE c.name = '总结Client' AND m.name = 'GPT-4o-mini'
  AND NOT EXISTS (
    SELECT 1 FROM client_model cm
    WHERE cm.client_id = c.id AND cm.model_id = m.id
);

-- ============================================
-- 7. Client-RAG 关联关系
-- ============================================
-- 任务分析Client和任务执行Client关联知识库
INSERT INTO client_rag (client_id, rag_id)
SELECT c.id, r.id
FROM client c, rag r
WHERE c.name IN ('任务分析Client', '任务执行Client') AND r.name = '本地知识库RAG'
  AND NOT EXISTS (
    SELECT 1 FROM client_rag cr
    WHERE cr.client_id = c.id AND cr.rag_id = r.id
);
