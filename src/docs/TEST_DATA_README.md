# 测试数据说明

## 文件说明

- `sql.sql` - 数据库表结构定义（DDL）
- `data.sql` - 基础测试数据（简化版，包含默认数据）
- `test_data.sql` - 完整测试数据（包含多个场景的测试数据）

## 使用步骤

### 1. 创建数据库和表结构

```bash
# 连接到 PostgreSQL
psql -U postgres -d agent

# 执行表结构创建
\i src/docs/sql.sql
```

### 2. 插入测试数据

**方式一：使用基础测试数据（推荐用于快速测试）**
```bash
psql -U postgres -d agent -f src/docs/data.sql
```

**方式二：使用完整测试数据（推荐用于完整功能测试）**
```bash
psql -U postgres -d agent -f src/docs/test_data.sql
```

## 测试数据说明

### 基础测试数据（data.sql）

包含：
- 1个 Agent：Default Agent
- 1个 Client：Default Client
- 1个 Model：gpt-4o
- 1个 RAG：Default RAG
- 完整的关联关系

### 完整测试数据（test_data.sql）

包含：

#### Agents（3个）
1. **智能客服Agent** - 用于处理客户咨询
2. **文档助手Agent** - 帮助用户查询和分析文档
3. **代码助手Agent** - 帮助开发者编写和调试代码

#### Clients（3个）
1. **Web客户端** - Web前端应用客户端
2. **移动端客户端** - 移动应用客户端
3. **API客户端** - 第三方API调用客户端

#### Models（4个）
1. **GPT-4o** - OpenAI GPT-4o模型（4096 tokens）
2. **GPT-4o-mini** - OpenAI GPT-4o-mini模型（16384 tokens）
3. **GPT-3.5-turbo** - OpenAI GPT-3.5-turbo模型（4096 tokens）
4. **GPT-4-turbo** - OpenAI GPT-4-turbo模型（4096 tokens）

#### RAGs（3个）
1. **产品知识库RAG** - 产品相关文档（chunk_size: 1000）
2. **技术文档RAG** - 技术文档（chunk_size: 1500）
3. **FAQ知识库RAG** - 常见问题（chunk_size: 800）

#### MCPs（2个，可选）
1. **文件系统MCP** - 文件系统访问
2. **数据库MCP** - 数据库访问

### 关联关系

#### Agent-Client 关联
- 智能客服Agent ↔ Web客户端, 移动端客户端
- 文档助手Agent ↔ Web客户端, API客户端
- 代码助手Agent ↔ Web客户端

#### Client-Model 关联
- Web客户端 ↔ GPT-4o, GPT-4o-mini
- 移动端客户端 ↔ GPT-4o-mini, GPT-3.5-turbo
- API客户端 ↔ GPT-4-turbo, GPT-4o

#### Client-RAG 关联
- Web客户端 ↔ 产品知识库RAG, 技术文档RAG
- 移动端客户端 ↔ FAQ知识库RAG
- API客户端 ↔ 技术文档RAG

#### Client-MCP 关联（可选）
- Web客户端 ↔ 文件系统MCP, 数据库MCP
- API客户端 ↔ 数据库MCP

## 验证数据

执行测试数据后，可以运行以下SQL验证数据：

```sql
-- 查看数据统计
SELECT 'Agent数量' AS type, COUNT(*) AS count FROM agent
UNION ALL
SELECT 'Client数量', COUNT(*) FROM client
UNION ALL
SELECT 'Model数量', COUNT(*) FROM model
UNION ALL
SELECT 'RAG数量', COUNT(*) FROM rag
UNION ALL
SELECT 'MCP数量', COUNT(*) FROM mcp
UNION ALL
SELECT 'Agent-Client关联', COUNT(*) FROM agent_client
UNION ALL
SELECT 'Client-Model关联', COUNT(*) FROM client_model
UNION ALL
SELECT 'Client-RAG关联', COUNT(*) FROM client_rag
UNION ALL
SELECT 'Client-MCP关联', COUNT(*) FROM client_mcp;

-- 查看完整关联关系
SELECT 
    a.name AS agent_name,
    c.name AS client_name,
    m.name AS model_name,
    r.name AS rag_name
FROM agent a
JOIN agent_client ac ON a.id = ac.agent_id
JOIN client c ON ac.client_id = c.id
LEFT JOIN client_model cm ON c.id = cm.client_id
LEFT JOIN model m ON cm.model_id = m.id
LEFT JOIN client_rag cr ON c.id = cr.client_id
LEFT JOIN rag r ON cr.rag_id = r.id
ORDER BY a.name, c.name, m.name, r.name;
```

## 注意事项

1. **API Key**：测试数据中的 API Key 来自配置文件，请确保配置文件中的 Key 有效
2. **数据库连接**：RAG 配置中的数据库连接信息需要与实际环境匹配
3. **时间戳**：所有时间戳使用 Unix 时间戳（秒）
4. **冲突处理**：所有 INSERT 语句都使用了 `ON CONFLICT DO NOTHING`，可以安全地重复执行

## 测试场景

使用完整测试数据可以测试以下场景：

1. **多 Agent 场景**：测试不同 Agent 的构建
2. **多 Client 场景**：测试不同 Client 的配置
3. **多 Model 场景**：测试不同 Model 的注册和使用
4. **RAG 增强场景**：测试带 RAG 和不带 RAG 的 ChatApi 构建
5. **关联关系场景**：测试复杂的多对多关联关系


