/**
 * 标签页管理 Composable
 * 实现 Chrome 风格的标签页系统
 */
import { ref, computed } from 'vue'

export function useTabs() {
  // 定义左侧一级功能（固定标签页的视图）
  const fixedViews = ['chat', 'rag', 'docs']
  
  // 获取视图标签
  const getViewLabel = (view) => {
    const labels = {
      chat: '对话',
      rag: 'RAG知识库',
      docs: '文档库'
    }
    return labels[view] || view
  }
  
  // 获取视图图标
  const getViewIcon = (view) => {
    const icons = {
      chat: '💬',
      rag: '📚',
      docs: '📄'
    }
    return icons[view] || '📄'
  }
  
  // 标签页列表 - 只有一个固定标签页（用于左侧一级功能）
  const tabs = ref([
    {
      id: 'main',
      label: getViewLabel('chat'),
      view: 'chat',
      icon: getViewIcon('chat'),
      closable: false, // 固定标签页不可关闭
      isFixed: true,
      history: ['chat'], // 历史记录栈，初始包含chat视图
      historyIndex: 0, // 当前历史记录索引
      isLoading: false // 加载状态
    }
  ])

  // 当前激活的标签页ID（默认激活主标签页）
  const activeTabId = ref('main')

  // 获取当前激活的标签页
  const activeTab = computed(() => {
    return tabs.value.find(tab => tab.id === activeTabId.value) || tabs.value[0]
  })

  // 获取当前视图
  const currentView = computed(() => {
    return activeTab.value?.view || 'chat'
  })

  // 检查是否可以后退
  const canGoBack = computed(() => {
    const tab = activeTab.value
    return tab && tab.historyIndex > 0
  })

  // 检查是否可以前进
  const canGoForward = computed(() => {
    const tab = activeTab.value
    return tab && tab.historyIndex < tab.history.length - 1
  })

  // 添加历史记录
  const addHistory = (tabId, view) => {
    const tab = tabs.value.find(t => t.id === tabId)
    if (!tab) return

    // 如果当前不在历史记录末尾，删除后面的记录
    if (tab.historyIndex < tab.history.length - 1) {
      tab.history = tab.history.slice(0, tab.historyIndex + 1)
    }

    // 添加新的历史记录
    tab.history.push(view)
    tab.historyIndex = tab.history.length - 1
  }

  // 打开新标签页
  // forceNew: 如果为 true，即使 view 是固定视图，也创建新的可关闭标签页
  const openTab = (view, label, icon = '📄', forceNew = false) => {
    // 如果是固定视图且不强制新建，切换到主标签页并更新视图
    if (fixedViews.includes(view) && !forceNew) {
      switchToFixedTab(view)
      return 'main'
    }

    // 如果强制新建，直接创建新标签页
    if (forceNew) {
      const newTab = {
        id: `tab-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
        label: label || getViewLabel(view),
        view: view,
        icon: icon,
        closable: true,
        isFixed: false,
        history: [view],
        historyIndex: 0,
        isLoading: false
      }
      tabs.value.push(newTab)
      activeTabId.value = newTab.id
      return newTab.id
    }

    // 检查是否已存在相同视图的标签页（非固定）
    const existingTab = tabs.value.find(tab => tab.view === view && !tab.isFixed)
    if (existingTab) {
      // 如果已存在，直接激活它
      activeTabId.value = existingTab.id
      addHistory(existingTab.id, view)
      return existingTab.id
    }

    // 创建新标签页（可关闭）
    const newTab = {
      id: `tab-${Date.now()}-${Math.random().toString(36).substr(2, 9)}`,
      label: label || getViewLabel(view),
      view: view,
      icon: icon,
      closable: true,
      isFixed: false,
      history: [view],
      historyIndex: 0,
      isLoading: false
    }

    tabs.value.push(newTab)
    activeTabId.value = newTab.id
    return newTab.id
  }
  
  // 切换到固定标签页（左侧一级功能）- 更新主标签页的视图
  const switchToFixedTab = (view) => {
    if (!fixedViews.includes(view)) {
      console.warn(`View "${view}" is not a fixed view`)
      return
    }
    
    // 找到主标签页并更新其视图、标签和图标
    const mainTab = tabs.value.find(tab => tab.id === 'main')
    if (mainTab) {
      const oldView = mainTab.view
      mainTab.view = view
      mainTab.label = getViewLabel(view)
      mainTab.icon = getViewIcon(view)
      
      // 如果视图改变，添加历史记录
      if (oldView !== view) {
        addHistory('main', view)
      }
    }
    
    // 激活主标签页
    activeTabId.value = 'main'
  }

  // 关闭标签页
  const closeTab = (tabId) => {
    const index = tabs.value.findIndex(tab => tab.id === tabId)
    if (index === -1) return

    const tab = tabs.value[index]
    if (!tab.closable || tab.isFixed) return // 固定标签页不可关闭

    // 如果关闭的是当前激活的标签页，需要切换到其他标签页
    if (tab.id === activeTabId.value) {
      // 优先切换到右侧的标签页，如果没有则切换到左侧
      if (index < tabs.value.length - 1) {
        activeTabId.value = tabs.value[index + 1].id
      } else if (index > 0) {
        activeTabId.value = tabs.value[index - 1].id
      } else {
        activeTabId.value = 'main' // 如果只剩固定标签页，激活主标签页
      }
    }

    tabs.value.splice(index, 1)
  }

  // 切换标签页
  const switchTab = (tabId) => {
    const tab = tabs.value.find(t => t.id === tabId)
    if (tab) {
      activeTabId.value = tabId
    }
  }

  // 后退
  const goBack = () => {
    const tab = activeTab.value
    if (!tab || tab.historyIndex <= 0) return

    tab.historyIndex--
    const view = tab.history[tab.historyIndex]
    tab.view = view
    tab.label = getViewLabel(view)
    tab.icon = getViewIcon(view)
  }

  // 前进
  const goForward = () => {
    const tab = activeTab.value
    if (!tab || tab.historyIndex >= tab.history.length - 1) return

    tab.historyIndex++
    const view = tab.history[tab.historyIndex]
    tab.view = view
    tab.label = getViewLabel(view)
    tab.icon = getViewIcon(view)
  }

  // 刷新标签页
  const refreshTab = (tabId) => {
    const tab = tabs.value.find(t => t.id === tabId)
    if (!tab) return

    // 设置加载状态
    tab.isLoading = true

    // 模拟刷新（实际应用中可能需要重新加载数据）
    setTimeout(() => {
      tab.isLoading = false
      // 触发刷新事件，由组件处理具体刷新逻辑
    }, 300)
  }

  return {
    tabs,
    activeTabId,
    activeTab,
    currentView,
    canGoBack,
    canGoForward,
    openTab,
    closeTab,
    switchTab,
    switchToFixedTab,
    goBack,
    goForward,
    refreshTab,
    fixedViews
  }
}

