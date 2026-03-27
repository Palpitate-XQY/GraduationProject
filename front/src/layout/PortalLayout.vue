<script setup lang="ts">
/**
 * PortalLayout - 门户布局（含 Navbar）
 */
import HeroNavbar from '@/components/hero/HeroNavbar.vue'
</script>

<template>
  <div class="min-h-screen relative">
    <div class="fixed inset-0 z-0 pointer-events-none">
      <div class="absolute inset-0 bg-gradient-to-br from-slate-950/65 via-slate-900/45 to-slate-950/65" />
      <div class="absolute top-0 left-1/4 w-[500px] h-[500px] bg-teal-900/12 rounded-full blur-3xl" />
      <div class="absolute bottom-0 right-1/4 w-[400px] h-[400px] bg-indigo-900/12 rounded-full blur-3xl" />
    </div>

    <HeroNavbar />
    <main class="relative z-10">
      <router-view />
    </main>
  </div>
</template>
