<template>
  <div class="outline-panel">
    <div class="outline-header">
      <button @click="$emit('close')" class="outline-close-btn" title="收起">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path d="M10 4l-4 4 4 4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </button>
      <div class="outline-title-section">
        <h3 class="outline-title">{{ documentTitle }}</h3>
        <span class="outline-time">现在</span>
      </div>
      <div class="outline-actions">
        <button class="outline-action-btn" :class="{ active: viewMode === 'list' }" @click="viewMode = 'list'" title="列表">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <rect x="2" y="3" width="12" height="1.5" rx="0.75" fill="currentColor"/>
            <rect x="2" y="7" width="12" height="1.5" rx="0.75" fill="currentColor"/>
            <rect x="2" y="11" width="12" height="1.5" rx="0.75" fill="currentColor"/>
          </svg>
        </button>
        <button class="outline-action-btn" :class="{ active: viewMode === 'eye' }" @click="viewMode = 'eye'" title="预览">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M8 3C4 3 1.5 6 1.5 8s2.5 5 6.5 5 6.5-3 6.5-5-2.5-5-6.5-5z" stroke="currentColor" stroke-width="1.5" fill="none"/>
            <circle cx="8" cy="8" r="2" stroke="currentColor" stroke-width="1.5" fill="none"/>
          </svg>
        </button>
        <button class="outline-action-btn" :class="{ active: viewMode === 'attach' }" @click="viewMode = 'attach'" title="附件">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M10 4v6a2 2 0 0 1-4 0V5a1 1 0 0 1 2 0v4" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </button>
        <button class="outline-action-btn" :class="{ active: viewMode === 'doc' }" @click="viewMode = 'doc'" title="文档">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <rect x="3" y="3" width="10" height="10" rx="1" stroke="currentColor" stroke-width="1.5" fill="none"/>
            <path d="M6 6h4M6 9h4M6 12h3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </button>
      </div>
    </div>
    
    <div class="outline-content">
      <div class="outline-section">
        <h4 class="outline-section-title">目录</h4>
        <div class="outline-list">
          <div
            v-for="(item, index) in outlineItems"
            :key="index"
            class="outline-item"
            :class="{ active: item.active, 'is-heading': item.type === 'heading' }"
            @click="scrollToItem(item)"
          >
            <span class="outline-item-text">{{ item.text }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const props = defineProps({
  documentTitle: {
    type: String,
    default: '未命名文档'
  },
  editor: {
    type: Object,
    default: null
  }
})

const viewMode = ref('list')

const outlineItems = ref([
  { text: 'Getting Started', type: 'heading', level: 1, active: true },
  { text: 'Think of Craft as your personal notebook...', type: 'text', active: false },
  { text: 'Have fun with writing/ again', type: 'text', active: false },
  { text: 'More than just words', type: 'heading', level: 2, active: false },
  { text: 'Get stuff done', type: 'text', active: false },
  { text: 'Create a masterpiece', type: 'text', active: false },
  { text: "Add a Task to your doc by typing 'x' and hitting sp...", type: 'text', active: false },
  { text: 'Play around with this doc', type: 'text', active: false },
  { text: 'How to get started on iPad', type: 'heading', level: 2, active: false }
])

const scrollToItem = (item) => {
  // TODO: 实现滚动到对应位置
  console.log('Scroll to:', item)
}
</script>

<style scoped>
.outline-panel {
  width: 280px;
  background: rgba(250, 250, 252, 0.85);
  backdrop-filter: blur(30px) saturate(180%);
  -webkit-backdrop-filter: blur(30px) saturate(180%);
  border: 1px solid rgba(255, 255, 255, 0.4);
  border-radius: 0;
  display: flex;
  flex-direction: column;
  height: 100%;
  overflow-y: auto;
  flex-shrink: 0;
  box-shadow: 
    0 0 0 1px rgba(0, 0, 0, 0.05),
    0 4px 16px rgba(0, 0, 0, 0.1),
    0 8px 32px rgba(0, 0, 0, 0.08);
  position: relative;
  z-index: 10;
  margin: 12px 0 12px 12px;
  border-radius: 12px;
  /* 优化性能 */
  transform: translateZ(0);
  will-change: transform;
  contain: layout style paint;
  /* 优化滚动 */
  -webkit-overflow-scrolling: touch;
  backface-visibility: hidden;
  /* 隐藏滚动条但保留滚动功能 */
  scrollbar-width: none; /* Firefox */
  -ms-overflow-style: none; /* IE 10+ */
}

.outline-panel::-webkit-scrollbar {
  display: none; /* Chrome, Safari, Edge */
  width: 0;
  height: 0;
}

.outline-close-btn {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  border-radius: 4px;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s;
  margin-left: auto;
}

.outline-close-btn:hover {
  background: rgba(0, 0, 0, 0.05);
  color: #111827;
}

.outline-header {
  padding: 16px 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.08);
  position: sticky;
  top: 0;
  background: rgba(250, 250, 252, 0.9);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  z-index: 10;
  display: flex;
  align-items: center;
  gap: 12px;
}

.outline-title-section {
  margin-bottom: 12px;
}

.outline-title {
  font-size: 14px;
  font-weight: 600;
  color: #111827;
  margin: 0 0 4px 0;
}

.outline-time {
  font-size: 12px;
  color: #9ca3af;
}

.outline-actions {
  display: flex;
  gap: 4px;
}

.outline-action-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: none;
  background: transparent;
  border-radius: 6px;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.2s;
}

.outline-action-btn:hover {
  background: #f3f4f6;
  color: #111827;
}

.outline-action-btn.active {
  background: #165dff;
  color: #ffffff;
}

.outline-content {
  flex: 1;
  padding: 16px 20px;
  overflow: visible;
  /* 完全隐藏滚动条 */
  scrollbar-width: none !important; /* Firefox */
  -ms-overflow-style: none !important; /* IE 10+ */
}

.outline-content::-webkit-scrollbar {
  display: none !important;
  width: 0 !important;
  height: 0 !important;
  background: transparent !important;
}

.outline-content::-webkit-scrollbar-track {
  display: none !important;
  background: transparent !important;
}

.outline-content::-webkit-scrollbar-thumb {
  display: none !important;
  background: transparent !important;
}

.outline-section {
  margin-bottom: 24px;
}

.outline-section-title {
  font-size: 12px;
  font-weight: 600;
  color: #6b7280;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin: 0 0 12px 0;
}

.outline-list {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.outline-item {
  padding: 8px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  font-size: 13px;
  color: #374151;
  line-height: 1.5;
}

.outline-item:hover {
  background: #f9fafb;
}

.outline-item.active {
  background: rgba(22, 93, 255, 0.1);
  color: #165dff;
  font-weight: 500;
}

.outline-item.is-heading {
  font-weight: 600;
  color: #111827;
  padding-left: 0;
}

.outline-item.is-heading.active {
  color: #165dff;
}

.outline-item-text {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}
</style>

