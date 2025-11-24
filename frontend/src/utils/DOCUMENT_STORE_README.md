# 文档库数据结构设计文档

## 概述

这是一个支持**协同编辑**和**分页分段存储**的文档库系统。文档被分解为多个页面（Page），每个页面包含多个分段（Segment），支持多人实时协同编辑。

## 核心设计理念

### 1. 分页分段存储
- **文档（Document）**：顶层容器，包含多个页面
- **页面（Page）**：文档的分页单位，每个页面有独立的标题和分段列表
- **分段（Segment）**：文档的最小存储单元，可以是段落、标题、列表、代码块等

### 2. 协同编辑支持
- **操作（Operation）**：记录每次编辑操作，支持插入、删除、更新、移动等
- **向量时钟（Vector Clock）**：用于检测和解决冲突
- **协作者（Collaborator）**：管理多用户协同编辑状态

### 3. 版本控制
- **快照（Snapshot）**：保存文档的历史版本
- **版本号（Version）**：每个文档和分段都有版本号，用于冲突检测

## 数据类型定义

### Document（文档）

```javascript
{
  id: string,                    // 文档ID
  name: string,                  // 文档名称
  type: 'text' | 'markdown' | 'rich_text',  // 文档类型
  description: string,          // 文档描述
  coverImage: string | null,    // 封面图片
  owner: string,                // 所有者ID
  spaceId: string | null,       // 所属空间ID
  
  // 分页分段数据
  pages: DocumentPage[],        // 页面列表
  segments: DocumentSegment[],  // 分段列表（所有页面的分段）
  
  // 协同编辑数据
  collaborators: Collaborator[], // 协作者列表
  collaborationStatus: 'idle' | 'editing' | 'conflict' | 'syncing',
  pendingOperations: CollaborationOperation[],  // 待处理操作
  vectorClock: { [userId: string]: number },    // 向量时钟
  
  // 版本控制
  version: number,              // 文档版本号
  lastModified: number,         // 最后修改时间
  lastModifiedBy: string,       // 最后修改者ID
  
  // 元数据
  favorite: boolean,            // 是否收藏
  shared: boolean,              // 是否共享
  tags: string[],               // 标签列表
  metadata: object,             // 扩展元数据
  
  // 时间戳
  createdAt: number,
  updatedAt: number,
  deleted: boolean,
  deletedAt: number | null
}
```

### DocumentPage（文档页面）

```javascript
{
  id: string,                   // 页面ID
  documentId: string,           // 所属文档ID
  pageNumber: number,           // 页码（从1开始）
  title: string,                // 页面标题
  segmentIds: string[],         // 分段ID列表（按顺序）
  metadata: object,            // 页面元数据
  createdAt: number,
  updatedAt: number,
  createdBy: string,
  updatedBy: string
}
```

### DocumentSegment（文档分段）

```javascript
{
  id: string,                   // 分段ID（全局唯一）
  documentId: string,           // 所属文档ID
  pageId: string,              // 所属页面ID
  index: number,                // 在页面中的索引位置
  type: 'paragraph' | 'heading' | 'list' | 'code_block' | 'table' | 'image' | 'quote' | 'divider' | 'custom',
  content: string,              // 分段内容（HTML或Markdown）
  metadata: object,             // 元数据（格式、样式等）
  version: number,              // 版本号（用于冲突解决）
  createdAt: number,
  updatedAt: number,
  createdBy: string,
  updatedBy: string,
  deleted: boolean,
  deletedAt: number | null
}
```

### CollaborationOperation（协同编辑操作）

```javascript
{
  id: string,                   // 操作ID
  documentId: string,           // 文档ID
  userId: string,               // 操作用户ID
  type: 'insert' | 'delete' | 'update' | 'format' | 'move' | 'split' | 'merge',
  targetId: string,             // 目标分段ID或页面ID
  targetType: 'segment' | 'page',
  position: number,             // 操作位置（索引或偏移）
  content: string,              // 操作内容
  metadata: object,             // 操作元数据
  status: 'pending' | 'applied' | 'conflict' | 'rejected',
  timestamp: number,            // 操作时间戳
  vectorClock: { [userId: string]: number },  // 向量时钟
  parentOperationId: string | null,  // 父操作ID（用于操作链）
  conflictResolution: object | null   // 冲突解决策略
}
```

### Collaborator（协作者）

```javascript
{
  userId: string,               // 用户ID
  name: string,                 // 用户名
  avatar: string,               // 头像
  color: string,                // 用户颜色（用于光标显示）
  cursorPosition: {            // 光标位置
    pageId: string,
    segmentId: string,
    offset: number
  } | null,
  selectionRange: {             // 选择范围
    start: { pageId, segmentId, offset },
    end: { pageId, segmentId, offset }
  } | null,
  isActive: boolean,            // 是否在线
  lastActiveAt: number,        // 最后活跃时间
  permissions: string[]        // 权限：['read', 'write']
}
```

### DocumentSnapshot（文档快照）

```javascript
{
  id: string,                   // 快照ID
  documentId: string,           // 文档ID
  version: number,              // 版本号
  snapshot: object,             // 文档快照数据（Document的JSON）
  description: string,          // 快照描述
  createdBy: string,            // 创建者ID
  createdAt: number             // 创建时间
}
```

## 使用示例

### 1. 基本使用

```javascript
import { useDocumentStore } from '@/composables/useDocumentStore.js'
import { getDocumentStore } from '@/utils/DocumentStore.js'

// 设置当前用户
const store = getDocumentStore()
store.setCurrentUserId('user123')

// 使用 composable
const {
  currentDocument,
  currentPage,
  documents,
  createDocument,
  loadDocument,
  createSegment,
  updateSegment
} = useDocumentStore()

// 创建文档
const doc = createDocument('我的文档', 'rich_text')

// 加载文档
loadDocument(doc.id)

// 创建新页面
const page = createPage('第一章')

// 创建分段
const segment = createSegment('paragraph', '<p>这是第一段内容</p>')
```

### 2. 分页分段操作

```javascript
// 切换页面
switchPage(pageId)

// 获取当前页面的分段
const segments = getCurrentPageSegments.value

// 更新分段内容
updateSegment(segmentId, {
  content: '<p>更新后的内容</p>'
})

// 移动分段到其他页面
moveSegment(segmentId, targetPageId, targetIndex)

// 删除分段
deleteSegment(segmentId)
```

### 3. 协同编辑操作

```javascript
// 插入文本
insertText(segmentId, 10, '插入的文本')

// 删除文本
deleteText(segmentId, 5, 3)  // 从位置5删除3个字符

// 添加协作者
addCollaborator({
  userId: 'user456',
  name: '张三',
  avatar: 'https://...',
  color: '#165dff',
  permissions: ['read', 'write']
})

// 更新协作者光标位置
updateCollaboratorCursor('user456', {
  pageId: 'page123',
  segmentId: 'seg456',
  offset: 10
})
```

### 4. 版本管理

```javascript
// 创建快照
const snapshot = createSnapshot('保存点1')

// 获取所有快照
const snapshots = getSnapshots.value

// 恢复快照
restoreSnapshot(snapshotId)
```

### 5. 事件监听

```javascript
import { getDocumentStore } from '@/utils/DocumentStore.js'

const store = getDocumentStore()

// 监听文档更新
store.on('documentUpdated', (data) => {
  console.log('文档已更新:', data.document)
})

// 监听分段更新
store.on('segmentUpdated', (data) => {
  console.log('分段已更新:', data.segment)
})

// 监听冲突
store.on('conflictDetected', (data) => {
  console.warn('检测到冲突:', data.conflict)
  // 显示冲突解决UI
})

// 监听协作者加入
store.on('collaboratorJoined', (data) => {
  console.log('新协作者加入:', data.collaborator)
})
```

## 数据结构关系图

```
Document (文档)
├── pages[] (页面列表)
│   ├── Page 1
│   │   └── segmentIds[] → segments[]
│   ├── Page 2
│   │   └── segmentIds[] → segments[]
│   └── ...
├── segments[] (所有分段)
│   ├── Segment 1 (pageId → Page 1)
│   ├── Segment 2 (pageId → Page 1)
│   ├── Segment 3 (pageId → Page 2)
│   └── ...
├── collaborators[] (协作者列表)
├── pendingOperations[] (待处理操作)
└── vectorClock{} (向量时钟)
```

## 冲突检测机制

### 1. 向量时钟冲突检测

每个操作都携带向量时钟，用于检测操作的先后顺序：

```javascript
// 操作A的向量时钟
{ user1: 5, user2: 3 }

// 操作B的向量时钟
{ user1: 4, user2: 4 }

// 如果操作B的时钟值小于等于本地时钟，可能存在冲突
```

### 2. 版本冲突检测

分段有版本号，操作基于特定版本：

```javascript
// 分段当前版本：10
// 操作基于版本：8
// → 冲突！分段已被其他操作修改
```

### 3. 冲突解决策略

- **自动合并**：如果操作不重叠，自动合并
- **用户选择**：如果操作重叠，提示用户选择保留哪个版本
- **最后写入获胜**：使用时间戳决定

## 存储策略

### 本地存储（LocalStorage）

- 所有文档数据存储在 `localStorage` 中
- 键名：`document_store`
- 格式：JSON字符串

### 数据结构

```javascript
{
  documents: Document[],      // 所有文档
  operations: CollaborationOperation[],  // 所有操作
  snapshots: DocumentSnapshot[]  // 所有快照
}
```

### 同步机制

- 定期将待处理操作同步到后端
- 从后端拉取其他用户的操作
- 应用远程操作并检测冲突

## 性能优化

### 1. 懒加载
- 只加载当前页面的分段
- 其他页面按需加载

### 2. 增量更新
- 只更新变化的分段
- 使用版本号避免重复更新

### 3. 操作批处理
- 将多个操作合并为一个批次
- 减少同步频率

## 扩展性

### 1. 自定义分段类型
```javascript
const customSegment = new DocumentSegment({
  type: SegmentType.CUSTOM,
  metadata: {
    customType: 'chart',
    config: { ... }
  }
})
```

### 2. 自定义操作类型
```javascript
const customOperation = new CollaborationOperation({
  type: 'custom_action',
  metadata: {
    action: 'add_comment',
    data: { ... }
  }
})
```

## 注意事项

1. **分段ID全局唯一**：确保分段ID在整个文档库中唯一
2. **版本号递增**：每次更新都要增加版本号
3. **软删除**：删除操作使用软删除，保留数据用于恢复
4. **向量时钟同步**：确保向量时钟正确同步
5. **冲突处理**：及时处理冲突，避免数据不一致

## 后续优化方向

1. **后端同步**：实现与后端的实时同步
2. **WebSocket支持**：使用WebSocket实现实时协同
3. **离线支持**：支持离线编辑和同步
4. **压缩存储**：对大量数据进行压缩存储
5. **索引优化**：为快速检索添加索引



