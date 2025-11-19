# Agent Demo Frontend (Vue.js)

这是使用Vue.js开发的前端项目。

## 项目结构

```
frontend/
├── src/
│   ├── App.vue                    # 主组件
│   ├── components/                # Vue组件
│   │   ├── InputArea.vue
│   │   ├── MessageItem.vue
│   │   ├── TaskList.vue
│   │   └── RagManagement.vue
│   ├── composables/               # Vue Composables
│   │   ├── useSession.js
│   │   ├── useMessages.js
│   │   ├── useEventHandlers.js
│   │   ├── useTaskExecution.js
│   │   └── usePdfExport.js
│   ├── utils/                     # 工具函数
│   │   ├── markdown.js
│   │   ├── message.js
│   │   ├── task.js
│   │   └── url.js
│   ├── main.js                    # 入口文件
│   └── style.css                  # 全局样式
├── index.html                     # HTML模板
├── vite.config.js                 # Vite配置
└── package.json                   # 依赖配置
```

## 安装依赖

```bash
cd frontend
npm install
```

## 运行开发服务器

```bash
npm run dev
```

应用将在 http://localhost:3000 运行

## 构建生产版本

```bash
npm run build
```

构建输出将位于 `../src/main/resources/static`

## 主要特性

- ✅ Vue 3 Composition API
- ✅ 响应式设计
- ✅ 支持Markdown渲染和代码高亮
- ✅ 支持PDF导出
- ✅ 任务列表可视化（列表视图和图状视图）
- ✅ RAG知识库管理

## 技术栈

- Vue 3
- Vite
- highlight.js (代码高亮)
- markdown-it (Markdown渲染)
- vis-network (任务图可视化)
- html2pdf.js (PDF导出)
