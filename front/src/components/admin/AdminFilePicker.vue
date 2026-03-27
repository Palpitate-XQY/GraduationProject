<script setup lang="ts">
import { computed, reactive, watch } from 'vue'
import { ElMessage } from 'element-plus'
import type { UploadRequestOptions } from 'element-plus'
import { getFileDetail, uploadFile } from '@/api/file'

interface FileToken {
  id: number
  name: string
}

interface Props {
  modelValue: number[]
  multiple?: boolean
  bizType?: string
}

const props = withDefaults(defineProps<Props>(), {
  multiple: true,
  bizType: 'ADMIN',
})

const emit = defineEmits<{
  'update:modelValue': [value: number[]]
}>()

const fileMap = reactive<Record<number, FileToken>>({})

watch(
  () => props.modelValue,
  async (ids) => {
    for (const id of ids) {
      if (!fileMap[id]) {
        fileMap[id] = { id, name: `file-${id}` }
        try {
          const res = await getFileDetail(id)
          const file = res.data
          fileMap[id] = {
            id,
            name: file.originFileName || file.fileName || `file-${id}`,
          }
        } catch {
          // Keep fallback file name.
        }
      }
    }
    Object.keys(fileMap).forEach((key) => {
      const id = Number(key)
      if (!ids.includes(id)) {
        delete fileMap[id]
      }
    })
  },
  { immediate: true },
)

const tokens = computed(() => props.modelValue.map((id) => fileMap[id]).filter(Boolean))

async function handleUpload(options: UploadRequestOptions) {
  const raw = options.file as File
  try {
    const res = await uploadFile({ file: raw, bizType: props.bizType })
    const token = res.data
    fileMap[token.id] = {
      id: token.id,
      name: token.originFileName || token.fileName || `file-${token.id}`,
    }
    const exists = props.modelValue.includes(token.id)
    const next = props.multiple
      ? exists
        ? props.modelValue
        : [...props.modelValue, token.id]
      : [token.id]
    emit('update:modelValue', next)
    ElMessage.success('文件上传成功')
    options.onSuccess?.(res)
  } catch (e) {
    options.onError?.(e as any)
  }
}

function removeFile(id: number) {
  emit(
    'update:modelValue',
    props.modelValue.filter((item) => item !== id),
  )
}
</script>

<template>
  <div class="space-y-2">
    <el-upload :show-file-list="false" :http-request="handleUpload" :multiple="props.multiple">
      <el-button type="primary" plain size="small">上传文件</el-button>
    </el-upload>
    <div class="flex flex-wrap gap-2">
      <el-tag v-for="token in tokens" :key="token.id" closable @close="removeFile(token.id)">
        {{ token.name }} ({{ token.id }})
      </el-tag>
    </div>
  </div>
</template>

