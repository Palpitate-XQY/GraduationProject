<script setup lang="ts">
/**
 * RepairCreateView - 发起报修
 * 与后端 RepairCreateRequest 保持一致，自动注入 complexOrgId。
 */
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Send } from 'lucide-vue-next'
import { createRepair } from '@/api/repair'
import { getMyProfile } from '@/api/profile'
import type { RepairCreateRequest } from '@/types/repair'

const router = useRouter()
const loading = ref(false)

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

          <button
            type="submit"
            :disabled="loading"
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
