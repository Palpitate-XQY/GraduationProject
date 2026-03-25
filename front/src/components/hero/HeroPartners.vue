<script setup lang="ts">
/**
 * HeroPartners — 底部能力背书区
 * 展示平台核心技术能力标签
 * 映射后端已实现的核心模块能力
 */
import { ref, onMounted } from 'vue'
import { gsap } from 'gsap'
import type { PartnerItem } from '@/types/hero'

defineProps<{
  partners: PartnerItem[]
  slogan: string
}>()

const sectionRef = ref<HTMLElement | null>(null)

onMounted(() => {
  if (sectionRef.value) {
    gsap.fromTo(
      sectionRef.value,
      { opacity: 0, y: 30 },
      { opacity: 1, y: 0, duration: 0.8, delay: 2, ease: 'power2.out' },
    )
  }
})
</script>

<template>
  <div
    ref="sectionRef"
    class="mt-auto pt-16 pb-8"
    :style="{ opacity: 0 }"
  >
    <!-- Slogan 标签 -->
    <div class="flex justify-center mb-8">
      <span
        class="liquid-glass rounded-full px-4 py-1.5 text-xs font-medium text-white font-body tracking-wide"
      >
        {{ slogan }}
      </span>
    </div>

    <!-- 能力/品牌名称列表 -->
    <div class="flex flex-wrap items-center justify-center gap-8 md:gap-16">
      <span
        v-for="partner in partners"
        :key="partner.name"
        class="text-xl md:text-3xl font-heading italic text-white/60 tracking-tight
               hover:text-white/90 transition-colors duration-300 cursor-default select-none"
      >
        {{ partner.name }}
      </span>
    </div>
  </div>
</template>
