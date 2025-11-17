/**
 * 任务执行 Composable
 */
import { ref, nextTick } from 'vue'

export function useTaskExecution(messagesManager, eventHandlers, formRef, messagesContainerRef) {

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

    scrollToBottom()

    try {
      const response = await fetch('/task/execute', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({
          orchestratorId: form.orchestratorId,
          message: userMessage
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
          eventHandlers.isExecuting.value = false
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

        scrollToBottom()
        return readStream()
      })
    }

    await readStream()
  }

  /**
   * 滚动到底部
   */
  const scrollToBottom = () => {
    nextTick(() => {
      if (messagesContainerRef && messagesContainerRef.value) {
        messagesContainerRef.value.scrollTop = messagesContainerRef.value.scrollHeight
      }
    })
  }

  return {
    executeTask,
    scrollToBottom
  }
}

