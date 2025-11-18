/**
 * 事件处理 Composable
 */
import { ref, nextTick } from 'vue'

// 事件状态码常量
const EVENT_CODES = {
  PLANNING_START: 1001,
  PLANNING_RUNNING: 1002,
  PLANNING_COMPLETE: 1003,
  PLAN_READY: 1004,
  PLANNING_FAILED: 1005,
  PLAN_PARSE_FAILED: 1006,
  TASK_START: 2001,
  TASK_RUNNING: 2002,
  TASK_COMPLETE: 2003,
  TASK_FAILED: 2004,
  STREAMING: 3001,
  EXECUTION_FAILED: 4001,
  ERROR: 5001
}

export function useEventHandlers(messagesManager) {
  const isExecuting = ref(false)
  const isPlanning = ref(false)

  /**
   * 处理事件
   */
  const handleEvent = (event) => {
    console.log('收到事件:', event)
    
    // 优先使用状态码判断，如果没有则回退到字符串类型
    const eventCode = event.code || (event.type ? getCodeFromType(event.type) : null)

    switch (eventCode) {
      case EVENT_CODES.PLANNING_START:
        isPlanning.value = true
        // 确保不会清空已有的任务列表
        console.log('planning_start: 当前消息数量:', messagesManager.messages.value.length)
        messagesManager.messages.value.forEach((msg, idx) => {
          if (msg.role === 'assistant' && msg.tasks && msg.tasks.length > 0) {
            console.log(`planning_start: 消息 ${idx} 有 ${msg.tasks.length} 个任务，保留任务列表`)
          }
        })
        break

      case EVENT_CODES.PLANNING_COMPLETE:
        // 规划完成，如果有 message 就显示到对话框
        if (event.message) {
          // 确保有一条 assistant 消息
          let lastMessage = messagesManager.messages.value[messagesManager.messages.value.length - 1]
          if (!lastMessage || lastMessage.role !== 'assistant') {
            messagesManager.addAssistantMessage('', false)
            lastMessage = messagesManager.messages.value[messagesManager.messages.value.length - 1]
          }
          // 设置规划完成的 message
          messagesManager.updateLastAssistantMessage(event.message, false)
        }
        break

      case EVENT_CODES.PLAN_READY:
        isPlanning.value = false
        // 在隐藏进度条之前，确保任务列表已经准备好
        console.log('plan_ready: 准备处理任务计划，当前消息数量:', messagesManager.messages.value.length)
        handlePlanReady(event)
        // 如果有 message，覆盖到对话框
        if (event.message) {
          messagesManager.updateLastAssistantMessage(event.message, false)
        }
        break

      case EVENT_CODES.PLANNING_FAILED:
      case EVENT_CODES.PLAN_PARSE_FAILED:
        isPlanning.value = false
        const errorMsg = event.message || event.error || '任务计划生成失败'
        messagesManager.setError(errorMsg)
        messagesManager.addAssistantMessage(`❌ ${errorMsg}`)
        isExecuting.value = false
        break

      case EVENT_CODES.TASK_START:
        // task 类型事件只更新任务状态，不添加到消息列表
        if (event.taskId) {
          messagesManager.updateTaskInLastMessage(event.taskId, 'RUNNING')
        }
        break

      case EVENT_CODES.TASK_RUNNING:
        // task 类型事件只更新任务状态，不添加到消息列表
        if (event.taskId) {
          messagesManager.updateTaskInLastMessage(event.taskId, 'RUNNING')
        }
        break

      case EVENT_CODES.STREAMING:
        // streaming 事件处理：第一次覆盖规划消息，后续追加
        if (event.taskId) {
          messagesManager.updateTaskInLastMessage(event.taskId, 'RUNNING')
        }
        if (event.content !== undefined && event.content !== null) {
          const lastMessage = messagesManager.messages.value[messagesManager.messages.value.length - 1]
          // 如果最后一条消息不是 streaming 状态，说明是第一次 streaming，覆盖内容
          // 如果已经是 streaming 状态，说明是后续的 streaming，追加内容
          if (lastMessage && lastMessage.role === 'assistant' && lastMessage.streaming) {
            // 已经是 streaming 状态，追加内容
            messagesManager.appendToLastAssistantMessage(event.content)
          } else {
            // 第一次 streaming，覆盖内容（包括规划完成的 message）
            messagesManager.updateLastAssistantMessage(event.content, true)
          }
        }
        break

      case EVENT_CODES.TASK_COMPLETE:
        // task 类型事件只更新任务状态，不添加到消息列表
        if (event.taskId) {
          messagesManager.updateTaskInLastMessage(event.taskId, 'DONE', event.content)
          // 不再添加消息内容到消息列表
        }
        break

      case EVENT_CODES.TASK_FAILED:
        // task 类型事件只更新任务状态，不添加到消息列表
        if (event.taskId) {
          messagesManager.updateTaskInLastMessage(event.taskId, 'FAILED', null, event.error)
          // 不再添加错误消息到消息列表
        }
        break

      case EVENT_CODES.EXECUTION_FAILED:
        // execution_failed 类型的事件，在对话下方显示红色错误框
        isPlanning.value = false
        isExecuting.value = false
        const executionErrorMsg = event.error || event.message || '执行失败'
        // 将错误信息附加到最后一条消息上
        const lastMessage = messagesManager.messages.value[messagesManager.messages.value.length - 1]
        if (lastMessage) {
          lastMessage.executionError = executionErrorMsg
          // 强制触发响应式更新
          messagesManager.messages.value = [...messagesManager.messages.value]
        } else {
          // 如果没有消息，创建一个新的错误消息
          messagesManager.addAssistantMessage('')
          const newLastMessage = messagesManager.messages.value[messagesManager.messages.value.length - 1]
          newLastMessage.executionError = executionErrorMsg
        }
        break

      case EVENT_CODES.ERROR:
        isPlanning.value = false
        const errorMsg2 = event.message || event.error || '发生错误'
        messagesManager.setError(errorMsg2)
        messagesManager.addAssistantMessage(`❌ ${errorMsg2}`)
        isExecuting.value = false
        break
        
      default:
        // 如果没有匹配的状态码，尝试使用字符串类型（向后兼容）
        if (event.type) {
          console.warn('未识别的事件状态码:', eventCode, '使用字符串类型:', event.type)
        }
        break
    }
  }
  
  /**
   * 从字符串类型获取状态码（向后兼容）
   */
  const getCodeFromType = (type) => {
    const typeToCode = {
      'planning_start': EVENT_CODES.PLANNING_START,
      'planning_running': EVENT_CODES.PLANNING_RUNNING,
      'planning_complete': EVENT_CODES.PLANNING_COMPLETE,
      'plan_ready': EVENT_CODES.PLAN_READY,
      'planning_failed': EVENT_CODES.PLANNING_FAILED,
      'plan_parse_failed': EVENT_CODES.PLAN_PARSE_FAILED,
      'task_start': EVENT_CODES.TASK_START,
      'task_running': EVENT_CODES.TASK_RUNNING,
      'task_complete': EVENT_CODES.TASK_COMPLETE,
      'task_failed': EVENT_CODES.TASK_FAILED,
      'streaming': EVENT_CODES.STREAMING,
      'execution_failed': EVENT_CODES.EXECUTION_FAILED,
      'error': EVENT_CODES.ERROR
    }
    return typeToCode[type] || null
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

