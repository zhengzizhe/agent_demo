<template>
  <div class="loading-state">
    <div class="loading-spinner"></div>
    <p>{{ message }}</p>
  </div>
</template>

<script setup>
defineProps({
  message: {
    type: String,
    default: '加载中...'
  }
})
</script>

<style scoped>
@import '../styles/common.css';
</style>




