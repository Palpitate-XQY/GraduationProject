<script setup lang="ts">
/**
 * PortalLayout — 门户布局（含 Navbar）
 * 首页不添加额外顶部间距（Navbar 已是浮动设计, Hero 自带 pt）
 * 其他子页面需要 pt-24 以补偿 Navbar 遮挡（由各 View 自行处理）
 */
import HeroNavbar from '@/components/hero/HeroNavbar.vue'
</script>

<template>
  <div class="min-h-screen bg-gray-950 relative">
    <!-- 所有子页面共享的暗色渐变背景 -->
    <div class="fixed inset-0 z-0 pointer-events-none">
      <div class="absolute inset-0 bg-gradient-to-br from-gray-950 via-gray-900 to-gray-950" />
      <div class="absolute top-0 left-1/4 w-[500px] h-[500px] bg-teal-900/10 rounded-full blur-3xl" />
      <div class="absolute bottom-0 right-1/4 w-[400px] h-[400px] bg-indigo-900/10 rounded-full blur-3xl" />
    </div>

    <HeroNavbar />
    <main class="relative z-10">
      <router-view />
    </main>
  </div>
</template>
