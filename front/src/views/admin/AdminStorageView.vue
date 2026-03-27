<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import AdminPageShell from '@/components/admin/AdminPageShell.vue'
import { getStorageConfig, updateStorageConfig } from '@/api/storage'

const loading = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  storageType: 'LOCAL',
  localBasePath: '',
  localAccessBaseUrl: '',
  qiniuAccessKey: '',
  qiniuSecretKey: '',
  qiniuBucket: '',
  qiniuRegion: '',
  qiniuDomain: '',
  remark: '',
})

const maskedSecret = ref('')

const isLocal = computed(() => form.storageType === 'LOCAL')
const isQiniu = computed(() => form.storageType === 'QINIU')

const formRules: FormRules = {
  storageType: [{ required: true, message: '请选择存储类型', trigger: 'change' }],
  localBasePath: [
    {
      validator: (_rule, value, callback) => {
        if (!isLocal.value || String(value || '').trim()) return callback()
        callback(new Error('LOCAL 模式下本地根目录不能为空'))
      },
      trigger: 'blur',
    },
  ],
  localAccessBaseUrl: [
    {
      validator: (_rule, value, callback) => {
        if (!isLocal.value || String(value || '').trim()) return callback()
        callback(new Error('LOCAL 模式下本地访问前缀不能为空'))
      },
      trigger: 'blur',
    },
  ],
  qiniuAccessKey: [
    {
      validator: (_rule, value, callback) => {
        if (!isQiniu.value || String(value || '').trim()) return callback()
        callback(new Error('QINIU 模式下 AccessKey 不能为空'))
      },
      trigger: 'blur',
    },
  ],
  qiniuBucket: [
    {
      validator: (_rule, value, callback) => {
        if (!isQiniu.value || String(value || '').trim()) return callback()
        callback(new Error('QINIU 模式下 Bucket 不能为空'))
      },
      trigger: 'blur',
    },
  ],
  qiniuRegion: [
    {
      validator: (_rule, value, callback) => {
        if (!isQiniu.value || String(value || '').trim()) return callback()
        callback(new Error('QINIU 模式下 Region 不能为空'))
      },
      trigger: 'blur',
    },
  ],
  qiniuDomain: [
    {
      validator: (_rule, value, callback) => {
        if (!isQiniu.value || String(value || '').trim()) return callback()
        callback(new Error('QINIU 模式下 Domain 不能为空'))
      },
      trigger: 'blur',
    },
  ],
}

async function fetchConfig() {
  loading.value = true
  try {
    const res = await getStorageConfig()
    const data = res.data
    form.storageType = data.storageType || 'LOCAL'
    form.localBasePath = data.localBasePath || ''
    form.localAccessBaseUrl = data.localAccessBaseUrl || ''
    form.qiniuAccessKey = data.qiniuAccessKey || ''
    form.qiniuBucket = data.qiniuBucket || ''
    form.qiniuRegion = data.qiniuRegion || ''
    form.qiniuDomain = data.qiniuDomain || ''
    form.remark = data.remark || ''
    maskedSecret.value = data.qiniuSecretKeyMasked || ''
    form.qiniuSecretKey = ''
  } finally {
    loading.value = false
  }
}

async function submit() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    await updateStorageConfig({
      storageType: form.storageType,
      localBasePath: form.localBasePath.trim() || undefined,
      localAccessBaseUrl: form.localAccessBaseUrl.trim() || undefined,
      qiniuAccessKey: form.qiniuAccessKey.trim() || undefined,
      qiniuSecretKey: form.qiniuSecretKey.trim() || undefined,
      qiniuBucket: form.qiniuBucket.trim() || undefined,
      qiniuRegion: form.qiniuRegion.trim() || undefined,
      qiniuDomain: form.qiniuDomain.trim() || undefined,
      remark: form.remark.trim() || undefined,
    })
    ElMessage.success('存储配置更新成功')
    form.qiniuSecretKey = ''
    await fetchConfig()
  } finally {
    saving.value = false
  }
}

onMounted(() => {
  void fetchConfig()
})
</script>

<template>
  <AdminPageShell title="存储配置" description="维护 LOCAL/QINIU 存储参数与访问域名。">
    <div class="liquid-glass rounded-3xl p-6 md:p-7" v-loading="loading">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="150px" class="max-w-3xl">
        <el-form-item label="存储类型" prop="storageType">
          <el-select v-model="form.storageType" style="width: 220px">
            <el-option label="LOCAL" value="LOCAL" />
            <el-option label="QINIU" value="QINIU" />
          </el-select>
        </el-form-item>

        <el-form-item label="本地根目录" prop="localBasePath">
          <el-input v-model="form.localBasePath" :disabled="!isLocal" />
        </el-form-item>
        <el-form-item label="本地访问前缀" prop="localAccessBaseUrl">
          <el-input v-model="form.localAccessBaseUrl" :disabled="!isLocal" />
        </el-form-item>

        <el-divider />

        <el-form-item label="七牛 AccessKey" prop="qiniuAccessKey">
          <el-input v-model="form.qiniuAccessKey" :disabled="!isQiniu" />
        </el-form-item>
        <el-form-item label="七牛 SecretKey">
          <el-input
            v-model="form.qiniuSecretKey"
            type="password"
            show-password
            :disabled="!isQiniu"
            placeholder="留空则不更新"
          />
          <p class="mt-1 text-xs text-white/60">当前密钥：{{ maskedSecret || '-' }}</p>
        </el-form-item>
        <el-form-item label="七牛 Bucket" prop="qiniuBucket">
          <el-input v-model="form.qiniuBucket" :disabled="!isQiniu" />
        </el-form-item>
        <el-form-item label="七牛 Region" prop="qiniuRegion">
          <el-input v-model="form.qiniuRegion" :disabled="!isQiniu" />
        </el-form-item>
        <el-form-item label="七牛 Domain" prop="qiniuDomain">
          <el-input v-model="form.qiniuDomain" :disabled="!isQiniu" />
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>

        <el-form-item>
          <el-button type="primary" :loading="saving" @click="submit">保存配置</el-button>
        </el-form-item>
      </el-form>
    </div>
  </AdminPageShell>
</template>
