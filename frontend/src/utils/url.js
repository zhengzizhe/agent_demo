/**
 * URL 工具函数
 */

/**
 * 提取域名
 */
export const getDomain = (url) => {
  try {
    const urlObj = new URL(url)
    return urlObj.hostname.replace('www.', '')
  } catch {
    return url
  }
}

/**
 * 提取URL信息
 */
export const getUrlInfo = (url) => {
  try {
    const urlObj = new URL(url)
    return {
      domain: urlObj.hostname.replace('www.', ''),
      protocol: urlObj.protocol.replace(':', ''),
      path: urlObj.pathname + urlObj.search
    }
  } catch {
    return {
      domain: url,
      protocol: 'http',
      path: ''
    }
  }
}

