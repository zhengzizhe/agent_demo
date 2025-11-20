# Electron 客户端快速启动

## 第一步：安装依赖

```bash
cd frontend
npm install
```

## 第二步：开发模式

**终端 1 - 启动 Vite 开发服务器：**
```bash
npm run dev
```

**终端 2 - 启动 Electron：**
```bash
npm run electron:dev
```

## 第三步：构建应用

```bash
# 构建当前平台
npm run electron:build

# 或构建特定平台
npm run electron:build:win    # Windows
npm run electron:build:mac    # macOS
npm run electron:build:linux # Linux
```

构建完成后，安装包在 `release/` 目录。

## 重要提示

1. **确保后端服务运行**: 应用需要后端服务在 `http://localhost:8080`
2. **开发模式**: 需要同时运行 `npm run dev` 和 `npm run electron:dev`
3. **生产构建**: 先运行 `npm run build` 构建前端，然后打包 Electron

## 文件说明

- `electron/main.js` - Electron 主进程（窗口管理）
- `electron/preload.js` - 安全通信桥接
- `package.json` - 包含 Electron 配置和构建脚本

详细文档请查看 `ELECTRON-SETUP.md`



