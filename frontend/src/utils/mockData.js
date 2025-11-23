// 假数据生成器 - 用于文档库demo

// 生成随机ID
const generateId = () => {
  return Date.now().toString(36) + Math.random().toString(36).substr(2)
}

// 生成随机时间（最近30天内）
const randomTime = () => {
  const now = Date.now()
  const daysAgo = Math.floor(Math.random() * 30)
  const hoursAgo = Math.floor(Math.random() * 24)
  return now - (daysAgo * 24 * 60 * 60 * 1000) - (hoursAgo * 60 * 60 * 1000)
}

// 用户列表
const users = [
  { id: '1', name: '张三', avatar: '张', email: 'zhangsan@example.com' },
  { id: '2', name: '李四', avatar: '李', email: 'lisi@example.com' },
  { id: '3', name: '王五', avatar: '王', email: 'wangwu@example.com' },
  { id: '4', name: '赵六', avatar: '赵', email: 'zhaoliu@example.com' },
  { id: '5', name: '钱七', avatar: '钱', email: 'qianqi@example.com' },
  { id: '6', name: '孙八', avatar: '孙', email: 'sunba@example.com' },
]

// 文档类型
const docTypes = ['文档', '表格', '演示文稿', '思维导图', '白板']

// 文档名称模板
const docNameTemplates = [
  '项目计划书',
  '会议纪要',
  '产品需求文档',
  '技术方案',
  '用户调研报告',
  '运营数据分析',
  '市场推广策略',
  '团队协作规范',
  '开发进度跟踪',
  '客户反馈汇总',
  '季度总结报告',
  '年度规划',
  '培训材料',
  '操作手册',
  'FAQ文档'
]

// 生成单个文档
const generateDocument = (index = 0) => {
  const name = docNameTemplates[index % docNameTemplates.length] + (index > docNameTemplates.length ? ` ${Math.floor(index / docNameTemplates.length) + 1}` : '')
  const owner = users[Math.floor(Math.random() * users.length)]
  const createdAt = randomTime()
  const updatedAt = createdAt + Math.random() * 7 * 24 * 60 * 60 * 1000
  const size = Math.floor(Math.random() * 50000) + 1000
  const favorite = Math.random() > 0.7
  const liked = Math.random() > 0.8
  const likeCount = liked ? Math.floor(Math.random() * 20) + 1 : 0
  
  // 随机生成协作者
  const collaborators = []
  if (Math.random() > 0.4) {
    const count = Math.floor(Math.random() * 4) + 1
    const availableUsers = users.filter(u => u.id !== owner.id)
    const shuffled = [...availableUsers].sort(() => 0.5 - Math.random())
    for (let i = 0; i < Math.min(count, shuffled.length); i++) {
      collaborators.push({
        user: shuffled[i],
        role: Math.random() > 0.5 ? 'editor' : 'viewer', // editor: 可编辑, viewer: 仅查看
        addedAt: createdAt + Math.random() * (updatedAt - createdAt)
      })
    }
  }
  
  // 随机生成分享链接
  const shareLink = Math.random() > 0.5 ? {
    enabled: true,
    link: `https://docs.example.com/share/${generateId()}`,
    permission: Math.random() > 0.5 ? 'view' : 'edit', // view: 仅查看, edit: 可编辑
    password: Math.random() > 0.7 ? '1234' : null,
    expiresAt: Math.random() > 0.8 ? Date.now() + 7 * 24 * 60 * 60 * 1000 : null
  } : null
  
  // 生成层级结构：前几个作为文件夹，后面的文档可能属于这些文件夹
  const isFolder = index < 5 && Math.random() > 0.5
  const parentId = !isFolder && index > 5 && Math.random() > 0.6 
    ? `folder_${Math.floor(Math.random() * 5)}` 
    : null
  
  return {
    id: isFolder ? `folder_${index}` : generateId(),
    name: isFolder ? `文件夹 ${index + 1}` : name,
    type: isFolder ? 'folder' : docTypes[Math.floor(Math.random() * docTypes.length)],
    description: isFolder ? `这是${name}文件夹` : `这是${name}的详细描述，包含了相关的内容和说明。`,
    owner,
    createdAt,
    updatedAt,
    size: isFolder ? 0 : size,
    favorite,
    liked,
    likeCount,
    collaborators,
    shareLink,
    parentId, // 父级ID，用于构建层级结构
    isFolder, // 是否为文件夹
    spaceId: (() => {
      // 确保个人空间有更多文档（前20个）
      if (index < 20) {
        return 'personal'
      } else if (index < 35) {
        return 'work'
      } else if (index < 45) {
        return 'team'
      } else {
        return 'project'
      }
    })(),
    deleted: false,
    tags: Math.random() > 0.6 ? ['重要', '待办', '项目'].slice(0, Math.floor(Math.random() * 3) + 1) : []
  }
}

// 生成文档列表
export const generateDocuments = (count = 20) => {
  return Array.from({ length: count }, (_, i) => generateDocument(i))
}

// 生成空间列表
export const generateSpaces = () => {
  return [
    {
      id: 'personal',
      name: '个人空间',
      icon: '👤',
      color: '#6366f1',
      documentCount: 12,
      type: 'personal'
    },
    {
      id: 'work',
      name: '工作空间',
      icon: '💼',
      color: '#10b981',
      documentCount: 28,
      type: 'work'
    },
    {
      id: 'team',
      name: '团队协作',
      icon: '👥',
      color: '#f59e0b',
      documentCount: 15,
      type: 'team'
    },
    {
      id: 'project',
      name: '项目文档',
      icon: '📁',
      color: '#8b5cf6',
      documentCount: 8,
      type: 'project'
    }
  ]
}

// 生成任务列表
export const generateTasks = () => {
  const taskStatuses = ['todo', 'in-progress', 'done']
  const priorities = ['low', 'medium', 'high']
  
  return Array.from({ length: 15 }, (_, i) => {
    const status = taskStatuses[Math.floor(Math.random() * taskStatuses.length)]
    const priority = priorities[Math.floor(Math.random() * priorities.length)]
    const assignee = users[Math.floor(Math.random() * users.length)]
    
    return {
      id: generateId(),
      title: `任务 ${i + 1}: ${docNameTemplates[i % docNameTemplates.length]}`,
      description: `这是任务${i + 1}的详细描述`,
      status,
      priority,
      assignee,
      dueDate: Math.random() > 0.5 ? Date.now() + Math.random() * 7 * 24 * 60 * 60 * 1000 : null,
      createdAt: randomTime(),
      tags: ['重要', '紧急', '项目'].slice(0, Math.floor(Math.random() * 3))
    }
  })
}

// 生成日历事件
export const generateCalendarEvents = () => {
  const eventTypes = ['meeting', 'deadline', 'reminder']
  
  return Array.from({ length: 10 }, (_, i) => {
    const type = eventTypes[Math.floor(Math.random() * eventTypes.length)]
    const startDate = Date.now() + Math.random() * 30 * 24 * 60 * 60 * 1000
    const duration = [30, 60, 90, 120][Math.floor(Math.random() * 4)]
    
    return {
      id: generateId(),
      title: `事件 ${i + 1}: ${docNameTemplates[i % docNameTemplates.length]}`,
      type,
      startDate,
      endDate: startDate + duration * 60 * 1000,
      location: Math.random() > 0.5 ? '会议室A' : null,
      attendees: users.slice(0, Math.floor(Math.random() * 4) + 1),
      description: `这是事件${i + 1}的详细描述`
    }
  })
}

// 导出用户列表
export { users }

