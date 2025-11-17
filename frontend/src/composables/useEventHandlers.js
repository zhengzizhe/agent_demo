/**
 * 事件处理 Composable
 */
import { ref, nextTick } from 'vue'

export function useEventHandlers(messagesManager) {
  const isExecuting = ref(false)
  const isPlanning = ref(false)

  /**
   * 处理事件
   */
  const handleEvent = (event) => {
    console.log('收到事件:', event)

    switch (event.type) {
      case 'planning_start':
        isPlanning.value = true
        // 确保不会清空已有的任务列表
        console.log('planning_start: 当前消息数量:', messagesManager.messages.value.length)
        messagesManager.messages.value.forEach((msg, idx) => {
          if (msg.role === 'assistant' && msg.tasks && msg.tasks.length > 0) {
            console.log(`planning_start: 消息 ${idx} 有 ${msg.tasks.length} 个任务，保留任务列表`)
          }
        })
        break

      case 'planning_complete':
        // 不显示消息，等待 plan_ready
        break

      case 'plan_ready':
        isPlanning.value = false
        // 在隐藏进度条之前，确保任务列表已经准备好
        console.log('plan_ready: 准备处理任务计划，当前消息数量:', messagesManager.messages.value.length)
        handlePlanReady(event)
        break

      case 'planning_failed':
      case 'plan_parse_failed':
        isPlanning.value = false
        const errorMsg = event.message || event.error || '任务计划生成失败'
        messagesManager.setError(errorMsg)
        messagesManager.addAssistantMessage(`❌ ${errorMsg}`)
        isExecuting.value = false
        break

      case 'task_start':
        if (event.taskId) {
          messagesManager.updateTaskInLastMessage(event.taskId, 'RUNNING')
        }
        break

      case 'task_running':
        if (event.taskId) {
          messagesManager.updateTaskInLastMessage(event.taskId, 'RUNNING')
        }
        break

      case 'streaming':
        if (event.taskId && event.content) {
          messagesManager.updateTaskInLastMessage(event.taskId, 'RUNNING')
          messagesManager.appendToLastAssistantMessage(event.content)
        }
        break

      case 'task_complete':
        if (event.taskId) {
          messagesManager.updateTaskInLastMessage(event.taskId, 'DONE', event.content)
          if (event.content) {
            messagesManager.appendToLastAssistantMessage('\n\n' + event.content)
          }
        }
        break

      case 'task_failed':
        if (event.taskId) {
          messagesManager.updateTaskInLastMessage(event.taskId, 'FAILED', null, event.error)
          if (event.error) {
            messagesManager.appendToLastAssistantMessage(`\n\n❌ 任务失败: ${event.error}`)
          }
        }
        break

      case 'error':
        isPlanning.value = false
        const errorMsg2 = event.message || event.error || '发生错误'
        messagesManager.setError(errorMsg2)
        messagesManager.addAssistantMessage(`❌ ${errorMsg2}`)
        isExecuting.value = false
        break
    }
  }

  /**
   * 处理计划就绪事件
   */
  const handlePlanReady = (event) => {
    if (!event.content) {
      console.warn('plan_ready 事件没有 content 字段')
      return
    }

    try {
      // 处理 content 可能是字符串或对象的情况
      let taskPlan
      if (typeof event.content === 'string') {
        taskPlan = JSON.parse(event.content)
      } else {
        taskPlan = event.content
      }
      
      console.log('收到任务计划:', taskPlan)
      
      // 确保有一条 assistant 消息
      let lastMessage = messagesManager.messages.value[messagesManager.messages.value.length - 1]
      if (!lastMessage || lastMessage.role !== 'assistant') {
        messagesManager.addAssistantMessage('', false)
        lastMessage = messagesManager.messages.value[messagesManager.messages.value.length - 1]
      }
      
      // 将任务列表添加到最后一条AI消息
      const taskList = taskPlan.tasks || []
      console.log('准备附加任务列表，任务数量:', taskList.length, '任务列表:', taskList)
      
      if (taskList.length > 0) {
        // 确保 tasks 数组已初始化
        if (!lastMessage.tasks) {
          lastMessage.tasks = []
        }
        messagesManager.attachTasksToLastMessage(taskList)
        
        // 立即强制更新，确保UI刷新
        nextTick(() => {
          messagesManager.messages.value = [...messagesManager.messages.value]
          console.log('plan_ready: 任务列表已更新，当前消息:', messagesManager.messages.value.map((m, i) => ({
            index: i,
            role: m.role,
            tasksCount: m.tasks?.length || 0
          })))
        })
      } else {
        console.warn('任务列表为空，无法显示')
      }
    } catch (e) {
      console.error('解析任务计划失败:', e, '原始内容:', event.content)
      const errorMsg3 = '解析任务计划失败: ' + e.message
      messagesManager.setError(errorMsg3)
      messagesManager.addAssistantMessage(`❌ ${errorMsg3}`)
    }
  }

  return {
    isExecuting,
    isPlanning,
    handleEvent
  }
}

