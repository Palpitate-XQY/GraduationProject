<script setup lang="ts">
/**
 * HeroActions — CTA 按钮组
 * 主按钮: liquid-glass-strong 胶囊风格
 * 次按钮: 轻量文字按钮
 * 按钮语义对应后端模块入口
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { gsap } from 'gsap'
import { ArrowUpRight, Play } from 'lucide-vue-next'
import type { CtaButton } from '@/types/hero'

defineProps<{
  ctas: CtaButton[]
  delay?: number
}>()

const router = useRouter()
const containerRef = ref<HTMLElement | null>(null)

onMounted(() => {
  if (containerRef.value) {
    gsap.fromTo(
      containerRef.value,
      { opacity: 0, y: 20, filter: 'blur(8px)' },
      { opacity: 1, y: 0, filter: 'blur(0px)', duration: 0.6, delay: 1.1, ease: 'power2.out' },
    )
  }
})

function handleClick(href: string) {
  if (href.startsWith('#')) {
    document.querySelector(href)?.scrollIntoView({ behavior: 'smooth' })
  } else {
    router.push(href)
  }
}
</script>

<template>
  <div
    ref="containerRef"
    class="flex items-center gap-6 mt-6 flex-wrap"
    :style="{ opacity: 0 }"
  >
    <button
      v-for="(cta, i) in ctas"
      :key="i"
      :class="[
        'flex items-center gap-2 font-body text-sm font-medium transition-all duration-300 cursor-pointer select-none',
        cta.primary
          ? 'liquid-glass-strong rounded-full px-6 py-3 text-white hover:scale-105 hover:shadow-lg hover:shadow-white/5'
          : 'text-white/80 hover:text-white px-2 py-2',
      ]"
      @click="handleClick(cta.href)"
    >
      <ArrowUpRight v-if="cta.icon === 'ArrowUpRight'" :size="18" />
      <Play v-if="cta.icon === 'Play'" :size="18" />
      {{ cta.text }}
    </button>
  </div>
</template>
