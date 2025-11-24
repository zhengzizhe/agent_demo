<template>
  <div class="top-bar">
    <div class="top-bar-left">
      <!-- 现代分段控制器风格标签页 -->
      <nav class="tabs-nav" aria-label="标签页导航">
        <div class="tabs-container">
          <div class="tabs-slider" :style="tabsSliderStyle"></div>
          <button
            v-for="tab in tabs"
            :key="tab.id"
            class="tab-item"
            :class="{ active: currentView === tab.id }"
            @click="handleTabClick(tab.id)"
            :title="tab.title"
            :data-tab="tab.id"
          >
            <span class="tab-icon" v-if="tab.icon">{{ tab.icon }}</span>
            <span class="tab-label">{{ tab.label }}</span>
            <span v-if="tab.badge" class="tab-badge">{{ tab.badge }}</span>
          </button>
        </div>
      </nav>
    </div>
    
    <div class="top-bar-right">
      <div class="top-bar-actions">
        <button class="action-btn" title="搜索">
          <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
            <circle cx="8" cy="8" r="5" stroke="currentColor" stroke-width="1.5" fill="none"/>
            <path d="M13 13l3 3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </button>
        <button class="action-btn" title="设置">
          <svg width="18" height="18" viewBox="0 0 18 18" fill="none">
            <circle cx="9" cy="9" r="2" stroke="currentColor" stroke-width="1.5" fill="none"/>
            <path d="M9 1v2M9 15v2M17 9h-2M3 9H1M14.66 3.34l-1.41 1.41M4.75 13.25l-1.41 1.41M14.66 14.66l-1.41-1.41M4.75 4.75l-1.41-1.41" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  currentView: {
    type: String,
    default: 'chat'
  }
})

const emit = defineEmits(['view-change'])

// 标签页配置
const tabs = computed(() => [
  {
    id: 'chat',
    label: '对话',
    icon: '💬',
    title: '对话'
  },
  {
    id: 'docs',
    label: '文档库',
    icon: '📄',
    title: '文档库'
  }
])

const handleTabClick = (viewId) => {
  emit('view-change', viewId)
}

// 滑动指示器样式
const tabsSliderStyle = computed(() => {
  const index = tabs.value.findIndex(tab => tab.id === props.currentView)
  if (index === -1) return { transform: 'translateX(0)', width: '0' }
  
  const width = 100 / tabs.value.length
  return {
    transform: `translateX(${index * 100}%)`,
    width: `${width}%`
  }
})
</script>

<style scoped>
.top-bar {
  height: 56px;
  background: #ffffff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: sticky;
  top: 0;
  z-index: 50;
  border-bottom: 1px solid #e5e7eb; /* 更清晰的边框 */
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.03); /* 更微妙的阴影 */
  /* 客户端风格：更紧凑的顶部栏 */
  backdrop-filter: blur(10px);
  background: rgba(255, 255, 255, 0.95);
}

.top-bar-left {
  display: flex;
  align-items: center;
  gap: 16px;
  flex: 1;
  min-width: 0;
}

/* 网站风格标签页 */
.tabs-nav {
  display: flex;
  align-items: center;
  height: 100%;
}

.tabs-container {
  position: relative;
  display: inline-flex;
  align-items: center;
  background: rgba(0, 0, 0, 0.04);
  border-radius: 12px;
  padding: 4px;
  gap: 0;
  height: 40px;
  box-shadow: 
    0 1px 3px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
}

.tabs-slider {
  position: absolute;
  top: 4px;
  left: 4px;
  height: calc(100% - 8px);
  background: linear-gradient(135deg, var(--theme-accent, #165dff) 0%, #4c7fff 50%, #7b9fff 100%);
  border-radius: 8px;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow: 
    0 2px 8px rgba(22, 93, 255, 0.3),
    0 1px 3px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
  z-index: 1;
}

.tab-item {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 8px 16px;
  border: none;
  background: transparent;
  font-size: 14px;
  font-weight: 600;
  color: #86909c;
  cursor: pointer;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
  border-radius: 8px;
  height: 100%;
  line-height: 1.5;
  min-width: 80px;
}

.tab-item:hover {
  color: #1d2129;
  transform: translateY(-1px);
}

.tab-item.active {
  color: white;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.1);
}

.tab-item.active .tab-icon {
  transform: scale(1.15);
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.2));
}

.tab-icon {
  font-size: 16px;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.3s;
}

.tab-label {
  font-size: 14px;
  letter-spacing: -0.01em;
}

.tab-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 6px;
  background: #f2f3f5;
  color: #86909c;
  font-size: 11px;
  font-weight: 600;
  border-radius: 9px;
  margin-left: 4px;
}

.tab-item.active .tab-badge {
  background: rgba(22, 93, 255, 0.15);
  color: #165dff;
}

.top-bar-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.top-bar-actions {
  display: flex;
  align-items: center;
  gap: 4px;
}

.action-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  border-radius: 6px;
  color: #86909c;
  cursor: pointer;
  transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
}

.action-btn:hover {
  background: #f2f3f5;
  color: #1d2129;
}

.action-btn:active {
  background: #e5e6eb;
  transform: scale(0.95);
}

.action-btn svg {
  width: 18px;
  height: 18px;
}
</style>

