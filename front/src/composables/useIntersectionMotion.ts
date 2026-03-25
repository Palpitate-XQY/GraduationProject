/**
 * IntersectionObserver 进场动画组合式函数
 * 用于 Hero 标题、副标题等元素的视口触发动效
 */
import { ref, onMounted, onUnmounted, type Ref } from 'vue'

interface MotionOptions {
  /** 触发阈值，默认 0.1 */
  threshold?: number
  /** 是否只触发一次，默认 true */
  once?: boolean
  /** 根元素的 margin */
  rootMargin?: string
}

/**
 * 监听元素进入视口
 * @returns isVisible — 元素是否可见
 */
export function useIntersectionMotion(
  target: Ref<HTMLElement | null>,
  options: MotionOptions = {},
) {
  const { threshold = 0.1, once = true, rootMargin = '0px' } = options
  const isVisible = ref(false)
  let observer: IntersectionObserver | null = null

  onMounted(() => {
    if (!target.value) return

    observer = new IntersectionObserver(
      ([entry]) => {
        if (entry.isIntersecting) {
          isVisible.value = true
          if (once && observer && target.value) {
            observer.unobserve(target.value)
          }
        } else if (!once) {
          isVisible.value = false
        }
      },
      { threshold, rootMargin },
    )

    observer.observe(target.value)
  })

  onUnmounted(() => {
    observer?.disconnect()
  })

  return { isVisible }
}
