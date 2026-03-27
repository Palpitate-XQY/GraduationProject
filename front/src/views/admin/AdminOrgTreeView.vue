<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import AdminPageShell from '@/components/admin/AdminPageShell.vue'
import { useActionPermission } from '@/composables/useActionPermission'
import { createOrg, deleteOrg, orgTree, updateOrg } from '@/api/org'
import type { OrgCreateRequest, OrgTreeVO, OrgUpdateRequest } from '@/types/org'

const { can } = useActionPermission()

const loading = ref(false)
const error = ref('')
const treeData = ref<OrgTreeVO[]>([])
const expandedRowKeys = ref<number[]>([])

const query = reactive({
  keyword: '',
  orgType: '',
  status: undefined as number | undefined,
})

const editVisible = ref(false)
const editingId = ref<number | null>(null)
const saving = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  parentId: undefined as number | undefined,
  orgCode: '',
  orgName: '',
  orgType: 'COMMUNITY',
  status: 1,
  sort: 0,
})

const formRules: FormRules = {
  orgCode: [{ required: true, message: '请输入组织编码', trigger: 'blur' }],
  orgName: [{ required: true, message: '请输入组织名称', trigger: 'blur' }],
  orgType: [{ required: true, message: '请选择组织类型', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'change' }],
}

const flattened = computed(() => {
  const result: Array<{ id: number; orgName: string }> = []
  const walk = (nodes: OrgTreeVO[]) => {
    nodes.forEach((node) => {
      result.push({ id: node.id, orgName: node.orgName })
      if (Array.isArray(node.children) && node.children.length) {
        walk(node.children)
      }
    })
  }
  walk(treeData.value)
  return result
})

function resetForm() {
  editingId.value = null
  form.parentId = undefined
  form.orgCode = ''
  form.orgName = ''
  form.orgType = 'COMMUNITY'
  form.status = 1
  form.sort = 0
}

async function fetchTree() {
  loading.value = true
  error.value = ''
  try {
    const res = await orgTree(query)
    const normalized = normalizeTreeNodes(res.data)
    treeData.value = normalized
    expandedRowKeys.value = collectExpandedKeys(normalized)
  } catch (e) {
    error.value = e instanceof Error ? e.message : '加载失败'
    treeData.value = []
    expandedRowKeys.value = []
  } finally {
    loading.value = false
  }
}

function normalizeTreeNodes(raw: unknown): OrgTreeVO[] {
  if (!Array.isArray(raw)) {
    return []
  }
  return raw.map((item) => {
    const node = item as OrgTreeVO
    return {
      ...node,
      children: normalizeTreeNodes(node.children),
    }
  })
}

function collectExpandedKeys(nodes: OrgTreeVO[]) {
  const keys: number[] = []
  const walk = (rows: OrgTreeVO[]) => {
    rows.forEach((row) => {
      keys.push(row.id)
      if (Array.isArray(row.children) && row.children.length) {
        walk(row.children)
      }
    })
  }
  walk(nodes)
  return keys
}

function openCreate(parentId?: number) {
  resetForm()
  if (parentId) form.parentId = parentId
  editVisible.value = true
}

function openEdit(row: OrgTreeVO) {
  resetForm()
  editingId.value = row.id
  form.parentId = row.parentId || undefined
  form.orgCode = row.orgCode
  form.orgName = row.orgName
  form.orgType = row.orgType
  form.status = row.status
  form.sort = row.sort
  editVisible.value = true
}

async function submitForm() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  try {
    if (editingId.value) {
      const payload: OrgUpdateRequest = {
        id: editingId.value,
        parentId: form.parentId || null,
        orgName: form.orgName.trim(),
        orgType: form.orgType,
        status: form.status,
        sort: form.sort,
      }
      await updateOrg(payload)
      ElMessage.success('组织更新成功')
    } else {
      const payload: OrgCreateRequest = {
        parentId: form.parentId || null,
        orgCode: form.orgCode.trim(),
        orgName: form.orgName.trim(),
        orgType: form.orgType,
        status: form.status,
        sort: form.sort,
      }
      await createOrg(payload)
      ElMessage.success('组织创建成功')
    }
    editVisible.value = false
    await fetchTree()
  } finally {
    saving.value = false
  }
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确认删除该组织节点吗？', '提示', { type: 'warning' })
  await deleteOrg(id)
  ElMessage.success('删除成功')
  await fetchTree()
}

onMounted(() => {
  void fetchTree()
})
</script>

<template>
  <AdminPageShell title="组织架构" description="树形维护街道/社区/小区/物业公司/部门节点。">
    <div class="liquid-glass rounded-3xl p-4 md:p-5 space-y-4">
      <el-form inline>
        <el-form-item>
          <el-input v-model="query.keyword" placeholder="关键词" clearable />
        </el-form-item>
        <el-form-item>
          <el-select v-model="query.orgType" placeholder="组织类型" clearable style="width: 170px">
            <el-option label="街道" value="STREET" />
            <el-option label="社区" value="COMMUNITY" />
            <el-option label="小区" value="COMPLEX" />
            <el-option label="物业公司" value="PROPERTY_COMPANY" />
            <el-option label="部门" value="DEPARTMENT" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="query.status" placeholder="状态" clearable style="width: 140px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchTree">查询</el-button>
          <el-button @click="query.keyword=''; query.orgType=''; query.status=undefined; fetchTree()">重置</el-button>
          <el-button v-if="can('org:create')" type="success" @click="openCreate()">新建组织</el-button>
        </el-form-item>
      </el-form>

      <el-alert v-if="error" type="error" :closable="false" :title="error" />

      <el-table
        :data="treeData"
        row-key="id"
        border
        class="admin-table"
        :tree-props="{ children: 'children' }"
        :expand-row-keys="expandedRowKeys"
        default-expand-all
        v-loading="loading"
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orgCode" label="组织编码" width="180" />
        <el-table-column prop="orgName" label="组织名称" min-width="220" />
        <el-table-column prop="orgType" label="类型" width="160" />
        <el-table-column prop="status" label="状态" width="90" />
        <el-table-column prop="sort" label="排序" width="90" />
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="scope">
            <el-space wrap>
              <el-button v-if="can('org:create')" size="small" @click="openCreate(scope.row.id)">新增子节点</el-button>
              <el-button v-if="can('org:update')" size="small" type="primary" @click="openEdit(scope.row)">编辑</el-button>
              <el-button v-if="can('org:delete')" size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
            </el-space>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="editVisible" :title="editingId ? '编辑组织' : '新建组织'" width="720px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="120px">
        <el-form-item label="父节点ID">
          <el-select v-model="form.parentId" clearable filterable style="width: 260px">
            <el-option v-for="item in flattened" :key="item.id" :label="`${item.id} - ${item.orgName}`" :value="item.id" />
          </el-select>
        </el-form-item>
        <el-form-item v-if="!editingId" label="组织编码" prop="orgCode">
          <el-input v-model="form.orgCode" />
        </el-form-item>
        <el-form-item label="组织名称" prop="orgName">
          <el-input v-model="form.orgName" />
        </el-form-item>
        <el-form-item label="组织类型" prop="orgType">
          <el-select v-model="form.orgType" style="width: 260px">
            <el-option label="街道" value="STREET" />
            <el-option label="社区" value="COMMUNITY" />
            <el-option label="小区" value="COMPLEX" />
            <el-option label="物业公司" value="PROPERTY_COMPANY" />
            <el-option label="部门" value="DEPARTMENT" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="form.status" style="width: 260px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="排序" prop="sort">
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
