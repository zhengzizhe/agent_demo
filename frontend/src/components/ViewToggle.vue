<template>
  <div class="view-toggle">
    <button 
      class="view-btn" 
      :class="{ active: modelValue === 'grid' }" 
      @click="$emit('update:modelValue', 'grid')"
      :title="gridTitle"
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
    >
      <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
        <path d="M2 4h12M2 8h12M2 12h8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
      </svg>
    </button>
  </div>
</template>

<script setup>
defineProps({
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
</script>

<style scoped>
@import '../styles/common.css';
</style>


