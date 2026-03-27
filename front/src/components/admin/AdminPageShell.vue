<script setup lang="ts">
interface Props {
  title: string
  description?: string
}

defineProps<Props>()
</script>

<template>
  <section class="admin-page space-y-5">
    <header class="liquid-glass admin-page-header rounded-3xl px-6 py-6 md:px-7 md:py-7">
      <div class="flex flex-wrap items-start justify-between gap-4">
        <div>
          <p class="inline-flex rounded-full border border-white/20 bg-white/[0.05] px-3 py-1 text-[11px] font-body uppercase tracking-[0.18em] text-white/70">
            Admin Module
          </p>
          <h2 class="mt-3 text-3xl font-heading italic tracking-tight text-white md:text-[2.15rem]">{{ title }}</h2>
          <p v-if="description" class="mt-2 max-w-3xl text-sm font-body text-white/70 md:text-[0.95rem]">
            {{ description }}
          </p>
        </div>
        <slot name="header-extra" />
      </div>
    </header>
    <slot />
  </section>
</template>
