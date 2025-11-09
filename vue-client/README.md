# Agent SSE 流式对话 Vue 客户端

这是一个Vue 3项目，用于测试Agent的SSE流式对话接口。

## 功能特性

- ✅ SSE流式输出（类似ChatGPT的打字效果）
- ✅ 实时显示流式响应
- ✅ 支持停止和清空操作
- ✅ 美观的UI界面
- ✅ 自动滚动到底部

## 安装依赖

```bash
cd vue-client
npm install
```

## 配置后端端口

**重要：** 请先确认你的后端服务运行在哪个端口！

1. 查看后端启动日志，找到类似 `Server started on port: XXXX` 的信息
2. 或者检查 `application.yml` 中的 `micronaut.server.port` 配置
3. 如果没有配置，Micronaut默认端口是 **8080**

修改 `vite.config.js` 中的代理配置：

```javascript
proxy: {
  '/openai': {
    target: 'http://localhost:8080',  // 修改为你的后端实际端口
    changeOrigin: true,
    secure: false
  }
}
```

## 开发运行

```bash
npm run dev
```

访问 http://localhost:3000

## 构建生产版本

```bash
npm run build
```

## 使用说明

1. **确认后端端口**：确保后端服务正在运行，并记住端口号
2. **配置代理**：修改 `vite.config.js` 中的 `target` 为后端实际地址
3. 启动Vue客户端：`npm run dev`
4. 在浏览器中打开 http://localhost:3000
5. 输入Agent ID和消息
6. 点击"开始对话"按钮
7. 观察流式输出效果

## 常见端口

- **Micronaut默认端口**: 8080
- **Spring Boot默认端口**: 8080
- **如果修改过**: 查看 `application.yml` 或启动日志

## 技术栈

- Vue 3
- Vite
- Fetch API (用于SSE)
- 原生CSS（无UI框架依赖）

## 注意事项

- 确保后端SSE接口正常运行
- 如果后端运行在不同端口，**必须**修改 `vite.config.js` 中的代理配置
- 浏览器需要支持Fetch Stream API（现代浏览器都支持）
- 如果遇到CORS问题，确保后端配置了CORS支持

## 故障排查

### 端口不对
- 检查后端实际运行端口
- 修改 `vite.config.js` 中的 `target`
- 重启Vue开发服务器

### 连接失败
- 确认后端服务正在运行
- 检查防火墙设置
- 查看浏览器控制台错误信息
