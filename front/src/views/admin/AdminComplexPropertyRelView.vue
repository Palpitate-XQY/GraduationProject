<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AdminPageShell from '@/components/admin/AdminPageShell.vue'
import { useActionPermission } from '@/composables/useActionPermission'
import { useOrgOptions } from '@/composables/useOrgOptions'
import {
  createComplexPropertyRel,
  deleteComplexPropertyRel,
  listComplexPropertyRel,
} from '@/api/org'
import type { ComplexPropertyRelVO } from '@/types/org'

const { can } = useActionPermission()
const { options: orgOptions, byType: orgByType, loadOrgOptions } = useOrgOptions()

const complexOrgId = ref<number | undefined>()
const loading = ref(false)
const rows = ref<ComplexPropertyRelVO[]>([])
const error = ref('')

const createVisible = ref(false)
const creating = ref(false)
const newPropertyOrgId = ref<number | undefined>()

const complexOptions = computed(() => orgByType.value.COMPLEX || [])
const propertyOptions = computed(() => orgByType.value.PROPERTY_COMPANY || [])

const selectedComplex = computed(() =>
  complexOptions.value.find((item) => item.value === complexOrgId.value),
)

const rowsView = computed(() => {
  const optionMap = new Map<number, string>()
  orgOptions.value.forEach((item) => {
    optionMap.set(item.value, item.label)
  })
  return rows.value.map((row) => ({
    ...row,
    complexOrgName: optionMap.get(row.complexOrgId) || `组织ID ${row.complexOrgId}`,
    propertyOrgName: optionMap.get(row.propertyCompanyOrgId) || `组织ID ${row.propertyCompanyOrgId}`,
  }))
})

const activeCount = computed(() => rows.value.filter((item) => item.status === 1).length)
const inactiveCount = computed(() => rows.value.filter((item) => item.status !== 1).length)

async function fetchList(showSelectWarning = true) {
  if (!complexOrgId.value) {
    if (showSelectWarning) {
      ElMessage.warning('请先选择小区组织')
    }
    rows.value = []
    return
  }
  loading.value = true
  error.value = ''
  try {
    const res = await listComplexPropertyRel(complexOrgId.value)
    rows.value = Array.isArray(res.data) ? res.data : []
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
    rows.value = []
  } finally {
    loading.value = false
  }
}

function openCreateDialog() {
  if (!complexOrgId.value) {
    ElMessage.warning('请先选择小区组织，再新增关联')
    return
  }
  newPropertyOrgId.value = undefined
  createVisible.value = true
}

async function submitCreate() {
  if (!complexOrgId.value || !newPropertyOrgId.value) {
    ElMessage.warning('请选择小区组织和物业公司组织')
    return
  }
  const existed = rows.value.some(
    (item) => item.propertyCompanyOrgId === newPropertyOrgId.value && item.status === 1,
  )
  if (existed) {
    ElMessage.warning('该小区已存在该物业公司的有效关联')
    return
  }

  creating.value = true
  try {
    await createComplexPropertyRel({
      complexOrgId: complexOrgId.value,
      propertyCompanyOrgId: newPropertyOrgId.value,
    })
    ElMessage.success('关联创建成功')
    createVisible.value = false
    newPropertyOrgId.value = undefined
    await fetchList(false)
  } finally {
    creating.value = false
  }
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确认删除该关联吗？', '提示', { type: 'warning' })
  await deleteComplexPropertyRel(id)
  ElMessage.success('删除成功')
  await fetchList(false)
}

onMounted(() => {
  void loadOrgOptions()
})

watch(complexOrgId, async (value) => {
  error.value = ''
  if (!value) {
    rows.value = []
    return
  }
  await fetchList(false)
})
</script>

<template>
  <AdminPageShell title="小区-物业关联" description="先选小区，再查看和维护服务该小区的物业公司关系。">
    <div class="liquid-glass rounded-3xl p-4 md:p-5 space-y-4">
      <el-form inline class="mb-2">
        <el-form-item label="当前小区">
          <el-select v-model="complexOrgId" filterable clearable placeholder="请选择小区组织" style="width: 320px">
            <el-option v-for="item in complexOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchList(true)">刷新关联</el-button>
          <el-button
            v-if="can('org:complex-property:create')"
            type="success"
            :disabled="!complexOrgId"
            @click="openCreateDialog"
          >
            新增关联
          </el-button>
        </el-form-item>
      </el-form>

      <div class="grid gap-3 md:grid-cols-3">
        <div class="relation-stat-card rounded-2xl px-4 py-3">
          <p class="text-xs font-body text-white/55">已选小区</p>
          <p class="mt-1 truncate text-sm font-body text-white">
            {{ selectedComplex?.label || '未选择' }}
          </p>
        </div>
        <div class="relation-stat-card rounded-2xl px-4 py-3">
          <p class="text-xs font-body text-white/55">有效关联数</p>
          <p class="mt-1 text-2xl font-heading italic text-white">{{ activeCount }}</p>
        </div>
        <div class="relation-stat-card rounded-2xl px-4 py-3">
          <p class="text-xs font-body text-white/55">停用关联数</p>
          <p class="mt-1 text-2xl font-heading italic text-white/80">{{ inactiveCount }}</p>
        </div>
      </div>

      <el-alert v-if="error" type="error" :closable="false" :title="error" />

      <el-empty
        v-if="!complexOrgId"
        description="请先选择一个小区，系统将自动加载该小区的物业关联。"
      />

      <el-table v-else :data="rowsView" v-loading="loading" border class="admin-table" row-key="id">
        <el-table-column prop="id" label="ID" width="90" />
        <el-table-column label="小区组织" min-width="260">
          <template #default="scope">
            <div class="flex items-center gap-2">
              <span class="inline-flex rounded-full bg-cyan-500/20 px-2 py-0.5 text-xs text-cyan-100">小区</span>
              <span class="truncate text-sm text-white/90">{{ scope.row.complexOrgName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="服务物业公司" min-width="280">
          <template #default="scope">
            <div class="flex items-center gap-2">
              <span class="inline-flex rounded-full bg-emerald-500/20 px-2 py-0.5 text-xs text-emerald-100">物业</span>
              <span class="truncate text-sm text-white/90">{{ scope.row.propertyOrgName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="130">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
              {{ scope.row.status === 1 ? '启用' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态说明" min-width="180">
          <template #default="scope">
            <span class="text-sm text-white/80">
              {{ scope.row.status === 1 ? '有效服务关系' : '停用关系' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="scope">
            <el-button
              v-if="can('org:complex-property:delete')"
              size="small"
              type="danger"
              @click="handleDelete(scope.row.id)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="createVisible" title="新增关联" width="560px" destroy-on-close>
      <el-form label-width="130px">
        <el-form-item label="小区组织">
          <el-select v-model="complexOrgId" filterable style="width: 100%">
            <el-option v-for="item in complexOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="物业公司组织">
          <el-select v-model="newPropertyOrgId" filterable style="width: 100%">
            <el-option v-for="item in propertyOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createVisible = false">取消</el-button>
        <el-button type="primary" :loading="creating" @click="submitCreate">保存</el-button>
      </template>
    </el-dialog>
  </AdminPageShell>
</template>

<style scoped>
.relation-stat-card {
  border: 1px solid rgba(255, 255, 255, 0.16);
  background: rgba(9, 24, 41, 0.34);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
}
</style>
