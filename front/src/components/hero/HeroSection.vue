<script setup lang="ts">
/**
 * HeroSection - Cinematic hero content layer.
 * 背景视频由 App.vue 全局提供。
 */
import { onMounted, ref } from 'vue'
import { gsap } from 'gsap'
import HeroActions from './HeroActions.vue'
import HeroBlurText from './HeroBlurText.vue'
import HeroPartners from './HeroPartners.vue'
import HeroQuickEntries from './HeroQuickEntries.vue'
import { useHeroContent } from '@/composables/useHeroContent'

const { heroContent } = useHeroContent()
const subtitleRef = ref<HTMLElement | null>(null)

onMounted(() => {
  if (!subtitleRef.value) return
  gsap.fromTo(
    subtitleRef.value,
    { opacity: 0, y: 20, filter: 'blur(10px)' },
    { opacity: 1, y: 0, filter: 'blur(0px)', duration: 0.6, delay: 0.8, ease: 'power2.out' },
  )
})
</script>

<template>
  <section id="features" class="relative min-h-screen flex flex-col overflow-hidden">
    <div class="absolute inset-0 bg-black/40 z-0" />

    <div class="relative z-10 flex flex-col flex-1 px-6 md:px-8 lg:px-16 pt-32 md:pt-40">
      <div class="max-w-4xl">
        <HeroBlurText
          :text="heroContent.title"
          tag="h1"
          class="text-6xl md:text-7xl lg:text-[5.5rem] font-heading italic text-foreground leading-[0.8] max-w-2xl tracking-[-4px]"
        />

        <p
          ref="subtitleRef"
          class="mt-4 text-sm md:text-base text-white/90 max-w-2xl font-body leading-relaxed"
          :style="{ opacity: 0 }"
        >
          {{ heroContent.subtitle }}
        </p>

        <HeroActions :ctas="heroContent.ctas" />
        <HeroQuickEntries :entries="heroContent.quickEntries" />
      </div>

      <HeroPartners :partners="heroContent.partners" :slogan="heroContent.partnerSlogan" />
    </div>
  </section>
</template>
