<template>
  <div 
    v-if="tasks && Array.isArray(tasks) && tasks.length > 0" 
    class="message-task-box"
    :style="{ animationDelay: `${animationDelay}s` }"
  >
    <div class="task-box-header">
      <div class="task-box-title-wrapper">
        <span class="task-box-icon">📋</span>
        <span class="task-box-title">任务列表</span>
      </div>
      <div class="task-box-header-right">
        <span class="task-box-count">
          <span class="count-number">{{ tasks.length }}</span>
          <span class="count-label">个任务</span>
        </span>
        <div class="view-toggle">
          <button
            class="view-toggle-btn"
            :class="{ active: viewMode === 'list' }"
            @click="viewMode = 'list'"
            title="列表视图"
          >
            <span class="toggle-icon">☰</span>
          </button>
          <button
            class="view-toggle-btn"
            :class="{ active: viewMode === 'chart' }"
            @click="viewMode = 'chart'"
            title="图状视图"
          >
            <span class="toggle-icon">⊞</span>
          </button>
        </div>
      </div>
    </div>
    <div class="task-box-content">
      <!-- 列表视图 -->
      <div v-if="viewMode === 'list'" class="task-list-view">
        <div 
          v-for="(task, taskIndex) in tasks" 
          :key="task.id"
          :class="['task-chip', getTaskStatusClass(task.status)]"
          :style="{ animationDelay: `${animationDelay + (taskIndex * 0.05)}s` }"
        >
          <span v-if="isTaskRunning(task.status)" class="chip-spinner"></span>
          <span class="chip-title">{{ task.title || task.description || task.id }}</span>
          <span class="chip-status">{{ getTaskStatusIcon(task.status) }}</span>
        </div>
      </div>
      
      <!-- 图状视图 -->
      <div v-else class="task-graph-view">
        <div ref="graphContainer" class="graph-container"></div>
        <!-- 任务详情 Tooltip -->
        <div 
          v-if="hoveredTask"
          ref="taskTooltip"
          class="task-tooltip"
          :style="tooltipStyle"
        >
          <div class="tooltip-header">
            <div class="tooltip-status-badge" :class="`status-${hoveredTask.status?.toLowerCase()}`">
              <span class="status-icon">{{ getTaskStatusIcon(hoveredTask.status) }}</span>
              <span class="status-text">{{ getStatusLabel(hoveredTask.status) }}</span>
            </div>
            <div class="tooltip-close" @click="hoveredTask = null">×</div>
          </div>
          <div class="tooltip-content">
            <div class="tooltip-section">
              <div class="tooltip-label">任务标题</div>
              <div class="tooltip-value">{{ hoveredTask.title || hoveredTask.description || hoveredTask.id }}</div>
            </div>
            <div v-if="hoveredTask.description && hoveredTask.description !== hoveredTask.title" class="tooltip-section">
              <div class="tooltip-label">任务描述</div>
              <div class="tooltip-value">{{ hoveredTask.description }}</div>
            </div>
            <div v-if="hoveredTask.inputs?.fromTask && hoveredTask.inputs.fromTask.length > 0" class="tooltip-section">
              <div class="tooltip-label">依赖任务</div>
              <div class="tooltip-value">
                <span 
                  v-for="(depId, idx) in hoveredTask.inputs.fromTask" 
                  :key="depId"
                  class="tooltip-tag"
                >
                  {{ getTaskTitleById(depId) || depId }}<span v-if="idx < hoveredTask.inputs.fromTask.length - 1">, </span>
                </span>
              </div>
            </div>
            <div v-if="hoveredTask.inputs?.fromUser" class="tooltip-section">
              <div class="tooltip-label">输入来源</div>
              <div class="tooltip-value tooltip-tag user-input">用户输入</div>
            </div>
            <div v-if="hoveredTask.executionStrategy" class="tooltip-section">
              <div class="tooltip-label">执行策略</div>
              <div class="tooltip-value tooltip-tag strategy-tag">{{ hoveredTask.executionStrategy }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { watch, onMounted, ref, computed, nextTick, onBeforeUnmount } from 'vue'
import { getTaskStatusClass, getTaskStatusIcon, isTaskRunning } from '../utils/task.js'
import { Network } from 'vis-network'
import 'vis-network/styles/vis-network.css'

const props = defineProps({
  tasks: {
    type: Array,
    default: () => []
  },
  animationDelay: {
    type: Number,
    default: 0
  }
})

const viewMode = ref('list') // 'list' 或 'chart'
const graphContainer = ref(null)
let network = null
let updateGraphThrottleTimer = null // 节流定时器

// Tooltip 相关
const hoveredTask = ref(null)
const taskTooltip = ref(null)
const tooltipStyle = ref({})
let hideTooltipTimer = null // 用于清除延迟隐藏的定时器

// 更新 tooltip 位置
const updateTooltipPosition = (event) => {
  if (!taskTooltip.value || !graphContainer.value) return
  
  nextTick(() => {
    const containerRect = graphContainer.value.getBoundingClientRect()
    const tooltipRect = taskTooltip.value.getBoundingClientRect()
    const mouseX = event.clientX || (event.touches && event.touches[0]?.clientX) || 0
    const mouseY = event.clientY || (event.touches && event.touches[0]?.clientY) || 0
    
    // 计算 tooltip 位置（鼠标右侧，垂直居中）
    let left = mouseX - containerRect.left + 20 // 鼠标右侧 20px
    let top = mouseY - containerRect.top - tooltipRect.height / 2 // 垂直居中
    
    // 边界检查：确保 tooltip 不超出容器
    if (left + tooltipRect.width > containerRect.width) {
      left = mouseX - containerRect.left - tooltipRect.width - 20 // 显示在鼠标左侧
    }
    if (top < 0) {
      top = 10 // 顶部对齐
    }
    if (top + tooltipRect.height > containerRect.height) {
      top = containerRect.height - tooltipRect.height - 10 // 底部对齐
    }
    
    tooltipStyle.value = {
      left: `${left}px`,
      top: `${top}px`,
      opacity: 1,
      visibility: 'visible'
    }
  })
}

// 监听鼠标移动，更新 tooltip 位置
const handleMouseMove = (event) => {
  if (hoveredTask.value) {
    updateTooltipPosition(event)
  }
}

// 获取状态颜色 - 改进为更美观的渐变和颜色
const getStatusColor = (status) => {
  const colors = {
    'PENDING': { 
      background: 'linear-gradient(135deg, #f5f5f5 0%, #e8e8e8 100%)', 
      border: '#d1d1d1', 
      font: '#666',
      shadow: 'rgba(0,0,0,0.1)'
    },
    'RUNNING': { 
      background: 'linear-gradient(135deg, #2196f3 0%, #42a5f5 50%, #64b5f6 100%)', 
      border: '#1565c0', 
      font: '#ffffff',
      shadow: 'rgba(33, 150, 243, 0.3)'
    },
    'DONE': { 
      background: 'linear-gradient(135deg, #4caf50 0%, #66bb6a 50%, #81c784 100%)', 
      border: '#2e7d32', 
      font: '#ffffff',
      shadow: 'rgba(76, 175, 80, 0.25)'
    },
    'FAILED': { 
      background: 'linear-gradient(135deg, #f44336 0%, #e57373 50%, #ef5350 100%)', 
      border: '#c62828', 
      font: '#ffffff',
      shadow: 'rgba(244, 67, 54, 0.25)'
    },
    'SKIPPED': { 
      background: 'linear-gradient(135deg, #e0e0e0 0%, #bdbdbd 100%)', 
      border: '#9e9e9e', 
      font: '#616161',
      shadow: 'rgba(0,0,0,0.1)'
    }
  }
  return colors[status?.toUpperCase()] || colors['PENDING']
}

// 构建图形数据
const buildGraphData = () => {
  const nodes = []
  const edges = []
  
  props.tasks.forEach(task => {
    const status = task.status?.toUpperCase() || 'PENDING'
    const colors = getStatusColor(status)
    const label = task.title || task.description || task.id
    const isRunning = status === 'RUNNING'
    const isDone = status === 'DONE'
    const isFailed = status === 'FAILED'
    
    // 创建丰富的SVG图标节点 - 使用image形状配合SVG数据URL
    const statusIcon = isRunning ? '⚙' : (isDone ? '✓' : (isFailed ? '✕' : '○'))
    const bgColor = isRunning 
      ? '#2196f3' // 蓝色主题
      : (isDone 
        ? '#4caf50' // 绿色主题（已完成）
        : (isFailed 
          ? '#f44336' // 红色主题（失败）
          : '#f5f5f5')) // 浅灰色主题（待执行）
    
    const borderColor = isRunning 
      ? '#1565c0' // 蓝色边框
      : (isDone 
        ? '#2e7d32' // 绿色边框（已完成）
        : (isFailed 
          ? '#c62828' // 红色边框（失败）
          : '#d1d1d1')) // 浅灰色边框（待执行）
    
    const textColor = isRunning || isDone || isFailed ? '#ffffff' : '#333333' // 待执行状态用深色文字
    const shortLabel = label.length > 14 ? label.substring(0, 14) + '...' : label
    
    // 创建SVG数据URL - 圆形节点
    const svgSize = 140
    const radius = (svgSize - 8) / 2
    const centerX = svgSize / 2
    const centerY = svgSize / 2
    
    // 确保SVG是正方形，这样circularImage才能正确显示为圆形
    const svgContent = `<svg width="${svgSize}" height="${svgSize}" xmlns="http://www.w3.org/2000/svg">
<defs>
<linearGradient id="grad-${task.id}" x1="0%" y1="0%" x2="100%" y2="100%">
<stop offset="0%" style="stop-color:${bgColor};stop-opacity:1" />
            <stop offset="100%" style="stop-color:${isRunning ? '#42a5f5' : (isDone ? '#66bb6a' : (isFailed ? '#e57373' : '#e8e8e8'))};stop-opacity:1" />
</linearGradient>
<filter id="shadow-${task.id}">
<feGaussianBlur in="SourceAlpha" stdDeviation="2"/>
<feOffset dx="0" dy="1" result="offsetblur"/>
<feComponentTransfer>
<feFuncA type="linear" slope="0.15"/>
</feComponentTransfer>
<feMerge>
<feMergeNode/>
<feMergeNode in="SourceGraphic"/>
</feMerge>
</filter>
</defs>
<circle cx="${centerX}" cy="${centerY}" r="${radius - 2}" fill="url(#grad-${task.id})" stroke="${borderColor}" stroke-width="${isRunning ? '3' : '2'}" filter="url(#shadow-${task.id})"/>
<text x="${centerX}" y="${centerY - 20}" font-family="Arial, sans-serif" font-size="24" font-weight="bold" text-anchor="middle" fill="${textColor}">${statusIcon}</text>
<text x="${centerX}" y="${centerY + 8}" font-family="Arial, sans-serif" font-size="11" font-weight="600" text-anchor="middle" fill="${textColor}">${shortLabel}</text>
<text x="${centerX}" y="${centerY + 24}" font-family="Arial, sans-serif" font-size="9" text-anchor="middle" fill="${textColor}" opacity="0.8">${status}</text>
</svg>`
    
    // 正确编码SVG为data URL - 使用URI编码方式，更兼容
    const svgDataUrl = 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svgContent)
    
    // 根据任务数量动态调整节点大小
    const taskCount = props.tasks.length
    const baseSize = taskCount > 10 ? 50 : (taskCount > 5 ? 55 : 60)
    const runningSize = taskCount > 10 ? 55 : (taskCount > 5 ? 60 : 70)
    const doneSize = taskCount > 10 ? 50 : (taskCount > 5 ? 55 : 65)
    
    const nodeConfig = {
      id: task.id,
      label: shortLabel, // 保留标签用于搜索
      // 移除 title 属性，禁用 vis-network 默认的黄色 tooltip
      shape: 'circularImage', // 使用圆形图片形状
      image: svgDataUrl,
      size: isRunning ? runningSize : (isDone ? doneSize : (isFailed ? doneSize : baseSize)),
      brokenImage: svgDataUrl, // 备用图片
      mass: isRunning ? 1.5 : 1.2, // 减小质量，让节点更容易分散
      font: {
        size: 0, // 隐藏默认文字，使用SVG中的文字
        color: textColor
      }
    }
    
    // 运行中的任务添加脉冲效果（通过动画实现）
    if (isRunning) {
      nodeConfig.borderWidth = 3
    }
    
    nodes.push(nodeConfig)
    
    // 添加依赖关系边
    if (task.inputs && task.inputs.fromTask && Array.isArray(task.inputs.fromTask)) {
      task.inputs.fromTask.forEach(fromTaskId => {
        if (fromTaskId && fromTaskId !== task.id) {
          const fromTask = props.tasks.find(t => t.id === fromTaskId)
          const fromStatus = fromTask?.status?.toUpperCase() || 'PENDING'
          const isFromDone = fromStatus === 'DONE'
          const isFromRunning = fromStatus === 'RUNNING'
          
          edges.push({
            from: fromTaskId,
            to: task.id,
            arrows: 'to',
            color: {
              color: isFromDone ? '#4caf50' : (isFromRunning ? '#2196f3' : '#999'), // 已完成用绿色，运行中用蓝色
              highlight: isFromDone ? '#2e7d32' : (isFromRunning ? '#1565c0' : '#333'),
              opacity: isFromDone ? 0.8 : (isFromRunning ? 0.9 : 0.5)
            },
            width: isFromDone ? 3 : (isFromRunning ? 3 : 2),
            dashes: isFromDone ? false : (isFromRunning ? false : [5, 5]),
            smooth: {
              type: 'curvedCW',
              roundness: 0.2
            },
            animation: {
              enabled: isFromRunning,
              duration: 1000,
              easingFunction: 'linear'
            }
          })
        }
      })
    }
  })
  
  return { nodes, edges }
}

// 初始化图形网络
const initGraph = () => {
  if (!graphContainer.value) return
  
  const data = buildGraphData()
  
  const options = {
    nodes: {
      shape: 'circularImage', // 使用圆形图片形状
      font: {
        size: 0, // 隐藏默认文字
        face: 'Arial, sans-serif'
      },
      margin: 15, // 增加节点边距，让节点之间距离更远
      size: 60, // 默认大小
      borderWidth: 0, // SVG中已包含边框
      chosen: {
        node: (values, id, selected, hovering) => {
          if (selected || hovering) {
            values.size = values.size * 1.15 // 放大选中节点
            values.shadow = {
              enabled: true,
              color: 'rgba(33, 150, 243, 0.3)', // 蓝色阴影
              size: 10, // 减少阴影大小
              x: 0,
              y: 2 // 减少阴影偏移
            }
          }
        }
      },
      scaling: {
        min: 35, // 缩小最小尺寸
        max: 70, // 缩小最大尺寸
        label: {
          enabled: false // 不使用标签缩放
        }
      }
    },
    edges: {
      arrows: {
        to: {
          enabled: true,
          scaleFactor: 0.8,
          type: 'arrow'
        }
      },
      smooth: {
        type: 'curvedCW',
        roundness: 0.2
      },
      color: {
        color: '#999',
        highlight: '#333'
      },
      selectionWidth: 3
    },
    physics: {
      enabled: true,
      stabilization: {
        enabled: true,
        iterations: 150, // 增加迭代次数，让布局更稳定
        updateInterval: 50 // 增加更新间隔
      },
      barnesHut: {
        gravitationalConstant: -3000, // 增加引力常数，让节点更分散
        centralGravity: 0.05, // 减小中心引力，让节点更分散
        springLength: props.tasks.length > 10 ? 180 : (props.tasks.length > 5 ? 150 : 120), // 根据节点数量动态调整弹簧长度
        springConstant: 0.03, // 减小弹簧常数，让节点更容易分散
        damping: 0.2, // 增加阻尼，减少抖动
        avoidOverlap: 1.0 // 增加避免重叠的强度
      },
      solver: 'barnesHut',
      timestep: 0.4 // 稍微减小时间步长，让布局更稳定
    },
    interaction: {
      dragNodes: true,
      dragView: true,
      zoomView: true,
      hover: true,
      tooltipDelay: 0, // 设置为0禁用默认tooltip
      hideEdgesOnDrag: false,
      hideEdgesOnZoom: false,
      hoverConnectedEdges: true,
      tooltip: {
        delay: 0,
        enabled: false // 禁用默认tooltip
      }
    },
    layout: {
      improvedLayout: true,
      hierarchical: {
        enabled: false
      }
    }
  }
  
  network = new Network(graphContainer.value, data, options)
  
  // 添加事件监听
  network.on('click', (params) => {
    if (params.nodes.length > 0) {
      const nodeId = params.nodes[0]
      const task = props.tasks.find(t => t.id === nodeId)
      if (task) {
        console.log('点击任务:', task)
      }
    }
  })
  
  // 添加 hover 事件监听，显示任务详情 tooltip
  network.on('hoverNode', (params) => {
    if (params.node) {
      // 清除之前的隐藏定时器
      if (hideTooltipTimer) {
        clearTimeout(hideTooltipTimer)
        hideTooltipTimer = null
      }
      
      const nodeId = params.node
      const task = props.tasks.find(t => t.id === nodeId)
      if (task) {
        hoveredTask.value = task
        // 立即更新位置
        if (params.event) {
          updateTooltipPosition(params.event)
        } else {
          // 如果没有event，使用鼠标当前位置
          nextTick(() => {
            if (graphContainer.value) {
              const rect = graphContainer.value.getBoundingClientRect()
              const fakeEvent = {
                clientX: rect.left + rect.width / 2,
                clientY: rect.top + rect.height / 2
              }
              updateTooltipPosition(fakeEvent)
            }
          })
        }
      }
    }
  })
  
  // 鼠标移出节点时隐藏 tooltip
  network.on('blurNode', () => {
    // 清除之前的定时器
    if (hideTooltipTimer) {
      clearTimeout(hideTooltipTimer)
    }
    
    // 延迟隐藏，允许鼠标移动到 tooltip 上
    hideTooltipTimer = setTimeout(() => {
      hoveredTask.value = null
      hideTooltipTimer = null
    }, 200)
  })
  
  // 监听鼠标移动，更新 tooltip 位置
  if (graphContainer.value) {
    graphContainer.value.addEventListener('mousemove', handleMouseMove)
  }
  
  // 添加动画效果：运行中的任务脉冲闪烁动画（优化性能）
  let animationFrameId = null
  let pulsePhase = 0
  let lastUpdateTime = performance.now()
  const UPDATE_INTERVAL = 50 // 每50ms更新一次，约20fps，减少卡顿
  
  const animateRunningTasks = (currentTime) => {
    if (!network) return
    
    const runningTasks = props.tasks.filter(t => t.status?.toUpperCase() === 'RUNNING')
    
    // 使用时间戳控制更新频率，避免每帧都更新
    if (runningTasks.length > 0 && (currentTime - lastUpdateTime >= UPDATE_INTERVAL)) {
      pulsePhase += 0.1 // 稍微加快动画速度
      const pulseValue = Math.sin(pulsePhase) // -1 到 1
      const normalizedPulse = (pulseValue + 1) / 2 // 0 到 1
      
      // 准备更新的节点数据
      const nodesToUpdate = runningTasks.map(task => {
        const nodeId = task.id
        // 更新节点的阴影大小和颜色，创建明显的脉冲效果
        const shadowSize = 8 + normalizedPulse * 6 // 8-14之间变化，减少阴影
        const shadowOpacity = 0.2 + normalizedPulse * 0.3 // 0.2-0.5之间变化，减少透明度
        const scale = 1 + normalizedPulse * 0.08 // 1-1.08之间变化
        
        return {
          id: nodeId,
          shadow: {
            enabled: true,
            color: `rgba(33, 150, 243, ${shadowOpacity})`, // 蓝色阴影
            size: shadowSize,
            x: 0,
            y: 2 // 减少阴影偏移
          },
          size: 70 * scale // SVG节点大小
        }
      })
      
      // 批量更新节点（只在需要时更新）
      if (nodesToUpdate.length > 0) {
        network.updateNodes(nodesToUpdate)
        lastUpdateTime = currentTime
      }
    }
    
    // 继续动画循环
    animationFrameId = requestAnimationFrame(animateRunningTasks)
  }
  
  // 启动动画
  animationFrameId = requestAnimationFrame(animateRunningTasks)
  
  // 保存动画引用，以便清理
  network._animationFrameId = animationFrameId
  
  // 监听网络销毁，清理动画
  const originalDestroy = network.destroy
  network.destroy = function() {
    if (animationFrameId) {
      cancelAnimationFrame(animationFrameId)
      animationFrameId = null
    }
    originalDestroy.call(this)
  }
}

// 更新图形（优化性能，避免频繁更新）
const updateGraph = () => {
  if (!network || !graphContainer.value) return
  
  // 使用节流，避免频繁更新
  if (updateGraphThrottleTimer) {
    clearTimeout(updateGraphThrottleTimer)
  }
  
  updateGraphThrottleTimer = setTimeout(() => {
    // 停止旧的动画
    if (network._animationFrameId) {
      cancelAnimationFrame(network._animationFrameId)
      network._animationFrameId = null
    }
    
    const data = buildGraphData()
    
    // 保存当前视图位置和缩放
    const view = network.getViewPosition()
    const scale = network.getScale()
    
    // 使用 setData 而不是 updateNodes，性能更好
    network.setData(data)
    
    // 恢复视图位置和缩放（禁用动画以提高性能）
    network.moveTo({
      position: view,
      scale: scale,
      animation: false // 禁用动画，提高性能
    })
    
    // 重新启动脉冲动画（优化版本）
    let animationFrameId = null
    let pulsePhase = 0
    let lastUpdateTime = performance.now()
    const UPDATE_INTERVAL = 50 // 每50ms更新一次
    
    const animateRunningTasks = (currentTime) => {
      if (!network) return
      
      const runningTasks = props.tasks.filter(t => t.status?.toUpperCase() === 'RUNNING')
      
      // 使用时间戳控制更新频率
      if (runningTasks.length > 0 && (currentTime - lastUpdateTime >= UPDATE_INTERVAL)) {
        pulsePhase += 0.1
        const pulseValue = Math.sin(pulsePhase)
        const normalizedPulse = (pulseValue + 1) / 2
        
        const nodesToUpdate = runningTasks.map(task => {
          const nodeId = task.id
          const shadowSize = 8 + normalizedPulse * 6 // 8-14之间变化，减少阴影
          const shadowOpacity = 0.2 + normalizedPulse * 0.3 // 0.2-0.5之间变化，减少透明度
          const scale = 1 + normalizedPulse * 0.08
          
          return {
            id: nodeId,
            shadow: {
              enabled: true,
              color: `rgba(33, 150, 243, ${shadowOpacity})`, // 蓝色阴影
              size: shadowSize,
              x: 0,
              y: 2 // 减少阴影偏移
            },
            size: 70 * scale // SVG节点大小
          }
        })
        
        if (nodesToUpdate.length > 0) {
          network.updateNodes(nodesToUpdate)
          lastUpdateTime = currentTime
        }
      }
      
      animationFrameId = requestAnimationFrame(animateRunningTasks)
      network._animationFrameId = animationFrameId
    }
    
    // 启动动画
    animationFrameId = requestAnimationFrame(animateRunningTasks)
    network._animationFrameId = animationFrameId
    
    // 检测状态变化的节点（简化处理，避免过多动画）
    const runningTasks = props.tasks.filter(t => t.status?.toUpperCase() === 'RUNNING')
    const doneTasks = props.tasks.filter(t => t.status?.toUpperCase() === 'DONE')
    
    // 移除选中高亮动画，减少性能开销
    // 已完成的任务动画也简化，只在状态刚变化时执行一次
    if (doneTasks.length > 0) {
      doneTasks.forEach(task => {
        const nodeId = task.id
        // 简化成功动画，只更新一次大小
        network.updateNodes([{
          id: nodeId,
          size: 65 // SVG节点大小
        }])
      })
    }
    
    updateGraphThrottleTimer = null
  }, 100) // 100ms节流，减少更新频率
}

// 按状态分组任务（保留用于列表视图）
const groupedTasks = computed(() => {
  const groups = {
    'PENDING': { status: 'PENDING', tasks: [] },
    'RUNNING': { status: 'RUNNING', tasks: [] },
    'DONE': { status: 'DONE', tasks: [] },
    'FAILED': { status: 'FAILED', tasks: [] },
    'SKIPPED': { status: 'SKIPPED', tasks: [] }
  }
  
  props.tasks.forEach(task => {
    const status = task.status?.toUpperCase() || 'PENDING'
    if (groups[status]) {
      groups[status].tasks.push(task)
    }
  })
  
  return Object.values(groups).filter(group => group.tasks.length > 0)
})

// 获取状态标签
const getStatusLabel = (status) => {
  const labels = {
    'PENDING': '待执行',
    'RUNNING': '执行中',
    'DONE': '已完成',
    'FAILED': '失败',
    'SKIPPED': '已跳过'
  }
  return labels[status?.toUpperCase()] || status
}

// 根据任务ID获取任务标题
const getTaskTitleById = (taskId) => {
  const task = props.tasks.find(t => t.id === taskId)
  return task ? (task.title || task.description || task.id) : null
}

// 监听视图模式变化
watch(viewMode, (newMode) => {
  if (newMode === 'chart') {
    nextTick(() => {
      initGraph()
    })
  }
})

// 监听任务变化
watch(() => props.tasks, (newTasks) => {
  console.log('TaskList: 任务列表更新，数量:', newTasks?.length || 0)
  if (viewMode.value === 'chart' && network) {
    updateGraph()
  }
}, { deep: true, immediate: true })

onMounted(() => {
  console.log('TaskList: 组件已挂载，任务数量:', props.tasks?.length || 0)
  if (viewMode.value === 'chart') {
    nextTick(() => {
      initGraph()
    })
  }
})

onBeforeUnmount(() => {
  // 清理节流定时器
  if (updateGraphThrottleTimer) {
    clearTimeout(updateGraphThrottleTimer)
    updateGraphThrottleTimer = null
  }
  
  // 清理tooltip隐藏定时器
  if (hideTooltipTimer) {
    clearTimeout(hideTooltipTimer)
    hideTooltipTimer = null
  }
  
  // 清理鼠标移动监听
  if (graphContainer.value) {
    graphContainer.value.removeEventListener('mousemove', handleMouseMove)
  }
  
  // 清理网络和动画
  if (network) {
    if (network._animationFrameId) {
      cancelAnimationFrame(network._animationFrameId)
      network._animationFrameId = null
    }
    network.destroy()
    network = null
  }
})
</script>

<style scoped>
/* 任务列表容器 */
.message-task-box {
  background: #ffffff;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 12px;
  margin: 16px 0;
  max-width: 75%;
  width: fit-content;
  overflow: hidden;
  flex-shrink: 0;
  animation: taskBoxSlideIn 0.4s cubic-bezier(0.16, 1, 0.3, 1) both;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.08);
  position: relative;
  align-self: flex-start;
}


/* 移除顶部渐变条 */

.task-box-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  background: #f7f8fa;
  font-size: 13px;
  font-weight: 600;
  color: #1d2129;
}

.task-box-header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

/* 移除头部 shimmer 动画 */

.task-box-title-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  position: relative;
  z-index: 1;
}

.task-box-icon {
  font-size: 16px;
  opacity: 0.7;
}

.task-box-title {
  display: flex;
  align-items: center;
  gap: 6px;
  text-shadow: none;
}

.task-box-count {
  display: flex;
  align-items: baseline;
  gap: 3px;
  font-size: 11px;
  color: rgba(0, 0, 0, 0.5);
  background: rgba(255, 255, 255, 0.5);
  padding: 4px 10px;
  border-radius: 12px;
  font-weight: 600;
  border: 1px solid rgba(0, 0, 0, 0.06);
  position: relative;
  z-index: 1;
  transition: all 0.3s ease;
  opacity: 0.8;
}

.task-box-count:hover {
  opacity: 1;
}

.count-number {
  font-size: 13px;
  font-weight: 700;
  color: rgba(0, 0, 0, 0.6);
}

.count-label {
  font-size: 10px;
  color: #8e8ea0;
  font-weight: 500;
}

.task-box-content {
  padding: 16px 18px;
  background: #ffffff;
  min-height: auto;
  overflow: visible;
  position: relative;
}

.task-list-view {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
  margin: -4px;
}

.task-graph-view {
  width: 100%;
  height: 500px;
  min-height: 400px;
  position: relative;
}

.graph-container {
  width: 100%;
  height: 100%;
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 8px;
  background: #ffffff;
  overflow: hidden;
  position: relative;
}

/* 任务详情 Tooltip */
.task-tooltip {
  position: absolute;
  width: 320px;
  max-height: 500px;
  background: linear-gradient(135deg, #ffffff 0%, #fafafa 50%, #ffffff 100%);
  border: 1.5px solid rgba(16, 163, 127, 0.15);
  border-radius: 12px;
  box-shadow: 
    0 8px 32px rgba(0, 0, 0, 0.12),
    0 4px 16px rgba(0, 0, 0, 0.08),
    0 2px 4px rgba(0, 0, 0, 0.04),
    inset 0 1px 0 rgba(255, 255, 255, 0.9);
  z-index: 1000;
  opacity: 0;
  visibility: hidden;
  transition: opacity 0.2s ease, visibility 0.2s ease, transform 0.2s ease;
  transform: translateY(-10px);
  pointer-events: auto;
  overflow: hidden;
}

.task-tooltip:hover {
  opacity: 1 !important;
  visibility: visible !important;
}

.tooltip-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.8) 0%, rgba(250, 250, 250, 0.6) 100%);
}

.tooltip-status-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.tooltip-status-badge.status-pending {
  background: linear-gradient(135deg, #f5f5f5 0%, #e8e8e8 100%);
  color: #666;
  border: 1px solid #d1d1d1;
}

.tooltip-status-badge.status-running {
  background: linear-gradient(135deg, #2196f3 0%, #42a5f5 50%, #64b5f6 100%);
  color: #ffffff;
  border: 1px solid #1565c0;
  box-shadow: 0 2px 8px rgba(33, 150, 243, 0.3);
  animation: statusPulse 2s ease-in-out infinite;
}

.tooltip-status-badge.status-done {
  background: linear-gradient(135deg, #4caf50 0%, #66bb6a 50%, #81c784 100%);
  color: #ffffff;
  border: 1px solid #2e7d32;
  box-shadow: 0 2px 8px rgba(76, 175, 80, 0.25);
}

.tooltip-status-badge.status-failed {
  background: linear-gradient(135deg, #f44336 0%, #e57373 50%, #ef5350 100%);
  color: #ffffff;
  border: 1px solid #c62828;
  box-shadow: 0 2px 8px rgba(244, 67, 54, 0.25);
}

.tooltip-status-badge.status-skipped {
  background: linear-gradient(135deg, #e0e0e0 0%, #bdbdbd 100%);
  color: #616161;
  border: 1px solid #9e9e9e;
}

.status-icon {
  font-size: 14px;
}

.status-text {
  font-size: 11px;
  font-weight: 600;
}

.tooltip-close {
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 6px;
  background: transparent;
  color: rgba(0, 0, 0, 0.4);
  font-size: 20px;
  line-height: 1;
  cursor: pointer;
  transition: all 0.2s ease;
  font-weight: 300;
}

.tooltip-close:hover {
  background: rgba(0, 0, 0, 0.05);
  color: rgba(0, 0, 0, 0.7);
}

.tooltip-content {
  padding: 16px;
  max-height: 450px;
  overflow-y: auto;
  overflow-x: hidden;
}

.tooltip-content::-webkit-scrollbar {
  width: 6px;
}

.tooltip-content::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.02);
  border-radius: 3px;
}

.tooltip-content::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 3px;
}

.tooltip-content::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.15);
}

.tooltip-section {
  margin-bottom: 16px;
}

.tooltip-section:last-child {
  margin-bottom: 0;
}

.tooltip-label {
  font-size: 11px;
  font-weight: 600;
  color: rgba(0, 0, 0, 0.5);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  margin-bottom: 6px;
}

.tooltip-value {
  font-size: 13px;
  color: rgba(0, 0, 0, 0.8);
  line-height: 1.6;
  word-break: break-word;
}

.tooltip-value.tooltip-code {
  font-family: 'SF Mono', 'Monaco', 'Consolas', 'Courier New', monospace;
  background: rgba(0, 0, 0, 0.04);
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  color: rgba(0, 0, 0, 0.7);
  border: 1px solid rgba(0, 0, 0, 0.06);
}

.tooltip-tag {
  display: inline-block;
  padding: 3px 8px;
  background: linear-gradient(135deg, rgba(33, 150, 243, 0.1) 0%, rgba(66, 165, 245, 0.08) 100%);
  border: 1px solid rgba(33, 150, 243, 0.2);
  border-radius: 6px;
  font-size: 11px;
  font-weight: 500;
  color: #2196f3;
  margin-right: 4px;
  margin-bottom: 4px;
  font-family: 'SF Mono', 'Monaco', 'Consolas', monospace;
}

.tooltip-tag.user-input {
  background: linear-gradient(135deg, rgba(76, 175, 80, 0.1) 0%, rgba(102, 187, 106, 0.08) 100%);
  border-color: rgba(76, 175, 80, 0.2);
  color: #4caf50;
}

.tooltip-tag.strategy-tag {
  background: linear-gradient(135deg, rgba(156, 39, 176, 0.1) 0%, rgba(171, 71, 188, 0.08) 100%);
  border-color: rgba(156, 39, 176, 0.2);
  color: #9c27b0;
}

/* 运行中任务的脉冲动画 */
.graph-container :deep(.vis-network) {
  position: relative;
}

.graph-container :deep(.vis-network canvas) {
  transition: opacity 0.3s ease;
}

/* SVG节点样式增强 */
.graph-container :deep(.vis-node) {
  transition: transform 0.3s ease;
}

.graph-container :deep(.vis-node:hover) {
  transform: scale(1.05);
}

/* 为运行中的节点添加CSS动画 */
@keyframes nodePulse {
  0%, 100% {
    filter: drop-shadow(0 0 4px rgba(33, 150, 243, 0.25)); /* 蓝色阴影，减少大小和透明度 */
  }
  50% {
    filter: drop-shadow(0 0 10px rgba(33, 150, 243, 0.4)); /* 减少最大阴影 */
  }
}

@keyframes doneGlow {
  0%, 100% {
    filter: drop-shadow(0 0 3px rgba(33, 150, 243, 0.3)); /* 蓝色阴影，减少大小 */
  }
  50% {
    filter: drop-shadow(0 0 8px rgba(33, 150, 243, 0.5)); /* 减少最大阴影 */
  }
}

.view-toggle {
  display: flex;
  gap: 4px;
  background: rgba(255, 255, 255, 0.6);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 8px;
  padding: 2px;
}

.view-toggle-btn {
  width: 28px;
  height: 28px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: transparent;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: rgba(0, 0, 0, 0.5);
  font-size: 14px;
}

.view-toggle-btn:hover {
  background: rgba(0, 0, 0, 0.05);
  color: rgba(0, 0, 0, 0.7);
}

.view-toggle-btn.active {
  background: linear-gradient(135deg, rgba(33, 150, 243, 0.15), rgba(66, 165, 245, 0.12));
  color: #2196f3;
  box-shadow: 0 1px 3px rgba(33, 150, 243, 0.2);
}

.toggle-icon {
  font-size: 14px;
}

/* 图状视图样式 */
.chart-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.chart-group {
  animation: chartGroupSlideIn 0.4s cubic-bezier(0.16, 1, 0.3, 1) both;
}

.chart-group-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.8), rgba(250, 250, 250, 0.6));
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 8px;
  margin-bottom: 8px;
  font-size: 12px;
  font-weight: 600;
}

.chart-group-icon {
  font-size: 14px;
}

.chart-group-title {
  flex: 1;
  color: rgba(0, 0, 0, 0.7);
}

.chart-group-count {
  padding: 2px 8px;
  background: rgba(33, 150, 243, 0.1);
  border-radius: 12px;
  font-size: 11px;
  font-weight: 700;
  color: #2196f3;
}

.chart-group-content {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 8px;
  padding-left: 4px;
}

.chart-task-card {
  padding: 10px 12px;
  background: rgba(255, 255, 255, 0.95);
  border: 1px solid rgba(0, 0, 0, 0.08);
  border-radius: 8px;
  font-size: 11px;
  transition: all 0.3s ease;
  animation: chartCardPopIn 0.3s cubic-bezier(0.34, 1.56, 0.64, 1) both;
  box-shadow: 0 1px 2px rgba(0, 0, 0, 0.04);
}

.chart-task-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.08);
  border-color: rgba(33, 150, 243, 0.2);
}

.chart-task-header {
  display: flex;
  align-items: center;
  gap: 6px;
}

.chart-task-spinner {
  display: inline-block;
  width: 10px;
  height: 10px;
  border: 2px solid rgba(33, 150, 243, 0.15);
  border-top-color: rgba(33, 150, 243, 0.6);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  flex-shrink: 0;
}

.chart-task-title {
  flex: 1;
  color: inherit;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 400;
}

.chart-task-card.status-pending {
  background: #f8f8f8;
  border-color: #d1d1d1;
  color: #666;
}

.chart-task-card.status-running {
  background: linear-gradient(135deg, rgba(227, 242, 253, 0.8), rgba(187, 222, 251, 0.7));
  border-color: rgba(33, 150, 243, 0.3);
  color: rgba(13, 71, 161, 0.8);
}

.chart-task-card.status-done {
  background: linear-gradient(135deg, rgba(232, 245, 233, 0.8), rgba(200, 230, 201, 0.7));
  border-color: rgba(76, 175, 80, 0.3);
  color: rgba(27, 94, 32, 0.8);
}

.chart-task-card.status-failed {
  background: linear-gradient(135deg, rgba(255, 235, 238, 0.8), rgba(255, 205, 210, 0.7));
  border-color: rgba(244, 67, 54, 0.3);
  color: rgba(183, 28, 28, 0.8);
}

@keyframes chartGroupSlideIn {
  from {
    opacity: 0;
    transform: translateX(-10px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

@keyframes chartCardPopIn {
  from {
    opacity: 0;
    transform: scale(0.8);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.task-chip {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  background: rgba(16, 163, 127, 0.05); /* 浅绿色背景 */
  border: 1px solid #10a37f; /* 绿色边框 */
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #1d2129;
  cursor: pointer;
  transition: all 0.2s ease;
  position: relative;
  overflow: hidden;
  box-shadow: 0 1px 2px rgba(16, 163, 127, 0.1);
  margin: 4px;
  white-space: nowrap;
  max-width: 100%;
  animation: chipPopIn 0.4s cubic-bezier(0.16, 1, 0.3, 1) both;
}

.task-chip:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 6px rgba(16, 163, 127, 0.2);
  background: rgba(16, 163, 127, 0.1);
}

.task-chip.status-pending {
  background: rgba(16, 163, 127, 0.05);
  border-color: #10a37f;
  color: #1d2129;
}

.task-chip.status-running {
  background: rgba(16, 163, 127, 0.1);
  border-color: #10a37f;
  color: #1d2129;
  box-shadow: 0 2px 6px rgba(16, 163, 127, 0.2);
}

.task-chip.status-done {
  background: rgba(16, 163, 127, 0.1);
  border-color: #10a37f;
  color: #1d2129;
  box-shadow: 0 2px 6px rgba(16, 163, 127, 0.2);
}

.task-chip.status-failed {
  background: rgba(16, 163, 127, 0.05);
  border-color: #10a37f;
  color: #1d2129;
  box-shadow: 0 2px 6px rgba(16, 163, 127, 0.15);
}

.chip-spinner {
  display: inline-block;
  width: 12px;
  height: 12px;
  border: 2px solid rgba(33, 150, 243, 0.15);
  border-top-color: rgba(33, 150, 243, 0.6);
  border-right-color: rgba(66, 165, 245, 0.5);
  border-radius: 50%;
  animation: spin 0.8s linear infinite;
  flex-shrink: 0;
  box-shadow: 0 0 4px rgba(33, 150, 243, 0.2);
  opacity: 0.8;
}

.chip-title {
  color: inherit;
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-weight: 400;
}

.chip-status {
  display: none;
}

@keyframes taskBoxSlideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes gradientShift {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

@keyframes chipPopIn {
  0% {
    opacity: 0;
    transform: scale(0.5) translateY(-15px) rotate(-10deg);
  }
  40% {
    opacity: 0.8;
    transform: scale(1.1) translateY(-3px) rotate(3deg);
  }
  70% {
    transform: scale(0.95) translateY(2px) rotate(-1deg);
  }
  100% {
    opacity: 1;
    transform: scale(1) translateY(0) rotate(0deg);
  }
}

@keyframes statusPulse {
  0%, 100% {
    box-shadow: 
      0 0 0 0 rgba(33, 150, 243, 0.2),
      0 1px 3px rgba(33, 150, 243, 0.1);
    transform: scale(1);
  }
  50% {
    box-shadow: 
      0 0 0 4px rgba(33, 150, 243, 0),
      0 2px 6px rgba(33, 150, 243, 0.12);
    transform: scale(1.01);
  }
}

@keyframes statusSuccess {
  0% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
  100% {
    transform: scale(1);
  }
}

@keyframes statusShake {
  0%, 100% {
    transform: translateX(0);
  }
  10%, 30%, 50%, 70%, 90% {
    transform: translateX(-3px);
  }
  20%, 40%, 60%, 80% {
    transform: translateX(3px);
  }
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

@keyframes shimmer {
  0% {
    left: -100%;
  }
  100% {
    left: 100%;
  }
}

@keyframes iconBounce {
  0%, 100% {
    transform: translateY(0) rotate(0deg);
  }
  50% {
    transform: translateY(-2px) rotate(5deg);
  }
}

@keyframes checkmarkPop {
  0% {
    transform: scale(0) rotate(-180deg);
    opacity: 0;
  }
  60% {
    transform: scale(1.2) rotate(10deg);
  }
  100% {
    transform: scale(1) rotate(0deg);
    opacity: 1;
  }
}
</style>


