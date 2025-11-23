<template>
  <transition name="toast-fade">
    <div v-if="visible" class="message-toast" :class="type">
      <div class="toast-content">
        <svg v-if="type === 'success'" class="toast-icon" width="16" height="16" viewBox="0 0 16 16" fill="none">
          <circle cx="8" cy="8" r="6" fill="#D1FAE5" stroke="currentColor" stroke-width="1.2"/>
          <path d="M5 8l2 2 4-4" stroke="currentColor" stroke-width="1.2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <svg v-else class="toast-icon" width="16" height="16" viewBox="0 0 16 16" fill="none">
          <circle cx="8" cy="8" r="6" fill="#DBEAFE" stroke="currentColor" stroke-width="1.2"/>
          <path d="M8 5v3M8 11h.01" stroke="currentColor" stroke-width="1.2" stroke-linecap="round"/>
        </svg>
        <span class="toast-message">{{ message }}</span>
      </div>
      <button class="toast-close" @click="handleClose">
        <svg width="14" height="14" viewBox="0 0 14 14" fill="none">
          <path d="M3.5 3.5l7 7M10.5 3.5l-7 7" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
        </svg>
      </button>
    </div>
  </transition>
</template>

<script setup>
import { watch, onMounted } from 'vue'

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  message: {
    type: String,
    required: true
  },
  type: {
    type: String,
    default: 'info', // 'info', 'success' (error 类型使用 ErrorDialog)
    validator: (value) => ['info', 'success'].includes(value)
  },
  duration: {
    type: Number,
    default: 3000 // 自动关闭时间（毫秒）
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
.message-toast {
  position: fixed;
  top: 12px;
  left: 50%;
  transform: translateX(-50%);
  z-index: 3000;
  min-width: 280px;
  max-width: 480px;
  width: auto;
  animation: slideInTop 0.3s ease-out;
}

/* 成功弹窗 - 淡绿色背景 */
.message-toast.success {
  background: #F0FDF4;
  border: none;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(16, 185, 129, 0.15);
  padding: 8px 12px;
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

/* 信息弹窗 - 淡蓝色背景 */
.message-toast.info {
  background: #EFF6FF;
  border: none;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(37, 99, 235, 0.15);
  padding: 8px 12px;
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.toast-content {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  flex: 1;
}

.toast-icon {
  flex-shrink: 0;
  margin-top: 1px;
  width: 16px;
  height: 16px;
}

.message-toast.success .toast-icon {
  color: #059669;
}

.message-toast.info .toast-icon {
  color: #2563eb;
}

.toast-message {
  font-size: 13px;
  line-height: 1.4;
  flex: 1;
}

.message-toast.success .toast-message {
  color: #047857;
}

.message-toast.info .toast-message {
  color: #1E40AF;
}

.toast-close {
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.15s ease-out;
  flex-shrink: 0;
  margin-top: -1px;
}

.message-toast.success .toast-close {
  color: #047857;
}

.message-toast.success .toast-close:hover {
  background: rgba(5, 150, 105, 0.1);
  color: #065F46;
}

.message-toast.info .toast-close {
  color: #1E40AF;
}

.message-toast.info .toast-close:hover {
  background: rgba(37, 99, 235, 0.1);
  color: #1E3A8A;
}

/* 过渡动画 - 从顶部滑入（飞书风格） */
.toast-fade-enter-active {
  transition: all 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
}

.toast-fade-leave-active {
  transition: all 0.25s cubic-bezier(0.4, 0, 1, 1);
}

.toast-fade-enter-from {
  opacity: 0;
  transform: translateX(-50%) translateY(-120%) scale(0.9);
}

.toast-fade-leave-to {
  opacity: 0;
  transform: translateX(-50%) translateY(-40px) scale(0.95);
}

@keyframes slideInTop {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(-100%);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}
</style>

