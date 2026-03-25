<script setup lang="ts">
/**
 * HeroPartners - 首屏底部能力背书区
 */
import { onMounted, ref } from 'vue'
import { gsap } from 'gsap'
import type { PartnerItem } from '@/types/hero'

defineProps<{
  partners: PartnerItem[]
  slogan: string
}>()

const sectionRef = ref<HTMLElement | null>(null)

onMounted(() => {
  if (!sectionRef.value) return
  gsap.fromTo(
    sectionRef.value,
    { opacity: 0, y: 24, filter: 'blur(8px)' },
    { opacity: 1, y: 0, filter: 'blur(0px)', duration: 0.8, delay: 1.9, ease: 'power2.out' },
  )
})
</script>

<template>
  <div
    id="partners"
    ref="sectionRef"
    class="mt-auto pt-14 pb-8"
    :style="{ opacity: 0 }"
  >
    <div class="flex justify-center mb-7">
      <span class="liquid-glass rounded-full px-3.5 py-1 text-xs font-medium text-white font-body">
        {{ slogan }}
      </span>
    </div>

    <div class="flex flex-wrap items-center justify-center gap-12 md:gap-16">
      <span
        v-for="partner in partners"
        :key="partner.name"
        class="text-2xl md:text-3xl font-heading italic text-white/80 tracking-tight"
      >
        {{ partner.name }}
      </span>
    </div>
  </div>
</template>
