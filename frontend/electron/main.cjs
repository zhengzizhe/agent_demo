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
      webSecurity: true
    },
    ...(hasIcon && { icon: iconPath }),
    show: false,
    // 窗口阴影和视觉效果
    hasShadow: true,
    roundedCorners: true,
    // macOS 特定设置
    ...(isMac && {
      vibrancy: 'under-window',
      visualEffectState: 'active'
    })
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

