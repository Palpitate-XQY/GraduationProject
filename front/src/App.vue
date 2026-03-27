<script setup lang="ts">
/**
 * 根组件
 * 全站统一 Mux 视频背景。
 */
import { HERO_MUX_IFRAME_SRC } from '@/constants/background'
</script>

<template>
  <div class="min-h-screen relative overflow-x-hidden">
    <div class="fixed inset-0 z-0 pointer-events-none overflow-hidden">
      <iframe
        :src="HERO_MUX_IFRAME_SRC"
        title="Global Smart Community Background"
        class="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 min-w-full min-h-full w-[177.78vh] h-[56.25vw] max-w-none border-0"
        allow="accelerometer; gyroscope; autoplay; encrypted-media; picture-in-picture;"
        allowfullscreen
        loading="eager"
        aria-hidden="true"
      />
    </div>
    <div class="relative z-10">
      <router-view />
    </div>
  </div>
</template>
