# 文档库模块 (DocumentLibrary)

基于Vue 3开发的完整文档管理系统，参考了Angular版本的docs模块设计。

## 功能特性

### 📋 核心功能
- ✅ **文档列表管理** - 网格视图和列表视图切换
- ✅ **文档创建** - 支持空白文档和Markdown文档
- ✅ **文档搜索** - 实时搜索文档名称和描述
- ✅ **文档排序** - 按时间、名称、大小排序
- ✅ **文档操作** - 打开、重命名、复制、删除、分享、收藏
- ✅ **空间管理** - 多空间支持，个人空间和工作空间
- ✅ **视图切换** - 文档库、共享给我、收藏三个视图
- ✅ **右键菜单** - 完整的上下文菜单操作

### 🎨 UI特性
- ✅ **响应式设计** - 适配不同屏幕尺寸
- ✅ **现代化UI** - 简洁美观的界面设计
- ✅ **动画效果** - 流畅的交互动画
- ✅ **加载状态** - 友好的加载和空状态提示
- ✅ **网格/列表视图** - 两种视图模式自由切换

## 组件结构

```
DocumentLibrary.vue
├── 左侧边栏 (dl-sidebar)
│   ├── 用户信息区域
│   └── 导航菜单
│       ├── 文档库
│       ├── 共享给我
│       ├── 空间列表
│       └── 收藏
├── 主内容区 (dl-main)
│   ├── 顶部标题栏 (dl-header)
│   │   ├── 标题和统计
│   │   └── 操作栏（搜索、排序、视图切换、新建）
│   └── 文档列表 (dl-content)
│       ├── 网格视图 (dl-grid-view)
│       └── 列表视图 (dl-list-view)
└── 对话框
    ├── 新建文档对话框
    ├── 重命名对话框
    └── 右键菜单
```

## 使用方法

### 基本使用

```vue
<template>
  <DocumentLibrary />
</template>

<script setup>
import DocumentLibrary from './components/DocumentLibrary.vue'
</script>
```

### 集成到App.vue

文档库已经集成到主应用中，通过导航栏的"文档库"标签页访问。

## API接口说明

组件内部使用以下API接口（需要后端实现）：

### 空间相关
- `POST /api/spaces` - 获取空间列表
- `POST /api/spaces/{id}/documents` - 获取空间下的文档列表

### 文档相关
- `POST /api/documents` - 创建文档
- `GET /api/documents/{id}` - 获取文档详情
- `PATCH /api/documents/{id}` - 更新文档
- `DELETE /api/documents/{id}` - 删除文档
- `POST /api/documents/{id}/duplicate` - 复制文档
- `POST /api/documents/{id}/favorite` - 收藏文档
- `DELETE /api/documents/{id}/favorite` - 取消收藏
- `POST /api/documents/search` - 搜索文档

### 视图相关
- `POST /api/documents/shared` - 获取共享给我的文档
- `POST /api/documents/favorites` - 获取收藏的文档

## 数据结构

### 文档对象 (Document)
```javascript
{
  id: string,              // 文档ID
  name: string,            // 文档名称
  description?: string,    // 文档描述
  type: 'text' | 'markdown', // 文档类型
  createdAt: number,       // 创建时间戳
  updatedAt: number,       // 更新时间戳
  size?: number,          // 文档大小（字节）
  favorite: boolean,      // 是否收藏
  shared: boolean,        // 是否共享
  owner: {                // 所有者信息
    id: string,
    name: string
  },
  spaceId?: string        // 所属空间ID
}
```

### 空间对象 (Space)
```javascript
{
  id: string,             // 空间ID
  name: string,           // 空间名称
  type: 'personal' | 'work' | 'enterprise' | 'custom', // 空间类型
  description?: string    // 空间描述
}
```

## Composable使用

组件内部使用了 `useDocumentLibrary` composable来管理状态和API调用：

```javascript
import { useDocumentLibrary } from '../composables/useDocumentLibrary.js'

const {
  documents,        // 文档列表
  spaces,           // 空间列表
  currentSpace,     // 当前空间
  loading,          // 加载状态
  error,            // 错误信息
  loadSpaces,       // 加载空间列表
  loadDocuments,    // 加载文档列表
  createDocument,   // 创建文档
  updateDocument,   // 更新文档
  deleteDocument,   // 删除文档
  duplicateDocument,// 复制文档
  toggleFavorite,  // 切换收藏状态
  searchDocuments, // 搜索文档
  setCurrentSpace   // 设置当前空间
} = useDocumentLibrary()
```

## 样式定制

组件使用scoped样式，可以通过以下CSS变量进行定制：

```css
/* 主色调 */
--primary-color: #2563eb;
--primary-hover: #1d4ed8;

/* 文本颜色 */
--text-primary: #111827;
--text-secondary: #6b7280;
--text-tertiary: #9ca3af;

/* 背景颜色 */
--bg-primary: #ffffff;
--bg-secondary: #f5f5f7;
--bg-hover: #f9fafb;

/* 边框颜色 */
--border-color: #e5e7eb;
--border-hover: #d1d5db;
```

## 事件说明

组件可以emit以下事件（当前版本未实现，可扩展）：

- `@open-document` - 打开文档时触发
- `@create-document` - 创建文档时触发
- `@delete-document` - 删除文档时触发
- `@share-document` - 分享文档时触发

## 待实现功能

以下功能在组件中预留了接口，需要后端API支持：

1. **文档编辑器集成** - 打开文档时需要集成文档编辑器
2. **分享功能** - 分享对话框和权限管理
3. **文件夹管理** - 文件夹的创建、移动、删除
4. **批量操作** - 批量删除、批量移动等
5. **文档预览** - 文档预览功能
6. **版本管理** - 文档版本历史
7. **标签管理** - 文档标签功能
8. **协作功能** - 多人协作编辑

## 开发说明

### 本地开发

组件使用模拟数据，可以直接运行查看UI效果。要连接真实API，需要：

1. 修改 `useDocumentLibrary.js` 中的API端点
2. 确保后端API已实现相应的接口
3. 处理API响应格式，确保与组件期望的数据结构一致

### 扩展开发

1. **添加新视图** - 在 `currentView` 中添加新的视图类型
2. **添加新操作** - 在右键菜单和操作按钮中添加新的操作项
3. **自定义样式** - 修改scoped样式或添加全局样式覆盖

## 参考

- 参考了Angular版本的 `docs` 模块设计
- UI设计参考了现代文档管理系统的交互模式
- 使用Vue 3 Composition API实现响应式逻辑

