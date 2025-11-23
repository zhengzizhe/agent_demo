/**
 * API 工具类
 * 封装 HTTP 请求，统一处理认证、错误等
 */

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'

/**
 * 获取存储的 Token
 */
const getToken = () => {
  return localStorage.getItem('auth_token')
}

/**
 * 设置 Token
 */
const setToken = (token) => {
  if (token) {
    localStorage.setItem('auth_token', token)
  } else {
    localStorage.removeItem('auth_token')
  }
}

/**
 * 获取请求头
 */
const getHeaders = (customHeaders = {}) => {
  const headers = {
    'Content-Type': 'application/json',
    ...customHeaders
  }

  const token = getToken()
  if (token) {
    headers['Authorization'] = `Bearer ${token}`
  }

  return headers
}

/**
 * 处理响应
 */
const handleResponse = async (response) => {
  const contentType = response.headers.get('content-type')
  const isJson = contentType && contentType.includes('application/json')
  
  const data = isJson ? await response.json() : await response.text()

  if (!response.ok) {
    const error = {
      status: response.status,
      statusText: response.statusText,
      message: data?.message || data?.error || '请求失败',
      data
    }
    throw error
  }

  return data
}

/**
 * GET 请求
 */
export const get = async (url, options = {}) => {
  const response = await fetch(`${API_BASE_URL}${url}`, {
    method: 'GET',
    headers: getHeaders(options.headers),
    ...options
  })
  return handleResponse(response)
}

/**
 * POST 请求
 */
export const post = async (url, data = null, options = {}) => {
  const response = await fetch(`${API_BASE_URL}${url}`, {
    method: 'POST',
    headers: getHeaders(options.headers),
    body: data ? JSON.stringify(data) : null,
    ...options
  })
  return handleResponse(response)
}

/**
 * PUT 请求
 */
export const put = async (url, data = null, options = {}) => {
  const response = await fetch(`${API_BASE_URL}${url}`, {
    method: 'PUT',
    headers: getHeaders(options.headers),
    body: data ? JSON.stringify(data) : null,
    ...options
  })
  return handleResponse(response)
}

/**
 * DELETE 请求
 */
export const del = async (url, options = {}) => {
  const response = await fetch(`${API_BASE_URL}${url}`, {
    method: 'DELETE',
    headers: getHeaders(options.headers),
    ...options
  })
  return handleResponse(response)
}

/**
 * 导出 Token 管理方法
 */
export { getToken, setToken }



