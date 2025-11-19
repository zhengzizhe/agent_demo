<template>
  <div class="empty-state">
    <svg class="empty-icon" width="64" height="64" viewBox="0 0 64 64" fill="none">
      <rect x="12" y="8" width="40" height="48" rx="2" stroke="currentColor" stroke-width="1.5" fill="none"/>
      <path d="M16 16h32M16 24h24M16 32h32M16 40h20" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
    </svg>
    <h3 class="empty-title">{{ title }}</h3>
    <p class="empty-desc">{{ description }}</p>
    <button v-if="showButton" class="empty-btn" @click="$emit('action')">
      <slot name="button">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="none" style="margin-right: 8px;">
          <path d="M8 2v12M2 8h12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
        {{ buttonText }}
      </slot>
    </button>
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: '暂无内容'
  },
  description: {
    type: String,
    default: ''
  },
  showButton: {
    type: Boolean,
    default: false
  },
  buttonText: {
    type: String,
    default: '创建'
  }
})

defineEmits(['action'])
</script>

<style scoped>
@import '../styles/common.css';
</style>


