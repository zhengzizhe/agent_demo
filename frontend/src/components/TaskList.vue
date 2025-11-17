<template>
  <div 
    v-if="tasks && Array.isArray(tasks) && tasks.length > 0" 
    class="message-task-box"
    :style="{ animationDelay: `${animationDelay}s` }"
  >
    <div class="task-box-header">
      <span class="task-box-title">任务列表</span>
      <span class="task-box-count">{{ tasks.length }} 个任务</span>
    </div>
    <div class="task-box-content">
      <div 
        v-for="(task, taskIndex) in tasks" 
        :key="task.id"
        :class="['task-chip', getTaskStatusClass(task.status)]"
        :style="{ animationDelay: `${animationDelay + (taskIndex * 0.05)}s` }"
      >
        <span v-if="isTaskRunning(task.status)" class="chip-spinner"></span>
        <span class="chip-title">{{ task.title || task.description || task.id }}</span>
        <span class="chip-status">{{ getTaskStatusIcon(task.status) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { watch, onMounted } from 'vue'
import { getTaskStatusClass, getTaskStatusIcon, isTaskRunning } from '../utils/task.js'

const props = defineProps({
  tasks: {
    type: Array,
    default: () => []
  },
  animationDelay: {
    type: Number,
    default: 0
  }
})

// 确保任务列表的响应式更新
watch(() => props.tasks, (newTasks) => {
  console.log('TaskList: 任务列表更新，数量:', newTasks?.length || 0)
}, { deep: true, immediate: true })

onMounted(() => {
  console.log('TaskList: 组件已挂载，任务数量:', props.tasks?.length || 0)
})
</script>

<style scoped>
/* 任务列表小框（在AI消息上方）- Cursor 风格 */
.message-task-box {
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  border: 1.5px solid rgba(16, 163, 127, 0.15);
  border-radius: 12px;
  margin: 16px auto 12px auto;
  max-width: 768px;
  width: calc(100% - 48px);
  overflow: visible;
  min-height: 60px;
  flex-shrink: 0;
  animation: taskBoxSlideIn 0.5s cubic-bezier(0.16, 1, 0.3, 1) both;
  will-change: transform, opacity;
  box-shadow: 
    0 4px 16px rgba(16, 163, 127, 0.08),
    0 2px 4px rgba(16, 163, 127, 0.04),
    inset 0 1px 0 rgba(255, 255, 255, 0.8);
  position: relative;
  z-index: 1;
}

.message-task-box::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, #10a37f, #19c37d, #10a37f);
  background-size: 200% 100%;
  animation: gradientShift 3s ease infinite;
}

.task-box-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid rgba(16, 163, 127, 0.1);
  background: linear-gradient(135deg, rgba(16, 163, 127, 0.05) 0%, rgba(25, 195, 125, 0.03) 100%);
  font-size: 12px;
  font-weight: 600;
  color: #10a37f;
  position: relative;
}

.task-box-title {
  display: flex;
  align-items: center;
  gap: 6px;
}

.task-box-count {
  font-size: 11px;
  color: #8e8ea0;
  background: rgba(16, 163, 127, 0.1);
  padding: 4px 10px;
  border-radius: 12px;
  font-weight: 600;
}

.task-box-content {
  padding: 12px 16px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  background: transparent;
  min-height: 40px;
  overflow: visible;
}

.task-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  background: linear-gradient(135deg, #ffffff 0%, #fafafa 100%);
  border: 1.5px solid rgba(0, 0, 0, 0.08);
  border-radius: 8px;
  font-size: 12px;
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
  cursor: default;
  transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  color: #333;
  line-height: 1.5;
  animation: chipPopIn 0.4s cubic-bezier(0.34, 1.56, 0.64, 1) both;
  will-change: transform, opacity;
  box-shadow: 
    0 2px 6px rgba(0, 0, 0, 0.04),
    0 1px 2px rgba(0, 0, 0, 0.02);
  position: relative;
  overflow: hidden;
}

.task-chip::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.4), transparent);
  transition: left 0.5s;
}

.task-chip:hover {
  background: linear-gradient(135deg, #f8f9fa 0%, #ffffff 100%);
  border-color: rgba(16, 163, 127, 0.2);
  transform: translateY(-2px);
  box-shadow: 
    0 4px 12px rgba(16, 163, 127, 0.1),
    0 2px 4px rgba(16, 163, 127, 0.05);
}

.task-chip:hover::before {
  left: 100%;
}

.task-chip.status-pending {
  background: #f8f8f8;
  border-color: #d1d1d1;
  color: #666;
}

.task-chip.status-running {
  background: linear-gradient(135deg, #e8f5e9 0%, #c8e6c9 100%);
  border-color: #4caf50;
  color: #1b5e20;
  animation: statusPulse 2s ease-in-out infinite;
  box-shadow: 
    0 0 0 0 rgba(76, 175, 80, 0.4),
    0 2px 6px rgba(76, 175, 80, 0.15);
}

.task-chip.status-done {
  background: linear-gradient(135deg, #e3f2fd 0%, #bbdefb 100%);
  border-color: #2196f3;
  color: #0d47a1;
  animation: statusSuccess 0.6s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow: 
    0 2px 8px rgba(33, 150, 243, 0.2),
    0 1px 3px rgba(33, 150, 243, 0.15);
}

.task-chip.status-failed {
  background: linear-gradient(135deg, #ffebee 0%, #ffcdd2 100%);
  border-color: #f44336;
  color: #b71c1c;
  animation: statusShake 0.5s cubic-bezier(0.36, 0.07, 0.19, 0.97);
  box-shadow: 
    0 2px 8px rgba(244, 67, 54, 0.2),
    0 1px 3px rgba(244, 67, 54, 0.15);
}

.chip-spinner {
  display: inline-block;
  width: 8px;
  height: 8px;
  border: 1.5px solid rgba(76, 175, 80, 0.3);
  border-top-color: #4caf50;
  border-radius: 50%;
  animation: spin 0.6s linear infinite;
  flex-shrink: 0;
}

.chip-title {
  color: inherit;
  max-width: 250px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 400;
}

.chip-status {
  display: none;
}

@keyframes taskBoxSlideIn {
  from {
    opacity: 0;
    transform: translateY(-15px) scale(0.96);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

@keyframes gradientShift {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

@keyframes chipPopIn {
  from {
    opacity: 0;
    transform: scale(0.7) translateY(-8px) rotate(-5deg);
  }
  50% {
    transform: scale(1.05) translateY(-2px) rotate(2deg);
  }
  to {
    opacity: 1;
    transform: scale(1) translateY(0) rotate(0deg);
  }
}

@keyframes statusPulse {
  0%, 100% {
    box-shadow: 
      0 0 0 0 rgba(76, 175, 80, 0.4),
      0 2px 6px rgba(76, 175, 80, 0.15);
    transform: scale(1);
  }
  50% {
    box-shadow: 
      0 0 0 6px rgba(76, 175, 80, 0),
      0 4px 12px rgba(76, 175, 80, 0.2);
    transform: scale(1.02);
  }
}

@keyframes statusSuccess {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes statusShake {
  0%, 100% {
    transform: translateX(0);
  }
  10%, 30%, 50%, 70%, 90% {
    transform: translateX(-3px);
  }
  20%, 40%, 60%, 80% {
    transform: translateX(3px);
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>


