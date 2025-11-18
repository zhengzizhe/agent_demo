/**
 * PDF 导出功能
 */
import html2pdf from 'html2pdf.js'
import html2canvas from 'html2canvas'
import { jsPDF } from 'jspdf'

export function usePdfExport() {
  /**
   * 导出消息为 PDF
   * @param {HTMLElement} containerElement - 包含消息的容器元素
   * @param {string} filename - PDF 文件名（可选）
   */
  const exportToPdf = async (containerElement, filename = 'messages.pdf') => {
    if (!containerElement) {
      console.error('容器元素不存在')
      return
    }

    try {
      // 创建临时容器用于 PDF 导出
      const tempContainer = document.createElement('div')
      tempContainer.style.position = 'absolute'
      tempContainer.style.left = '-9999px'
      tempContainer.style.top = '0'
      tempContainer.style.width = '800px' // A4 宽度（像素）
      tempContainer.style.padding = '40px'
      tempContainer.style.backgroundColor = '#ffffff'
      tempContainer.style.fontFamily = '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif'
      tempContainer.style.color = '#353740'
      
      // 克隆消息内容
      const clonedContent = containerElement.cloneNode(true)
      
      // 移除不需要的元素（如输入框、滚动条等）
      const elementsToRemove = clonedContent.querySelectorAll('.input-container, .typing-indicator, .streaming-indicator, .welcome-message, .loading-message')
      elementsToRemove.forEach(el => el.remove())
      
      // 展开所有折叠块
      const collapseBlocks = clonedContent.querySelectorAll('.feishu-collapse-block')
      collapseBlocks.forEach(block => {
        block.classList.remove('collapsed')
        const content = block.querySelector('.feishu-collapse-content')
        if (content) {
          content.style.display = 'block'
        }
      })
      
      // 确保代码块使用浅色主题（PDF 中深色主题可能显示不佳）
      const codeBlocks = clonedContent.querySelectorAll('.feishu-code-block-wrapper')
      codeBlocks.forEach(block => {
        block.classList.remove('theme-dark')
        block.classList.add('theme-light')
        const content = block.querySelector('.feishu-code-content')
        if (content) {
          content.style.background = '#f7f8fa'
          content.style.color = '#1d2129'
        }
      })
      
      // 处理样式，确保在 PDF 中正确显示
      const style = document.createElement('style')
      style.textContent = `
        * {
          box-sizing: border-box;
        }
        body {
          margin: 0;
          padding: 0;
          font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
          color: #353740;
          background: #ffffff;
        }
        .message {
          margin-bottom: 24px;
          page-break-inside: avoid;
        }
        .message-card {
          page-break-inside: avoid;
        }
        table {
          page-break-inside: avoid;
        }
        .feishu-code-block-wrapper {
          page-break-inside: avoid;
        }
        .feishu-table-wrapper {
          page-break-inside: avoid;
        }
        .feishu-spreadsheet-wrapper {
          page-break-inside: avoid;
        }
        .feishu-info-block,
        .feishu-tip-block,
        .feishu-success-block,
        .feishu-warning-block,
        .feishu-error-block,
        .feishu-note-block,
        .feishu-question-block {
          page-break-inside: avoid;
        }
        .feishu-collapse-block {
          page-break-inside: avoid;
        }
        .feishu-collapse-content {
          display: block !important;
        }
        img {
          max-width: 100%;
          height: auto;
          page-break-inside: avoid;
        }
        .task-list {
          page-break-inside: avoid;
        }
      `
      tempContainer.appendChild(style)
      tempContainer.appendChild(clonedContent)
      
      document.body.appendChild(tempContainer)
      
      // 等待内容渲染完成
      await new Promise(resolve => setTimeout(resolve, 500))
      
      // PDF 配置选项
      const options = {
        margin: [10, 10, 10, 10],
        filename: filename,
        image: { type: 'jpeg', quality: 0.98 },
        html2canvas: { 
          scale: 2,
          useCORS: true,
          logging: false,
          backgroundColor: '#ffffff',
          windowWidth: 800,
          windowHeight: clonedContent.scrollHeight
        },
        jsPDF: { 
          unit: 'mm', 
          format: 'a4', 
          orientation: 'portrait',
          compress: true
        },
        pagebreak: { 
          mode: ['avoid-all', 'css', 'legacy'],
          before: '.page-break-before',
          after: '.page-break-after',
          avoid: ['.message', '.message-card', 'table', '.feishu-code-block-wrapper', '.task-list']
        }
      }
      
      // 生成 PDF
      await html2pdf().set(options).from(tempContainer).save()
      
      // 清理临时元素
      document.body.removeChild(tempContainer)
      
      return true
    } catch (error) {
      console.error('导出 PDF 失败:', error)
      throw error
    }
  }

  /**
   * 导出所有消息为 PDF
   * @param {HTMLElement} messagesContainer - 消息容器元素
   * @param {string} filename - PDF 文件名（可选）
   */
  const exportAllMessages = async (messagesContainer, filename) => {
    if (!filename) {
      const date = new Date().toISOString().split('T')[0]
      filename = `messages-${date}.pdf`
    }
    return await exportToPdf(messagesContainer, filename)
  }

  /**
   * 导出单条消息为 PDF
   * @param {HTMLElement} messageElement - 单条消息元素（通常是 .message-card）
   * @param {string} filename - PDF 文件名（可选）
   */
  const exportSingleMessage = async (messageElement, filename) => {
    if (!messageElement) {
      console.error('消息元素不存在')
      return
    }
    
    if (!filename) {
      const date = new Date().toISOString().split('T')[0]
      const timestamp = Date.now()
      filename = `message-${date}-${timestamp}.pdf`
    }
    
    try {
      // 克隆元素，避免影响原始元素
      const clonedElement = messageElement.cloneNode(true)
      
      // 展开所有折叠块
      const collapseBlocks = clonedElement.querySelectorAll('.feishu-collapse-block')
      collapseBlocks.forEach(block => {
        block.classList.remove('collapsed')
        const content = block.querySelector('.feishu-collapse-content')
        if (content) {
          content.style.display = 'block'
          content.style.maxHeight = 'none'
        }
      })
      
      // 确保代码块使用浅色主题
      const codeBlocks = clonedElement.querySelectorAll('.feishu-code-block-wrapper')
      codeBlocks.forEach(block => {
        block.classList.remove('theme-dark')
        block.classList.add('theme-light')
        const content = block.querySelector('.feishu-code-content')
        if (content) {
          content.style.background = '#f7f8fa'
          content.style.color = '#1d2129'
        }
      })
      
      // 获取原始元素尺寸
      const rect = messageElement.getBoundingClientRect()
      
      // 创建临时容器，优化PDF布局
      const tempContainer = document.createElement('div')
      tempContainer.style.position = 'fixed'
      tempContainer.style.left = '0px'
      tempContainer.style.top = '0px'
      tempContainer.style.width = '800px' // 固定宽度，适合A4
      tempContainer.style.padding = '60px 80px' // 增加内边距，更美观
      tempContainer.style.zIndex = '-9999'
      tempContainer.style.opacity = '0'
      tempContainer.style.pointerEvents = 'none'
      tempContainer.style.backgroundColor = '#ffffff'
      tempContainer.style.overflow = 'visible'
      tempContainer.style.fontFamily = '-apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif'
      
      // 添加PDF专用样式
      const pdfStyle = document.createElement('style')
      pdfStyle.textContent = `
        .message-card {
          background: #ffffff !important;
          border-radius: 0 !important;
          padding: 0 !important;
          box-shadow: none !important;
          border: none !important;
        }
        .message-text {
          color: #1a1a1a !important;
          font-size: 14px !important;
          line-height: 1.8 !important;
          max-width: 100% !important;
        }
        .message-text p {
          margin: 12px 0 !important;
          color: #1a1a1a !important;
        }
        .message-text h1 {
          font-size: 24px !important;
          font-weight: 700 !important;
          margin: 24px 0 16px 0 !important;
          color: #1a1a1a !important;
          border-bottom: 2px solid #2196f3 !important;
          padding-bottom: 8px !important;
        }
        .message-text h2 {
          font-size: 20px !important;
          font-weight: 600 !important;
          margin: 20px 0 12px 0 !important;
          color: #1a1a1a !important;
        }
        .message-text h3 {
          font-size: 18px !important;
          font-weight: 600 !important;
          margin: 16px 0 10px 0 !important;
          color: #1a1a1a !important;
        }
        .message-text ul, .message-text ol {
          margin: 16px 0 !important;
          padding-left: 0 !important;
          list-style: none !important;
        }
        .message-text ul li {
          margin: 10px 0 !important;
          padding-left: 24px !important;
          line-height: 1.8 !important;
          color: #1a1a1a !important;
          position: relative !important;
        }
        .message-text ul li::before {
          content: "•" !important;
          position: absolute !important;
          left: 8px !important;
          color: #2196f3 !important;
          font-weight: bold !important;
          font-size: 18px !important;
        }
        .message-text ol {
          counter-reset: list-counter !important;
        }
        .message-text ol li {
          margin: 10px 0 !important;
          padding-left: 32px !important;
          line-height: 1.8 !important;
          color: #1a1a1a !important;
          position: relative !important;
          counter-increment: list-counter !important;
        }
        .message-text ol li::before {
          content: counter(list-counter) "." !important;
          position: absolute !important;
          left: 8px !important;
          color: #2196f3 !important;
          font-weight: 600 !important;
          font-size: 14px !important;
        }
        .message-text ul ul, .message-text ol ol {
          margin: 8px 0 !important;
          padding-left: 0 !important;
        }
        .message-text ul ul li::before {
          content: "◦" !important;
          color: #646a73 !important;
          font-size: 16px !important;
        }
        .message-text ul ul ul li::before {
          content: "▪" !important;
          color: #8e8ea0 !important;
          font-size: 14px !important;
        }
        .message-text code {
          background: #f5f5f5 !important;
          padding: 2px 6px !important;
          border-radius: 3px !important;
          font-size: 13px !important;
          color: #d63384 !important;
        }
        .feishu-code-block-wrapper {
          margin: 16px 0 !important;
          border: 1px solid #e5e6eb !important;
          border-radius: 8px !important;
          overflow: hidden !important;
        }
        .feishu-code-content {
          background: #f7f8fa !important;
          padding: 16px !important;
        }
        table {
          width: 100% !important;
          border-collapse: collapse !important;
          margin: 16px 0 !important;
        }
        table th {
          background: #f8f9fa !important;
          padding: 12px !important;
          text-align: left !important;
          font-weight: 600 !important;
          border: 1px solid #e5e6eb !important;
        }
        table td {
          padding: 10px 12px !important;
          border: 1px solid #e5e6eb !important;
        }
        .feishu-info-block,
        .feishu-tip-block,
        .feishu-success-block,
        .feishu-warning-block,
        .feishu-error-block,
        .feishu-note-block,
        .feishu-question-block {
          margin: 16px 0 !important;
          padding: 16px !important;
          border-radius: 8px !important;
          border-left: 4px solid !important;
        }
        .feishu-quote-block {
          margin: 16px 0 !important;
          padding: 12px 16px !important;
          border-left: 4px solid #2196f3 !important;
          background: #f8f9fa !important;
        }
      `
      tempContainer.appendChild(pdfStyle)
      
      // 设置克隆元素的样式
      clonedElement.style.width = '100%'
      clonedElement.style.maxWidth = '100%'
      clonedElement.style.margin = '0'
      clonedElement.style.padding = '0'
      clonedElement.style.position = 'relative'
      clonedElement.style.background = 'transparent'
      
      tempContainer.appendChild(clonedElement)
      document.body.appendChild(tempContainer)
      
      // 等待布局稳定
      await new Promise(resolve => setTimeout(resolve, 300))
      
      // 等待样式应用
      await new Promise(resolve => setTimeout(resolve, 200))
      
      // 使用 html2canvas 捕获整个容器，确保包含所有样式
      const canvas = await html2canvas(tempContainer, {
        scale: 1.5, // 降低scale从2到1.5，减小文件大小
        useCORS: true,
        logging: false,
        backgroundColor: '#ffffff',
        width: tempContainer.scrollWidth || tempContainer.offsetWidth,
        height: tempContainer.scrollHeight || tempContainer.offsetHeight,
        windowWidth: tempContainer.scrollWidth || tempContainer.offsetWidth,
        windowHeight: tempContainer.scrollHeight || tempContainer.offsetHeight,
        scrollX: 0,
        scrollY: 0
      })
      
      // 清理临时容器
      document.body.removeChild(tempContainer)
      
      // 检查canvas是否有内容
      if (!canvas || canvas.width === 0 || canvas.height === 0) {
        throw new Error('Canvas生成失败，内容为空')
      }
      
      // 使用JPEG格式，质量0.85，大幅减小文件大小（从PNG改为JPEG，文件大小可减少80-90%）
      const imgData = canvas.toDataURL('image/jpeg', 0.85)
      
      const pdf = new jsPDF({
        unit: 'mm',
        format: 'a4',
        orientation: 'portrait'
      })
      
      const imgWidth = 210 // A4 width in mm
      const pageHeight = 297 // A4 height in mm
      const imgHeight = (canvas.height * imgWidth) / canvas.width
      
      // 如果内容高度小于一页，直接添加
      if (imgHeight <= pageHeight) {
        pdf.addImage(imgData, 'JPEG', 0, 0, imgWidth, imgHeight)
      } else {
        // 多页处理
        let heightLeft = imgHeight
        let position = 0
        
        pdf.addImage(imgData, 'JPEG', 0, position, imgWidth, imgHeight)
        heightLeft -= pageHeight
        
        while (heightLeft > 0) {
          position = heightLeft - imgHeight
          pdf.addPage()
          pdf.addImage(imgData, 'JPEG', 0, position, imgWidth, imgHeight)
          heightLeft -= pageHeight
        }
      }
      
      // 保存PDF
      pdf.save(filename)
      
      // 等待PDF保存完成
      await new Promise(resolve => setTimeout(resolve, 300))
      
      return true
    } catch (error) {
      console.error('导出单条消息 PDF 失败:', error)
      console.error('错误堆栈:', error.stack)
      throw error
    }
  }

  return {
    exportToPdf,
    exportAllMessages,
    exportSingleMessage
  }
}

