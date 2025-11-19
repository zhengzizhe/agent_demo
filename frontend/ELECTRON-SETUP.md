# Electron 客户端设置指南

## 快速开始

### 1. 安装依赖

```bash
cd frontend
npm install
```

这会安装所有依赖，包括 Electron 和 electron-builder。

### 2. 开发模式

**步骤 1**: 启动 Vite 开发服务器
```bash
npm run dev
```

**步骤 2**: 在另一个终端启动 Electron
```bash
npm run electron:dev
```

现在你应该能看到 Electron 窗口打开，显示你的应用。

## 构建生产版本

### 构建当前平台的应用

```bash
npm run electron:build
```

### 构建特定平台

**Windows:**
```bash
npm run electron:build:win
```

**macOS:**
```bash
npm run electron:build:mac
```

**Linux:**
```bash
npm run electron:build:linux
```

构建完成后，安装包会在 `release` 目录下。

## 项目结构

```
frontend/
├── electron/
│   ├── main.js          # Electron 主进程
│   └── preload.js       # 预加载脚本（安全通信桥接）
├── src/                 # Vue 应用源码
├── dist/                # 构建输出（Electron 使用）
├── release/             # 打包输出目录
└── package.json         # 包含 Electron 配置
```

## 配置说明

### 应用信息

在 `package.json` 中修改：
- `name`: 应用名称
- `version`: 版本号
- `description`: 应用描述
- `build.appId`: 应用唯一标识
- `build.productName`: 显示名称

### 窗口设置

在 `electron/main.js` 中修改 `createWindow()` 函数：
- `width` / `height`: 窗口大小
- `minWidth` / `minHeight`: 最小尺寸
- `titleBarStyle`: 标题栏样式（macOS）

### 应用图标

将图标文件放在 `build/` 目录：
- Windows: `build/icon.ico` (256x256)
- macOS: `build/icon.icns` (512x512)
- Linux: `build/icon.png` (512x512)

如果没有图标，应用会使用默认图标。

## 后端服务配置

应用需要后端服务运行在 `http://localhost:8080`。

### 开发环境
确保后端服务已启动，Electron 会通过代理访问。

### 生产环境
你可能需要修改 API 地址。可以在 `electron/main.js` 中添加配置：

```javascript
// 在 createWindow 之前
const API_BASE_URL = process.env.API_URL || 'http://localhost:8080'
```

然后在渲染进程中通过 `window.electronAPI` 访问。

## 常见问题

### 1. Electron 窗口空白

**原因**: Vite 开发服务器未启动或端口不对。

**解决**: 
- 确保 `npm run dev` 正在运行
- 检查端口是否为 3000
- 查看控制台错误信息

### 2. 构建失败

**原因**: 缺少依赖或路径错误。

**解决**:
```bash
# 清理并重新安装
rm -rf node_modules package-lock.json
npm install

# 确保先构建前端
npm run build
npm run electron:build
```

### 3. 应用无法连接后端

**原因**: CORS 或网络配置问题。

**解决**:
- 检查后端服务是否运行
- 在 `electron/main.js` 中调整 `webSecurity` 设置（仅开发环境）
- 检查代理配置

### 4. macOS 安全警告

**原因**: 未签名的应用。

**解决**: 
- 开发环境：在系统设置中允许运行
- 生产环境：需要代码签名（需要 Apple Developer 账号）

## 高级功能

### 自动更新

可以集成 `electron-updater` 实现自动更新功能。

### 系统托盘

可以在 `electron/main.js` 中添加系统托盘图标。

### 原生菜单

可以自定义应用菜单栏。

## 调试技巧

1. **渲染进程调试**: 开发模式下自动打开 DevTools
2. **主进程调试**: 使用 VS Code 的调试配置
3. **日志**: 使用 `console.log` 在终端查看主进程日志

## 下一步

- [ ] 添加应用图标
- [ ] 配置自动更新
- [ ] 添加系统托盘
- [ ] 自定义应用菜单
- [ ] 配置代码签名（生产环境）

