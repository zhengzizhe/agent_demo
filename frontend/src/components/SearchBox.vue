<template>
  <div class="search-wrapper">
    <svg class="search-icon" width="16" height="16" viewBox="0 0 16 16" fill="none">
      <circle cx="7" cy="7" r="5" stroke="currentColor" stroke-width="1.5" fill="none"/>
      <path d="M11 11l3 3" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
    </svg>
    <input 
      :value="modelValue"
      @input="$emit('update:modelValue', $event.target.value)"
      type="text" 
      class="search-input" 
      :placeholder="placeholder"
    />
    <button v-if="modelValue" class="search-clear" @click="$emit('update:modelValue', '')">
      <svg width="12" height="12" viewBox="0 0 12 12" fill="none">
        <path d="M3 3l6 6M9 3l-6 6" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
      </svg>
    </button>
  </div>
</template>

<script setup>
defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '搜索...'
  }
})

defineEmits(['update:modelValue'])
</script>

<style scoped>
@import '../styles/common.css';
</style>




