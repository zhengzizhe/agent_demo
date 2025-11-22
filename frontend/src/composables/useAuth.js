/**
 * 认证管理 Composable
 * 管理用户登录状态、Token 等
 */
import { ref, computed } from 'vue'
import { getToken } from '../utils/api.js'
import * as authApi from '../api/auth.js'

const user = ref(null)
const isAuthenticated = computed(() => {
  return !!getToken() && !!user.value
})

/**
 * 初始化用户信息
 */
export const initAuth = async () => {
  const token = getToken()
  if (!token) {
    return
  }

  try {
    const response = await authApi.getCurrentUser()
    if (response.data) {
      user.value = response.data
    }
  } catch (error) {
    console.error('获取用户信息失败:', error)
    // Token 可能已过期，清除
    if (error.status === 401) {
      await authApi.logout()
    }
  }
}

/**
 * 登录
 */
export const login = async (params) => {
  const response = await authApi.login(params)
  if (response.data) {
    user.value = {
      userUuid: response.data.userUuid,
      email: response.data.email,
      displayName: response.data.displayName,
      isFirstLogin: response.data.isFirstLogin
    }
  }
  return response
}

/**
 * 注册
 */
export const register = async (params) => {
  const response = await authApi.register(params)
  if (response.data) {
    user.value = {
      userUuid: response.data.userUuid,
      email: response.data.email,
      displayName: response.data.displayName,
      isFirstLogin: response.data.isFirstLogin
    }
  }
  return response
}

/**
 * 登出
 */
export const logout = async () => {
  await authApi.logout()
  user.value = null
}

/**
 * 发送验证码
 */
export const sendVerificationCode = authApi.sendVerificationCode

/**
 * 刷新 Token
 */
export const refreshToken = async () => {
  const response = await authApi.refreshToken()
  if (response.data) {
    // Token 已更新
  }
  return response
}

export function useAuth() {
  return {
    user,
    isAuthenticated,
    initAuth,
    login,
    register,
    logout,
    sendVerificationCode,
    refreshToken
  }
}

