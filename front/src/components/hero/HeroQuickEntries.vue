<script setup lang="ts">
/**
 * HeroQuickEntries - 首屏业务快捷入口
 */
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { gsap } from 'gsap'
import { CalendarHeart, Megaphone, UserCircle, Wrench } from 'lucide-vue-next'
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
  if (!listRef.value) return
  const items = listRef.value.querySelectorAll('.quick-entry-item')
  gsap.fromTo(
    items,
    { opacity: 0, y: 20, filter: 'blur(6px)' },
    {
      opacity: 1,
      y: 0,
      filter: 'blur(0px)',
      duration: 0.5,
      stagger: 0.08,
      delay: 1.4,
      ease: 'power2.out',
    },
  )
})
</script>

<template>
  <div
    v-if="entries.length"
    ref="listRef"
    class="mt-8 flex flex-wrap gap-3 max-w-3xl"
  >
    <button
      v-for="entry in entries"
      :key="entry.label"
      type="button"
      class="quick-entry-item liquid-glass rounded-full px-4 py-2 text-left text-white/90 hover:text-white hover:bg-white/8 transition-all duration-300"
      :style="{ opacity: 0 }"
      @click="router.push(entry.href)"
    >
      <span class="flex items-center gap-2 text-xs md:text-sm font-body font-medium">
        <component :is="iconMap[entry.icon]" :size="16" />
        {{ entry.label }}
      </span>
    </button>
  </div>
</template>
