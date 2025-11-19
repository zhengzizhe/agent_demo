<template>
  <transition name="notification-slide">
    <div v-if="visible" class="error-notification">
      <div class="notification-content">
        <div class="notification-icon-wrapper">
          <svg class="notification-icon" width="16" height="16" viewBox="0 0 16 16" fill="none">
            <circle cx="8" cy="8" r="6" fill="#FEE4E2" stroke="#DC2626" stroke-width="1.2"/>
            <path d="M8 5v3M8 11h.01" stroke="#DC2626" stroke-width="1.2" stroke-linecap="round"/>
          </svg>
        </div>
        <div class="notification-text">
          <div v-if="title" class="notification-title">{{ title }}</div>
          <div class="notification-message">{{ message }}</div>
        </div>
        <button class="notification-close" @click="handleClose">
          <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
            <path d="M3.5 3.5l7 7M10.5 3.5l-7 7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
          </svg>
        </button>
      </div>
    </div>
  </transition>
</template>

<script setup>
import { defineProps, defineEmits, watch, onMounted } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: ''
  },
  message: {
    type: String,
    required: true
  },
  duration: {
    type: Number,
    default: 4000 // 自动关闭时间（毫秒），0 表示不自动关闭
  }
})

const emit = defineEmits(['close'])

let timer = null

const handleClose = () => {
  if (timer) {
    clearTimeout(timer)
    timer = null
  }
  emit('close')
}

watch(() => props.visible, (newVal) => {
  if (newVal && props.duration > 0) {
    if (timer) {
      clearTimeout(timer)
    }
    timer = setTimeout(() => {
      handleClose()
    }, props.duration)
  }
})

onMounted(() => {
  if (props.visible && props.duration > 0) {
    timer = setTimeout(() => {
      handleClose()
    }, props.duration)
  }
})
</script>

<style scoped>
.error-notification {
  position: fixed;
  top: 12px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 3000;
  min-width: 280px;
  max-width: 480px;
  width: auto;
}

.notification-content {
  background: #FEF2F2;
  border: none;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(240, 68, 56, 0.15);
  padding: 8px 12px;
  display: flex;
  align-items: flex-start;
  gap: 8px;
  position: relative;
}

.notification-icon-wrapper {
  flex-shrink: 0;
  margin-top: 1px;
}

.notification-icon {
  width: 16px;
  height: 16px;
}

.notification-text {
  flex: 1;
  min-width: 0;
}

.notification-title {
  font-size: 13px;
  font-weight: 600;
  color: #991B1B;
  margin-bottom: 2px;
  line-height: 1.4;
}

.notification-message {
  font-size: 13px;
  color: #B91C1C;
  line-height: 1.4;
  word-break: break-word;
}

.notification-close {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: 4px;
  color: #B91C1C;
  cursor: pointer;
  transition: all 0.15s ease-out;
  flex-shrink: 0;
  margin-top: -1px;
}

.notification-close:hover {
  background: rgba(239, 68, 68, 0.1);
  color: #991B1B;
}

/* 过渡动画 - 从顶部滑入（飞书风格） */
.notification-slide-enter-active {
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.notification-slide-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 1, 1);
}

.notification-slide-enter-from {
  opacity: 0;
  transform: translateX(-50%) translateY(-120%) scale(0.9);
}

.notification-slide-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-40px) scale(0.95);
}
</style>

