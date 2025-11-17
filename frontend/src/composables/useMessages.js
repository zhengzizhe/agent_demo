/**
 * 消息管理 Composable
 */
import { ref } from 'vue'
import { getTaskStatusClass, findTaskInArray } from '../utils/task.js'

export function useMessages() {
  const messages = ref([])
  const error = ref('')
  const executionError = ref('')  // execution_failed 类型的错误

  /**
   * 添加用户消息
   */
  const addUserMessage = (content) => {
    messages.value.push({
      role: 'user',
      content: String(content || ''),
      timestamp: new Date()
    })
  }

  /**
   * 添加助手消息
   */
  const addAssistantMessage = (content = '', streaming = false) => {
    const newMessage = {
      role: 'assistant',
      content: String(content || ''),
      streaming: streaming,
      tasks: [],
      timestamp: new Date()
    }
    messages.value.push(newMessage)
    console.log('添加 assistant 消息:', newMessage)
    return newMessage
  }

  /**
   * 更新最后一条助手消息
   */
  const updateLastAssistantMessage = (content, streaming = false) => {
    const lastMessage = messages.value[messages.value.length - 1]
    if (lastMessage && lastMessage.role === 'assistant') {
      // 创建新消息对象以确保响应式更新，同时保留 tasks
      const newMessages = messages.value.map((msg, idx) => {
        if (idx === messages.value.length - 1 && msg.role === 'assistant') {
          return {
            ...msg,
            content: String(content || ''),
            streaming: streaming,
            tasks: msg.tasks ? [...msg.tasks] : [] // 保留并复制 tasks 数组
          }
        }
        return msg
      })
      messages.value = newMessages
    } else {
      addAssistantMessage(content, streaming)
    }
  }

  /**
   * 追加到最后一条助手消息
   */
  const appendToLastAssistantMessage = (content) => {
    const lastMessage = messages.value[messages.value.length - 1]
    if (lastMessage && lastMessage.role === 'assistant') {
      // 创建新消息对象以确保响应式更新，同时保留 tasks
      const newMessages = messages.value.map((msg, idx) => {
        if (idx === messages.value.length - 1 && msg.role === 'assistant') {
          return {
            ...msg,
            content: (msg.content || '') + String(content || ''),
            tasks: msg.tasks ? [...msg.tasks] : [] // 保留并复制 tasks 数组
          }
        }
        return msg
      })
      messages.value = newMessages
    } else {
      addAssistantMessage(content)
    }
  }

  /**
   * 将任务列表附加到最后一条AI消息
   */
  const attachTasksToLastMessage = (taskList) => {
    if (!taskList || !Array.isArray(taskList) || taskList.length === 0) {
      console.warn('attachTasksToLastMessage: 任务列表为空或无效', taskList)
      return
    }
    
    const lastMessage = messages.value[messages.value.length - 1]
    console.log('attachTasksToLastMessage 调用:', {
      hasLastMessage: !!lastMessage,
      lastMessageRole: lastMessage?.role,
      taskListLength: taskList?.length,
      taskList: taskList,
      currentTasksCount: lastMessage?.tasks?.length || 0
    })
    
    if (lastMessage && lastMessage.role === 'assistant') {
      const tasksWithStatus = taskList.map(task => ({
        ...task,
        status: task.status || 'PENDING',
        id: task.id || `task-${Date.now()}-${Math.random()}`
      }))
      
      // 强制触发响应式更新 - 创建新数组引用
      const newMessages = messages.value.map((msg, idx) => {
        if (idx === messages.value.length - 1 && msg.role === 'assistant') {
          return {
            ...msg,
            tasks: [...tasksWithStatus], // 创建新数组
            timestamp: msg.timestamp || new Date() // 确保有timestamp
          }
        }
        return msg
      })
      messages.value = newMessages
      
      console.log('任务列表已附加:', {
        messageIndex: messages.value.length - 1,
        tasksCount: messages.value[messages.value.length - 1].tasks?.length || 0,
        tasks: messages.value[messages.value.length - 1].tasks
      })
    } else {
      console.warn('没有找到 assistant 消息来附加任务列表', {
        lastMessage: lastMessage,
        messagesLength: messages.value.length
      })
      // 如果没有assistant消息，创建一个
      if (messages.value.length === 0 || messages.value[messages.value.length - 1].role !== 'assistant') {
        const newMessage = addAssistantMessage('', false)
        newMessage.tasks = taskList.map(task => ({
          ...task,
          status: task.status || 'PENDING',
          id: task.id || `task-${Date.now()}-${Math.random()}`
        }))
        console.log('创建新的 assistant 消息并附加任务列表:', {
          tasksCount: newMessage.tasks.length,
          tasks: newMessage.tasks
        })
      }
    }
  }

  /**
   * 更新最后一条AI消息中的任务状态
   */
  const updateTaskInLastMessage = (taskId, status, content = null, errorMsg = null) => {
    const lastMessage = messages.value[messages.value.length - 1]
    if (!lastMessage || lastMessage.role !== 'assistant' || !lastMessage.tasks) {
      return
    }

    // 创建新数组以确保响应式更新
    const updateTaskInArray = (taskList) => {
      if (!taskList) return taskList
      return taskList.map(t => {
        if (t.id === taskId) {
          const updated = { ...t, status }
          if (content) {
            updated.result = (t.result || '') + content
          }
          if (errorMsg) {
            updated.error = errorMsg
          }
          return updated
        }
        if (t.children) {
          return { ...t, children: updateTaskInArray(t.children) }
        }
        return t
      })
    }
    
    const updatedTasks = updateTaskInArray(lastMessage.tasks)
    
    // 创建新消息对象以确保响应式更新
    const newMessages = messages.value.map((msg, idx) => {
      if (idx === messages.value.length - 1 && msg.role === 'assistant') {
        return {
          ...msg,
          tasks: updatedTasks
        }
      }
      return msg
    })
    messages.value = newMessages
  }

  /**
   * 设置错误消息
   */
  const setError = (errorMessage) => {
    error.value = errorMessage
  }

  /**
   * 清除错误消息
   */
  const clearError = () => {
    error.value = ''
  }

  /**
   * 设置执行错误（execution_failed）
   */
  const setExecutionError = (errorMessage) => {
    executionError.value = errorMessage
  }

  /**
   * 清除执行错误
   */
  const clearExecutionError = () => {
    executionError.value = ''
  }

  /**
   * 清除所有消息
   */
  const clearMessages = () => {
    messages.value = []
    error.value = ''
    executionError.value = ''
  }

  return {
    messages,
    error,
    executionError,
    addUserMessage,
    addAssistantMessage,
    updateLastAssistantMessage,
    appendToLastAssistantMessage,
    attachTasksToLastMessage,
    updateTaskInLastMessage,
    setError,
    clearError,
    setExecutionError,
    clearExecutionError,
    clearMessages
  }
}

