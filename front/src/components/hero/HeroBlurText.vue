<script setup lang="ts">
/**
 * HeroBlurText - 语义分段模糊入场标题动画
 * 动画轨迹：blur(10 -> 5 -> 0), opacity(0 -> 0.5 -> 1), y(50 -> -5 -> 0)
 */
import { computed, nextTick, ref, watch } from 'vue'
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
const hasAnimated = ref(false)
const { isVisible } = useIntersectionMotion(containerRef, { threshold: 0.2, once: true })

function splitSemanticSegments(text: string): string[] {
  if (/[\u4e00-\u9fa5]/.test(text)) {
    return text
      .split(/(?<=[，。！？；、])/)
      .map((segment) => segment.trim())
      .filter(Boolean)
  }

  const words = text.trim().split(/\s+/).filter(Boolean)
  const segments: string[] = []
  for (let i = 0; i < words.length; i += 2) {
    segments.push(words.slice(i, i + 2).join(' '))
  }
  return segments
}

const segments = computed(() => splitSemanticSegments(props.text))

watch(isVisible, async (visible) => {
  if (!visible || hasAnimated.value || !containerRef.value) return
  hasAnimated.value = true
  await nextTick()

  const nodes = containerRef.value.querySelectorAll('.blur-segment')
  gsap.fromTo(
    nodes,
    {
      filter: 'blur(10px)',
      opacity: 0,
      y: 50,
    },
    {
      keyframes: [
        {
          filter: 'blur(5px)',
          opacity: 0.5,
          y: -5,
          duration: props.stepDuration,
          ease: 'power2.out',
        },
        {
          filter: 'blur(0px)',
          opacity: 1,
          y: 0,
          duration: props.stepDuration,
          ease: 'power2.out',
        },
      ],
      stagger: props.staggerDelay,
    },
  )
})
</script>

<template>
  <component :is="tag" ref="containerRef" class="hero-blur-text">
    <span
      v-for="(segment, index) in segments"
      :key="`${segment}-${index}`"
      class="blur-segment"
      :class="{ 'mr-2': !/[\u4e00-\u9fa5]/.test(text) }"
      :style="{ opacity: 0 }"
    >
      {{ segment }}
    </span>
  </component>
</template>

<style scoped>
.hero-blur-text {
  display: flex;
  flex-wrap: wrap;
}

.blur-segment {
  display: inline-block;
  will-change: filter, opacity, transform;
}
</style>
