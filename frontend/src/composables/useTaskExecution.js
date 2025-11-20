/**
 * 任务执行 Composable
 */
import { ref, nextTick } from 'vue'
import { useSession } from './useSession.js'

export function useTaskExecution(messagesManager, eventHandlers, formRef, messagesContainerRef, session) {
  // 使用传入的 session 实例（确保使用同一个 session），如果没有传入则创建新的
  const sessionInstance = session || useSession()
  const { userId, sessionId } = sessionInstance

  /**
   * 执行任务
   */
  const executeTask = async () => {
    const form = formRef.value || formRef
    if (!form.message.trim() || eventHandlers.isExecuting.value) {
      return
    }

    const userMessage = form.message.trim()
    
    // 添加用户消息
    messagesManager.addUserMessage(userMessage)

    // 清空输入
    form.message = ''
    messagesManager.clearError()
    eventHandlers.isExecuting.value = true

    // 用户发送消息后立即滚动到底部
    scrollToBottom(true)

    try {
      const response = await fetch('/task/execute', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          orchestratorId: form.orchestratorId,
          message: userMessage,
          userId: userId.value,
          sessionId: sessionId.value
        })
      })

      if (!response.ok) {
        throw new Error('请求失败')
      }

      await processStream(response)
    } catch (err) {
      console.error('请求失败:', err)
      messagesManager.setError('请求失败: ' + err.message)
      eventHandlers.isExecuting.value = false
      eventHandlers.isPlanning.value = false
    }
  }

  /**
   * 处理流式响应
   */
  const processStream = async (response) => {
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = ''

    const readStream = () => {
      return reader.read().then(({ done, value }) => {
        if (done) {
          try {
            eventHandlers.isExecuting.value = false
            eventHandlers.isPlanning.value = false
            // 流式响应结束时，将最后一条消息的 streaming 状态设置为 false
            const lastMessage = messagesManager.messages.value[messagesManager.messages.value.length - 1]
            if (lastMessage && lastMessage.role === 'assistant' && lastMessage.streaming) {
              messagesManager.updateLastAssistantMessage(lastMessage.content, false)
            }
          } catch (error) {
            console.error('处理流式响应结束失败:', error)
          }
          return
        }

        buffer += decoder.decode(value, { stream: true })
        
        // 处理 SSE 格式：data: {...}\n\n
        const chunks = buffer.split('\n\n')
        buffer = chunks.pop() || ''

        for (const chunk of chunks) {
          if (!chunk.trim()) continue
          
          const lines = chunk.split('\n')
          for (const line of lines) {
            if (line.startsWith('data: ')) {
              const data = line.substring(6).trim()
              if (data) {
                try {
                  const event = JSON.parse(data)
                  eventHandlers.handleEvent(event)
                } catch (e) {
                  console.error('解析事件失败:', e, data)
                }
              }
            }
          }
        }

        // 减少滚动频率，只在必要时滚动
        scrollToBottom()
        return readStream()
      })
    }

    await readStream()
  }

  /**
   * 滚动到底部（优化版本，减少滚动频率）
   */
  let scrollTimer = null
  let lastScrollTime = 0
  let isFirstScroll = true
  let rafId = null
  let pendingScroll = false
  
  const scrollToBottom = (immediate = false) => {
    if (messagesContainerRef && messagesContainerRef.value) {
      const container = messagesContainerRef.value
      
      // 清除之前的定时器和动画帧
      if (scrollTimer) {
        clearTimeout(scrollTimer)
        scrollTimer = null
      }
      if (rafId) {
        cancelAnimationFrame(rafId)
        rafId = null
      }
      
      // 立即滚动模式（用户发送消息时）
      if (immediate) {
        rafId = requestAnimationFrame(() => {
          container.scrollTop = container.scrollHeight
          lastScrollTime = Date.now()
          isFirstScroll = false
          rafId = null
        })
        return
      }
      
      // 流式输出时的滚动：使用节流，减少滚动频率
      pendingScroll = true
      const now = Date.now()
      const timeSinceLastScroll = now - lastScrollTime
      
      // 如果距离上次滚动超过 100ms，立即滚动；否则延迟滚动
      if (timeSinceLastScroll > 100) {
        rafId = requestAnimationFrame(() => {
          if (pendingScroll) {
            container.scrollTop = container.scrollHeight
            lastScrollTime = Date.now()
            pendingScroll = false
          }
          rafId = null
        })
      } else {
        scrollTimer = setTimeout(() => {
          rafId = requestAnimationFrame(() => {
            if (pendingScroll) {
              container.scrollTop = container.scrollHeight
              lastScrollTime = Date.now()
              pendingScroll = false
            }
            rafId = null
          })
          scrollTimer = null
        }, 50) // 50ms 节流
      }
    }
  }

  return {
    executeTask,
    scrollToBottom
  }
}

