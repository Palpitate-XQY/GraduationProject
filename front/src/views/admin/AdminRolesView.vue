<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import AdminPageShell from '@/components/admin/AdminPageShell.vue'
import AdminTablePager from '@/components/admin/AdminTablePager.vue'
import { useActionPermission } from '@/composables/useActionPermission'
import { useUserStore } from '@/stores/user'
import { DATA_SCOPE_TYPE_OPTIONS, isScopeTypeNeedOrgRef } from '@/constants/domain'
import { createRole, deleteRole, permissionPage, roleDetail, rolePage, updateRole } from '@/api/system'
import { orgTree } from '@/api/org'
import type { OrgTreeVO } from '@/types/org'
import type { PermissionVO, RoleCreateRequest, RoleScopeConfigItem, RoleUpdateRequest, RoleVO } from '@/types/system'

const ROLE_PAGE_SIZE_FETCH = 100
const PERMISSION_PAGE_SIZE_FETCH = 100

type RoleTreeNode = RoleVO & { children: RoleTreeNode[] }
type CreateEntryMode = 'top' | 'node'

interface ScopeConfigFormItem {
  scopeType: string
  scopeRefId?: number | null
}

interface OrgOption {
  value: number
  label: string
  orgType: string
}

const SCOPE_ORG_TYPE_MAP: Record<string, string | undefined> = {
  STREET: 'STREET',
  COMMUNITY: 'COMMUNITY',
  COMPLEX: 'COMPLEX',
  PROPERTY_COMPANY: 'PROPERTY_COMPANY',
}

const SCOPE_TYPE_LABEL_MAP = DATA_SCOPE_TYPE_OPTIONS.reduce<Record<string, string>>((acc, item) => {
  acc[item.value] = item.label
  return acc
}, {})

const { can } = useActionPermission()
const userStore = useUserStore()

const query = reactive({
  keyword: '',
  current: 1,
  size: 10,
})

const loading = ref(false)
const error = ref('')

const allRoles = ref<RoleVO[]>([])
const permissionOptions = ref<PermissionVO[]>([])
const permissionLoadFailed = ref(false)

const orgOptions = ref<OrgOption[]>([])
const descendantMap = ref<Map<number, Set<number>>>(new Map())

const detailVisible = ref(false)
const detail = ref<RoleVO | null>(null)

const editVisible = ref(false)
const saving = ref(false)
const formRef = ref<FormInstance>()
const scopeError = ref('')
const createEntryMode = ref<CreateEntryMode>('top')
const parentLocked = ref(false)

const editingId = ref<number | null>(null)
const scopeConfigs = ref<ScopeConfigFormItem[]>([])

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

const treeProps = {
  children: 'children',
  label: 'roleName',
}

const formRules: FormRules = {
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }],
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  permissionIds: [{ required: true, type: 'array', min: 1, message: '请至少选择一个权限', trigger: 'change' }],
}

const roleById = computed(() => {
  const map = new Map<number, RoleVO>()
  allRoles.value.forEach((item) => map.set(item.id, item))
  return map
})

const orgLabelMap = computed(() => {
  const map = new Map<number, string>()
  orgOptions.value.forEach((item) => map.set(item.value, item.label))
  return map
})

const allOrgIdSet = computed(() => new Set(orgOptions.value.map((item) => item.value)))

const permissionLabelMap = computed(() => {
  const map = new Map<number, string>()
  permissionOptions.value.forEach((item) => {
    map.set(item.id, `${item.permissionName} (${item.permissionCode})`)
  })
  return map
})

const canCreateRoleByPermission = computed(() => can('sys:role:create'))
const currentRoleCodeSet = computed(() => new Set(userStore.roleCodes || []))

const myAllowCreateParentRoles = computed(() => {
  return allRoles.value.filter(
    (role) => role.allowCreateChild === 1 && role.status === 1 && currentRoleCodeSet.value.has(role.roleCode),
  )
})

const canCreateRole = computed(() => canCreateRoleByPermission.value || myAllowCreateParentRoles.value.length > 0)

const selectableParentRoles = computed(() => {
  const base = allRoles.value.filter((role) => role.status === 1 && (!editingId.value || role.id !== editingId.value))
  if (canCreateRoleByPermission.value) {
    return base
  }
  const allowedParentIds = new Set(myAllowCreateParentRoles.value.map((item) => item.id))
  return base.filter((item) => allowedParentIds.has(item.id))
})

const selectedParentRole = computed(() => {
  if (!form.parentRoleId) return null
  return roleById.value.get(form.parentRoleId) || null
})

const isChildCreateMode = computed(() => !editingId.value && Boolean(form.parentRoleId))

const parentHasAllScope = computed(() => {
  if (!isChildCreateMode.value || !selectedParentRole.value) return false
  return (selectedParentRole.value.scopeConfigs || []).some((item) => item.scopeType === 'ALL')
})

const parentHasOrgScope = computed(() => {
  if (!isChildCreateMode.value || !selectedParentRole.value) return false
  return (selectedParentRole.value.scopeConfigs || []).some((item) => isScopeTypeNeedOrgRef(item.scopeType) && !!item.scopeRefId)
})

const allowedScopeTypeValues = computed(() => {
  if (!isChildCreateMode.value) {
    return DATA_SCOPE_TYPE_OPTIONS.map((item) => item.value)
  }

  if (parentHasAllScope.value) {
    return DATA_SCOPE_TYPE_OPTIONS.map((item) => item.value)
  }

  const values = new Set<string>()
  values.add('SELF')

  if (parentHasOrgScope.value) {
    values.add('STREET')
    values.add('COMMUNITY')
    values.add('COMPLEX')
    values.add('PROPERTY_COMPANY')
    values.add('CUSTOM')
  }

  return DATA_SCOPE_TYPE_OPTIONS
    .map((item) => item.value)
    .filter((value) => values.has(value))
})

const scopeTypeOptionsForForm = computed(() => {
  const allowSet = new Set(allowedScopeTypeValues.value)
  return DATA_SCOPE_TYPE_OPTIONS.filter((item) => allowSet.has(item.value))
})

const parentPermissionIdSet = computed(() => {
  if (!isChildCreateMode.value || !selectedParentRole.value) {
    return new Set<number>()
  }
  return new Set(selectedParentRole.value.permissionIds || [])
})

const parentCoveredOrgIdSet = computed(() => {
  if (!isChildCreateMode.value || !selectedParentRole.value) {
    return new Set(allOrgIdSet.value)
  }

  if (parentHasAllScope.value) {
    return new Set(allOrgIdSet.value)
  }

  const covered = new Set<number>()
  const scopes = selectedParentRole.value.scopeConfigs || []
  scopes.forEach((scope) => {
    if (!isScopeTypeNeedOrgRef(scope.scopeType) || !scope.scopeRefId) {
      return
    }
    const descendants = descendantMap.value.get(scope.scopeRefId)
    if (descendants?.size) {
      descendants.forEach((id) => covered.add(id))
    } else {
      covered.add(scope.scopeRefId)
    }
  })

  return covered
})

const permissionSelectOptions = computed(() => {
  if (!isChildCreateMode.value) {
    return permissionOptions.value.map((item) => ({
      value: item.id,
      label: `${item.permissionName} (${item.permissionCode})`,
    }))
  }

  const ids = new Set<number>()
  parentPermissionIdSet.value.forEach((id) => ids.add(id))
  form.permissionIds.forEach((id) => ids.add(id))

  return Array.from(ids)
    .sort((a, b) => a - b)
    .map((id) => ({
      value: id,
      label: permissionLabelMap.value.get(id) || `权限ID #${id}`,
    }))
})

const roleTreeRoots = computed(() => buildRoleTree(allRoles.value))

const filteredRoleTreeRoots = computed(() => {
  const keyword = query.keyword.trim().toLowerCase()
  if (!keyword) return roleTreeRoots.value
  return filterRoleTree(roleTreeRoots.value, keyword)
})

const total = computed(() => filteredRoleTreeRoots.value.length)

const pagedRoleTreeRoots = computed(() => {
  const start = (query.current - 1) * query.size
  return filteredRoleTreeRoots.value.slice(start, start + query.size)
})

function resolveScopeTypeLabel(scopeType: string) {
  return SCOPE_TYPE_LABEL_MAP[scopeType] || scopeType
}

function resolvePermissionLabel(permissionId: number) {
  return permissionLabelMap.value.get(permissionId) || `权限ID #${permissionId}`
}

function toRoleNode(role: RoleVO): RoleTreeNode {
  return {
    ...role,
    children: [],
  }
}

function sortNodes(nodes: RoleTreeNode[]) {
  nodes.sort((a, b) => {
    const sortDiff = (a.sort || 0) - (b.sort || 0)
    if (sortDiff !== 0) return sortDiff
    return a.id - b.id
  })
  nodes.forEach((node) => sortNodes(node.children))
}

function buildRoleTree(roles: RoleVO[]) {
  const nodeMap = new Map<number, RoleTreeNode>()
  roles.forEach((role) => nodeMap.set(role.id, toRoleNode(role)))

  const roots: RoleTreeNode[] = []
  nodeMap.forEach((node) => {
    if (node.parentRoleId && node.parentRoleId !== node.id && nodeMap.has(node.parentRoleId)) {
      nodeMap.get(node.parentRoleId)!.children.push(node)
    } else {
      roots.push(node)
    }
  })

  sortNodes(roots)
  return roots
}

function roleMatches(node: RoleTreeNode, keyword: string) {
  const code = (node.roleCode || '').toLowerCase()
  const name = (node.roleName || '').toLowerCase()
  return code.includes(keyword) || name.includes(keyword)
}

function filterRoleTree(nodes: RoleTreeNode[], keyword: string): RoleTreeNode[] {
  const output: RoleTreeNode[] = []
  nodes.forEach((node) => {
    const children = filterRoleTree(node.children || [], keyword)
    if (roleMatches(node, keyword) || children.length > 0) {
      output.push({
        ...node,
        children,
      })
    }
  })
  return output
}

function flattenOrgTree(nodes: OrgTreeVO[], output: OrgOption[]) {
  nodes.forEach((node) => {
    output.push({
      value: node.id,
      label: `${node.orgName} (${node.orgType})`,
      orgType: node.orgType,
    })
    if (Array.isArray(node.children) && node.children.length) {
      flattenOrgTree(node.children, output)
    }
  })
}

function collectDescendants(node: OrgTreeVO, map: Map<number, Set<number>>): Set<number> {
  const set = new Set<number>([node.id])
  if (Array.isArray(node.children)) {
    node.children.forEach((child) => {
      const childSet = collectDescendants(child, map)
      childSet.forEach((id) => set.add(id))
    })
  }
  map.set(node.id, set)
  return set
}

async function loadOrgContext() {
  const res = await orgTree({})
  const nodes = Array.isArray(res.data) ? res.data : []

  const flattened: OrgOption[] = []
  flattenOrgTree(nodes, flattened)
  orgOptions.value = flattened

  const map = new Map<number, Set<number>>()
  nodes.forEach((node) => collectDescendants(node, map))
  descendantMap.value = map
}

async function fetchAllRoles() {
  const result: RoleVO[] = []
  let current = 1
  let totalCount = 0

  while (true) {
    const res = await rolePage({ current, size: ROLE_PAGE_SIZE_FETCH, keyword: '' })
    const records = Array.isArray(res.data.records) ? res.data.records : []
    totalCount = Number(res.data.total || 0)
    result.push(...records)

    if (!records.length || result.length >= totalCount) {
      break
    }
    current += 1
  }

  const dedupe = new Map<number, RoleVO>()
  result.forEach((item) => dedupe.set(item.id, item))
  allRoles.value = Array.from(dedupe.values())
}

async function fetchAllPermissions() {
  permissionLoadFailed.value = false
  try {
    const result: PermissionVO[] = []
    let current = 1
    let totalCount = 0

    while (true) {
      const res = await permissionPage({ current, size: PERMISSION_PAGE_SIZE_FETCH, keyword: '' })
      const records = Array.isArray(res.data.records) ? res.data.records : []
      totalCount = Number(res.data.total || 0)
      result.push(...records)

      if (!records.length || result.length >= totalCount) {
        break
      }
      current += 1
    }

    const dedupe = new Map<number, PermissionVO>()
    result.forEach((item) => dedupe.set(item.id, item))
    permissionOptions.value = Array.from(dedupe.values())
  } catch {
    permissionLoadFailed.value = true
    permissionOptions.value = []
  }
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
  parentLocked.value = false
  createEntryMode.value = 'top'
}

function newScopeConfig(): ScopeConfigFormItem {
  const allowValues = allowedScopeTypeValues.value
  const fallback = allowValues.includes('COMMUNITY') ? 'COMMUNITY' : allowValues[0] || 'SELF'
  return {
    scopeType: fallback,
    scopeRefId: isScopeTypeNeedOrgRef(fallback) ? undefined : null,
  }
}

function getScopeOrgOptions(scopeType: string): OrgOption[] {
  const targetType = SCOPE_ORG_TYPE_MAP[scopeType]
  const typeFiltered = targetType
    ? orgOptions.value.filter((item) => item.orgType === targetType)
    : orgOptions.value

  if (!isChildCreateMode.value) {
    return typeFiltered
  }

  if (!parentCoveredOrgIdSet.value.size) {
    return []
  }

  return typeFiltered.filter((item) => parentCoveredOrgIdSet.value.has(item.value))
}

function sanitizeScopeRows() {
  const allowSet = new Set<string>(allowedScopeTypeValues.value as string[])

  scopeConfigs.value = scopeConfigs.value.map((row) => {
    const scopeType = allowSet.has(row.scopeType)
      ? row.scopeType
      : allowedScopeTypeValues.value[0] || 'SELF'

    if (!isScopeTypeNeedOrgRef(scopeType)) {
      return {
        scopeType,
        scopeRefId: null,
      }
    }

    const options = getScopeOrgOptions(scopeType)
    const hasCurrent = options.some((item) => item.value === row.scopeRefId)

    return {
      scopeType,
      scopeRefId: hasCurrent ? row.scopeRefId : undefined,
    }
  })

  if (!scopeConfigs.value.length) {
    scopeConfigs.value = [newScopeConfig()]
  }
}

function applyParentInheritance(parentRole: RoleVO) {
  form.permissionIds = Array.isArray(parentRole.permissionIds) ? [...parentRole.permissionIds] : []

  scopeConfigs.value = Array.isArray(parentRole.scopeConfigs) && parentRole.scopeConfigs.length
    ? parentRole.scopeConfigs.map((item) => ({
        scopeType: item.scopeType,
        scopeRefId: item.scopeRefId,
      }))
    : [newScopeConfig()]

  sanitizeScopeRows()
}

function handleParentRoleChange() {
  if (editingId.value) return

  const parent = selectedParentRole.value
  if (!parent) {
    form.permissionIds = []
    scopeConfigs.value = [newScopeConfig()]
    scopeError.value = ''
    return
  }

  applyParentInheritance(parent)
}

function addScopeRow() {
  scopeConfigs.value.push(newScopeConfig())
  sanitizeScopeRows()
}

function removeScopeRow(index: number) {
  scopeConfigs.value.splice(index, 1)
  if (!scopeConfigs.value.length) {
    scopeConfigs.value = [newScopeConfig()]
  }
}

function handleScopeTypeChange(index: number) {
  const item = scopeConfigs.value[index]
  if (!item) return

  if (!isScopeTypeNeedOrgRef(item.scopeType)) {
    item.scopeRefId = null
  } else {
    const options = getScopeOrgOptions(item.scopeType)
    if (!options.some((opt) => opt.value === item.scopeRefId)) {
      item.scopeRefId = undefined
    }
  }
}

function buildScopePayload() {
  if (!scopeConfigs.value.length) {
    scopeError.value = '请至少配置一个数据范围'
    return null
  }

  const payload: RoleScopeConfigItem[] = []
  const allowScopeSet = new Set<string>(allowedScopeTypeValues.value as string[])

  for (const item of scopeConfigs.value) {
    if (!item.scopeType) {
      scopeError.value = '范围类型不能为空'
      return null
    }

    if (isChildCreateMode.value && !allowScopeSet.has(item.scopeType)) {
      scopeError.value = '子角色范围类型超出父角色允许范围'
      return null
    }

    if (item.scopeType === 'ALL' && isChildCreateMode.value && !parentHasAllScope.value) {
      scopeError.value = '父角色不包含 ALL 范围，子角色不可使用 ALL'
      return null
    }

    if (isScopeTypeNeedOrgRef(item.scopeType)) {
      if (!item.scopeRefId) {
        scopeError.value = '请选择范围组织'
        return null
      }

      if (isChildCreateMode.value) {
        const validOrgIds = new Set(getScopeOrgOptions(item.scopeType).map((org) => org.value))
        if (!validOrgIds.has(item.scopeRefId)) {
          scopeError.value = '范围组织超出父角色可达范围'
          return null
        }
      }
    }

    payload.push({
      scopeType: item.scopeType,
      scopeRefId: item.scopeRefId || null,
    })
  }

  if (isChildCreateMode.value) {
    const parentPermissionIds = parentPermissionIdSet.value
    const hasOutOfRangePermission = form.permissionIds.some((id) => !parentPermissionIds.has(id))
    if (hasOutOfRangePermission) {
      scopeError.value = '子角色权限不可超出父角色权限范围'
      return null
    }
  }

  scopeError.value = ''
  return payload
}

function formatScopeItem(item: RoleScopeConfigItem) {
  const typeLabel = resolveScopeTypeLabel(item.scopeType)
  if (!isScopeTypeNeedOrgRef(item.scopeType)) {
    return typeLabel
  }
  const orgLabel = item.scopeRefId ? (orgLabelMap.value.get(item.scopeRefId) || `组织ID #${item.scopeRefId}`) : '未设置组织'
  return `${typeLabel} · ${orgLabel}`
}

function summarizeScopeItems(items: RoleScopeConfigItem[], max = 2) {
  const labels = (items || []).map((item) => formatScopeItem(item))
  return {
    labels: labels.slice(0, max),
    remain: Math.max(0, labels.length - max),
  }
}

function openCreate() {
  if (!canCreateRole.value) {
    ElMessage.warning('当前账号暂无角色创建能力')
    return
  }

  resetForm()
  createEntryMode.value = 'top'
  parentLocked.value = false

  if (!canCreateRoleByPermission.value && myAllowCreateParentRoles.value.length === 1) {
    form.parentRoleId = myAllowCreateParentRoles.value[0].id
    applyParentInheritance(myAllowCreateParentRoles.value[0])
  }

  editVisible.value = true
}

function canCreateChildUnder(role: RoleVO) {
  if (role.allowCreateChild !== 1 || role.status !== 1) {
    return false
  }
  if (canCreateRoleByPermission.value) {
    return true
  }
  return myAllowCreateParentRoles.value.some((item) => item.id === role.id)
}

function openCreateChild(parentRole: RoleVO) {
  if (!canCreateChildUnder(parentRole)) {
    ElMessage.warning('当前账号无权在该父角色下创建子角色')
    return
  }

  resetForm()
  createEntryMode.value = 'node'
  parentLocked.value = true
  form.parentRoleId = parentRole.id
  form.roleName = `${parentRole.roleName}-子角色`
  applyParentInheritance(parentRole)
  editVisible.value = true
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
  scopeConfigs.value = Array.isArray(row.scopeConfigs) && row.scopeConfigs.length
    ? row.scopeConfigs.map((item) => ({
        scopeType: item.scopeType,
        scopeRefId: item.scopeRefId,
      }))
    : [newScopeConfig()]

  scopeError.value = ''
  parentLocked.value = false
  editVisible.value = true
}

async function openDetail(id: number) {
  const res = await roleDetail(id)
  detail.value = res.data
  detailVisible.value = true
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确认删除该角色吗？', '提示', { type: 'warning' })
  await deleteRole(id)
  ElMessage.success('删除成功')
  await refreshData()
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  if (!editingId.value && !form.parentRoleId) {
    ElMessage.warning('请先选择父角色')
    return
  }

  if (!editingId.value && !canCreateRoleByPermission.value) {
    const allowedParentIds = new Set(myAllowCreateParentRoles.value.map((role) => role.id))
    if (!form.parentRoleId || !allowedParentIds.has(form.parentRoleId)) {
      ElMessage.warning('当前账号仅允许在本人可建父角色下创建子角色')
      return
    }
  }

  const scopePayload = buildScopePayload()
  if (!scopePayload) {
    return
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
    await refreshData()
  } finally {
    saving.value = false
  }
}

async function refreshData() {
  loading.value = true
  error.value = ''
  try {
    await Promise.all([fetchAllRoles(), fetchAllPermissions(), loadOrgContext()])
    if ((query.current - 1) * query.size >= total.value && query.current > 1) {
      query.current = Math.max(1, Math.ceil(total.value / query.size))
    }
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  query.current = 1
}

function handleReset() {
  query.keyword = ''
  query.current = 1
  query.size = 10
}

function handlePageChange(page: number) {
  query.current = page
}

function handlePageSizeChange(size: number) {
  query.size = size
  query.current = 1
}

onMounted(async () => {
  await refreshData()
})
</script>

<template>
  <AdminPageShell title="角色管理" description="组织树视图管理父子角色、权限集合与数据范围。">
    <div class="liquid-glass rounded-3xl p-4 md:p-5 space-y-4">
      <el-form inline>
        <el-form-item>
          <el-input v-model="query.keyword" placeholder="角色编码/名称" clearable @input="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
          <el-button v-if="canCreateRole" type="success" @click="openCreate">新建角色</el-button>
        </el-form-item>
      </el-form>

      <el-alert
        v-if="!canCreateRoleByPermission && !myAllowCreateParentRoles.length"
        type="warning"
        :closable="false"
        title="当前账号仅有角色查看权限，暂无可用父角色用于创建子角色。"
      />

      <el-alert
        v-if="permissionLoadFailed"
        type="warning"
        :closable="false"
        title="权限选项加载失败，创建子角色仍可基于父角色继承权限配置。"
      />

      <el-alert v-if="error" type="error" :closable="false" :title="error" />

      <div class="role-tree-wrapper" v-loading="loading">
        <el-tree
          :data="pagedRoleTreeRoots"
          :props="treeProps"
          node-key="id"
          default-expand-all
          empty-text="暂无角色数据"
          class="role-tree"
        >
          <template #default="{ data }">
            <div class="role-node w-full">
              <div class="role-node-main">
                <div class="role-name-wrap">
                  <span class="role-name">{{ data.roleName }}</span>
                  <span class="role-code">{{ data.roleCode }}</span>
                  <el-tag size="small" :type="data.status === 1 ? 'success' : 'danger'">{{ data.status === 1 ? '启用' : '禁用' }}</el-tag>
                  <el-tag size="small" :type="data.allowCreateChild === 1 ? 'warning' : 'info'">{{ data.allowCreateChild === 1 ? '可建子角色' : '不可建子角色' }}</el-tag>
                </div>
                <div class="role-scope-wrap">
                  <template v-for="label in summarizeScopeItems(data.scopeConfigs || []).labels" :key="label">
                    <el-tag size="small" effect="plain" type="info">{{ label }}</el-tag>
                  </template>
                  <el-tag
                    v-if="summarizeScopeItems(data.scopeConfigs || []).remain > 0"
                    size="small"
                    effect="plain"
                    type="info"
                  >
                    +{{ summarizeScopeItems(data.scopeConfigs || []).remain }}
                  </el-tag>
                </div>
              </div>
              <div class="role-node-actions">
                <el-button size="small" @click="openDetail(data.id)">详情</el-button>
                <el-button v-if="can('sys:role:update')" size="small" type="primary" @click="openEdit(data.id)">编辑</el-button>
                <el-button
                  v-if="canCreateChildUnder(data)"
                  size="small"
                  type="success"
                  plain
                  @click="openCreateChild(data)"
                >
                  建子角色
                </el-button>
                <el-button v-if="can('sys:role:delete')" size="small" type="danger" @click="handleDelete(data.id)">删除</el-button>
              </div>
            </div>
          </template>
        </el-tree>
      </div>

      <AdminTablePager
        :current="query.current"
        :size="query.size"
        :total="total"
        @update:current="handlePageChange"
        @update:size="handlePageSizeChange"
      />
    </div>

    <el-drawer v-model="detailVisible" title="角色详情" size="42%">
      <div v-if="detail" class="space-y-3 text-sm">
        <p><strong>角色编码：</strong>{{ detail.roleCode }}</p>
        <p><strong>角色名称：</strong>{{ detail.roleName }}</p>
        <p><strong>父角色ID：</strong>{{ detail.parentRoleId || '-' }}</p>
        <p><strong>状态：</strong>{{ detail.status === 1 ? '启用' : '禁用' }}</p>
        <p><strong>可建子角色：</strong>{{ detail.allowCreateChild === 1 ? '是' : '否' }}</p>

        <div>
          <p class="mb-2"><strong>权限集合：</strong></p>
          <div class="flex flex-wrap gap-2">
            <el-tag v-for="pid in detail.permissionIds || []" :key="pid" size="small" effect="plain">
              {{ resolvePermissionLabel(pid) }}
            </el-tag>
            <span v-if="!(detail.permissionIds || []).length">-</span>
          </div>
        </div>

        <div>
          <p class="mb-2"><strong>数据范围：</strong></p>
          <div class="flex flex-wrap gap-2">
            <el-tag v-for="(scope, idx) in detail.scopeConfigs || []" :key="`${scope.scopeType}-${scope.scopeRefId}-${idx}`" size="small" effect="plain" type="info">
              {{ formatScopeItem(scope) }}
            </el-tag>
            <span v-if="!(detail.scopeConfigs || []).length">-</span>
          </div>
        </div>
      </div>
    </el-drawer>

    <el-dialog v-model="editVisible" :title="editingId ? '编辑角色' : '新建角色'" width="920px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="130px">
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
            placeholder="请选择父角色"
            :disabled="Boolean(editingId) || parentLocked"
            @change="handleParentRoleChange"
          >
            <el-option
              v-for="item in selectableParentRoles"
              :key="item.id"
              :label="`${item.roleName} (${item.roleCode})`"
              :value="item.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item v-if="!editingId && selectedParentRole" label="创建上下文">
          <span class="text-xs text-white/75">
            当前正在父角色「{{ selectedParentRole.roleName }} ({{ selectedParentRole.roleCode }})」下创建子角色。
          </span>
        </el-form-item>

        <el-form-item v-if="!editingId && createEntryMode === 'top'" label="创建约束">
          <span class="text-xs text-white/70">
            顶部入口创建时必须先选择父角色；子角色权限与数据范围只允许在父角色范围内收缩。
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
              v-for="item in permissionSelectOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value"
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
                <el-select v-model="item.scopeType" style="width: 100%" @change="handleScopeTypeChange(index)">
                  <el-option
                    v-for="option in scopeTypeOptionsForForm"
                    :key="option.value"
                    :label="option.label"
                    :value="option.value"
                  />
                </el-select>
              </div>

              <div class="col-span-6">
                <el-select
                  v-model="item.scopeRefId"
                  filterable
                  clearable
                  placeholder="选择范围组织（ALL/SELF 可留空）"
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
.role-tree-wrapper {
  min-height: 320px;
}

:deep(.role-tree .el-tree-node__content) {
  height: auto;
  padding: 6px 0;
}

.role-node {
  width: 100%;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 14px;
  padding: 10px 12px;
  background: rgba(8, 18, 32, 0.4);
}

.role-node-main {
  min-width: 0;
  flex: 1;
}

.role-name-wrap {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.role-name {
  color: #fff;
  font-weight: 600;
}

.role-code {
  color: rgba(255, 255, 255, 0.65);
  font-size: 12px;
}

.role-scope-wrap {
  margin-top: 8px;
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.role-node-actions {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 6px;
  justify-content: flex-end;
}
</style>
