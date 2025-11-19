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
  </div>
</template>

<script setup>
import { ref, watch, nextTick, computed } from 'vue'

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
  height: 36px;
  background: var(--theme-background, #ffffff);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  border-bottom: 1px solid rgba(229, 231, 235, 0.8);
  display: flex;
  align-items: center;
  gap: 0;
  padding: 0 4px;
  overflow: hidden;
  position: relative;
  flex-shrink: 0;
  z-index: 10;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.02);
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
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: #86909c;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  border-radius: 6px;
}

.nav-btn::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 6px;
  background: currentColor;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.nav-btn:hover:not(:disabled) {
  background: rgba(0, 0, 0, 0.06);
  color: #1d2129;
  transform: translateY(-1px);
}

.nav-btn:active:not(:disabled) {
  background: rgba(0, 0, 0, 0.1);
  transform: translateY(0);
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
  gap: 0;
  padding: 2px 0 0 0;
}

.tab {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 16px;
  border: none;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(229, 231, 235, 0.5);
  border-bottom: none;
  font-size: 13px;
  font-weight: 500;
  color: #4e5969;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
  position: relative;
  height: 32px;
  border-radius: 8px 8px 0 0;
  min-width: 120px;
  max-width: 320px;
  margin-right: 1px;
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}

.tab::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 8px 8px 0 0;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.8) 0%, rgba(255, 255, 255, 0.4) 100%);
  opacity: 0;
  transition: opacity 0.25s ease;
  pointer-events: none;
}

.tab:hover {
  background: rgba(255, 255, 255, 0.85);
  color: #1d2129;
  border-color: rgba(229, 231, 235, 0.8);
  transform: translateY(-1px);
}

.tab:hover::before {
  opacity: 1;
}

.tab.active {
  background: var(--theme-background, #ffffff);
  color: var(--theme-accent, #165dff);
  font-weight: 600;
  border-color: rgba(229, 231, 235, 0.8);
  border-bottom-color: var(--theme-background, #ffffff);
  z-index: 1;
  height: 34px;
  margin-bottom: -2px;
  box-shadow: 
    0 -2px 12px rgba(0, 0, 0, 0.06),
    0 1px 2px rgba(0, 0, 0, 0.04);
  transform: translateY(0);
}

.tab.active::before {
  opacity: 0;
}

.tab.active::after {
  content: '';
  position: absolute;
  bottom: 0;
  left: 8px;
  right: 8px;
  height: 3px;
  background: var(--theme-accent, #165dff);
  border-radius: 2px 2px 0 0;
  box-shadow: 0 0 8px color-mix(in srgb, var(--theme-accent, #165dff) 30%, transparent);
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
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  color: #86909c;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  flex-shrink: 0;
  border-radius: 6px;
  margin: 0 4px;
}

.new-tab-btn:hover {
  background: rgba(0, 0, 0, 0.06);
  color: #1d2129;
  transform: translateY(-1px);
}

.new-tab-btn:active {
  background: rgba(0, 0, 0, 0.1);
  transform: translateY(0);
}

.new-tab-btn svg {
  width: 14px;
  height: 14px;
  stroke-width: 2;
}
</style>
