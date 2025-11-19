const { contextBridge, ipcRenderer } = require('electron')

// 暴露受保护的方法给渲染进程
contextBridge.exposeInMainWorld('electronAPI', {
  // 获取应用版本
  getVersion: () => ipcRenderer.invoke('get-app-version'),
  
  // 平台信息
  platform: process.platform,
  
  // 窗口控制
  minimize: () => ipcRenderer.send('window-minimize'),
  maximize: () => ipcRenderer.send('window-maximize'),
  close: () => ipcRenderer.send('window-close'),
  isMaximized: () => ipcRenderer.invoke('window-is-maximized'),
  
  // 监听窗口状态变化
  onWindowStateChange: (callback) => {
    const handler = (event, isMaximized) => {
      callback(isMaximized)
    }
    ipcRenderer.on('window-state-changed', handler)
    // 返回清理函数
    return () => {
      ipcRenderer.removeListener('window-state-changed', handler)
    }
  }
})

