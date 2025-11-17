# Agent Demo Frontend

Vue 3 + Vite 前端项目

## 安装依赖

```bash
npm install
```

## 开发

```bash
npm run dev
```

开发服务器会在 `http://localhost:3000` 启动，并自动代理后端 API 请求到 `http://localhost:8080`

## 构建

```bash
npm run build
```

构建产物会输出到 `../src/main/resources/static` 目录，可以直接被后端服务。

## 预览构建结果

```bash
npm run preview
```

## 项目结构

```
frontend/
├── src/
│   ├── components/      # Vue 组件
│   │   ├── TaskCard.vue
│   │   └── SummarySection.vue
│   ├── App.vue         # 主应用组件
│   ├── main.js          # 入口文件
│   └── style.css        # 全局样式
├── index.html           # HTML 模板
├── vite.config.js       # Vite 配置
└── package.json         # 项目配置
```

## 功能特性

- ✅ Vue 3 Composition API
- ✅ 组件化设计
- ✅ 实时任务状态更新
- ✅ SSE 流式数据接收
- ✅ 响应式 UI 设计
- ✅ 嵌套任务支持

