<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import AdminPageShell from '@/components/admin/AdminPageShell.vue'
import AdminTablePager from '@/components/admin/AdminTablePager.vue'
import { useAdminTable } from '@/composables/useAdminTable'
import { useActionPermission } from '@/composables/useActionPermission'
import { useOrgOptions } from '@/composables/useOrgOptions'
import { createUser, deleteUser, updateUser, updateUserStatus, userDetail, userPage } from '@/api/system'
import { rolePage } from '@/api/system'
import type { RoleVO, UserCreateRequest, UserUpdateRequest, UserVO } from '@/types/system'

const { can } = useActionPermission()
const { options: orgOptions, loadOrgOptions } = useOrgOptions()

const table = useAdminTable<UserVO, { current: number; size: number; keyword: string; status?: number; orgId?: number }>(
  { current: 1, size: 10, keyword: '', status: undefined, orgId: undefined },
  (query) => userPage(query),
)

const roles = ref<RoleVO[]>([])
const detailVisible = ref(false)
const detail = ref<UserVO | null>(null)

const editVisible = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  username: '',
  password: '',
  nickname: '',
  phone: '',
  email: '',
  orgId: undefined as number | undefined,
  status: 1,
  roleIds: [] as number[],
})

const formRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 4, max: 32, message: '用户名长度需在 4-32 位', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入初始密码', trigger: 'blur' },
    { min: 8, max: 64, message: '密码长度需在 8-64 位', trigger: 'blur' },
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  orgId: [{ required: true, message: '请选择组织', trigger: 'change' }],
  roleIds: [{ required: true, type: 'array', min: 1, message: '请至少选择一个角色', trigger: 'change' }],
}

function resetForm() {
  editingId.value = null
  form.username = ''
  form.password = ''
  form.nickname = ''
  form.phone = ''
  form.email = ''
  form.orgId = undefined
  form.status = 1
  form.roleIds = []
}

async function loadRoles() {
  try {
    const res = await rolePage({ current: 1, size: 100 })
    roles.value = Array.isArray(res.data.records) ? res.data.records : []
  } catch {
    roles.value = []
  }
}

async function openDetail(id: number) {
  const res = await userDetail(id)
  detail.value = res.data
  detailVisible.value = true
}

function openCreate() {
  resetForm()
  editVisible.value = true
}

async function openEdit(id: number) {
  resetForm()
  const res = await userDetail(id)
  const row = res.data
  editingId.value = row.id
  form.username = row.username
  form.nickname = row.nickname
  form.phone = row.phone || ''
  form.email = row.email || ''
  form.orgId = row.orgId
  form.status = row.status
  form.roleIds = row.roleIds || []
  editVisible.value = true
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (editingId.value) {
      const payload: UserUpdateRequest = {
        id: editingId.value,
        nickname: form.nickname.trim(),
        phone: form.phone.trim() || undefined,
        email: form.email.trim() || undefined,
        orgId: form.orgId!,
        status: form.status,
        roleIds: form.roleIds,
      }
      await updateUser(payload)
      ElMessage.success('用户更新成功')
    } else {
      const payload: UserCreateRequest = {
        username: form.username.trim(),
        password: form.password.trim(),
        nickname: form.nickname.trim(),
        phone: form.phone.trim() || undefined,
        email: form.email.trim() || undefined,
        orgId: form.orgId!,
        status: form.status,
        roleIds: form.roleIds,
      }
      await createUser(payload)
      ElMessage.success('用户创建成功')
    }
    editVisible.value = false
    await table.load()
  } finally {
    saving.value = false
  }
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确认删除该用户吗？', '提示', { type: 'warning' })
  await deleteUser(id)
  ElMessage.success('删除成功')
  await table.load()
}

async function toggleStatus(row: UserVO) {
  const nextStatus = row.status === 1 ? 0 : 1
  await updateUserStatus(row.id, nextStatus)
  ElMessage.success('状态更新成功')
  await table.load()
}

onMounted(async () => {
  await Promise.all([table.load(), loadRoles(), loadOrgOptions()])
})
</script>

<template>
  <AdminPageShell title="用户管理" description="管理系统用户、状态与角色分配。">
    <div class="liquid-glass rounded-3xl p-4 md:p-5 space-y-4">
      <el-form inline>
        <el-form-item>
          <el-input v-model="table.query.keyword" placeholder="用户名/昵称" clearable />
        </el-form-item>
        <el-form-item>
          <el-select v-model="table.query.status" placeholder="状态" clearable style="width: 140px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="table.query.orgId" filterable clearable placeholder="组织" style="width: 220px">
            <el-option v-for="item in orgOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="table.search">查询</el-button>
          <el-button @click="table.resetQuery(); table.search()">重置</el-button>
          <el-button v-if="can('sys:user:create')" type="success" @click="openCreate">新建用户</el-button>
        </el-form-item>
      </el-form>

      <el-alert v-if="table.error.value" type="error" :closable="false" :title="table.error.value" />

      <el-table :data="table.records.value" v-loading="table.loading.value" row-key="id" border class="admin-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="nickname" label="昵称" width="140" />
        <el-table-column prop="orgName" label="组织" min-width="180" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">{{ scope.row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="roleCodes" label="角色编码" min-width="200" show-overflow-tooltip>
          <template #default="scope">{{ (scope.row.roleCodes || []).join(',') }}</template>
        </el-table-column>
        <el-table-column label="操作" width="340" fixed="right">
          <template #default="scope">
            <el-space wrap>
              <el-button size="small" @click="openDetail(scope.row.id)">详情</el-button>
              <el-button v-if="can('sys:user:update')" size="small" type="primary" @click="openEdit(scope.row.id)">编辑</el-button>
              <el-button v-if="can('sys:user:update')" size="small" @click="toggleStatus(scope.row)">
                {{ scope.row.status === 1 ? '禁用' : '启用' }}
              </el-button>
              <el-button v-if="can('sys:user:delete')" size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
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

    <el-drawer v-model="detailVisible" title="用户详情" size="40%">
      <div class="space-y-2 text-sm" v-if="detail">
        <p><strong>ID：</strong>{{ detail.id }}</p>
        <p><strong>用户名：</strong>{{ detail.username }}</p>
        <p><strong>昵称：</strong>{{ detail.nickname }}</p>
        <p><strong>手机号：</strong>{{ detail.phone || '-' }}</p>
        <p><strong>邮箱：</strong>{{ detail.email || '-' }}</p>
        <p><strong>组织：</strong>{{ detail.orgName || detail.orgId }}</p>
        <p><strong>角色：</strong>{{ (detail.roleCodes || []).join(',') }}</p>
      </div>
    </el-drawer>

    <el-dialog v-model="editVisible" :title="editingId ? '编辑用户' : '新建用户'" width="760px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="120px">
        <el-form-item v-if="!editingId" label="用户名" prop="username">
          <el-input v-model="form.username" />
        </el-form-item>
        <el-form-item v-if="!editingId" label="初始密码" prop="password">
          <el-input v-model="form.password" type="password" show-password />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="组织" prop="orgId">
          <el-select v-model="form.orgId" filterable style="width: 100%">
            <el-option v-for="item in orgOptions" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 180px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="form.roleIds" multiple filterable style="width: 100%">
            <el-option v-for="item in roles" :key="item.id" :label="`${item.roleName} (${item.roleCode})`" :value="item.id" />
          </el-select>
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
