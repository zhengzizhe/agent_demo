/**
 * 任务工具函数
 */

/**
 * 获取任务状态样式类
 */
export const getTaskStatusClass = (status) => {
  return `status-${status?.toLowerCase() || 'pending'}`
}

/**
 * 获取任务状态图标
 */
export const getTaskStatusIcon = (status) => {
  const map = {
    'PENDING': '⏳',
    'RUNNING': '🔄',
    'DONE': '✅',
    'FAILED': '❌',
    'SKIPPED': '⏭️'
  }
  return map[status] || '⏳'
}

/**
 * 判断任务是否正在运行
 */
export const isTaskRunning = (status) => {
  return status === 'RUNNING'
}

/**
 * 在任务列表中查找任务
 */
export const findTaskInArray = (taskList, taskId) => {
  if (!taskList) return null
  for (const t of taskList) {
    if (t.id === taskId) {
      return t
    }
    if (t.children) {
      const found = findTaskInArray(t.children, taskId)
      if (found) return found
    }
  }
  return null
}

