<template>
  <div class="title-bar" :class="{ 'is-maximized': isMaximized }">
    <div class="title-bar-drag-region">
      <div class="title-bar-content">
        <div class="title-bar-left">
          <!-- 左侧留空，与项目融为一体 -->
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
  height: 36px;
  background: rgba(247, 248, 250, 0.75);
  backdrop-filter: blur(24px) saturate(180%);
  -webkit-backdrop-filter: blur(24px) saturate(180%);
  display: flex;
  align-items: center;
  -webkit-app-region: drag;
  user-select: none;
  position: relative;
  flex-shrink: 0;
  border-bottom: 1px solid #e5e7eb;
  border-right: 1px solid #e5e7eb;
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
  justify-content: flex-end;
  padding: 0;
}

.title-bar-left {
  display: none;
}

.title-bar-right {
  display: flex;
  align-items: center;
  gap: 0;
  -webkit-app-region: no-drag;
}

.title-bar-button {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: #86909c;
  cursor: pointer;
  transition: all 0.15s ease;
  border-radius: 0;
  margin-left: 0;
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

.title-bar-button:hover:not(:disabled) {
  background: rgba(0, 0, 0, 0.04);
  color: #1d2129;
}

.title-bar-button:active:not(:disabled) {
  background: rgba(0, 0, 0, 0.06);
}

.title-bar-button.close-btn:hover {
  background: rgba(220, 38, 38, 0.1);
  color: #dc2626;
}

.title-bar-button.close-btn:active {
  background: rgba(220, 38, 38, 0.15);
}

.title-bar-button svg {
  width: 12px;
  height: 12px;
}

/* 移除所有圆角和暗色主题，保持与项目一致的浅色毛玻璃风格 */
</style>

