# Electron 客户端构建指南

本项目支持打包成 Electron 桌面客户端应用。

## 安装依赖

```bash
npm install
```

## 开发模式

### 启动开发服务器
```bash
npm run dev
```

### 启动 Electron 开发模式
在另一个终端运行：
```bash
npm run electron:dev
```

## 构建应用

### 构建所有平台
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

## 构建输出

构建完成后，安装包会在 `release` 目录下：

- **Windows**: `.exe` 安装包和便携版
- **macOS**: `.dmg` 安装包和 `.zip` 压缩包
- **Linux**: `.AppImage` 和 `.deb` 安装包

## 配置说明

### 应用图标

将应用图标放在以下位置：
- Windows: `build/icon.ico` (256x256)
- macOS: `build/icon.icns` (512x512)
- Linux: `build/icon.png` (512x512)

### 修改应用信息

编辑 `package.json` 中的 `build` 配置：
- `appId`: 应用唯一标识
- `productName`: 应用显示名称
- `version`: 应用版本号

## 注意事项

1. **后端服务**: Electron 应用需要后端服务运行。确保后端服务在 `http://localhost:8080` 运行。

2. **生产环境**: 在生产环境中，你可能需要：
   - 配置后端服务的地址（而不是硬编码 localhost）
   - 添加自动更新功能
   - 配置应用签名（macOS/Windows）

3. **跨域问题**: Electron 中可以直接访问本地文件，但 API 请求仍需要处理 CORS。

4. **打包大小**: 首次打包会下载 Electron 二进制文件，可能需要一些时间。

## 开发建议

- 使用 `electron:dev` 进行开发调试
- 使用 Chrome DevTools 调试渲染进程
- 使用 VS Code 调试主进程（需要配置 launch.json）

## 故障排除

### 构建失败
- 确保已安装所有依赖：`npm install`
- 确保已构建前端：`npm run build`
- 检查 Node.js 版本（推荐 18+）

### 应用无法启动
- 检查 `electron/main.js` 中的路径是否正确
- 确保 `dist` 目录存在且包含构建文件
- 查看控制台错误信息



