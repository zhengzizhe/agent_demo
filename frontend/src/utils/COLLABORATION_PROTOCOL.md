# 协同编辑协议说明

## 场景：用户输入一个字母 "A"

当用户在文档编辑器中输入一个字母 "A" 时，前端会向后端发送以下信息：

## 发送的数据结构

### 1. 基础操作信息

```javascript
{
  // 操作唯一标识
  id: "op_1703123456789_abc123def",
  
  // 文档标识
  documentId: "doc_1703123456789_xyz789",
  
  // 操作用户
  userId: "user_12345",
  
  // 操作类型：插入
  type: "insert",
  
  // 操作时间戳（毫秒）
  timestamp: 1703123456789
}
```

### 2. 目标位置信息

```javascript
{
  // 目标类型：分段（segment）或页面（page）
  targetType: "segment",
  
  // 目标分段ID
  targetId: "seg_1703123456789_para001",
  
  // 插入位置：在分段内容中的字符偏移量
  position: 15,  // 在第15个字符位置插入
  
  // 插入的内容
  content: "A"    // 单个字母 "A"
}
```

### 3. 版本和冲突检测信息

```javascript
{
  // 向量时钟（用于检测操作顺序和冲突）
  vectorClock: {
    "user_12345": 42,    // 当前用户的操作序号
    "user_67890": 38     // 其他用户的操作序号（已知的）
  },
  
  // 分段版本号（用于检测分段是否被修改）
  metadata: {
    segmentVersion: 5,           // 操作基于的分段版本
    documentVersion: 120,       // 操作基于的文档版本
    pageId: "page_001",          // 所属页面ID
    pageNumber: 1                // 页码
  }
}
```

### 4. 操作链信息（可选）

```javascript
{
  // 父操作ID（如果这是某个操作的子操作）
  parentOperationId: null,
  
  // 操作状态
  status: "pending"  // pending | applied | conflict | rejected
}
```

## 完整示例

### 场景描述
- 用户：张三（userId: "user_12345"）
- 文档：ID 为 "doc_001"
- 分段：ID 为 "seg_001"，当前内容为 "Hello World"，版本为 5
- 操作：在位置 5（"Hello" 后面）插入字母 "A"

### 发送的完整数据

```javascript
{
  // ========== 操作标识 ==========
  id: "op_1703123456789_abc123def",
  documentId: "doc_001",
  userId: "user_12345",
  type: "insert",
  timestamp: 1703123456789,
  
  // ========== 目标位置 ==========
  targetType: "segment",
  targetId: "seg_001",
  position: 5,              // 在 "Hello" 后面插入
  content: "A",             // 插入的内容是字母 "A"
  
  // ========== 版本信息 ==========
  vectorClock: {
    "user_12345": 42,       // 这是用户12345的第42个操作
    "user_67890": 38        // 已知用户67890的最新操作是第38个
  },
  
  metadata: {
    segmentVersion: 5,      // 分段当前版本是5
    documentVersion: 120,   // 文档当前版本是120
    pageId: "page_001",
    pageNumber: 1,
    // 可选：操作上下文信息
    beforeContent: "Hello",  // 插入位置前的内容
    afterContent: " World"   // 插入位置后的内容
  },
  
  // ========== 操作链 ==========
  parentOperationId: null,
  status: "pending"
}
```

## 不同操作类型的示例

### 1. 插入单个字符

```javascript
{
  id: "op_xxx",
  documentId: "doc_001",
  userId: "user_12345",
  type: "insert",
  targetType: "segment",
  targetId: "seg_001",
  position: 10,
  content: "A",                    // 单个字符
  vectorClock: { "user_12345": 42 },
  metadata: {
    segmentVersion: 5,
    documentVersion: 120
  },
  timestamp: 1703123456789
}
```

### 2. 删除字符

```javascript
{
  id: "op_xxx",
  documentId: "doc_001",
  userId: "user_12345",
  type: "delete",
  targetType: "segment",
  targetId: "seg_001",
  position: 10,                    // 删除位置
  content: "",                      // 删除的内容（空字符串）
  vectorClock: { "user_12345": 43 },
  metadata: {
    segmentVersion: 6,              // 分段版本已更新
    documentVersion: 121,
    deletedLength: 1,              // 删除的字符数
    deletedContent: "A"             // 被删除的内容（用于撤销）
  },
  timestamp: 1703123456790
}
```

### 3. 插入多个字符（批量操作）

```javascript
{
  id: "op_xxx",
  documentId: "doc_001",
  userId: "user_12345",
  type: "insert",
  targetType: "segment",
  targetId: "seg_001",
  position: 10,
  content: "Hello",                 // 多个字符
  vectorClock: { "user_12345": 44 },
  metadata: {
    segmentVersion: 7,
    documentVersion: 122,
    batchSize: 5                    // 批量插入的字符数
  },
  timestamp: 1703123456791
}
```

### 4. 格式化操作

```javascript
{
  id: "op_xxx",
  documentId: "doc_001",
  userId: "user_12345",
  type: "format",
  targetType: "segment",
  targetId: "seg_001",
  position: 0,                      // 格式化的起始位置
  content: "",                       // 格式化不改变内容
  vectorClock: { "user_12345": 45 },
  metadata: {
    segmentVersion: 8,
    documentVersion: 123,
    formatType: "bold",              // 格式类型
    formatRange: {                   // 格式范围
      start: 0,
      end: 5
    },
    formatValue: true                // 格式值（true=加粗，false=取消加粗）
  },
  timestamp: 1703123456792
}
```

### 5. 移动分段

```javascript
{
  id: "op_xxx",
  documentId: "doc_001",
  userId: "user_12345",
  type: "move",
  targetType: "segment",
  targetId: "seg_001",
  position: 0,                       // 目标位置索引
  content: "",
  vectorClock: { "user_12345": 46 },
  metadata: {
    segmentVersion: 9,
    documentVersion: 124,
    sourcePageId: "page_001",        // 源页面
    sourceIndex: 2,                  // 源位置
    targetPageId: "page_002",        // 目标页面
    targetIndex: 0                   // 目标位置
  },
  timestamp: 1703123456793
}
```

## 操作批处理（优化）

为了提高性能，可以将多个操作合并为一个批次：

```javascript
{
  id: "op_batch_xxx",
  documentId: "doc_001",
  userId: "user_12345",
  type: "batch",                     // 批量操作类型
  targetType: "segment",
  targetId: "seg_001",
  position: 0,
  content: "",
  vectorClock: { "user_12345": 50 },
  metadata: {
    segmentVersion: 10,
    documentVersion: 125,
    operations: [                    // 包含多个操作
      {
        type: "insert",
        position: 10,
        content: "A",
        timestamp: 1703123456789
      },
      {
        type: "insert",
        position: 11,
        content: "B",
        timestamp: 1703123456790
      },
      {
        type: "insert",
        position: 12,
        content: "C",
        timestamp: 1703123456791
      }
    ]
  },
  timestamp: 1703123456795
}
```

## 后端接收后的处理流程

### 1. 验证操作
```javascript
// 检查文档是否存在
if (!documentExists(operation.documentId)) {
  return { error: "文档不存在" }
}

// 检查分段版本是否匹配
const segment = getSegment(operation.targetId)
if (segment.version > operation.metadata.segmentVersion) {
  return { 
    error: "版本冲突",
    conflict: {
      expectedVersion: operation.metadata.segmentVersion,
      actualVersion: segment.version
    }
  }
}
```

### 2. 应用操作
```javascript
// 应用插入操作
if (operation.type === "insert") {
  const segment = getSegment(operation.targetId)
  const newContent = 
    segment.content.substring(0, operation.position) +
    operation.content +
    segment.content.substring(operation.position)
  
  segment.content = newContent
  segment.version++
  segment.updatedAt = Date.now()
  segment.updatedBy = operation.userId
}
```

### 3. 更新向量时钟
```javascript
// 更新文档的向量时钟
document.vectorClock[operation.userId] = 
  Math.max(
    document.vectorClock[operation.userId] || 0,
    operation.vectorClock[operation.userId]
  ) + 1
```

### 4. 广播给其他协作者
```javascript
// 将操作广播给其他在线协作者
broadcastToCollaborators(operation.documentId, {
  type: "operation",
  operation: operation.toJSON()
})
```

## 冲突检测示例

### 场景：两个用户同时编辑

**用户A的操作（先发送）**
```javascript
{
  type: "insert",
  position: 10,
  content: "A",
  vectorClock: { "user_A": 10 },
  metadata: { segmentVersion: 5 }
}
```

**用户B的操作（后发送，但基于旧版本）**
```javascript
{
  type: "insert",
  position: 10,
  content: "B",
  vectorClock: { "user_B": 8 },
  metadata: { segmentVersion: 4 }  // 基于版本4，但分段已经是版本5了
}
```

**后端检测到冲突**
```javascript
{
  status: "conflict",
  operation: { ... },
  conflict: {
    type: "version_conflict",
    message: "分段版本冲突：操作基于版本4，当前版本为5",
    resolution: {
      options: [
        "apply_after_A",    // 在A的操作之后应用
        "reject",           // 拒绝B的操作
        "merge"             // 尝试合并
      ]
    }
  }
}
```

## 实际发送的HTTP请求

### POST /api/documents/{documentId}/operations

```javascript
// Request Headers
{
  "Content-Type": "application/json",
  "Authorization": "Bearer token_xxx"
}

// Request Body
{
  "id": "op_1703123456789_abc123def",
  "documentId": "doc_001",
  "userId": "user_12345",
  "type": "insert",
  "targetType": "segment",
  "targetId": "seg_001",
  "position": 5,
  "content": "A",
  "vectorClock": {
    "user_12345": 42,
    "user_67890": 38
  },
  "metadata": {
    "segmentVersion": 5,
    "documentVersion": 120,
    "pageId": "page_001",
    "pageNumber": 1
  },
  "timestamp": 1703123456789,
  "status": "pending"
}

// Response
{
  "success": true,
  "data": {
    "operationId": "op_1703123456789_abc123def",
    "status": "applied",
    "documentVersion": 121,
    "segmentVersion": 6
  }
}
```

## 优化建议

### 1. 操作合并
- 短时间内（如100ms内）的多个插入操作可以合并
- 减少网络请求次数

### 2. 延迟发送
- 使用防抖（debounce）机制
- 用户停止输入一段时间后再发送

### 3. 本地优先
- 先应用本地操作，立即更新UI
- 后台异步发送到服务器
- 如果发送失败，加入重试队列

### 4. 操作压缩
- 对于连续的相同操作，可以压缩为一个操作
- 例如：连续插入 "ABC" 可以合并为一个操作


