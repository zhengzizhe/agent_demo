/**
 * 消息工具函数
 */

/**
 * 安全获取消息内容
 */
export const getMessageContent = (msg) => {
  if (!msg) {
    console.warn('消息对象为空')
    return ''
  }
  
  if (msg.content === undefined || msg.content === null) {
    return ''
  }
  
  // 如果 content 是对象，记录警告并转换
  if (typeof msg.content === 'object') {
    console.warn('消息内容是对象类型，正在转换:', {
      role: msg.role,
      content: msg.content,
      contentType: typeof msg.content,
      isArray: Array.isArray(msg.content)
    })
    
    if (msg.content.message && typeof msg.content.message === 'string') {
      return msg.content.message
    }
    if (msg.content.text && typeof msg.content.text === 'string') {
      return msg.content.text
    }
    if (msg.content.content && typeof msg.content.content === 'string') {
      return msg.content.content
    }
    
    // 尝试 JSON 格式化
    try {
      return JSON.stringify(msg.content, null, 2)
    } catch (e) {
      console.error('无法序列化消息内容:', e)
      return '[无法解析的对象]'
    }
  }
  
  // 确保返回字符串
  const result = String(msg.content || '')
  if (result === '[object Object]') {
    console.error('检测到 [object Object]，消息对象:', msg)
    return ''
  }
  
  return result
}

