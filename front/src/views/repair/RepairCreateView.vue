<script setup lang="ts">
/**
 * RepairCreateView - 发起报修
 * 与后端 RepairCreateRequest 保持一致，自动注入 complexOrgId。
 */
import { onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import type { UploadRequestOptions } from 'element-plus'
import { ArrowLeft, FileImage, Film, Send, Upload } from 'lucide-vue-next'
import { uploadFile } from '@/api/file'
import { createRepair } from '@/api/repair'
import { getMyProfile } from '@/api/profile'
import type { RepairCreateRequest } from '@/types/repair'

const router = useRouter()
const loading = ref(false)
const uploadLoading = ref(false)

interface UploadAttachmentItem {
  id: number
  name: string
  contentType: string
  fileSize: number
  previewUrl?: string
}

const form = reactive<RepairCreateRequest>({
  title: '',
  description: '',
  contactPhone: '',
  repairAddress: '',
  emergencyLevel: 'LOW',
  complexOrgId: 0,
})

const emergencyOptions = [
  { label: '低', value: 'LOW' },
  { label: '中', value: 'MEDIUM' },
  { label: '高（紧急）', value: 'HIGH' },
]

const attachments = ref<UploadAttachmentItem[]>([])

function isImage(type?: string) {
  return Boolean(type?.startsWith('image/'))
}

function isVideo(type?: string) {
  return Boolean(type?.startsWith('video/'))
}

function isAllowedFileType(file: File) {
  return isImage(file.type) || isVideo(file.type)
}

function formatFileSize(fileSize: number) {
  if (!fileSize) return '-'
  if (fileSize < 1024) return `${fileSize} B`
  if (fileSize < 1024 * 1024) return `${(fileSize / 1024).toFixed(1)} KB`
  return `${(fileSize / (1024 * 1024)).toFixed(2)} MB`
}

function beforeUpload(rawFile: File) {
  if (!isAllowedFileType(rawFile)) {
    ElMessage.warning('仅支持上传图片和视频文件')
    return false
  }
  return true
}

async function handleUpload(options: UploadRequestOptions) {
  const rawFile = options.file as File
  if (!beforeUpload(rawFile)) {
    options.onError?.(new Error('invalid file type') as any)
    return
  }

  const localPreviewUrl = isImage(rawFile.type) ? URL.createObjectURL(rawFile) : undefined
  uploadLoading.value = true
  try {
    const res = await uploadFile({
      file: rawFile,
      // 报修在创建前未绑定 bizId，按 UNBOUND 上传后通过 attachmentFileIds 进行引用。
      bizType: 'UNBOUND',
    })
    const file = res.data
    attachments.value.push({
      id: file.id,
      name: file.originFileName || file.fileName || `file-${file.id}`,
      contentType: file.contentType || rawFile.type,
      fileSize: file.fileSize || rawFile.size,
      previewUrl: localPreviewUrl,
    })
    form.attachmentFileIds = attachments.value.map((item) => item.id)
    ElMessage.success('附件上传成功')
    options.onSuccess?.(res)
  } catch (error) {
    if (localPreviewUrl) {
      URL.revokeObjectURL(localPreviewUrl)
    }
    options.onError?.(error as any)
  } finally {
    uploadLoading.value = false
  }
}

function removeAttachment(index: number) {
  const [removed] = attachments.value.splice(index, 1)
  if (removed?.previewUrl) {
    URL.revokeObjectURL(removed.previewUrl)
  }
  form.attachmentFileIds = attachments.value.length
    ? attachments.value.map((item) => item.id)
    : undefined
}

async function handleSubmit() {
  if (!form.title.trim()) {
    ElMessage.warning('请填写报修标题')
    return
  }
  if (!form.description.trim()) {
    ElMessage.warning('请描述报修内容')
    return
  }
  if (!form.contactPhone.trim()) {
    ElMessage.warning('请填写联系电话')
    return
  }
  if (!form.repairAddress.trim()) {
    ElMessage.warning('请填写报修地址')
    return
  }
  if (!form.complexOrgId) {
    ElMessage.warning('未获取到居民所在小区，请先完善档案信息')
    return
  }

  form.attachmentFileIds = attachments.value.length
    ? attachments.value.map((item) => item.id)
    : undefined

  loading.value = true
  try {
    await createRepair(form)
    ElMessage.success('报修提交成功')
    router.push('/repair')
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    const res = await getMyProfile()
    form.complexOrgId = res.data.complexOrgId
  } catch {
    // 非居民角色或档案缺失时由提交前校验提示
  }
})

onBeforeUnmount(() => {
  attachments.value.forEach((item) => {
    if (item.previewUrl) {
      URL.revokeObjectURL(item.previewUrl)
    }
  })
})
</script>

<template>
  <div class="min-h-screen pt-24 pb-12 px-4 md:px-8 lg:px-16">
    <div class="max-w-2xl mx-auto">
      <button
        class="flex items-center gap-2 text-white/50 text-sm font-body mb-6 hover:text-white transition-colors cursor-pointer"
        @click="router.push('/repair')"
      >
        <ArrowLeft :size="16" />
        返回我的报修
      </button>

      <div class="liquid-glass rounded-3xl p-6 md:p-10">
        <h1 class="text-2xl md:text-3xl font-heading italic text-white tracking-tight mb-8">发起报修</h1>

        <form class="space-y-6" @submit.prevent="handleSubmit">
          <div>
            <label class="block text-white/70 text-xs font-body mb-2 uppercase tracking-wider">报修标题</label>
            <input
              v-model="form.title"
              placeholder="如：客厅水管漏水"
              class="w-full px-4 py-3 rounded-xl bg-white/5 border border-white/10 text-white text-sm font-body placeholder-white/30 focus:outline-none focus:border-white/30 focus:bg-white/10 transition-all"
            />
          </div>

          <div>
            <label class="block text-white/70 text-xs font-body mb-2 uppercase tracking-wider">详细描述</label>
            <textarea
              v-model="form.description"
              rows="4"
              placeholder="请详细描述问题情况..."
              class="w-full px-4 py-3 rounded-xl bg-white/5 border border-white/10 text-white text-sm font-body placeholder-white/30 resize-none focus:outline-none focus:border-white/30 focus:bg-white/10 transition-all"
            />
          </div>

          <div>
            <label class="block text-white/70 text-xs font-body mb-2 uppercase tracking-wider">联系电话</label>
            <input
              v-model="form.contactPhone"
              type="tel"
              placeholder="请输入联系电话"
              class="w-full px-4 py-3 rounded-xl bg-white/5 border border-white/10 text-white text-sm font-body placeholder-white/30 focus:outline-none focus:border-white/30 focus:bg-white/10 transition-all"
            />
          </div>

          <div>
            <label class="block text-white/70 text-xs font-body mb-2 uppercase tracking-wider">报修地址</label>
            <input
              v-model="form.repairAddress"
              placeholder="如：3号楼 2单元 501室"
              class="w-full px-4 py-3 rounded-xl bg-white/5 border border-white/10 text-white text-sm font-body placeholder-white/30 focus:outline-none focus:border-white/30 focus:bg-white/10 transition-all"
            />
          </div>

          <div>
            <label class="block text-white/70 text-xs font-body mb-2 uppercase tracking-wider">紧急程度</label>
            <div class="flex gap-3">
              <button
                v-for="opt in emergencyOptions"
                :key="opt.value"
                type="button"
                :class="[
                  'px-4 py-2.5 rounded-full text-sm font-body transition-all cursor-pointer',
                  form.emergencyLevel === opt.value
                    ? 'liquid-glass-strong text-white font-medium'
                    : 'bg-white/5 text-white/50 hover:bg-white/10',
                ]"
                @click="form.emergencyLevel = opt.value"
              >
                {{ opt.label }}
              </button>
            </div>
          </div>

          <div>
            <label class="block text-white/70 text-xs font-body mb-2 uppercase tracking-wider">现场图片 / 视频</label>
            <div class="rounded-2xl border border-white/10 bg-white/[0.03] p-4">
              <el-upload
                :show-file-list="false"
                :http-request="handleUpload"
                :multiple="true"
                accept="image/*,video/*"
              >
                <button
                  type="button"
                  class="liquid-glass inline-flex cursor-pointer items-center gap-2 rounded-full px-4 py-2 text-sm font-body text-white/85"
                >
                  <Upload :size="15" />
                  上传图片或视频
                </button>
              </el-upload>
              <p class="mt-2 text-xs font-body text-white/45">
                支持 JPG/PNG/WebP/GIF、MP4/MOV 等常见格式。
              </p>

              <div v-if="attachments.length" class="mt-3 space-y-2">
                <div
                  v-for="(item, index) in attachments"
                  :key="item.id"
                  class="flex items-center justify-between gap-3 rounded-xl border border-white/12 bg-white/[0.02] px-3 py-2"
                >
                  <div class="flex min-w-0 items-center gap-3">
                    <img
                      v-if="isImage(item.contentType) && item.previewUrl"
                      :src="item.previewUrl"
                      :alt="item.name"
                      class="h-10 w-10 shrink-0 rounded-lg object-cover"
                    >
                    <div
                      v-else
                      class="flex h-10 w-10 shrink-0 items-center justify-center rounded-lg bg-white/8 text-white/70"
                    >
                      <Film v-if="isVideo(item.contentType)" :size="16" />
                      <FileImage v-else :size="16" />
                    </div>

                    <div class="min-w-0">
                      <p class="truncate text-sm font-body text-white/85">{{ item.name }}</p>
                      <p class="text-xs font-body text-white/45">
                        {{ isImage(item.contentType) ? '图片' : '视频' }} · {{ formatFileSize(item.fileSize) }}
                      </p>
                    </div>
                  </div>

                  <button
                    type="button"
                    class="cursor-pointer text-xs font-body text-rose-200/90 transition-colors hover:text-rose-100"
                    @click="removeAttachment(index)"
                  >
                    删除
                  </button>
                </div>
              </div>
            </div>
          </div>

          <button
            type="submit"
            :disabled="loading || uploadLoading"
            class="w-full py-3 rounded-full liquid-glass-strong text-white text-sm font-body font-medium flex items-center justify-center gap-2 hover:scale-[1.02] hover:shadow-lg transition-all cursor-pointer disabled:opacity-50"
          >
            <Send :size="18" />
            {{ loading ? '提交中...' : '提交报修' }}
          </button>
        </form>
      </div>
    </div>
  </div>
</template>
