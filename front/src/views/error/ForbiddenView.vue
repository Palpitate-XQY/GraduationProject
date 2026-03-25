<script setup lang="ts">
/**
 * ForbiddenView - 403 无权限页
 */
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <div class="min-h-screen bg-slate-950 flex items-center justify-center px-6">
    <div class="liquid-glass rounded-3xl p-8 max-w-md text-center">
      <p class="text-xs uppercase tracking-wider text-white/45 font-body">403</p>
      <h1 class="mt-2 text-3xl font-heading italic text-white">无权限访问</h1>
      <p class="mt-3 text-sm text-white/70 font-body">
        当前账号缺少目标页面所需权限，请联系管理员授权后重试。
      </p>
      <div class="mt-6 flex items-center justify-center gap-3">
        <button
          type="button"
          class="liquid-glass rounded-full px-4 py-2 text-sm font-body text-white/80 hover:text-white"
          @click="router.push('/')"
        >
          返回门户首页
        </button>
        <button
          type="button"
          class="liquid-glass-strong rounded-full px-4 py-2 text-sm font-body text-white"
          @click="router.push('/dashboard')"
        >
          进入看板
        </button>
      </div>
    </div>
  </div>
</template>
