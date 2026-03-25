<script setup lang="ts">
/**
 * HeroQuickEntries — 快捷业务入口
 * 映射后端核心居民端功能
 * 使用 liquid-glass 胶囊卡片，保持首屏电影感
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { gsap } from 'gsap'
import { Megaphone, CalendarHeart, Wrench, UserCircle } from 'lucide-vue-next'
import type { QuickEntry } from '@/types/hero'

defineProps<{
  entries: QuickEntry[]
}>()

const router = useRouter()
const listRef = ref<HTMLElement | null>(null)

const iconMap: Record<string, any> = {
  Megaphone,
  CalendarHeart,
  Wrench,
  UserCircle,
}

onMounted(() => {
  if (listRef.value) {
    const items = listRef.value.querySelectorAll('.quick-entry-item')
    gsap.fromTo(
      items,
      { opacity: 0, y: 30, scale: 0.95 },
      {
        opacity: 1,
        y: 0,
        scale: 1,
        duration: 0.5,
        stagger: 0.12,
        delay: 1.5,
        ease: 'power2.out',
      },
    )
  }
})
</script>

<template>
  <div
    ref="listRef"
    class="grid grid-cols-2 md:grid-cols-4 gap-3 mt-10 max-w-2xl"
  >
    <div
      v-for="entry in entries"
      :key="entry.label"
      class="quick-entry-item liquid-glass rounded-2xl p-4 cursor-pointer
             hover:bg-white/5 transition-all duration-300 hover:scale-105 group"
      :style="{ opacity: 0 }"
      @click="router.push(entry.href)"
    >
      <component
        :is="iconMap[entry.icon]"
        :size="24"
        class="text-white/70 group-hover:text-white transition-colors mb-2"
      />
      <p class="text-white text-sm font-body font-medium">{{ entry.label }}</p>
      <p class="text-white/50 text-xs font-body mt-1 leading-relaxed">
        {{ entry.description }}
      </p>
    </div>
  </div>
</template>
