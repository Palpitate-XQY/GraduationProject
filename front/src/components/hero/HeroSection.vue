<script setup lang="ts">
/**
 * HeroSection — 全屏 Cinematic Hero 首屏
 * 包含：视频背景 + 遮罩 + 标题 + 副标题 + CTA + 快捷入口 + 合作伙伴
 *
 * 视觉参考：高级品牌官网首屏
 * 业务语义：基于后端业务分析结果
 */
import { ref, onMounted } from 'vue'
import { gsap } from 'gsap'
import HeroBlurText from './HeroBlurText.vue'
import HeroActions from './HeroActions.vue'
import HeroQuickEntries from './HeroQuickEntries.vue'
import HeroPartners from './HeroPartners.vue'
import { useHeroContent } from '@/composables/useHeroContent'

const { heroContent } = useHeroContent()

const subtitleRef = ref<HTMLElement | null>(null)
const videoLoaded = ref(false)

function onVideoCanPlay() {
  videoLoaded.value = true
}

onMounted(() => {
  if (subtitleRef.value) {
    gsap.fromTo(
      subtitleRef.value,
      { opacity: 0, y: 20, filter: 'blur(6px)' },
      { opacity: 1, y: 0, filter: 'blur(0px)', duration: 0.6, delay: 0.8, ease: 'power2.out' },
    )
  }
})
</script>

<template>
  <section class="relative min-h-screen flex flex-col overflow-hidden">
    <!-- ===== 背景视频 ===== -->
    <video
      class="absolute inset-0 w-full h-full object-cover z-0"
      :poster="heroContent.posterUrl"
      autoplay
      loop
      muted
      playsinline
      preload="auto"
      @canplay="onVideoCanPlay"
    >
      <source :src="heroContent.videoUrl" type="video/mp4" />
    </video>

    <!-- ===== 暗色遮罩 ===== -->
    <div class="absolute inset-0 bg-black/40 z-0" />

    <!-- ===== 前景内容 ===== -->
    <div class="relative z-10 flex flex-col flex-1 px-6 lg:px-16 pt-32 md:pt-40">
      <div class="max-w-4xl">
        <!-- 主标题 -->
        <HeroBlurText
          :text="heroContent.title"
          tag="h1"
          class="text-5xl md:text-7xl lg:text-[5.5rem] font-heading italic text-white
                 leading-[0.85] max-w-2xl tracking-[-3px]"
        />

        <!-- 副标题 -->
        <p
          ref="subtitleRef"
          class="mt-5 text-sm md:text-base text-white/85 max-w-2xl font-body leading-relaxed"
          :style="{ opacity: 0 }"
        >
          {{ heroContent.subtitle }}
        </p>

        <!-- CTA 按钮组 -->
        <HeroActions :ctas="heroContent.ctas" />

        <!-- 快捷入口（基于后端核心居民端功能） -->
        <HeroQuickEntries :entries="heroContent.quickEntries" />
      </div>

      <!-- 底部合作伙伴 / 能力背书 -->
      <HeroPartners
        :partners="heroContent.partners"
        :slogan="heroContent.partnerSlogan"
      />
    </div>
  </section>
</template>
