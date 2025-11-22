import { ref, computed } from 'vue'

// 语言配置
export const languages = [
  { code: 'zh', name: '中文', flag: '🇨🇳' },
  { code: 'en', name: 'English', flag: '🇺🇸' }
]

// 翻译文本
const translations = {
  zh: {
    // 登录页面
    login: {
      title: '输入邮箱开始使用',
      subtitle: '我们将向您的邮箱发送<strong>验证码</strong>，您可以使用它登录。',
      emailLabel: '邮箱',
      emailPlaceholder: '您的邮箱',
      emailHint: '使用组织邮箱可以轻松与团队成员协作',
      continue: '继续',
      loading: '加载中...',
      or: '或',
      continueWithGitHub: '使用 GitHub 继续',
      continueWithGoogle: '使用 Google 继续',
      continueWithMicrosoft: '使用 Microsoft 继续',
      continueWithSSO: '使用 SSO 继续',
      legalText: '点击继续即表示您接受我们的<u>服务条款</u>和<u>隐私政策</u>'
    },
    // 通用
    common: {
      welcome: '欢迎',
      loading: '加载中...',
      error: '错误',
      success: '成功',
      cancel: '取消',
      confirm: '确认',
      save: '保存',
      delete: '删除',
      edit: '编辑',
      search: '搜索',
      close: '关闭'
    }
  },
  en: {
    // 登录页面
    login: {
      title: 'Enter an email to get started!',
      subtitle: 'We\'ll send a <strong>verification code</strong> in email, which you can use to sign in.',
      emailLabel: 'Email',
      emailPlaceholder: 'Your Email',
      emailHint: 'Use an organization email to easily collaborate with teammates',
      continue: 'Continue',
      loading: 'Loading...',
      or: 'or',
      continueWithGitHub: 'Continue with GitHub',
      continueWithGoogle: 'Continue with Google',
      continueWithMicrosoft: 'Continue with Microsoft',
      continueWithSSO: 'Continue with SSO',
      legalText: 'By tapping continue, you accept our <u>Terms and Conditions</u> and <u>Privacy Policy</u>'
    },
    // 通用
    common: {
      welcome: 'Welcome',
      loading: 'Loading...',
      error: 'Error',
      success: 'Success',
      cancel: 'Cancel',
      confirm: 'Confirm',
      save: 'Save',
      delete: 'Delete',
      edit: 'Edit',
      search: 'Search',
      close: 'Close'
    }
  }
}

// 获取初始语言（默认英文）
const getInitialLanguage = () => {
  if (typeof window !== 'undefined') {
    const saved = localStorage.getItem('language')
    return saved || 'en'
  }
  return 'en'
}

// 当前语言（默认英文）
const currentLang = ref(getInitialLanguage())

// 切换语言
export const switchLanguage = (langCode) => {
  if (languages.find(l => l.code === langCode)) {
    currentLang.value = langCode
    if (typeof window !== 'undefined') {
      localStorage.setItem('language', langCode)
    }
  }
}

// 获取当前语言
export const getCurrentLanguage = () => {
  return currentLang.value
}

// 翻译函数
export const t = (key, params = {}) => {
  const keys = key.split('.')
  let value = translations[currentLang.value]
  
  for (const k of keys) {
    if (value && typeof value === 'object') {
      value = value[k]
    } else {
      return key
    }
  }
  
  if (typeof value !== 'string') {
    return key
  }
  
  // 简单的参数替换
  if (Object.keys(params).length > 0) {
    return value.replace(/\{(\w+)\}/g, (match, paramKey) => {
      return params[paramKey] || match
    })
  }
  
  return value
}

// Composable
export const useI18n = () => {
  return {
    currentLang: computed(() => currentLang.value),
    languages,
    t,
    switchLanguage,
    getCurrentLanguage
  }
}

