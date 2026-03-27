<script setup lang="ts">
import { computed, ref } from 'vue'
import { Download, ExternalLink, File, FileImage, Film } from 'lucide-vue-next'
import type { FileAttachmentVO } from '@/types/notice'
import { withFileAccessToken } from '@/utils/file-url'

interface Props {
  attachments: FileAttachmentVO[]
}

const props = defineProps<Props>()

const imageDialogVisible = ref(false)
const fileDialogVisible = ref(false)
const activeAttachment = ref<FileAttachmentVO | null>(null)

const activeAccessUrl = computed(() => {
  if (!activeAttachment.value) return ''
  return withFileAccessToken(activeAttachment.value.accessUrl)
})

function isImage(item: FileAttachmentVO | null | undefined) {
  return Boolean(item?.contentType?.startsWith('image/'))
}

function isVideo(item: FileAttachmentVO | null | undefined) {
  return Boolean(item?.contentType?.startsWith('video/'))
}

function formatFileSize(fileSize?: number | null) {
  if (!fileSize || fileSize <= 0) return '-'
  if (fileSize < 1024) return `${fileSize} B`
  if (fileSize < 1024 * 1024) return `${(fileSize / 1024).toFixed(1)} KB`
  return `${(fileSize / (1024 * 1024)).toFixed(2)} MB`
}

function fileTypeLabel(item: FileAttachmentVO) {
  if (isImage(item)) return '图片'
  if (isVideo(item)) return '视频'
  return item.contentType || '文件'
}

function resolveItemIcon(item: FileAttachmentVO) {
  if (isImage(item)) return FileImage
  if (isVideo(item)) return Film
  return File
}

function openAttachment(item: FileAttachmentVO) {
  if (isImage(item)) {
    activeAttachment.value = item
    imageDialogVisible.value = true
    return
  }
  activeAttachment.value = item
  fileDialogVisible.value = true
}

function openCurrentFile() {
  if (!activeAccessUrl.value) return
  window.open(activeAccessUrl.value, '_blank', 'noopener,noreferrer')
}

async function downloadCurrentFile() {
  if (!activeAttachment.value || !activeAccessUrl.value) return
  try {
    const response = await fetch(activeAccessUrl.value)
    if (!response.ok) {
      throw new Error(`download failed: ${response.status}`)
    }
    const blob = await response.blob()
    const objectUrl = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = objectUrl
    link.download = activeAttachment.value.originFileName || activeAttachment.value.fileName || `file-${activeAttachment.value.fileId}`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    URL.revokeObjectURL(objectUrl)
  } catch {
    openCurrentFile()
  }
}
</script>

<template>
  <div class="space-y-2">
    <button
      v-for="item in props.attachments"
      :key="item.fileId"
      type="button"
      class="flex w-full items-center justify-between rounded-xl border border-white/12 bg-white/[0.03] px-3 py-2 text-left transition-colors hover:border-white/25 hover:bg-white/[0.08]"
      @click="openAttachment(item)"
    >
      <span class="flex min-w-0 items-center gap-2">
        <component :is="resolveItemIcon(item)" :size="14" class="shrink-0 text-white/70" />
        <span class="truncate text-sm font-body text-white/80">
          {{ item.originFileName || item.fileName || `文件-${item.fileId}` }}
        </span>
      </span>
      <span class="shrink-0 text-xs font-body text-white/45">
        {{ fileTypeLabel(item) }}
      </span>
    </button>
  </div>

  <el-dialog v-model="imageDialogVisible" append-to-body width="760px" title="图片预览">
    <template v-if="activeAttachment && isImage(activeAttachment)">
      <div class="overflow-hidden rounded-xl border border-white/15 bg-black/70">
        <img
          :src="withFileAccessToken(activeAttachment.accessUrl)"
          :alt="activeAttachment.originFileName || activeAttachment.fileName"
          class="max-h-[70vh] w-full object-contain"
        >
      </div>
      <div class="mt-4 flex flex-wrap items-center gap-3">
        <button
          type="button"
          class="liquid-glass-strong inline-flex cursor-pointer items-center gap-2 rounded-full px-4 py-2 text-sm font-body text-white"
          @click="openCurrentFile"
        >
          <ExternalLink :size="14" />
          打开原图
        </button>
        <button
          type="button"
          class="liquid-glass inline-flex cursor-pointer items-center gap-2 rounded-full px-4 py-2 text-sm font-body text-white/80"
          @click="downloadCurrentFile"
        >
          <Download :size="14" />
          下载图片
        </button>
      </div>
    </template>
  </el-dialog>

  <el-dialog v-model="fileDialogVisible" append-to-body width="620px" title="附件查看">
    <template v-if="activeAttachment">
      <div v-if="isVideo(activeAttachment)" class="overflow-hidden rounded-xl border border-white/15 bg-black">
        <video controls class="max-h-[360px] w-full" :src="activeAccessUrl" />
      </div>
      <div v-else class="rounded-xl border border-white/15 bg-white/[0.02] px-4 py-3 text-sm font-body text-white/70">
        当前文件不支持内嵌预览，请使用“打开附件”或“下载附件”。
      </div>

      <div class="mt-4 grid grid-cols-2 gap-3 text-sm font-body text-white/70">
        <p class="col-span-2 truncate">
          <span class="text-white/45">文件名：</span>
          {{ activeAttachment.originFileName || activeAttachment.fileName }}
        </p>
        <p>
          <span class="text-white/45">类型：</span>
          {{ fileTypeLabel(activeAttachment) }}
        </p>
        <p>
          <span class="text-white/45">大小：</span>
          {{ formatFileSize(activeAttachment.fileSize) }}
        </p>
      </div>

      <div class="mt-5 flex flex-wrap items-center gap-3">
        <button
          type="button"
          class="liquid-glass-strong inline-flex cursor-pointer items-center gap-2 rounded-full px-4 py-2 text-sm font-body text-white"
          @click="openCurrentFile"
        >
          <ExternalLink :size="14" />
          打开附件
        </button>
        <button
          type="button"
          class="liquid-glass inline-flex cursor-pointer items-center gap-2 rounded-full px-4 py-2 text-sm font-body text-white/80"
          @click="downloadCurrentFile"
        >
          <Download :size="14" />
          下载附件
        </button>
      </div>
    </template>
  </el-dialog>
</template>
