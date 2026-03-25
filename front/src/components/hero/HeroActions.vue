<script setup lang="ts">
/**
 * HeroActions - 首屏 CTA 按钮组
 */
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { gsap } from 'gsap'
import { ArrowUpRight, Play } from 'lucide-vue-next'
import type { CtaButton } from '@/types/hero'

defineProps<{
  ctas: CtaButton[]
}>()

const router = useRouter()
const containerRef = ref<HTMLElement | null>(null)

function navigate(href: string) {
  if (href.startsWith('#')) {
    document.querySelector(href)?.scrollIntoView({ behavior: 'smooth' })
    return
  }
  router.push(href)
}

onMounted(() => {
  if (!containerRef.value) return
  gsap.fromTo(
    containerRef.value,
    { opacity: 0, y: 20, filter: 'blur(8px)' },
    { opacity: 1, y: 0, filter: 'blur(0px)', duration: 0.6, delay: 1.1, ease: 'power2.out' },
  )
})
</script>

<template>
  <div
    ref="containerRef"
    class="flex items-center gap-6 mt-6 flex-wrap"
    :style="{ opacity: 0 }"
  >
    <button
      v-for="(cta, index) in ctas"
      :key="`${cta.href}-${index}`"
      type="button"
      :class="[
        'flex items-center gap-2 font-body text-sm transition-all duration-300 cursor-pointer',
        cta.primary
          ? 'liquid-glass-strong rounded-full px-5 py-2.5 text-foreground font-medium hover:scale-105'
          : 'px-1 py-2 text-white/80 hover:text-white',
      ]"
      @click="navigate(cta.href)"
    >
      <ArrowUpRight v-if="cta.icon === 'ArrowUpRight'" :size="16" />
      <Play v-if="cta.icon === 'Play'" :size="16" />
      {{ cta.text }}
    </button>
  </div>
</template>
