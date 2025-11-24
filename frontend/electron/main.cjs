const { app, BrowserWindow, ipcMain } = require('electron')
const path = require('path')
const fs = require('fs')

const isDev = process.env.NODE_ENV === 'development'

let mainWindow

function createWindow() {
  const iconPath = path.join(__dirname, '../build/icon.png')
  const hasIcon = fs.existsSync(iconPath)
  const isMac = process.platform === 'darwin'

  mainWindow = new BrowserWindow({
    width: 1400,
    height: 900,
    minWidth: 800,
    minHeight: 600,
    frame: false, // 无边框窗口
    transparent: false,
    backgroundColor: '#ffffff',
    titleBarStyle: isMac ? 'hiddenInset' : 'default',
    titleBarOverlay: isMac ? {
      color: '#ffffff',
      symbolColor: '#333333',
      height: 40
    } : false,
    webPreferences: {
      preload: path.join(__dirname, 'preload.cjs'),
      nodeIntegration: false,
      contextIsolation: true,
      enableRemoteModule: false,
      webSecurity: true,
      // 性能优化：启用硬件加速
      enableBlinkFeatures: '',
      disableBlinkFeatures: 'Auxclick',
      // 性能优化：启用 WebGL 和 WebGPU
      enableWebGPU: true,
      // 性能优化：启用 offscreen rendering
      offscreen: false,
      // 性能优化：启用背景节流
      backgroundThrottling: true,
      // 性能优化：启用 v8 缓存
      v8CacheOptions: 'code',
      // 性能优化：启用图片动画
      imageAnimationPolicy: 'animate',
      // 性能优化：启用平滑滚动
      enablePreferredSizeMode: false,
      // 性能优化：禁用 spellcheck（如果不需要）
      spellcheck: false
    },
    ...(hasIcon && { icon: iconPath }),
    show: false,
    // 窗口阴影和视觉效果
    hasShadow: true,
    roundedCorners: true,
    // macOS 特定设置
    // 性能优化：移除 vibrancy 效果（会显著影响性能）
    // ...(isMac && {
    //   vibrancy: 'under-window',
    //   visualEffectState: 'active'
    // })
  })

  // 加载应用
  if (isDev) {
    // 开发模式：加载Vite开发服务器
    mainWindow.loadURL('http://localhost:3000')
    // 打开开发者工具
    mainWindow.webContents.openDevTools()
  } else {
    // 生产模式：加载构建后的文件
    const distPath = path.join(__dirname, '../dist/index.html')
    mainWindow.loadFile(distPath).catch(err => {
      console.error('Failed to load file:', err)
      // 如果加载失败，尝试加载开发服务器（用于调试）
      mainWindow.loadURL('http://localhost:3000')
    })
  }

  // 性能优化：设置帧率限制为90fps（高刷新率）
  mainWindow.webContents.setFrameRate(90)
  
  // 性能优化：监听页面加载完成
  mainWindow.webContents.on('dom-ready', () => {
    // 可以在这里添加其他性能优化逻辑
    console.log('页面加载完成，性能优化已启用')
  })

  // 窗口准备好后显示
  mainWindow.once('ready-to-show', () => {
    mainWindow.show()
    
    // 开发模式下聚焦窗口
    if (isDev) {
      mainWindow.focus()
    }
  })

  mainWindow.on('closed', () => {
    mainWindow = null
  })

  // 监听窗口状态变化
  mainWindow.on('maximize', () => {
    mainWindow.webContents.send('window-state-changed', true)
  })

  mainWindow.on('unmaximize', () => {
    mainWindow.webContents.send('window-state-changed', false)
  })

  // 处理窗口错误
  mainWindow.webContents.on('did-fail-load', (event, errorCode, errorDescription) => {
    console.error('Failed to load:', errorCode, errorDescription)
  })
}

// 性能优化：启用硬件加速（适用于所有平台）
app.commandLine.appendSwitch('enable-gpu-rasterization')
// 性能优化：启用 GPU 合成
app.commandLine.appendSwitch('enable-gpu-compositing')
// 性能优化：禁用后台节流（提升前台性能）
app.commandLine.appendSwitch('disable-background-timer-throttling')
app.commandLine.appendSwitch('disable-backgrounding-occluded-windows')
app.commandLine.appendSwitch('disable-renderer-backgrounding')
// 性能优化：启用零拷贝（如果支持）
if (process.platform === 'darwin' || process.platform === 'linux') {
  app.commandLine.appendSwitch('enable-zero-copy')
}

// 应用准备就绪
app.whenReady().then(() => {
  createWindow()

  app.on('activate', () => {
    if (BrowserWindow.getAllWindows().length === 0) {
      createWindow()
    }
  })
})

// 所有窗口关闭时退出（macOS除外）
app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit()
  }
})

// IPC通信处理
ipcMain.handle('get-app-version', () => {
  return app.getVersion()
})

// 窗口控制
ipcMain.on('window-minimize', () => {
  if (mainWindow) mainWindow.minimize()
})

ipcMain.on('window-maximize', () => {
  if (mainWindow) {
    if (mainWindow.isMaximized()) {
      mainWindow.unmaximize()
    } else {
      mainWindow.maximize()
    }
  }
})

ipcMain.on('window-close', () => {
  if (mainWindow) mainWindow.close()
})

ipcMain.handle('window-is-maximized', () => {
  return mainWindow ? mainWindow.isMaximized() : false
})

// 处理外部链接
app.on('web-contents-created', (event, contents) => {
  contents.on('new-window', (event, navigationUrl) => {
    event.preventDefault()
    // 可以在这里打开外部浏览器
    require('electron').shell.openExternal(navigationUrl)
  })
})

