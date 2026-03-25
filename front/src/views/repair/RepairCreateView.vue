<script setup lang="ts">
/**
 * RepairCreateView — 发起报修
 * 表单页面，提交到 createRepair API
 */
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Send } from 'lucide-vue-next'
import { createRepair } from '@/api/repair'
import type { RepairCreateRequest } from '@/types/repair'

const router = useRouter()
const loading = ref(false)

const form = reactive<RepairCreateRequest>({
  title: '',
  description: '',
  contactPhone: '',
  repairAddress: '',
  emergencyLevel: 'LOW',
})

const emergencyOptions = [
  { label: '低', value: 'LOW' },
  { label: '中', value: 'MEDIUM' },
  { label: '高（紧急）', value: 'HIGH' },
]

async function handleSubmit() {
  if (!form.title.trim()) { ElMessage.warning('请填写报修标题'); return }
  if (!form.description.trim()) { ElMessage.warning('请描述报修内容'); return }
  if (!form.contactPhone.trim()) { ElMessage.warning('请填写联系电话'); return }
  if (!form.repairAddress.trim()) { ElMessage.warning('请填写报修地址'); return }

  loading.value = true
  try {
    await createRepair(form)
    ElMessage.success('报修提交成功')
    router.push('/repair')
  } catch { /* handled */ } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen pt-24 pb-12 px-4 md:px-8 lg:px-16">
    <div class="max-w-2xl mx-auto">
      <button
        class="flex items-center gap-2 text-white/50 text-sm font-body mb-6
               hover:text-white transition-colors cursor-pointer"
        @click="router.push('/repair')"
      >
        <ArrowLeft :size="16" /> 返回我的报修
      </button>

      <div class="liquid-glass rounded-3xl p-6 md:p-10">
        <h1 class="text-2xl md:text-3xl font-heading italic text-white tracking-tight mb-8">
          发起报修
        </h1>

        <form @submit.prevent="handleSubmit" class="space-y-6">
          <!-- 标题 -->
          <div>
            <label class="block text-white/70 text-xs font-body mb-2 uppercase tracking-wider">报修标题</label>
            <input
              v-model="form.title" placeholder="如：客厅水管漏水"
              class="w-full px-4 py-3 rounded-xl bg-white/5 border border-white/10
                     text-white text-sm font-body placeholder-white/30
                     focus:outline-none focus:border-white/30 focus:bg-white/10 transition-all"
            />
          </div>

          <!-- 描述 -->
          <div>
            <label class="block text-white/70 text-xs font-body mb-2 uppercase tracking-wider">详细描述</label>
            <textarea
              v-model="form.description" placeholder="请详细描述问题情况..." rows="4"
              class="w-full px-4 py-3 rounded-xl bg-white/5 border border-white/10
                     text-white text-sm font-body placeholder-white/30 resize-none
                     focus:outline-none focus:border-white/30 focus:bg-white/10 transition-all"
            />
          </div>

          <!-- 联系电话 -->
          <div>
            <label class="block text-white/70 text-xs font-body mb-2 uppercase tracking-wider">联系电话</label>
            <input
              v-model="form.contactPhone" placeholder="请输入联系电话" type="tel"
              class="w-full px-4 py-3 rounded-xl bg-white/5 border border-white/10
                     text-white text-sm font-body placeholder-white/30
                     focus:outline-none focus:border-white/30 focus:bg-white/10 transition-all"
            />
          </div>

          <!-- 报修地址 -->
          <div>
            <label class="block text-white/70 text-xs font-body mb-2 uppercase tracking-wider">报修地址</label>
            <input
              v-model="form.repairAddress" placeholder="如：3 号楼 2 单元 501 室"
              class="w-full px-4 py-3 rounded-xl bg-white/5 border border-white/10
                     text-white text-sm font-body placeholder-white/30
                     focus:outline-none focus:border-white/30 focus:bg-white/10 transition-all"
            />
          </div>

          <!-- 紧急程度 -->
          <div>
            <label class="block text-white/70 text-xs font-body mb-2 uppercase tracking-wider">紧急程度</label>
            <div class="flex gap-3">
              <button
                v-for="opt in emergencyOptions" :key="opt.value" type="button"
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

          <!-- 提交 -->
          <button
            type="submit" :disabled="loading"
            class="w-full py-3 rounded-full liquid-glass-strong text-white text-sm
                   font-body font-medium flex items-center justify-center gap-2
                   hover:scale-[1.02] hover:shadow-lg transition-all cursor-pointer
                   disabled:opacity-50"
          >
            <Send :size="18" />
            {{ loading ? '提交中...' : '提交报修' }}
          </button>
        </form>
      </div>
    </div>
  </div>
</template>
