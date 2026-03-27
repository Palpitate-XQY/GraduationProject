<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import AdminPageShell from '@/components/admin/AdminPageShell.vue'
import AdminTablePager from '@/components/admin/AdminTablePager.vue'
import { useAdminTable } from '@/composables/useAdminTable'
import { useActionPermission } from '@/composables/useActionPermission'
import { useOrgOptions } from '@/composables/useOrgOptions'
import { useUserStore } from '@/stores/user'
import { DATA_SCOPE_TYPE_OPTIONS, isScopeTypeNeedOrgRef } from '@/constants/domain'
import { createRole, deleteRole, permissionPage, roleDetail, rolePage, updateRole } from '@/api/system'
import type { PermissionVO, RoleCreateRequest, RoleScopeConfigItem, RoleUpdateRequest, RoleVO } from '@/types/system'

const SCOPE_ORG_TYPE_MAP: Record<string, string | undefined> = {
  STREET: 'STREET',
  COMMUNITY: 'COMMUNITY',
  COMPLEX: 'COMPLEX',
  PROPERTY_COMPANY: 'PROPERTY_COMPANY',
}

interface ScopeConfigFormItem {
  scopeType: string
  scopeRefId?: number | null
}

const { can } = useActionPermission()
const { options: orgOptions, loadOrgOptions } = useOrgOptions()
const userStore = useUserStore()

const table = useAdminTable<RoleVO, { current: number; size: number; keyword: string }>(
  {
    current: 1,
    size: 10,
    keyword: '',
  },
  (query) => rolePage(query),
)

const roleOptions = ref<RoleVO[]>([])
const permissionOptions = ref<PermissionVO[]>([])
const editingId = ref<number | null>(null)

const canCreateRoleByPermission = computed(() => can('sys:role:create'))
const myAllowCreateParentRoles = computed(() => {
  const currentRoleCodes = new Set<string>(Array.isArray(userStore.roleCodes) ? userStore.roleCodes : [])
  return roleOptions.value.filter((role) => role.allowCreateChild === 1 && currentRoleCodes.has(role.roleCode))
})
const canCreateRole = computed(
  () => canCreateRoleByPermission.value || myAllowCreateParentRoles.value.length > 0,
)
const selectableParentRoles = computed(() => {
  const base = roleOptions.value.filter((role) => !editingId.value || role.id !== editingId.value)
  if (canCreateRoleByPermission.value) {
    return base
  }
  const allowedParentIds = new Set(myAllowCreateParentRoles.value.map((role) => role.id))
  return base.filter((role) => allowedParentIds.has(role.id))
})

const detailVisible = ref(false)
const detail = ref<RoleVO | null>(null)

const editVisible = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()
const scopeError = ref('')

const form = reactive({
  roleCode: '',
  roleName: '',
  parentRoleId: undefined as number | undefined,
  allowCreateChild: 0,
  status: 1,
  sort: 0,
  remark: '',
  permissionIds: [] as number[],
})
const scopeConfigs = ref<ScopeConfigFormItem[]>([])

const formRules: FormRules = {
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  permissionIds: [{ required: true, type: 'array', min: 1, message: '请至少选择一个权限', trigger: 'change' }],
}

function newScopeConfig(): ScopeConfigFormItem {
  return {
    scopeType: 'COMMUNITY',
    scopeRefId: undefined,
  }
}

function getScopeOrgOptions(scopeType: string) {
  const target = SCOPE_ORG_TYPE_MAP[scopeType]
  if (!target) return orgOptions.value
  const matched = orgOptions.value.filter((item) => item.orgType === target)
  return matched.length ? matched : orgOptions.value
}

function addScopeRow() {
  scopeConfigs.value.push(newScopeConfig())
}

function removeScopeRow(index: number) {
  scopeConfigs.value.splice(index, 1)
}

function resetForm() {
  editingId.value = null
  form.roleCode = ''
  form.roleName = ''
  form.parentRoleId = undefined
  form.allowCreateChild = 0
  form.status = 1
  form.sort = 0
  form.remark = ''
  form.permissionIds = []
  scopeConfigs.value = [newScopeConfig()]
  scopeError.value = ''
}

async function loadPermissions() {
  const res = await permissionPage({ current: 1, size: 1000, keyword: '' })
  permissionOptions.value = Array.isArray(res.data.records) ? res.data.records : []
}

async function loadRoleOptions() {
  const res = await rolePage({ current: 1, size: 500, keyword: '' })
  roleOptions.value = Array.isArray(res.data.records) ? res.data.records : []
}

async function openDetail(id: number) {
  const res = await roleDetail(id)
  detail.value = res.data
  detailVisible.value = true
}

function openCreate() {
  if (!canCreateRole.value) {
    ElMessage.warning('当前账号暂无角色创建能力')
    return
  }
  resetForm()
  if (!canCreateRoleByPermission.value && myAllowCreateParentRoles.value.length === 1) {
    form.parentRoleId = myAllowCreateParentRoles.value[0].id
  }
  editVisible.value = true
}

function openCreateChild(parentRole: RoleVO) {
  if (!canCreateChildUnder(parentRole)) {
    ElMessage.warning('当前账号无权在该父角色下创建子角色')
    return
  }
  resetForm()
  form.parentRoleId = parentRole.id
  form.roleName = `${parentRole.roleName}-子角色`
  form.allowCreateChild = 0
  form.permissionIds = Array.isArray(parentRole.permissionIds) ? [...parentRole.permissionIds] : []
  scopeConfigs.value = Array.isArray(parentRole.scopeConfigs) && parentRole.scopeConfigs.length
    ? parentRole.scopeConfigs.map((item) => ({
        scopeType: item.scopeType,
        scopeRefId: item.scopeRefId,
      }))
    : [newScopeConfig()]
  editVisible.value = true
}

function canCreateChildUnder(role: RoleVO) {
  if (role.allowCreateChild !== 1) {
    return false
  }
  if (canCreateRoleByPermission.value) {
    return true
  }
  return myAllowCreateParentRoles.value.some((item) => item.id === role.id)
}

async function openEdit(id: number) {
  resetForm()
  const res = await roleDetail(id)
  const row = res.data
  editingId.value = row.id
  form.roleCode = row.roleCode
  form.roleName = row.roleName
  form.parentRoleId = row.parentRoleId || undefined
  form.allowCreateChild = row.allowCreateChild
  form.status = row.status
  form.sort = row.sort
  form.remark = row.remark || ''
  form.permissionIds = row.permissionIds || []
  scopeConfigs.value = (row.scopeConfigs || []).map((item) => ({
    scopeType: item.scopeType,
    scopeRefId: item.scopeRefId,
  }))
  if (!scopeConfigs.value.length) {
    scopeConfigs.value = [newScopeConfig()]
  }
  editVisible.value = true
}

function buildScopePayload() {
  if (!scopeConfigs.value.length) {
    scopeError.value = '请至少配置一个数据范围'
    return null
  }
  const payload: RoleScopeConfigItem[] = []
  for (const item of scopeConfigs.value) {
    if (!item.scopeType) {
      scopeError.value = '范围类型不能为空'
      return null
    }
    if (isScopeTypeNeedOrgRef(item.scopeType) && !item.scopeRefId) {
      scopeError.value = '请为范围选择组织'
      return null
    }
    payload.push({
      scopeType: item.scopeType,
      scopeRefId: item.scopeRefId || null,
    })
  }
  scopeError.value = ''
  return payload
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  const scopePayload = buildScopePayload()
  if (!scopePayload) return

  if (!editingId.value && !canCreateRoleByPermission.value) {
    const allowedParentIds = new Set(myAllowCreateParentRoles.value.map((role) => role.id))
    if (!form.parentRoleId || !allowedParentIds.has(form.parentRoleId)) {
      ElMessage.warning('仅允许在本人可建子角色下创建')
      return
    }
  }

  saving.value = true
  try {
    if (editingId.value) {
      const payload: RoleUpdateRequest = {
        id: editingId.value,
        roleName: form.roleName.trim(),
        allowCreateChild: form.allowCreateChild,
        status: form.status,
        sort: form.sort,
        remark: form.remark.trim() || undefined,
        permissionIds: form.permissionIds,
        scopeConfigs: scopePayload,
      }
      await updateRole(payload)
      ElMessage.success('角色更新成功')
    } else {
      const payload: RoleCreateRequest = {
        roleCode: form.roleCode.trim(),
        roleName: form.roleName.trim(),
        parentRoleId: form.parentRoleId || null,
        allowCreateChild: form.allowCreateChild,
        status: form.status,
        sort: form.sort,
        remark: form.remark.trim() || undefined,
        permissionIds: form.permissionIds,
        scopeConfigs: scopePayload,
      }
      await createRole(payload)
      ElMessage.success('角色创建成功')
    }
    editVisible.value = false
    await Promise.all([table.load(), loadRoleOptions()])
  } finally {
    saving.value = false
  }
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确认删除该角色吗？', '提示', { type: 'warning' })
  await deleteRole(id)
  ElMessage.success('删除成功')
  await Promise.all([table.load(), loadRoleOptions()])
}

onMounted(async () => {
  await Promise.all([table.load(), loadPermissions(), loadOrgOptions(), loadRoleOptions()])
})
</script>

<template>
  <AdminPageShell title="角色管理" description="管理角色权限集与数据范围配置。">
    <div class="liquid-glass rounded-3xl p-4 md:p-5 space-y-4">
      <el-form inline>
        <el-form-item>
          <el-input v-model="table.query.keyword" placeholder="角色编码/名称" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="table.search">查询</el-button>
          <el-button @click="table.resetQuery(); table.search()">重置</el-button>
          <el-button v-if="canCreateRole" type="success" @click="openCreate">新建角色</el-button>
        </el-form-item>
      </el-form>

      <el-alert v-if="table.error.value" type="error" :closable="false" :title="table.error.value" />

      <el-table :data="table.records.value" v-loading="table.loading.value" row-key="id" border class="admin-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleCode" label="角色编码" width="160" />
        <el-table-column prop="roleName" label="角色名称" width="170" />
        <el-table-column prop="status" label="状态" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">{{ scope.row.status === 1 ? '启用' : '禁用' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="allowCreateChild" label="可建子角色" width="120">
          <template #default="scope">
            <el-tag :type="scope.row.allowCreateChild === 1 ? 'success' : 'info'">
              {{ scope.row.allowCreateChild === 1 ? '允许' : '不允许' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="sort" label="排序" width="90" />
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="360" fixed="right">
          <template #default="scope">
            <el-space wrap>
              <el-button size="small" @click="openDetail(scope.row.id)">详情</el-button>
              <el-button v-if="can('sys:role:update')" size="small" type="primary" @click="openEdit(scope.row.id)">编辑</el-button>
              <el-button
                v-if="canCreateChildUnder(scope.row)"
                size="small"
                type="success"
                plain
                @click="openCreateChild(scope.row)"
              >
                建子角色
              </el-button>
              <el-button v-if="can('sys:role:delete')" size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
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

    <el-drawer v-model="detailVisible" title="角色详情" size="42%">
      <div v-if="detail" class="space-y-2 text-sm">
        <p><strong>编码：</strong>{{ detail.roleCode }}</p>
        <p><strong>名称：</strong>{{ detail.roleName }}</p>
        <p><strong>权限ID：</strong>{{ (detail.permissionIds || []).join(',') || '-' }}</p>
        <p><strong>范围配置：</strong>{{ JSON.stringify(detail.scopeConfigs || []) }}</p>
        <p><strong>备注：</strong>{{ detail.remark || '-' }}</p>
      </div>
    </el-drawer>

    <el-dialog v-model="editVisible" :title="editingId ? '编辑角色' : '新建角色'" width="900px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="120px">
        <el-form-item v-if="!editingId" label="角色编码" prop="roleCode">
          <el-input v-model="form.roleCode" />
        </el-form-item>
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" />
        </el-form-item>
        <el-form-item label="父角色">
          <el-select
            v-model="form.parentRoleId"
            filterable
            clearable
            style="width: 100%"
            :placeholder="canCreateRoleByPermission ? '可选' : '请选择本人可建子角色'"
          >
            <el-option
              v-for="item in selectableParentRoles"
              :key="item.id"
              :label="`${item.roleName} (${item.roleCode})`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!canCreateRoleByPermission && !editingId" label="创建约束">
          <span class="text-xs text-white/70">
            当前账号仅可在本人“可建子角色=允许”的角色下创建子角色。
          </span>
        </el-form-item>
        <el-form-item label="可建子角色">
          <el-switch v-model="form.allowCreateChild" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="form.status" style="width: 180px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sort" :min="0" />
        </el-form-item>
        <el-form-item label="权限集合" prop="permissionIds">
          <el-select v-model="form.permissionIds" multiple filterable style="width: 100%">
            <el-option
              v-for="item in permissionOptions"
              :key="item.id"
              :label="`${item.permissionName} (${item.permissionCode})`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="数据范围">
          <div class="w-full space-y-3">
            <div
              v-for="(item, index) in scopeConfigs"
              :key="index"
              class="grid grid-cols-12 gap-2 items-center rounded-2xl border border-white/15 p-3 bg-white/5"
            >
              <div class="col-span-5">
                <el-select v-model="item.scopeType" style="width: 100%">
                  <el-option v-for="option in DATA_SCOPE_TYPE_OPTIONS" :key="option.value" :label="option.label" :value="option.value" />
                </el-select>
              </div>
              <div class="col-span-6">
                <el-select
                  v-model="item.scopeRefId"
                  filterable
                  clearable
                  placeholder="选择组织（ALL/SELF 可留空）"
                  style="width: 100%"
                  :disabled="!isScopeTypeNeedOrgRef(item.scopeType)"
                >
                  <el-option
                    v-for="org in getScopeOrgOptions(item.scopeType)"
                    :key="org.value"
                    :label="org.label"
                    :value="org.value"
                  />
                </el-select>
              </div>
              <div class="col-span-1 text-right">
                <el-button text type="danger" @click="removeScopeRow(index)">删除</el-button>
              </div>
            </div>
            <div class="flex items-center justify-between">
              <el-button type="primary" plain @click="addScopeRow">新增范围</el-button>
              <span v-if="scopeError" class="text-xs text-red-300">{{ scopeError }}</span>
            </div>
          </div>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
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
