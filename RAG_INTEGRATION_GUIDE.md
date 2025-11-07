# RAG集成指南 - 使用PostgreSQL pgvector

本指南将教你如何在项目中使用RAG（Retrieval-Augmented Generation）功能，集成PostgreSQL的pgvector扩展进行向量存储和相似度搜索。

## 目录
1. [前置条件](#前置条件)
2. [数据库设置](#数据库设置)
3. [代码结构说明](#代码结构说明)
4. [使用示例](#使用示例)
5. [API说明](#api说明)

## 前置条件

### 1. 安装pgvector扩展

确保你的PostgreSQL数据库已安装pgvector扩展。可以使用以下方式：

**使用Docker（推荐）：**
```bash
docker run --name pgvector-container \
  -e POSTGRES_USER=admin \
  -e POSTGRES_PASSWORD=admin123 \
  -e POSTGRES_DB=agent \
  -p 5432:5432 \
  -d pgvector/pgvector:pg16
```

**在现有PostgreSQL中安装：**
```sql
-- 连接到数据库后执行
CREATE EXTENSION IF NOT EXISTS vector;
```

### 2. 运行数据库迁移

项目已包含数据库迁移文件，会自动创建必要的表结构：
- `V3__create_vector_store_with_pgvector.sql` - 创建pgvector扩展和向量文档表

迁移会在应用启动时自动执行（如果使用Flyway）。

## 代码结构说明

### 核心组件

1. **实体类**
   - `VectorDocumentEntity` - 向量文档实体（domain层）
   - `VectorDocumentPO` - 向量文档持久化对象（infrastructure层）

2. **数据访问层**
   - `IVectorDocumentDao` - 向量文档DAO，使用原生SQL处理pgvector类型
   - `IVectorDocumentRepository` - 向量文档仓储接口
   - `VectorDocumentRepository` - 向量文档仓储实现

3. **服务层**
   - `RagService` - RAG服务，提供文档分块、向量化、存储和检索功能

### 数据库表结构

**vector_document表：**
- `id` - 主键
- `rag_id` - 关联的RAG ID
- `content` - 文档内容
- `embedding` - 向量（pgvector类型，维度1536）
- `metadata` - 元数据（JSONB）
- `chunk_index` - 文档块索引
- `created_at` / `updated_at` - 时间戳

## 使用示例

### 1. 创建RAG配置

首先需要在数据库中创建一个RAG配置：

```sql
INSERT INTO rag (name, vector_store_type, embedding_model, chunk_size, chunk_overlap, status)
VALUES (
    'My RAG',
    'pgvector',
    'text-embedding-ada-002',
    1000,
    200,
    'ACTIVE'
);
```

### 2. 添加文档到RAG知识库

```java
@Inject
private RagService ragService;

// 添加文档
Long ragId = 1L; // RAG配置的ID
String documentText = "这是要添加到知识库的文档内容...";
Map<String, Object> metadata = new HashMap<>();
metadata.put("source", "文档来源");
metadata.put("title", "文档标题");

int chunksAdded = ragService.addDocument(ragId, documentText, metadata);
System.out.println("添加了 " + chunksAdded + " 个文档块");
```

### 3. 搜索相似文档

```java
// 搜索相似文档
String queryText = "查询内容";
int limit = 5; // 返回前5个最相似的结果
double similarityThreshold = 0.7; // 相似度阈值（0-1之间）

List<VectorDocumentEntity> results = ragService.search(
    ragId, 
    queryText, 
    limit, 
    similarityThreshold
);

// 处理搜索结果
for (VectorDocumentEntity doc : results) {
    System.out.println("内容: " + doc.getContent());
    System.out.println("相似度: " + doc.getMetadata());
}
```

### 4. 删除RAG的所有文档

```java
// 删除指定RAG的所有文档
int deletedCount = ragService.deleteAllDocuments(ragId);
System.out.println("删除了 " + deletedCount + " 个文档");
```

## API说明

### RagService

#### `addDocument(Long ragId, String text, Map<String, Object> metadata)`
添加文档到RAG知识库。

**参数：**
- `ragId` - RAG配置ID
- `text` - 文档文本内容
- `metadata` - 文档元数据（可选）

**返回：** 添加的文档块数量

**功能：**
1. 根据RAG配置对文档进行分块
2. 使用OpenAI Embedding模型将每个文档块向量化
3. 批量存储向量文档到PostgreSQL

#### `search(Long ragId, String queryText, int limit, double similarityThreshold)`
搜索相似文档。

**参数：**
- `ragId` - RAG配置ID
- `queryText` - 查询文本
- `limit` - 返回结果数量限制
- `similarityThreshold` - 相似度阈值（0-1之间，值越大要求越相似）

**返回：** 相似文档列表，按相似度降序排列

**功能：**
1. 将查询文本向量化
2. 使用pgvector的余弦相似度搜索
3. 返回相似度超过阈值的结果

#### `deleteAllDocuments(Long ragId)`
删除RAG的所有文档。

**参数：**
- `ragId` - RAG配置ID

**返回：** 删除的文档数量

### 向量相似度搜索说明

pgvector支持三种距离运算符：
- `<->` - 欧氏距离（L2距离）
- `<#>` - 负内积
- `<=>` - 余弦距离

本实现使用余弦距离（`<=>`），相似度计算公式为：`1 - (embedding <=> query_embedding)`

## 配置说明

### application.yml配置

确保`application.yml`中配置了OpenAI API信息：

```yaml
openai:
  api:
    url: https://apis.itedus.cn/v1/
    key: your-api-key
    model: gpt-4o
```

### RAG配置字段说明

- `vector_store_type` - 向量存储类型（如：pgvector）
- `embedding_model` - 嵌入模型名称（如：text-embedding-ada-002）
- `chunk_size` - 文档分块大小（默认：1000）
- `chunk_overlap` - 分块重叠大小（默认：200）

## 注意事项

1. **向量维度**：当前配置使用OpenAI的text-embedding-ada-002模型，向量维度为1536。如果使用其他模型，需要修改数据库迁移文件中的向量维度。

2. **索引优化**：当向量文档数量较大时（>1000条），建议创建ivfflat索引以提高搜索性能。索引已在迁移文件中创建，但需要先有一些数据。

3. **相似度阈值**：建议根据实际需求调整相似度阈值。值越大，返回的结果越相似，但可能返回的结果越少。

4. **批量插入**：使用`batchInsert`方法可以显著提高大量文档的插入性能。

5. **元数据处理**：metadata字段使用JSONB类型存储，可以灵活存储各种元数据信息。

## 故障排查

### 问题：pgvector扩展未安装
**解决：** 确保PostgreSQL已安装pgvector扩展，执行`CREATE EXTENSION vector;`

### 问题：向量维度不匹配
**解决：** 检查embedding_model配置，确保数据库表定义的向量维度与模型输出维度一致。

### 问题：搜索速度慢
**解决：** 
1. 确保已创建ivfflat索引
2. 调整索引的lists参数
3. 考虑使用HNSW索引（pgvector 0.5.0+支持）

## 扩展阅读

- [pgvector官方文档](https://github.com/pgvector/pgvector)
- [langchain4j文档](https://github.com/langchain4j/langchain4j)
- [OpenAI Embeddings API](https://platform.openai.com/docs/guides/embeddings)




