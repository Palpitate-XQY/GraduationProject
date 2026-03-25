<script setup lang="ts">
/**
 * HeroBlurText — 模糊入场文字动画组件
 * 将标题按词拆分，每个词从 blur(10px) + opacity(0) + y(50)
 * 逐渐过渡到 blur(0) + opacity(1) + y(0)
 * 使用 GSAP + IntersectionObserver 实现
 */
import { ref, onMounted, watch, nextTick } from 'vue'
import { gsap } from 'gsap'
import { useIntersectionMotion } from '@/composables/useIntersectionMotion'

const props = withDefaults(
  defineProps<{
    text: string
    tag?: string
    stepDuration?: number
    staggerDelay?: number
  }>(),
  {
    tag: 'h1',
    stepDuration: 0.35,
    staggerDelay: 0.1,
  },
)

const containerRef = ref<HTMLElement | null>(null)
const { isVisible } = useIntersectionMotion(containerRef, { threshold: 0.2 })

/** 将文本拆分为词 */
const words = props.text.split(/\s+/)

/** 动画已播放标记 */
let hasAnimated = false

watch(isVisible, async (visible) => {
  if (visible && !hasAnimated && containerRef.value) {
    hasAnimated = true
    await nextTick()
    const spans = containerRef.value.querySelectorAll('.blur-word')
    gsap.fromTo(
      spans,
      {
        filter: 'blur(10px)',
        opacity: 0,
        y: 50,
      },
      {
        filter: 'blur(0px)',
        opacity: 1,
        y: 0,
        duration: props.stepDuration,
        stagger: props.staggerDelay,
        ease: 'power2.out',
      },
    )
  }
})
</script>

<template>
  <component
    :is="tag"
    ref="containerRef"
    class="hero-blur-text"
  >
    <span
      v-for="(word, index) in words"
      :key="index"
      class="blur-word"
      :style="{ opacity: 0 }"
    >
      {{ word }}&nbsp;
    </span>
  </component>
</template>

<style scoped>
.hero-blur-text {
  display: flex;
  flex-wrap: wrap;
}
.blur-word {
  display: inline-block;
  will-change: filter, opacity, transform;
}
</style>
