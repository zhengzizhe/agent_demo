/**
 * Markdown 处理工具
 */
import MarkdownIt from 'markdown-it'
import { getUrlInfo } from './url.js'

/**
 * 初始化 markdown-it 实例
 */
export const createMarkdownRenderer = () => {
  // 为每个渲染器实例创建独立的链接信息映射
  const linkInfoMap = new Map()
  
  const md = new MarkdownIt({
    html: true,
    breaks: true,
    linkify: true,
    typographer: true,
  })

  // 自定义代码块渲染
  md.renderer.rules.fence = function(tokens, idx, options, env, renderer) {
    const token = tokens[idx]
    const lang = token.info ? token.info.trim().split(/\s+/g)[0] : ''
    const langClass = lang ? ` class="language-${lang}"` : ''
    const code = md.utils.escapeHtml(token.content)
    return `<pre class="code-block"><code${langClass}>${code}</code></pre>`
  }

  // 自定义行内代码渲染
  md.renderer.rules.code_inline = function(tokens, idx, options, env, renderer) {
    const token = tokens[idx]
    return `<code class="inline-code">${md.utils.escapeHtml(token.content)}</code>`
  }

  // 自定义链接渲染 - 卡片样式
  md.renderer.rules.link_open = function(tokens, idx, options, env, renderer) {
    const token = tokens[idx]
    const href = token.attrGet('href')
    const urlInfo = getUrlInfo(href)
    
    // 收集链接文本
    let linkText = ''
    for (let i = idx + 1; i < tokens.length; i++) {
      if (tokens[i].type === 'link_close') break
      if (tokens[i].type === 'text') {
        linkText += tokens[i].content
      }
    }
    if (!linkText || linkText.trim() === '') {
      linkText = urlInfo.domain
    }
    
    // 存储链接信息到当前渲染器的映射中
    linkInfoMap.set(idx, { href, urlInfo, linkText: linkText.trim() })
    
    return `<div class="link-card-wrapper"><a href="${href}" target="_blank" rel="noopener noreferrer" class="link-card"><span class="link-card-title">`
  }

  md.renderer.rules.link_close = function(tokens, idx, options, env, renderer) {
    // 找到对应的 link_open token
    let openIdx = idx - 1
    while (openIdx >= 0 && tokens[openIdx].type !== 'link_open') {
      openIdx--
    }
    
    if (openIdx >= 0 && linkInfoMap.has(openIdx)) {
      const linkInfo = linkInfoMap.get(openIdx)
      const { urlInfo } = linkInfo
      
      // 清理存储
      linkInfoMap.delete(openIdx)
      
      return `</span><div class="link-card-meta"><span class="link-card-domain">${urlInfo.domain}</span><svg class="link-card-icon" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M18 13v6a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2h6"></path><polyline points="15 3 21 3 21 9"></polyline><line x1="10" y1="14" x2="21" y2="3"></line></svg></div></a></div>`
    }
    
    return '</span></a></div>'
  }

  // 将 linkInfoMap 附加到 md 实例上，以便 formatMessage 可以访问
  md._linkInfoMap = linkInfoMap

  return md
}

/**
 * 格式化消息内容
 */
export const formatMessage = (content, md) => {
  // 清理链接信息映射（使用渲染器实例的映射）
  if (md._linkInfoMap) {
    md._linkInfoMap.clear()
  }
  
  if (content == null) return ''
  
  let textContent = ''
  
  // 处理字符串
  if (typeof content === 'string') {
    textContent = content.trim()
    if (!textContent) return ''
  } 
  // 处理对象类型
  else if (typeof content === 'object') {
    if (Array.isArray(content)) {
      textContent = content.map(item => {
        if (typeof item === 'string') return item
        if (typeof item === 'object' && item != null) {
          return item.message || item.text || JSON.stringify(item, null, 2)
        }
        return String(item)
      }).join('\n')
    } else {
      if (content.message && typeof content.message === 'string') {
        textContent = content.message
      } else if (content.text && typeof content.text === 'string') {
        textContent = content.text
      } else if (content.content && typeof content.content === 'string') {
        textContent = content.content
      } else {
        try {
          textContent = JSON.stringify(content, null, 2)
        } catch (e) {
          textContent = '[无法解析的对象]'
        }
      }
    }
  } else {
    textContent = String(content)
  }
  
  // 最终检查
  if (!textContent || textContent === 'null' || textContent === 'undefined' || textContent === '[object Object]') {
    return ''
  }
  
  // 使用 markdown-it 渲染
  try {
    const html = md.render(textContent)
    return html || ''
  } catch (error) {
    console.error('Markdown 渲染失败:', error, '原始内容:', textContent)
    return textContent
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/\n/g, '<br>')
  }
}

