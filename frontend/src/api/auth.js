/**
 * 认证相关 API
 */
import { post, get, setToken } from '../utils/api.js'

/**
 * 登录
 * @param {Object} params - 登录参数
 * @param {String} params.loginType - 登录方式: 'email' | 'email_code' | 'oauth'
 * @param {String} params.email - 邮箱（邮箱登录时使用）
 * @param {String} params.password - 密码（邮箱密码登录时使用）
 * @param {String} params.code - 验证码（邮箱验证码登录时使用）
 * @param {String} params.provider - OAuth 提供方（OAuth 登录时使用）
 * @param {String} params.authCode - OAuth 授权码（OAuth 登录时使用）
 * @param {String} params.state - OAuth 状态参数（OAuth 登录时使用）
 * @returns {Promise<Object>} 登录响应
 */
export const login = async (params) => {
  const response = await post('/api/auth/login', params)
  
  // 保存 Token
  if (response.data && response.data.token) {
    setToken(response.data.token)
    // 保存用户信息
    if (response.data.userUuid) {
      localStorage.setItem('user_uuid', response.data.userUuid)
    }
    if (response.data.email) {
      localStorage.setItem('user_email', response.data.email)
    }
    if (response.data.displayName) {
      localStorage.setItem('user_display_name', response.data.displayName)
    }
  }
  
  return response
}

/**
 * 发送验证码
 * @param {Object} params - 发送验证码参数
 * @param {String} params.email - 邮箱
 * @param {String} params.purpose - 用途: 'register' | 'login' | 'reset_password' | 'bind_email'
 * @returns {Promise<Object>} 发送结果
 */
export const sendVerificationCode = async (params) => {
  return await post('/api/auth/verification-code/send', params)
}

/**
 * 注册
 * @param {Object} params - 注册参数
 * @param {String} params.email - 邮箱
 * @param {String} params.code - 验证码
 * @param {String} params.password - 密码
 * @returns {Promise<Object>} 注册响应
 */
export const register = async (params) => {
  const requestParams = {
    loginType: 'email_code',
    email: params.email,
    code: params.code,
    password: params.password
  }
  
  const response = await post('/api/auth/register', requestParams)
  
  // 保存 Token
  if (response.data && response.data.token) {
    setToken(response.data.token)
    if (response.data.userUuid) {
      localStorage.setItem('user_uuid', response.data.userUuid)
    }
    if (response.data.email) {
      localStorage.setItem('user_email', response.data.email)
    }
    if (response.data.displayName) {
      localStorage.setItem('user_display_name', response.data.displayName)
    }
  }
  
  return response
}

/**
 * 刷新 Token
 * @returns {Promise<Object>} 新的 Token
 */
export const refreshToken = async () => {
  const response = await post('/api/auth/refresh-token')
  
  // 更新 Token
  if (response.data && response.data.token) {
    setToken(response.data.token)
  }
  
  return response
}

/**
 * 登出
 * @returns {Promise<Object>} 登出结果
 */
export const logout = async () => {
  try {
    await post('/api/auth/logout')
  } catch (error) {
    console.error('登出请求失败:', error)
  } finally {
    // 清除本地存储的 Token 和用户信息
    setToken(null)
    localStorage.removeItem('user_uuid')
    localStorage.removeItem('user_email')
    localStorage.removeItem('user_display_name')
  }
}

/**
 * 获取当前用户信息
 * @returns {Promise<Object>} 用户信息
 */
export const getCurrentUser = async () => {
  return await get('/api/auth/me')
}



