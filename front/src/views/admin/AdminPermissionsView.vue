<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import AdminPageShell from '@/components/admin/AdminPageShell.vue'
import AdminTablePager from '@/components/admin/AdminTablePager.vue'
import { useAdminTable } from '@/composables/useAdminTable'
import { useActionPermission } from '@/composables/useActionPermission'
import {
  createPermission,
  deletePermission,
  permissionDetail,
  permissionPage,
  updatePermission,
} from '@/api/system'
import type { PermissionCreateRequest, PermissionUpdateRequest, PermissionVO } from '@/types/system'

const { can } = useActionPermission()

const table = useAdminTable<PermissionVO, { current: number; size: number; keyword: string }>(
  { current: 1, size: 10, keyword: '' },
  (query) => permissionPage(query),
)

const parentOptions = ref<PermissionVO[]>([])
const detailVisible = ref(false)
const detail = ref<PermissionVO | null>(null)

const editVisible = ref(false)
const saving = ref(false)
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const form = reactive({
  permissionCode: '',
  permissionName: '',
  permissionType: 'API',
  parentId: undefined as number | undefined,
  path: '',
  method: '',
  sort: 0,
})

const formRules: FormRules = {
  permissionCode: [{ required: true, message: '请输入权限编码', trigger: 'blur' }],
  permissionName: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
  permissionType: [{ required: true, message: '请选择权限类型', trigger: 'change' }],
}

async function loadParentOptions() {
  const res = await permissionPage({ current: 1, size: 1000, keyword: '' })
  parentOptions.value = Array.isArray(res.data.records) ? res.data.records : []
}

function resetForm() {
  editingId.value = null
  form.permissionCode = ''
  form.permissionName = ''
  form.permissionType = 'API'
  form.parentId = undefined
  form.path = ''
  form.method = ''
  form.sort = 0
}

async function openDetail(id: number) {
  const res = await permissionDetail(id)
  detail.value = res.data
  detailVisible.value = true
}

function openCreate() {
  resetForm()
  editVisible.value = true
}

async function openEdit(id: number) {
  resetForm()
  const res = await permissionDetail(id)
  const row = res.data
  editingId.value = row.id
  form.permissionCode = row.permissionCode
  form.permissionName = row.permissionName
  form.permissionType = row.permissionType
  form.parentId = row.parentId || undefined
  form.path = row.path || ''
  form.method = row.method || ''
  form.sort = row.sort || 0
  editVisible.value = true
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (editingId.value) {
      const payload: PermissionUpdateRequest = {
        id: editingId.value,
        permissionName: form.permissionName.trim(),
        permissionType: form.permissionType,
        parentId: form.parentId || null,
        path: form.path.trim() || undefined,
        method: form.method.trim().toUpperCase() || undefined,
        sort: form.sort,
      }
      await updatePermission(payload)
      ElMessage.success('权限更新成功')
    } else {
      const payload: PermissionCreateRequest = {
        permissionCode: form.permissionCode.trim(),
        permissionName: form.permissionName.trim(),
        permissionType: form.permissionType,
        parentId: form.parentId || null,
        path: form.path.trim() || undefined,
        method: form.method.trim().toUpperCase() || undefined,
        sort: form.sort,
      }
      await createPermission(payload)
      ElMessage.success('权限创建成功')
    }
    editVisible.value = false
    await Promise.all([table.load(), loadParentOptions()])
  } finally {
    saving.value = false
  }
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确认删除该权限吗？', '提示', { type: 'warning' })
  await deletePermission(id)
  ElMessage.success('删除成功')
  await Promise.all([table.load(), loadParentOptions()])
}

onMounted(async () => {
  await Promise.all([table.load(), loadParentOptions()])
})
</script>

<template>
  <AdminPageShell title="权限管理" description="维护系统权限点与路由/接口权限码。">
    <div class="liquid-glass rounded-3xl p-4 md:p-5 space-y-4">
      <el-form inline>
        <el-form-item>
          <el-input v-model="table.query.keyword" placeholder="权限编码/名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="table.search">查询</el-button>
          <el-button @click="table.resetQuery(); table.search()">重置</el-button>
          <el-button v-if="can('sys:permission:create')" type="success" @click="openCreate">新建权限</el-button>
        </el-form-item>
      </el-form>

      <el-alert v-if="table.error.value" type="error" :closable="false" :title="table.error.value" />

      <el-table :data="table.records.value" v-loading="table.loading.value" row-key="id" border class="admin-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="permissionCode" label="权限编码" min-width="220" show-overflow-tooltip />
        <el-table-column prop="permissionName" label="权限名称" min-width="180" />
        <el-table-column prop="permissionType" label="类型" width="120" />
        <el-table-column prop="path" label="路径" min-width="180" show-overflow-tooltip />
        <el-table-column prop="method" label="方法" width="100" />
        <el-table-column prop="sort" label="排序" width="90" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <el-space wrap>
              <el-button size="small" @click="openDetail(scope.row.id)">详情</el-button>
              <el-button v-if="can('sys:permission:update')" size="small" type="primary" @click="openEdit(scope.row.id)">编辑</el-button>
              <el-button v-if="can('sys:permission:delete')" size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>

      <AdminTablePager
        :current="table.query.current"
        :size="table.query.size"
        :total="table.total.value"
        @update:current="table.changePage"
        @update:size="(s) => { table.query.size = s; table.search() }"
      />
    </div>

    <el-drawer v-model="detailVisible" title="权限详情" size="40%">
      <div v-if="detail" class="space-y-2 text-sm">
        <p><strong>编码：</strong>{{ detail.permissionCode }}</p>
        <p><strong>名称：</strong>{{ detail.permissionName }}</p>
        <p><strong>类型：</strong>{{ detail.permissionType }}</p>
        <p><strong>父ID：</strong>{{ detail.parentId || '-' }}</p>
        <p><strong>路径：</strong>{{ detail.path || '-' }}</p>
        <p><strong>方法：</strong>{{ detail.method || '-' }}</p>
      </div>
    </el-drawer>

    <el-dialog v-model="editVisible" :title="editingId ? '编辑权限' : '新建权限'" width="720px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="120px">
        <el-form-item v-if="!editingId" label="权限编码" prop="permissionCode">
          <el-input v-model="form.permissionCode" />
        </el-form-item>
        <el-form-item label="权限名称" prop="permissionName">
          <el-input v-model="form.permissionName" />
        </el-form-item>
        <el-form-item label="权限类型" prop="permissionType">
          <el-select v-model="form.permissionType" style="width: 200px">
            <el-option label="MENU" value="MENU" />
            <el-option label="BUTTON" value="BUTTON" />
            <el-option label="API" value="API" />
          </el-select>
        </el-form-item>
        <el-form-item label="父节点">
          <el-select v-model="form.parentId" filterable clearable style="width: 100%">
            <el-option
              v-for="item in parentOptions.filter((p) => !editingId || p.id !== editingId)"
              :key="item.id"
              :label="`${item.permissionName} (${item.permissionCode})`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="路径">
          <el-input v-model="form.path" />
        </el-form-item>
        <el-form-item label="方法">
          <el-input v-model="form.method" placeholder="GET/POST/PUT/DELETE" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitForm">保存</el-button>
      </template>
    </el-dialog>
  </AdminPageShell>
</template>

<style scoped>
:deep(.admin-table .el-table__cell) {
  background: rgba(8, 18, 32, 0.46);
  color: #f5f7fa;
}

:deep(.admin-table .el-table__header .el-table__cell) {
  background: rgba(8, 18, 32, 0.72);
  color: rgba(255, 255, 255, 0.8);
}
</style>
