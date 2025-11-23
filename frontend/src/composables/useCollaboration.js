import { ref, onMounted, onUnmounted } from 'vue'

/**
 * 协同编辑功能
 * 使用 WebSocket 实现实时协同编辑
 */
export function useCollaboration(documentId, enabled = false) {
  const collaborators = ref([])
  const isConnected = ref(false)
  const ws = ref(null)
  const reconnectAttempts = ref(0)
  const maxReconnectAttempts = 5
  const collaborationEnabled = ref(enabled)

  // 连接 WebSocket
  const connect = () => {
    if (!collaborationEnabled.value) {
      console.log('协同编辑已禁用，跳过 WebSocket 连接')
      return
    }
    try {
      const wsUrl = import.meta.env.VITE_WS_URL || `ws://localhost:8080/ws/document/${documentId}`
      ws.value = new WebSocket(wsUrl)

      ws.value.onopen = () => {
        isConnected.value = true
        reconnectAttempts.value = 0
        console.log('WebSocket 连接已建立')
      }

      ws.value.onmessage = (event) => {
        const data = JSON.parse(event.data)
        handleMessage(data)
      }

      ws.value.onerror = (error) => {
        console.error('WebSocket 错误:', error)
      }

      ws.value.onclose = () => {
        isConnected.value = false
        console.log('WebSocket 连接已关闭')
        
        // 尝试重连
        if (reconnectAttempts.value < maxReconnectAttempts) {
          reconnectAttempts.value++
          setTimeout(() => {
            console.log(`尝试重连 (${reconnectAttempts.value}/${maxReconnectAttempts})...`)
            connect()
          }, 1000 * reconnectAttempts.value)
        }
      }
    } catch (error) {
      console.error('WebSocket 连接失败:', error)
    }
  }

  // 处理接收到的消息
  const handleMessage = (data) => {
    switch (data.type) {
      case 'collaborator_joined':
        addCollaborator(data.user)
        break
      case 'collaborator_left':
        removeCollaborator(data.userId)
        break
      case 'content_update':
        // 触发内容更新事件
        if (data.content) {
          return data.content
        }
        break
      case 'cursor_update':
        // 更新其他用户的光标位置
        updateCursor(data.userId, data.position)
        break
      case 'comment_added':
        // 添加评论
        return { type: 'comment', data: data.comment }
        break
      default:
        console.log('未知消息类型:', data.type)
    }
  }

  // 发送消息
  const sendMessage = (type, data) => {
    if (!collaborationEnabled.value) {
      console.log('协同编辑已禁用，不发送消息')
      return
    }
    
    if (ws.value && ws.value.readyState === WebSocket.OPEN) {
      const message = {
        type,
        documentId,
        ...data,
        timestamp: Date.now()
      }
      ws.value.send(JSON.stringify(message))
    }
  }
  
  // 启用/禁用协同编辑
  const setEnabled = (enabled) => {
    collaborationEnabled.value = enabled
    if (enabled) {
      connect()
    } else {
      disconnect()
    }
  }

  // 发送内容更新
  const sendContentUpdate = (content) => {
    sendMessage('content_update', { content })
  }

  // 发送光标位置
  const sendCursorUpdate = (position) => {
    sendMessage('cursor_update', { position })
  }

  // 添加协作者
  const addCollaborator = (user) => {
    if (!collaborators.value.find(c => c.id === user.id)) {
      collaborators.value.push({
        ...user,
        color: generateColor(user.id)
      })
    }
  }

  // 移除协作者
  const removeCollaborator = (userId) => {
    const index = collaborators.value.findIndex(c => c.id === userId)
    if (index > -1) {
      collaborators.value.splice(index, 1)
    }
  }

  // 生成用户颜色
  const generateColor = (userId) => {
    const colors = [
      '#165dff', '#10b981', '#f59e0b', '#ef4444',
      '#8b5cf6', '#06b6d4', '#ec4899', '#14b8a6'
    ]
    const index = parseInt(userId) % colors.length
    return colors[index]
  }

  // 更新光标位置（用于显示其他用户的光标）
  const updateCursor = (userId, position) => {
    // TODO: 在编辑器中显示其他用户的光标
    console.log(`用户 ${userId} 的光标位置:`, position)
  }

  // 断开连接
  const disconnect = () => {
    if (ws.value) {
      ws.value.close()
      ws.value = null
    }
    collaborators.value = []
    isConnected.value = false
  }

  onMounted(() => {
    if (collaborationEnabled.value) {
      connect()
    }
  })

  onUnmounted(() => {
    disconnect()
  })

  return {
    collaborators,
    isConnected,
    collaborationEnabled,
    connect,
    disconnect,
    sendContentUpdate,
    sendCursorUpdate,
    sendMessage,
    setEnabled
  }
}

