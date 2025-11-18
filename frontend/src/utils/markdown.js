/**
 * Markdown 处理工具
 */
import MarkdownIt from 'markdown-it'
import hljs from 'highlight.js'
import 'highlight.js/styles/github-dark.css'
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

  // 自定义代码块渲染 - 飞书风格
  md.renderer.rules.fence = function(tokens, idx, options, env, renderer) {
    const token = tokens[idx]
    const lang = token.info ? token.info.trim().split(/\s+/g)[0] : ''
    const code = token.content
    
    // 使用 highlight.js 进行代码高亮
    let highlightedCode = ''
    let detectedLang = lang || ''
    
    if (lang && hljs.getLanguage(lang)) {
      try {
        const result = hljs.highlight(code, { language: lang, ignoreIllegals: true })
        highlightedCode = result.value
        detectedLang = result.language || lang
      } catch (err) {
        highlightedCode = md.utils.escapeHtml(code)
      }
    } else {
      // 尝试自动检测语言
      try {
        const result = hljs.highlightAuto(code)
        highlightedCode = result.value
        if (result.language) {
          detectedLang = result.language
        }
      } catch (err) {
        highlightedCode = md.utils.escapeHtml(code)
      }
    }
    
    const langClass = detectedLang ? ` class="hljs language-${detectedLang}"` : ' class="hljs"'
    const langLabel = getLangLabel(detectedLang)
    
    // 生成唯一的 ID
    const codeId = `feishu-code-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
    
    // Base64 编码代码内容
    const encodedCode = btoa(unescape(encodeURIComponent(code)))
    
    return `<div class="feishu-code-block-wrapper" data-code-id="${codeId}" data-code-encoded="${encodedCode}" data-lang="${detectedLang}">
      <div class="feishu-code-header">
        <div class="feishu-code-header-left">
          <div class="feishu-code-icon">代码块</div>
          <div class="feishu-code-lang-selector" data-code-id="${codeId}">
            <span class="feishu-code-lang-text">${langLabel}</span>
            <svg class="feishu-code-dropdown-icon" width="12" height="12" viewBox="0 0 12 12" fill="none">
              <path d="M3 4.5L6 7.5L9 4.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            <div class="feishu-code-lang-dropdown" style="display: none;">
              ${getLangOptions(detectedLang)}
            </div>
          </div>
        </div>
        <div class="feishu-code-header-right">
          <div class="feishu-code-divider"></div>
          <button class="feishu-code-action-btn feishu-code-theme" data-code-id="${codeId}" data-theme="dark" title="切换背景">
            <svg class="theme-icon-dark" width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M8 2V4M8 12V14M2 8H4M12 8H14M3.757 3.757L5.172 5.172M10.828 10.828L12.243 12.243M3.757 12.243L5.172 10.828M10.828 5.172L12.243 3.757" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
              <circle cx="8" cy="8" r="3" stroke="currentColor" stroke-width="1.5"/>
            </svg>
            <svg class="theme-icon-light" width="16" height="16" viewBox="0 0 16 16" fill="none" style="display: none;">
              <circle cx="8" cy="8" r="3" fill="currentColor"/>
              <path d="M8 1V3M8 13V15M1 8H3M13 8H15M2.343 2.343L3.757 3.757M12.243 12.243L13.657 13.657M2.343 13.657L3.757 12.243M12.243 3.757L13.657 2.343" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <span>背景</span>
          </button>
          <div class="feishu-code-divider"></div>
          <button class="feishu-code-action-btn feishu-code-wordwrap" data-code-id="${codeId}" title="自动换行">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M2 4h12M2 8h12M2 12h8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <span>自动换行</span>
          </button>
          <div class="feishu-code-divider"></div>
          <button class="feishu-code-action-btn feishu-code-copy" data-code-id="${codeId}" title="复制">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <rect x="5" y="5" width="8" height="8" rx="1" stroke="currentColor" stroke-width="1.5"/>
              <path d="M3 11V3a2 2 0 0 1 2-2h8" stroke="currentColor" stroke-width="1.5" stroke-linecap="round"/>
            </svg>
            <span>复制</span>
          </button>
        </div>
      </div>
      <div class="feishu-code-content" data-code-id="${codeId}">
        <pre class="feishu-code-pre"><code${langClass}>${highlightedCode}</code></pre>
      </div>
    </div>`
  }
  
  // 获取语言显示标签
  function getLangLabel(lang) {
    const langMap = {
      '': 'Plain Text',
      'javascript': 'JavaScript',
      'typescript': 'TypeScript',
      'python': 'Python',
      'java': 'Java',
      'cpp': 'C++',
      'c': 'C',
      'csharp': 'C#',
      'go': 'Go',
      'rust': 'Rust',
      'php': 'PHP',
      'ruby': 'Ruby',
      'swift': 'Swift',
      'kotlin': 'Kotlin',
      'html': 'HTML',
      'css': 'CSS',
      'scss': 'SCSS',
      'json': 'JSON',
      'xml': 'XML',
      'yaml': 'YAML',
      'markdown': 'Markdown',
      'sql': 'SQL',
      'bash': 'Bash',
      'shell': 'Shell',
      'dockerfile': 'Dockerfile',
      'nginx': 'Nginx',
      'vue': 'Vue',
      'react': 'React'
    }
    return langMap[lang] || (lang ? lang.toUpperCase() : 'Plain Text')
  }
  
  // 生成语言选项 HTML
  function getLangOptions(currentLang) {
    const languages = [
      { value: '', label: 'Plain Text' },
      { value: 'javascript', label: 'JavaScript' },
      { value: 'typescript', label: 'TypeScript' },
      { value: 'python', label: 'Python' },
      { value: 'java', label: 'Java' },
      { value: 'cpp', label: 'C++' },
      { value: 'c', label: 'C' },
      { value: 'csharp', label: 'C#' },
      { value: 'go', label: 'Go' },
      { value: 'rust', label: 'Rust' },
      { value: 'php', label: 'PHP' },
      { value: 'ruby', label: 'Ruby' },
      { value: 'swift', label: 'Swift' },
      { value: 'kotlin', label: 'Kotlin' },
      { value: 'html', label: 'HTML' },
      { value: 'css', label: 'CSS' },
      { value: 'scss', label: 'SCSS' },
      { value: 'json', label: 'JSON' },
      { value: 'xml', label: 'XML' },
      { value: 'yaml', label: 'YAML' },
      { value: 'markdown', label: 'Markdown' },
      { value: 'sql', label: 'SQL' },
      { value: 'bash', label: 'Bash' },
      { value: 'shell', label: 'Shell' },
      { value: 'dockerfile', label: 'Dockerfile' },
      { value: 'nginx', label: 'Nginx' },
      { value: 'vue', label: 'Vue' },
      { value: 'react', label: 'React' }
    ]
    
    return languages.map(lang => {
      const active = lang.value === currentLang ? 'active' : ''
      return `<div class="feishu-code-lang-option ${active}" data-lang="${lang.value}">${lang.label}</div>`
    }).join('')
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

  // 自定义图片渲染 - 增强版
  md.renderer.rules.image = function(tokens, idx, options, env, renderer) {
    const token = tokens[idx]
    const src = token.attrGet('src')
    const alt = token.content || token.attrGet('alt') || '图片'
    const title = token.attrGet('title') || ''
    
    return `<div class="image-wrapper">
      <img src="${src}" alt="${alt}" title="${title}" class="markdown-image" loading="lazy" />
      ${alt ? `<div class="image-caption">${alt}</div>` : ''}
    </div>`
  }

  // 自定义表格单元格渲染，支持代码块
  md.renderer.rules.td_open = function(tokens, idx, options, env, renderer) {
    return '<td>'
  }
  
  md.renderer.rules.th_open = function(tokens, idx, options, env, renderer) {
    return '<th>'
  }
  
  // 表格单元格中的代码块需要特殊处理
  // 在渲染前预处理表格中的代码块
  const originalRender = md.render.bind(md)
  md.render = function(src, env) {
    // 先处理表格中的代码块（支持多行）
    // 找到所有表格区域，然后处理其中的代码块
    const tableCodeBlocks = []
    let codeBlockIndex = 0
    
    // 识别表格区域（连续的行，每行以 | 开头和结尾）
    const lines = src.split('\n')
    let tableStart = -1
    let tableEnd = -1
    
    for (let i = 0; i < lines.length; i++) {
      const line = lines[i]
      const trimmed = line.trim()
      const isTableLine = trimmed.startsWith('|') && trimmed.endsWith('|') && trimmed.length > 2
      
      if (isTableLine && tableStart === -1) {
        tableStart = i
      } else if (!isTableLine && tableStart !== -1) {
        tableEnd = i
        // 处理这个表格区域
        const tableText = lines.slice(tableStart, tableEnd).join('\n')
        if (tableText.includes('```')) {
          // 替换表格中的代码块
          const processedTable = tableText.replace(/```(\w+)?\n?([\s\S]*?)```/g, function(codeMatch, lang, codeContent) {
            const placeholder = `__TABLE_CODE_${codeBlockIndex}__`
            tableCodeBlocks.push({ lang: lang || '', code: codeContent.trim() })
            codeBlockIndex++
            return placeholder
          })
          
          // 替换原表格区域
          for (let j = tableStart; j < tableEnd; j++) {
            lines[j] = processedTable.split('\n')[j - tableStart] || lines[j]
          }
        }
        tableStart = -1
        tableEnd = -1
      }
    }
    
    // 处理最后一个表格（如果存在）
    if (tableStart !== -1) {
      const tableText = lines.slice(tableStart).join('\n')
      if (tableText.includes('```')) {
        const processedTable = tableText.replace(/```(\w+)?\n?([\s\S]*?)```/g, function(codeMatch, lang, codeContent) {
          const placeholder = `__TABLE_CODE_${codeBlockIndex}__`
          tableCodeBlocks.push({ lang: lang || '', code: codeContent.trim() })
          codeBlockIndex++
          return placeholder
        })
        
        const processedLines = processedTable.split('\n')
        for (let j = tableStart; j < lines.length; j++) {
          lines[j] = processedLines[j - tableStart] || lines[j]
        }
      }
    }
    
    src = lines.join('\n')
    
    // 处理自定义信息块
    src = src.replace(/:::(\w+)\s*\n([\s\S]*?):::/g, function(match, type, content) {
      const blockTypes = {
        'info': { icon: 'ℹ️', class: 'feishu-info-block', title: '提示' },
        'tip': { icon: '💡', class: 'feishu-tip-block', title: '提示' },
        'success': { icon: '✅', class: 'feishu-success-block', title: '成功' },
        'warning': { icon: '⚠️', class: 'feishu-warning-block', title: '警告' },
        'error': { icon: '❌', class: 'feishu-error-block', title: '错误' },
        'note': { icon: '📝', class: 'feishu-note-block', title: '备注' },
        'question': { icon: '❓', class: 'feishu-question-block', title: '问题' }
      }
      
      const blockType = blockTypes[type.toLowerCase()] || blockTypes['info']
      const renderedContent = originalRender(content.trim(), env)
      return `<div class="${blockType.class}">
        <div class="feishu-block-header">
          <span class="feishu-block-icon">${blockType.icon}</span>
          <span class="feishu-block-title">${blockType.title}</span>
        </div>
        <div class="feishu-block-content">${renderedContent}</div>
      </div>`
    })
    
    // 处理折叠块
    src = src.replace(/<details>([\s\S]*?)<\/details>/g, function(match, content) {
      const summaryMatch = content.match(/<summary>(.*?)<\/summary>/)
      const summary = summaryMatch ? summaryMatch[1] : '点击展开'
      const detailsContent = content.replace(/<summary>.*?<\/summary>/, '')
      const blockId = `collapse-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`
      return `<div class="feishu-collapse-block" data-collapse-id="${blockId}">
        <div class="feishu-collapse-header">
          <svg class="feishu-collapse-icon" width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M6 4L10 8L6 12" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <span class="feishu-collapse-title">${summary}</span>
        </div>
        <div class="feishu-collapse-content">${originalRender(detailsContent.trim(), env)}</div>
      </div>`
    })
    
    // 处理电子表格（使用特殊标记）
    src = src.replace(/```spreadsheet\n([\s\S]*?)```/g, function(match, content) {
      return renderSpreadsheet(content.trim())
    })
    
    // 渲染 markdown
    let html = originalRender(src, env)
    
    // 恢复表格中的代码块并渲染
    html = html.replace(/__TABLE_CODE_(\d+)__/g, function(match, index) {
      const codeBlock = tableCodeBlocks[parseInt(index)]
      if (!codeBlock) return match
      
      const { lang, code } = codeBlock
      
      // 使用 highlight.js 高亮
      let highlightedCode = ''
      if (lang && hljs.getLanguage(lang)) {
        try {
          const result = hljs.highlight(code, { language: lang, ignoreIllegals: true })
          highlightedCode = result.value
        } catch (err) {
          highlightedCode = md.utils.escapeHtml(code)
        }
      } else {
        highlightedCode = md.utils.escapeHtml(code)
      }
      
      const langClass = lang ? ` class="hljs language-${lang}"` : ' class="hljs"'
      return `<code class="table-code-block${langClass}">${highlightedCode}</code>`
    })
    
    return html
  }
  
  // 自定义引用块渲染 - 飞书风格
  md.renderer.rules.blockquote_open = function() {
    return '<div class="feishu-quote-block"><div class="feishu-quote-icon"></div><div class="feishu-quote-content">'
  }
  
  md.renderer.rules.blockquote_close = function() {
    return '</div></div>'
  }
  
  // 自定义分隔线渲染
  md.renderer.rules.hr = function() {
    return '<div class="feishu-divider"><div class="feishu-divider-line"></div></div>'
  }
  
  // 渲染电子表格
  function renderSpreadsheet(content) {
    const lines = content.split('\n').filter(line => line.trim())
    if (lines.length === 0) return ''
    
    const rows = lines.map(line => {
      // 支持制表符分隔或管道符分隔
      const cells = line.split(/\t|\|/).map(cell => cell.trim()).filter(cell => cell)
      return cells
    })
    
    const maxCols = Math.max(...rows.map(row => row.length))
    
    let html = '<div class="feishu-spreadsheet-wrapper"><table class="feishu-spreadsheet">'
    
    rows.forEach((row, rowIndex) => {
      html += '<tr>'
      for (let colIndex = 0; colIndex < maxCols; colIndex++) {
        const cell = row[colIndex] || ''
        const isHeader = rowIndex === 0
        const tag = isHeader ? 'th' : 'td'
        // 使用 markdown-it 的转义工具
        const escapedCell = md.utils.escapeHtml(cell)
        html += `<${tag}>${escapedCell}</${tag}>`
      }
      html += '</tr>'
    })
    
    html += '</table></div>'
    return html
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

