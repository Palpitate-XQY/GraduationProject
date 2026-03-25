<script setup lang="ts">
/**
 * AdminSkeletonView - 管理端模块骨架页
 */
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()
const title = computed(() => (route.meta.title as string) || '模块页面')
const description = computed(() => (route.meta.description as string) || '该模块骨架已接入权限路由，可继续扩展业务 CRUD 页面。')
</script>

<template>
  <section class="liquid-glass rounded-3xl p-6 md:p-8">
    <h2 class="text-2xl font-heading italic tracking-tight text-white">{{ title }}</h2>
    <p class="mt-3 text-sm text-white/70 font-body leading-relaxed">
      {{ description }}
    </p>
    <div class="mt-6 grid grid-cols-1 md:grid-cols-3 gap-4">
      <div class="liquid-glass rounded-2xl p-4" v-for="index in 3" :key="index">
        <p class="text-xs text-white/55 font-body">待接入能力 {{ index }}</p>
        <p class="mt-2 text-sm text-white/80 font-body">接口映射、列表检索、详情编辑与按钮级权限控制</p>
      </div>
    </div>
  </section>
</template>
