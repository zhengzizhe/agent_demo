/**
 * GSAP 动画工具库
 * 统一管理所有动画效果，提供流畅、细腻的动画体验
 */
import { gsap } from 'gsap'

// ==================== 通用动画函数 ====================

/**
 * 淡入动画
 */
export const fadeIn = (element, options = {}) => {
  const {
    duration = 0.4,
    delay = 0,
    ease = 'power2.out',
    onComplete = null
  } = options

  return gsap.fromTo(
    element,
    { opacity: 0 },
    {
      opacity: 1,
      duration,
      delay,
      ease,
      onComplete
    }
  )
}

/**
 * 淡出动画
 */
export const fadeOut = (element, options = {}) => {
  const {
    duration = 0.3,
    delay = 0,
    ease = 'power2.in',
    onComplete = null
  } = options

  return gsap.to(element, {
    opacity: 0,
    duration,
    delay,
    ease,
    onComplete
  })
}

/**
 * 从下方滑入
 */
export const slideInUp = (element, options = {}) => {
  const {
    duration = 0.5,
    delay = 0,
    distance = 30,
    ease = 'power3.out',
    onComplete = null
  } = options

  return gsap.fromTo(
    element,
    { opacity: 0, y: distance },
    {
      opacity: 1,
      y: 0,
      duration,
      delay,
      ease,
      onComplete
    }
  )
}

/**
 * 从上方滑入
 */
export const slideInDown = (element, options = {}) => {
  const {
    duration = 0.5,
    delay = 0,
    distance = 30,
    ease = 'power3.out',
    onComplete = null
  } = options

  return gsap.fromTo(
    element,
    { opacity: 0, y: -distance },
    {
      opacity: 1,
      y: 0,
      duration,
      delay,
      ease,
      onComplete
    }
  )
}

/**
 * 从左侧滑入
 */
export const slideInLeft = (element, options = {}) => {
  const {
    duration = 0.5,
    delay = 0,
    distance = 30,
    ease = 'power3.out',
    onComplete = null
  } = options

  return gsap.fromTo(
    element,
    { opacity: 0, x: -distance },
    {
      opacity: 1,
      x: 0,
      duration,
      delay,
      ease,
      onComplete
    }
  )
}

/**
 * 从右侧滑入
 */
export const slideInRight = (element, options = {}) => {
  const {
    duration = 0.5,
    delay = 0,
    distance = 30,
    ease = 'power3.out',
    onComplete = null
  } = options

  return gsap.fromTo(
    element,
    { opacity: 0, x: distance },
    {
      opacity: 1,
      x: 0,
      duration,
      delay,
      ease,
      onComplete
    }
  )
}

/**
 * 缩放进入
 */
export const scaleIn = (element, options = {}) => {
  const {
    duration = 0.4,
    delay = 0,
    scale = 0.9,
    ease = 'back.out(1.7)',
    onComplete = null
  } = options

  return gsap.fromTo(
    element,
    { opacity: 0, scale },
    {
      opacity: 1,
      scale: 1,
      duration,
      delay,
      ease,
      onComplete
    }
  )
}

/**
 * 缩放退出
 */
export const scaleOut = (element, options = {}) => {
  const {
    duration = 0.3,
    delay = 0,
    scale = 0.9,
    ease = 'power2.in',
    onComplete = null
  } = options

  return gsap.to(element, {
    opacity: 0,
    scale,
    duration,
    delay,
    ease,
    onComplete
  })
}

/**
 * 3D 翻转进入
 */
export const flipIn = (element, options = {}) => {
  const {
    duration = 0.6,
    delay = 0,
    ease = 'power3.out',
    onComplete = null
  } = options

  return gsap.fromTo(
    element,
    {
      opacity: 0,
      x: 50,
      y: -20,
      scale: 0.9,
      rotationX: -15
    },
    {
      opacity: 1,
      x: 0,
      y: 0,
      scale: 1,
      rotationX: 0,
      duration,
      delay,
      ease,
      transformPerspective: 1000,
      onComplete
    }
  )
}

/**
 * 3D 翻转退出
 */
export const flipOut = (element, options = {}) => {
  const {
    duration = 0.4,
    delay = 0,
    ease = 'power2.in',
    onComplete = null
  } = options

  return gsap.to(element, {
    opacity: 0,
    x: 50,
    y: -20,
    scale: 0.9,
    rotationX: -15,
    duration,
    delay,
    ease,
    transformPerspective: 1000,
    onComplete
  })
}

/**
 * 弹跳进入
 */
export const bounceIn = (element, options = {}) => {
  const {
    duration = 0.6,
    delay = 0,
    ease = 'elastic.out(1, 0.5)',
    onComplete = null
  } = options

  return gsap.fromTo(
    element,
    { opacity: 0, scale: 0.3 },
    {
      opacity: 1,
      scale: 1,
      duration,
      delay,
      ease,
      onComplete
    }
  )
}

/**
 * 脉冲动画
 */
export const pulse = (element, options = {}) => {
  const {
    duration = 1,
    scale = 1.1,
    repeat = -1,
    yoyo = true,
    ease = 'power2.inOut'
  } = options

  return gsap.to(element, {
    scale,
    duration,
    repeat,
    yoyo,
    ease
  })
}

/**
 * 旋转动画
 */
export const rotate = (element, options = {}) => {
  const {
    duration = 1,
    rotation = 360,
    repeat = -1,
    ease = 'none'
  } = options

  return gsap.to(element, {
    rotation,
    duration,
    repeat,
    ease
  })
}

/**
 * 摇摆动画
 */
export const shake = (element, options = {}) => {
  const {
    duration = 0.5,
    x = 10,
    repeat = 5,
    yoyo = true,
    ease = 'power2.inOut'
  } = options

  return gsap.to(element, {
    x,
    duration,
    repeat,
    yoyo,
    ease
  })
}

/**
 * 按钮悬停动画
 */
export const buttonHover = (element, options = {}) => {
  const {
    scale = 1.05,
    y = -2,
    duration = 0.3,
    ease = 'power2.out'
  } = options

  return gsap.to(element, {
    scale,
    y,
    boxShadow: '0 4px 12px rgba(51, 112, 255, 0.3)',
    duration,
    ease
  })
}

/**
 * 按钮离开动画
 */
export const buttonLeave = (element, options = {}) => {
  const {
    duration = 0.3,
    ease = 'power2.out'
  } = options

  return gsap.to(element, {
    scale: 1,
    y: 0,
    boxShadow: '0 2px 4px rgba(0, 0, 0, 0.1)',
    duration,
    ease
  })
}

/**
 * 按钮点击动画
 */
export const buttonClick = (element, options = {}) => {
  const {
    scale = 0.95,
    duration = 0.1,
    ease = 'power2.out'
  } = options

  const tl = gsap.timeline()
  
  tl.to(element, {
    scale,
    duration,
    ease
  })
  .to(element, {
    scale: 1.05,
    duration: 0.2,
    ease: 'back.out(2)'
  })
  .to(element, {
    scale: 1,
    duration: 0.2,
    ease: 'power2.out'
  })

  return tl
}

/**
 * 列表项逐个出现
 */
export const staggerIn = (elements, options = {}) => {
  const {
    duration = 0.4,
    delay = 0.05,
    from = 'start',
    ease = 'power2.out',
    onComplete = null
  } = options

  return gsap.fromTo(
    elements,
    { opacity: 0, y: 20 },
    {
      opacity: 1,
      y: 0,
      duration,
      stagger: {
        each: delay,
        from
      },
      ease,
      onComplete
    }
  )
}

/**
 * 打字机效果
 */
export const typewriter = (element, text, options = {}) => {
  const {
    speed = 0.05,
    onComplete = null
  } = options

  element.textContent = ''
  const chars = text.split('')
  let index = 0

  const type = () => {
    if (index < chars.length) {
      element.textContent += chars[index]
      index++
      setTimeout(type, speed * 1000)
    } else if (onComplete) {
      onComplete()
    }
  }

  type()
}

/**
 * 加载旋转动画
 */
export const loadingSpinner = (element, options = {}) => {
  const {
    duration = 1,
    repeat = -1,
    ease = 'none'
  } = options

  return gsap.to(element, {
    rotation: 360,
    duration,
    repeat,
    ease
  })
}

/**
 * 淡入并上移（组合动画）
 */
export const fadeInUp = (element, options = {}) => {
  const {
    duration = 0.5,
    delay = 0,
    distance = 20,
    ease = 'power3.out',
    onComplete = null
  } = options

  return gsap.fromTo(
    element,
    { opacity: 0, y: distance },
    {
      opacity: 1,
      y: 0,
      duration,
      delay,
      ease,
      onComplete
    }
  )
}

/**
 * 淡入并下移（组合动画）
 */
export const fadeInDown = (element, options = {}) => {
  const {
    duration = 0.5,
    delay = 0,
    distance = 20,
    ease = 'power3.out',
    onComplete = null
  } = options

  return gsap.fromTo(
    element,
    { opacity: 0, y: -distance },
    {
      opacity: 1,
      y: 0,
      duration,
      delay,
      ease,
      onComplete
    }
  )
}

/**
 * 创建时间轴动画
 */
export const createTimeline = (options = {}) => {
  const {
    delay = 0,
    onComplete = null
  } = options

  return gsap.timeline({
    delay,
    onComplete
  })
}

/**
 * 设置元素初始状态（用于动画前准备）
 */
export const setInitialState = (element, state = {}) => {
  return gsap.set(element, state)
}

/**
 * 停止所有动画
 */
export const killAll = (element) => {
  return gsap.killTweensOf(element)
}



