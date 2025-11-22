<template>
  <div class="tabs-bar">
    <!-- 后退、前进和刷新按钮 -->
    <div class="nav-buttons">
      <button 
        class="nav-btn" 
        :disabled="!canGoBack"
        @click="$emit('go-back')"
        title="后退"
      >
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path d="M10 12L6 8l4-4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
      <button 
        class="nav-btn" 
        :disabled="!canGoForward"
        @click="$emit('go-forward')"
        title="前进"
      >
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path d="M6 12l4-4-4-4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
      <button 
        class="nav-btn refresh-btn"
        :class="{ loading: isLoading }"
        @click="$emit('refresh', activeTabId)"
        :title="isLoading ? '刷新中...' : '刷新'"
      >
        <svg 
          width="16" 
          height="16" 
          viewBox="0 0 16 16" 
          fill="none"
          :class="{ spinning: isLoading }"
        >
          <path d="M2 8C2 4.686 4.686 2 8 2C9.657 2 11.157 2.707 12.243 3.757M14 8C14 11.314 11.314 14 8 14C6.343 14 4.843 13.293 3.757 12.243M3.757 3.757L6.5 6.5M12.243 12.243L9.5 9.5" 
                stroke="currentColor" 
                stroke-width="1.5" 
                stroke-linecap="round" 
                stroke-linejoin="round"/>
        </svg>
      </button>
    </div>

    <!-- 标签页列表 -->
    <div class="tabs-scroll-container" ref="scrollContainerRef">
      <div class="tabs-list">
        <button
          v-for="tab in tabs"
          :key="tab.id"
          :ref="el => { if (el && activeTabId === tab.id) activeTabRef = el }"
          class="tab"
          :class="{ 
            active: activeTabId === tab.id,
            'main-tab': !tab.closable
          }"
          @click="$emit('tab-click', tab.id)"
          :title="tab.label"
        >
          <span class="tab-icon" v-if="tab.icon">{{ tab.icon }}</span>
          <span class="tab-label">{{ tab.label }}</span>
          <button
            v-if="tab.closable"
            class="tab-close"
            @click.stop="$emit('tab-close', tab.id)"
            :title="`关闭 ${tab.label}`"
          >
            <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
              <path d="M4 4l6 6M10 4l-6 6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </button>
        </button>
      </div>
    </div>

    <!-- 新建标签页按钮 -->
    <button class="new-tab-btn" @click="$emit('new-tab')" title="新建标签页">
      <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
        <path d="M8 3v10M3 8h10" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
      </svg>
    </button>

    <!-- Electron 窗口控制按钮 -->
    <div v-if="isElectron" class="window-controls">
      <button 
        class="window-control-btn minimize-btn" 
        @click="handleMinimize"
        title="最小化"
      >
        <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
          <path d="M2 6h8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
      </button>
      <button 
        class="window-control-btn maximize-btn" 
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
        class="window-control-btn close-btn" 
        @click="handleClose"
        title="关闭"
      >
        <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
          <path d="M3 3l6 6M9 3l-6 6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick, computed, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  tabs: {
    type: Array,
    required: true
  },
  activeTabId: {
    type: String,
    required: true
  },
  canGoBack: {
    type: Boolean,
    default: false
  },
  canGoForward: {
    type: Boolean,
    default: false
  }
})

defineEmits(['tab-click', 'tab-close', 'new-tab', 'go-back', 'go-forward', 'refresh'])

// 计算当前标签页是否正在加载
const isLoading = computed(() => {
  const activeTab = props.tabs.find(tab => tab.id === props.activeTabId)
  return activeTab?.isLoading || false
})

const scrollContainerRef = ref(null)
const activeTabRef = ref(null)

// Electron 相关
const isElectron = ref(typeof window !== 'undefined' && window.electronAPI !== undefined)
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
    setTimeout(checkMaximized, 100)
  }
}

const handleClose = () => {
  if (window.electronAPI) {
    window.electronAPI.close()
  }
}

if (isElectron.value) {
  onMounted(() => {
    checkMaximized()
    
    let cleanupWindowStateListener = null
    
    if (window.electronAPI && window.electronAPI.onWindowStateChange) {
      cleanupWindowStateListener = window.electronAPI.onWindowStateChange((maximized) => {
        isMaximized.value = maximized
      })
    }
    
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
}

// 监听活动标签页变化，自动滚动到活动标签
watch(() => props.activeTabId, () => {
  nextTick(() => {
    scrollToActiveTab()
  })
}, { immediate: true })

// 监听标签列表变化，自动滚动到活动标签
watch(() => props.tabs, () => {
  nextTick(() => {
    scrollToActiveTab()
  })
}, { deep: true })

// 滚动到活动标签页
function scrollToActiveTab() {
  if (!scrollContainerRef.value || !activeTabRef.value) return
  
  const container = scrollContainerRef.value
  const activeTab = activeTabRef.value
  
  const containerRect = container.getBoundingClientRect()
  const tabRect = activeTab.getBoundingClientRect()
  
  // 计算需要滚动的距离
  const scrollLeft = container.scrollLeft
  const tabLeft = tabRect.left - containerRect.left + scrollLeft
  const tabRight = tabLeft + tabRect.width
  const containerWidth = containerRect.width
  
  // 如果标签在可视区域外，则滚动
  if (tabLeft < scrollLeft) {
    // 标签在左侧，滚动到标签左边缘
    container.scrollTo({
      left: tabLeft - 16, // 留出一些边距
      behavior: 'smooth'
    })
  } else if (tabRight > scrollLeft + containerWidth) {
    // 标签在右侧，滚动到标签右边缘
    container.scrollTo({
      left: tabRight - containerWidth + 16, // 留出一些边距
      behavior: 'smooth'
    })
  }
}
</script>

<style scoped>
.tabs-bar {
  height: 40px;
  background: rgba(249, 250, 251, 0.4);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  display: flex;
  align-items: flex-end;
  gap: 0;
  padding: 0 12px 0 8px;
  overflow: hidden;
  position: relative;
  flex-shrink: 0;
  z-index: 10;
  -webkit-app-region: drag;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  border-bottom: 1px solid rgba(0, 0, 0, 0.04);
  border-radius: 16px 16px 0 0;
}

.tabs-bar > * {
  -webkit-app-region: no-drag;
}

/* 后退和前进按钮 */
.nav-buttons {
  display: flex;
  align-items: center;
  gap: 2px;
  padding: 0 4px;
  flex-shrink: 0;
}

.nav-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: rgba(0, 0, 0, 0.02);
  border-radius: 8px;
  color: #86909c;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  position: relative;
}

.nav-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 8px;
  background: rgba(0, 0, 0, 0.04);
  opacity: 0;
  transition: opacity 0.25s cubic-bezier(0.16, 1, 0.3, 1);
}

.nav-btn:hover:not(:disabled) {
  background: rgba(0, 0, 0, 0.05);
  color: #1d2129;
  transform: translateY(-2px) scale(1.05);
  box-shadow: 
    0 4px 12px rgba(0, 0, 0, 0.06),
    0 2px 4px rgba(0, 0, 0, 0.04);
}

.nav-btn:hover:not(:disabled)::before {
  opacity: 1;
}

.nav-btn:hover:not(:disabled) svg {
  transform: scale(1.1);
}

.nav-btn:active:not(:disabled) {
  background: rgba(0, 0, 0, 0.08);
  transform: translateY(0) scale(1);
}

.nav-btn svg {
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.nav-btn:disabled {
  opacity: 0.25;
  cursor: not-allowed;
}

.nav-btn svg {
  width: 14px;
  height: 14px;
  position: relative;
  z-index: 1;
  stroke-width: 2;
}

.refresh-btn svg.spinning {
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.tabs-scroll-container {
  flex: 1;
  overflow-x: auto;
  overflow-y: hidden;
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE/Edge */
}

.tabs-scroll-container::-webkit-scrollbar {
  display: none; /* Chrome/Safari */
}

.tabs-list {
  display: flex;
  align-items: flex-end;
  height: 100%;
  gap: 2px;
  padding: 0;
  margin: 0;
}

.tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 14px;
  border: none;
  background: transparent;
  font-size: 13px;
  font-weight: 500;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.16, 1, 0.3, 1);
  white-space: nowrap;
  position: relative;
  height: 32px;
  border-radius: 8px 8px 0 0;
  min-width: 100px;
  max-width: 280px;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  animation: tabSlideIn 0.35s cubic-bezier(0.16, 1, 0.3, 1) both;
  transform-origin: left center;
}

@keyframes tabSlideIn {
  0% {
    opacity: 0;
    transform: translateX(-12px) scale(0.95);
    filter: blur(2px);
  }
  100% {
    opacity: 1;
    transform: translateX(0) scale(1);
    filter: blur(0);
  }
}

.tab::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 8px 8px 0 0;
  background: rgba(0, 0, 0, 0.02);
  opacity: 0;
  transition: all 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  pointer-events: none;
}

.tab::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 50%;
  transform: translateX(-50%) scaleX(0);
  width: 0;
  height: 2px;
  background: var(--theme-accent, #165dff);
  border-radius: 2px 2px 0 0;
  opacity: 0;
  transition: all 0.35s cubic-bezier(0.16, 1, 0.3, 1);
}

.tab:hover {
  background: rgba(0, 0, 0, 0.02);
  color: #1d2129;
}

.tab:hover::before {
  opacity: 1;
}

.tab.active {
  background: rgba(249, 250, 251, 0.95);
  color: var(--theme-accent, #165dff);
  font-weight: 600;
  z-index: 1;
  height: 36px;
  margin-bottom: -1px;
  border-bottom: 1px solid rgba(249, 250, 251, 0.95);
  box-shadow: 
    0 -2px 8px rgba(0, 0, 0, 0.02),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
  animation: tabActivate 0.4s cubic-bezier(0.16, 1, 0.3, 1) both;
}

@keyframes tabActivate {
  0% {
    background: transparent;
    transform: translateY(0);
  }
  50% {
    transform: translateY(-1px);
  }
  100% {
    background: rgba(249, 250, 251, 0.95);
    transform: translateY(0);
  }
}

.tab.active::before {
  opacity: 0;
}

.tab.active::after {
  opacity: 1;
  transform: translateX(-50%) scaleX(1);
  width: 40px;
  height: 2.5px;
  animation: tabIndicatorSlide 0.4s cubic-bezier(0.16, 1, 0.3, 1) both;
}

@keyframes tabIndicatorSlide {
  0% {
    width: 0;
    opacity: 0;
  }
  100% {
    width: 40px;
    opacity: 1;
  }
}

/* 固定标签页样式（主标签页） */
.tab.main-tab {
  font-weight: 500;
}

.tab.main-tab.active {
  font-weight: 600;
}

.tab-icon {
  font-size: 14px;
  line-height: 1;
  flex-shrink: 0;
}

.tab-label {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  min-width: 0;
}

.tab-close {
  width: 18px;
  height: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  border-radius: 50%;
  color: #86909c;
  cursor: pointer;
  transition: all 0.15s ease;
  flex-shrink: 0;
  opacity: 0.6;
  margin-left: 8px;
  padding: 0;
  position: relative;
  z-index: 2;
}

/* 悬停标签页时显示关闭按钮 */
.tab:hover .tab-close {
  opacity: 1;
}

/* 激活的标签页始终显示关闭按钮（如果可关闭） */
.tab.active .tab-close {
  opacity: 1;
}

/* 不可关闭的标签页不显示关闭按钮 */
.tab:not(.main-tab) .tab-close {
  display: flex;
}

/* 悬停关闭按钮时的样式（Chrome 风格） */
.tab-close:hover {
  background: rgba(0, 0, 0, 0.08);
  color: #1d2129;
}

/* 悬停时如果是激活标签，背景变红（Chrome 风格） */
.tab.active .tab-close:hover {
  background: rgba(220, 38, 38, 0.15);
  color: #dc2626;
}

.tab-close:active {
  transform: scale(0.9);
  background: rgba(220, 38, 38, 0.2);
  color: #dc2626;
}

.tab-close svg {
  width: 14px;
  height: 14px;
}

.new-tab-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: rgba(0, 0, 0, 0.02);
  color: #86909c;
  cursor: pointer;
  transition: all 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
  flex-shrink: 0;
  border-radius: 8px;
  margin: 0 4px;
  border: 1px solid rgba(0, 0, 0, 0.04);
  position: relative;
  overflow: hidden;
}

.new-tab-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(circle at center, rgba(22, 93, 255, 0.1) 0%, transparent 70%);
  opacity: 0;
  transition: opacity 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  transform: scale(0);
}

.new-tab-btn:hover {
  background: rgba(22, 93, 255, 0.08);
  color: var(--theme-accent, #165dff);
  border-color: rgba(22, 93, 255, 0.2);
  transform: translateY(-2px) scale(1.1) rotate(90deg);
  box-shadow: 
    0 4px 12px rgba(22, 93, 255, 0.15),
    0 0 0 2px rgba(22, 93, 255, 0.1);
}

.new-tab-btn:hover::before {
  opacity: 1;
  transform: scale(1);
}

.new-tab-btn:hover svg {
  transform: rotate(180deg);
}

.new-tab-btn:active {
  background: rgba(22, 93, 255, 0.12);
  transform: translateY(0) scale(1.05) rotate(90deg);
}

.new-tab-btn svg {
  transition: transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;
  z-index: 1;
}

.new-tab-btn svg {
  width: 14px;
  height: 14px;
  stroke-width: 2;
}

/* Electron 窗口控制按钮 */
.window-controls {
  display: flex;
  align-items: center;
  gap: 0;
  margin-left: 4px;
  flex-shrink: 0;
}

.window-control-btn {
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
  position: relative;
}

.window-control-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 4px;
  background: currentColor;
  opacity: 0;
  transform: scale(0.8);
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
}

.window-control-btn:hover:not(:disabled) {
  background: rgba(0, 0, 0, 0.04);
  color: #1d2129;
}

.window-control-btn:active:not(:disabled) {
  background: rgba(0, 0, 0, 0.06);
}

.window-control-btn.close-btn:hover {
  background: rgba(220, 38, 38, 0.1);
  color: #dc2626;
}

.window-control-btn.close-btn:active {
  background: rgba(220, 38, 38, 0.15);
}

.window-control-btn svg {
  width: 12px;
  height: 12px;
  position: relative;
  z-index: 1;
}
</style>
