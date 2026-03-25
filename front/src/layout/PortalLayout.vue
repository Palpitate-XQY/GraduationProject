<script setup lang="ts">
/**
 * PortalLayout — 门户布局（含 Navbar）
 */
import HeroNavbar from '@/components/hero/HeroNavbar.vue'
</script>

<template>
  <div class="min-h-screen bg-gray-950">
    <HeroNavbar />
    <main>
      <router-view />
    </main>
  </div>
</template>
