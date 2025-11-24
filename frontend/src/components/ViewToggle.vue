<template>
  <div class="view-toggle">
    <div class="view-toggle-container">
      <div class="view-toggle-slider" :style="sliderStyle"></div>
      <button 
        class="view-btn" 
        :class="{ active: modelValue === 'grid' }" 
        @click="$emit('update:modelValue', 'grid')"
        :title="gridTitle"
        data-view="grid"
      >
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <rect x="2" y="2" width="5" height="5" rx="1" stroke="currentColor" stroke-width="1.5" fill="none"/>
          <rect x="9" y="2" width="5" height="5" rx="1" stroke="currentColor" stroke-width="1.5" fill="none"/>
          <rect x="2" y="9" width="5" height="5" rx="1" stroke="currentColor" stroke-width="1.5" fill="none"/>
          <rect x="9" y="9" width="5" height="5" rx="1" stroke="currentColor" stroke-width="1.5" fill="none"/>
        </svg>
      </button>
      <button 
        class="view-btn" 
        :class="{ active: modelValue === 'list' }" 
        @click="$emit('update:modelValue', 'list')"
        :title="listTitle"
        data-view="list"
      >
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
          <path d="M2 4h12M2 8h12M2 12h8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: 'grid',
    validator: (value) => ['grid', 'list'].includes(value)
  },
  gridTitle: {
    type: String,
    default: '网格视图'
  },
  listTitle: {
    type: String,
    default: '列表视图'
  }
})

defineEmits(['update:modelValue'])

// 滑动指示器样式
const sliderStyle = computed(() => {
  const index = props.modelValue === 'grid' ? 0 : 1
  return {
    transform: `translateX(${index * 100}%)`,
    width: '50%'
  }
})
</script>

<style scoped>
.view-toggle {
  display: inline-flex;
}

.view-toggle-container {
  position: relative;
  display: inline-flex;
  align-items: center;
  background: var(--glass-bg);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-medium);
  padding: 4px;
  gap: 0;
  box-shadow: var(--shadow-soft);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
}

.view-toggle-container::before {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: var(--radius-medium);
  background: linear-gradient(135deg, 
    rgba(255, 255, 255, 0.4) 0%, 
    rgba(255, 255, 255, 0.1) 50%,
    rgba(255, 255, 255, 0.2) 100%);
  pointer-events: none;
  z-index: 0;
  opacity: 0.5;
}

.view-toggle-slider {
  position: absolute;
  top: 4px;
  left: 4px;
  height: calc(100% - 8px);
  width: calc(50% - 4px);
  background: linear-gradient(135deg, var(--theme-accent) 0%, var(--theme-accent-light) 100%);
  border-radius: var(--radius-small);
  transition: all 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow: 
    0 4px 16px var(--theme-accent-glow),
    0 2px 6px rgba(0, 102, 255, 0.2),
    inset 0 1px 0 rgba(255, 255, 255, 0.4);
  z-index: 1;
}

.view-btn {
  position: relative;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  background: transparent;
  color: var(--color-text-secondary);
  cursor: pointer;
  border-radius: var(--radius-small);
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

.view-btn:hover {
  color: var(--color-text-primary);
  transform: translateY(-2px) scale(1.1);
}

.view-btn svg {
  transition: all 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  z-index: 1;
}

.view-btn:hover svg {
  transform: scale(1.15);
}

.view-btn.active {
  color: white;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.15);
}

.view-btn.active svg {
  transform: scale(1.2);
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.25));
}
</style>



