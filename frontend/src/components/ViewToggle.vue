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
  background: rgba(0, 0, 0, 0.04);
  border-radius: 10px;
  padding: 3px;
  gap: 0;
  box-shadow: 
    0 1px 3px rgba(0, 0, 0, 0.05),
    inset 0 1px 0 rgba(255, 255, 255, 0.6);
}

.view-toggle-slider {
  position: absolute;
  top: 3px;
  left: 3px;
  height: calc(100% - 6px);
  width: 50%;
  background: linear-gradient(135deg, var(--theme-accent, #165dff) 0%, #4c7fff 50%, #7b9fff 100%);
  border-radius: 7px;
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow: 
    0 2px 8px rgba(22, 93, 255, 0.3),
    0 1px 3px rgba(0, 0, 0, 0.1),
    inset 0 1px 0 rgba(255, 255, 255, 0.3);
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
  color: #6b7280;
  cursor: pointer;
  border-radius: 7px;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
}

.view-btn:hover {
  color: #111827;
  transform: translateY(-1px);
}

.view-btn svg {
  transition: all 0.3s;
}

.view-btn:hover svg {
  transform: scale(1.1);
}

.view-btn.active {
  color: white;
}

.view-btn.active svg {
  transform: scale(1.15);
  filter: drop-shadow(0 1px 2px rgba(0, 0, 0, 0.2));
}
</style>



