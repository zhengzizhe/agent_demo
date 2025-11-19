/**
 * 会话管理 Composable
 * 管理 userId 和 sessionId，使用 localStorage 持久化
 */
import { ref, onMounted } from 'vue'

const STORAGE_KEY_USER_ID = 'agent_user_id'
const STORAGE_KEY_SESSION_ID = 'agent_session_id'

export function useSession() {
  const userId = ref(null)
  const sessionId = ref(null)

  /**
   * 初始化会话信息
   */
  const initSession = () => {
    // 从 localStorage 读取或生成 userId
    let storedUserId = localStorage.getItem(STORAGE_KEY_USER_ID)
    if (!storedUserId) {
      // 生成新的 userId（使用时间戳 + 随机数）
      storedUserId = 'user_' + Date.now() + '_' + Math.random().toString(36).substr(2, 9)
      localStorage.setItem(STORAGE_KEY_USER_ID, storedUserId)
    }
    userId.value = storedUserId

    // 从 localStorage 读取或生成 sessionId
    let storedSessionId = localStorage.getItem(STORAGE_KEY_SESSION_ID)
    if (!storedSessionId) {
      // 生成新的 sessionId（UUID）
      storedSessionId = generateUUID()
      localStorage.setItem(STORAGE_KEY_SESSION_ID, storedSessionId)
    }
    sessionId.value = storedSessionId
  }

  /**
   * 生成新的会话ID（用于开始新会话）
   */
  const newSession = () => {
    const newSessionId = generateUUID()
    sessionId.value = newSessionId
    localStorage.setItem(STORAGE_KEY_SESSION_ID, newSessionId)
    return newSessionId
  }

  /**
   * 生成 UUID
   */
  const generateUUID = () => {
    return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
      const r = Math.random() * 16 | 0
      const v = c === 'x' ? r : (r & 0x3 | 0x8)
      return v.toString(16)
    })
  }

  /**
   * 设置 userId
   */
  const setUserId = (newUserId) => {
    if (newUserId && newUserId.trim()) {
      userId.value = newUserId.trim()
      localStorage.setItem(STORAGE_KEY_USER_ID, userId.value)
    }
  }

  /**
   * 设置 sessionId
   */
  const setSessionId = (newSessionId) => {
    if (newSessionId && newSessionId.trim()) {
      sessionId.value = newSessionId.trim()
      localStorage.setItem(STORAGE_KEY_SESSION_ID, sessionId.value)
    }
  }

  /**
   * 获取当前会话信息
   */
  const getSessionInfo = () => {
    return {
      userId: userId.value,
      sessionId: sessionId.value
    }
  }

  // 组件挂载时初始化
  onMounted(() => {
    initSession()
  })

  // 立即初始化（不等待挂载）
  initSession()

  return {
    userId,
    sessionId,
    initSession,
    newSession,
    setUserId,
    setSessionId,
    getSessionInfo
  }
}

