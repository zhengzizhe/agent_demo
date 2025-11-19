<template>
  <div class="title-bar" :class="{ 'is-maximized': isMaximized }">
    <div class="title-bar-drag-region">
      <div class="title-bar-content">
        <div class="title-bar-left">
          <div class="app-icon" v-if="showIcon">
            <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
              <rect x="2" y="2" width="16" height="16" rx="2" fill="#2563eb"/>
              <path d="M6 10l3 3 5-6" stroke="white" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
          <span class="app-title">{{ appTitle }}</span>
        </div>
        <div class="title-bar-right">
          <button 
            class="title-bar-button minimize-btn" 
            @click="handleMinimize"
            title="最小化"
          >
            <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
              <path d="M2 6h8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </button>
          <button 
            class="title-bar-button maximize-btn" 
            @click="handleMaximize"
            :title="isMaximized ? '还原' : '最大化'"
          >
            <svg v-if="!isMaximized" width="12" height="12" viewBox="0 0 12 12" fill="none">
              <rect x="2" y="2" width="8" height="8" rx="1" stroke="currentColor" stroke-width="1.5" fill="none"/>
            </svg>
            <svg v-else width="12" height="12" viewBox="0 0 12 12" fill="none">
              <path d="M2 4h6v6H2V4z" stroke="currentColor" stroke-width="1.5" fill="none"/>
              <path d="M4 2h6v6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
          <button 
            class="title-bar-button close-btn" 
            @click="handleClose"
            title="关闭"
          >
            <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
              <path d="M3 3l6 6M9 3l-6 6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  appTitle: {
    type: String,
    default: 'Agent 任务执行面板'
  },
  showIcon: {
    type: Boolean,
    default: true
  }
})

const isMaximized = ref(false)

const checkMaximized = async () => {
  if (window.electronAPI) {
    try {
      isMaximized.value = await window.electronAPI.isMaximized()
    } catch (error) {
      console.error('Failed to check window state:', error)
    }
  }
}

const handleMinimize = () => {
  if (window.electronAPI) {
    window.electronAPI.minimize()
  }
}

const handleMaximize = () => {
  if (window.electronAPI) {
    window.electronAPI.maximize()
    // 延迟检查状态，因为最大化是异步的
    setTimeout(checkMaximized, 100)
  }
}

const handleClose = () => {
  if (window.electronAPI) {
    window.electronAPI.close()
  }
}

onMounted(() => {
  checkMaximized()
  
  let cleanupWindowStateListener = null
  
  // 监听窗口状态变化
  if (window.electronAPI && window.electronAPI.onWindowStateChange) {
    cleanupWindowStateListener = window.electronAPI.onWindowStateChange((maximized) => {
      isMaximized.value = maximized
    })
  }
  
  // 监听窗口大小变化（备用方案）
  const handleResize = () => {
    setTimeout(checkMaximized, 100)
  }
  window.addEventListener('resize', handleResize)
  
  onUnmounted(() => {
    window.removeEventListener('resize', handleResize)
    if (cleanupWindowStateListener) {
      cleanupWindowStateListener()
    }
  })
})
</script>

<style scoped>
.title-bar {
  height: 40px;
  background: #ffffff;
  display: flex;
  align-items: center;
  -webkit-app-region: drag;
  user-select: none;
  position: relative;
  z-index: 1000;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.04);
}

.title-bar-drag-region {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
}

.title-bar-content {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 8px;
}

.title-bar-left {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.app-icon {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.app-title {
  font-size: 13px;
  font-weight: 500;
  color: #111827;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.title-bar-right {
  display: flex;
  align-items: center;
  gap: 0;
  -webkit-app-region: no-drag;
}

.title-bar-button {
  width: 46px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  border-radius: 4px;
  margin-left: 2px;
  position: relative;
}

.title-bar-button::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 4px;
  background: currentColor;
  opacity: 0;
  transform: scale(0.8);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.title-bar-button:hover {
  background: #f3f4f6;
  color: #111827;
  transform: scale(1.05);
}

.title-bar-button:active {
  transform: scale(0.95);
}

.title-bar-button.close-btn:hover {
  background: #ef4444;
  color: white;
  transform: scale(1.1);
}

.title-bar-button.close-btn:active {
  transform: scale(0.9);
}

.title-bar-button svg {
  width: 12px;
  height: 12px;
}

/* macOS 样式调整 */
@media (prefers-color-scheme: dark) {
  .title-bar {
    background: #1f2937;
    border-bottom-color: #374151;
  }
  
  .app-title {
    color: #f9fafb;
  }
  
  .title-bar-button {
    color: #9ca3af;
  }
  
  .title-bar-button:hover {
    background: #374151;
    color: #f9fafb;
  }
}

/* Windows/Linux 特定样式 */
.title-bar:not(.is-maximized) {
  border-radius: 8px 8px 0 0;
}

/* 最大化时的样式调整 */
.title-bar.is-maximized {
  border-radius: 0;
}
</style>

