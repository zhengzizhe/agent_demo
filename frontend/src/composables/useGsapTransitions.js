/**
 * GSAP Vue Transition Hooks
 * 用于 Vue 的 <transition> 组件
 */
import { gsap } from 'gsap'
import * as anims from '../utils/animations.js'

/**
 * 淡入淡出过渡
 */
export const fadeTransition = {
  enter(el, done) {
    anims.fadeIn(el, {
      duration: 0.3,
      onComplete: done
    })
  },
  leave(el, done) {
    anims.fadeOut(el, {
      duration: 0.2,
      onComplete: done
    })
  }
}

/**
 * 滑入滑出过渡（从下方）
 */
export const slideUpTransition = {
  enter(el, done) {
    anims.slideInUp(el, {
      duration: 0.4,
      onComplete: done
    })
  },
  leave(el, done) {
    gsap.to(el, {
      opacity: 0,
      y: 20,
      duration: 0.3,
      ease: 'power2.in',
      onComplete: done
    })
  }
}

/**
 * 滑入滑出过渡（从上方）
 */
export const slideDownTransition = {
  enter(el, done) {
    anims.slideInDown(el, {
      duration: 0.4,
      onComplete: done
    })
  },
  leave(el, done) {
    gsap.to(el, {
      opacity: 0,
      y: -20,
      duration: 0.3,
      ease: 'power2.in',
      onComplete: done
    })
  }
}

/**
 * 滑入滑出过渡（从左侧）
 */
export const slideLeftTransition = {
  enter(el, done) {
    anims.slideInLeft(el, {
      duration: 0.4,
      onComplete: done
    })
  },
  leave(el, done) {
    gsap.to(el, {
      opacity: 0,
      x: -30,
      duration: 0.3,
      ease: 'power2.in',
      onComplete: done
    })
  }
}

/**
 * 滑入滑出过渡（从右侧）
 */
export const slideRightTransition = {
  enter(el, done) {
    anims.slideInRight(el, {
      duration: 0.4,
      onComplete: done
    })
  },
  leave(el, done) {
    gsap.to(el, {
      opacity: 0,
      x: 30,
      duration: 0.3,
      ease: 'power2.in',
      onComplete: done
    })
  }
}

/**
 * 缩放过渡
 */
export const scaleTransition = {
  enter(el, done) {
    anims.scaleIn(el, {
      duration: 0.4,
      ease: 'back.out(1.7)',
      onComplete: done
    })
  },
  leave(el, done) {
    anims.scaleOut(el, {
      duration: 0.3,
      onComplete: done
    })
  }
}

/**
 * 3D 翻转过渡
 */
export const flipTransition = {
  enter(el, done) {
    anims.flipIn(el, {
      duration: 0.6,
      onComplete: done
    })
  },
  leave(el, done) {
    anims.flipOut(el, {
      duration: 0.4,
      onComplete: done
    })
  }
}

/**
 * 弹跳进入过渡
 */
export const bounceTransition = {
  enter(el, done) {
    anims.bounceIn(el, {
      duration: 0.6,
      onComplete: done
    })
  },
  leave(el, done) {
    anims.scaleOut(el, {
      duration: 0.3,
      onComplete: done
    })
  }
}

/**
 * 创建自定义过渡
 */
export const createTransition = (enterAnim, leaveAnim) => {
  return {
    enter(el, done) {
      enterAnim(el, done)
    },
    leave(el, done) {
      leaveAnim(el, done)
    }
  }
}




