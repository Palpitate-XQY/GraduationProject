<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import AdminPageShell from '@/components/admin/AdminPageShell.vue'
import AdminTablePager from '@/components/admin/AdminTablePager.vue'
import { useAdminTable } from '@/composables/useAdminTable'
import { useActionPermission } from '@/composables/useActionPermission'
import {
  createDictData,
  createDictType,
  deleteDictData,
  deleteDictType,
  dictDataDetail,
  dictDataPage,
  dictTypeDetail,
  dictTypePage,
  updateDictData,
  updateDictType,
} from '@/api/dict'
import type {
  DictDataCreateRequest,
  DictDataUpdateRequest,
  DictDataVO,
  DictTypeCreateRequest,
  DictTypeUpdateRequest,
  DictTypeVO,
} from '@/types/dict'

const { can } = useActionPermission()

const activeTab = ref<'type' | 'data'>('type')

const typeTable = useAdminTable<DictTypeVO, { current: number; size: number; keyword: string; status?: number }>(
  { current: 1, size: 10, keyword: '', status: undefined },
  (query) => dictTypePage(query),
)

const dataTable = useAdminTable<
  DictDataVO,
  { current: number; size: number; dictTypeCode: string; keyword: string; status?: number }
>(
  { current: 1, size: 10, dictTypeCode: '', keyword: '', status: undefined },
  (query) => dictDataPage(query),
)

const typeOptions = ref<DictTypeVO[]>([])

const typeDetailVisible = ref(false)
const typeDetail = ref<DictTypeVO | null>(null)
const typeEditVisible = ref(false)
const typeSaving = ref(false)
const typeEditingId = ref<number | null>(null)
const typeFormRef = ref<FormInstance>()
const typeForm = reactive({
  dictCode: '',
  dictName: '',
  status: 1,
})

const typeFormRules: FormRules = {
  dictCode: [
    { required: true, message: '请输入字典编码', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_:-]+$/, message: '编码仅支持字母、数字、下划线、冒号、中划线', trigger: 'blur' },
  ],
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

const dataDetailVisible = ref(false)
const dataDetail = ref<DictDataVO | null>(null)
const dataEditVisible = ref(false)
const dataSaving = ref(false)
const dataEditingId = ref<number | null>(null)
const dataFormRef = ref<FormInstance>()
const dataForm = reactive({
  dictTypeCode: '',
  dictLabel: '',
  dictValue: '',
  sort: 0,
  status: 1,
})

const dataFormRules: FormRules = {
  dictTypeCode: [{ required: true, message: '请选择字典类型', trigger: 'change' }],
  dictLabel: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入字典值', trigger: 'blur' }],
  sort: [{ required: true, message: '请输入排序', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

async function loadTypeOptions() {
  const res = await dictTypePage({ current: 1, size: 500 })
  typeOptions.value = Array.isArray(res.data.records) ? res.data.records : []
}

function resetTypeForm() {
  typeEditingId.value = null
  typeForm.dictCode = ''
  typeForm.dictName = ''
  typeForm.status = 1
}

function resetDataForm() {
  dataEditingId.value = null
  dataForm.dictTypeCode = dataTable.query.dictTypeCode || typeOptions.value[0]?.dictCode || ''
  dataForm.dictLabel = ''
  dataForm.dictValue = ''
  dataForm.sort = 0
  dataForm.status = 1
}

async function openTypeDetail(id: number) {
  const res = await dictTypeDetail(id)
  typeDetail.value = res.data
  typeDetailVisible.value = true
}

function openTypeCreate() {
  resetTypeForm()
  typeEditVisible.value = true
}

async function openTypeEdit(id: number) {
  resetTypeForm()
  const res = await dictTypeDetail(id)
  typeEditingId.value = res.data.id
  typeForm.dictCode = res.data.dictCode
  typeForm.dictName = res.data.dictName
  typeForm.status = res.data.status
  typeEditVisible.value = true
}

async function submitType() {
  const valid = await typeFormRef.value?.validate().catch(() => false)
  if (!valid) return

  typeSaving.value = true
  try {
    if (typeEditingId.value) {
      const payload: DictTypeUpdateRequest = {
        id: typeEditingId.value,
        dictCode: typeForm.dictCode.trim(),
        dictName: typeForm.dictName.trim(),
        status: typeForm.status,
      }
      await updateDictType(payload)
      ElMessage.success('字典类型更新成功')
    } else {
      const payload: DictTypeCreateRequest = {
        dictCode: typeForm.dictCode.trim(),
        dictName: typeForm.dictName.trim(),
        status: typeForm.status,
      }
      await createDictType(payload)
      ElMessage.success('字典类型创建成功')
    }
    typeEditVisible.value = false
    await Promise.all([typeTable.load(), loadTypeOptions(), dataTable.load()])
  } finally {
    typeSaving.value = false
  }
}

async function handleTypeDelete(id: number) {
  await ElMessageBox.confirm('确认删除该字典类型吗？', '提示', { type: 'warning' })
  await deleteDictType(id)
  ElMessage.success('删除成功')
  await Promise.all([typeTable.load(), loadTypeOptions(), dataTable.load()])
}

async function openDataDetail(id: number) {
  const res = await dictDataDetail(id)
  dataDetail.value = res.data
  dataDetailVisible.value = true
}

function openDataCreate() {
  resetDataForm()
  dataEditVisible.value = true
}

async function openDataEdit(id: number) {
  resetDataForm()
  const res = await dictDataDetail(id)
  dataEditingId.value = res.data.id
  dataForm.dictTypeCode = res.data.dictTypeCode
  dataForm.dictLabel = res.data.dictLabel
  dataForm.dictValue = res.data.dictValue
  dataForm.sort = res.data.sort
  dataForm.status = res.data.status
  dataEditVisible.value = true
}

async function submitData() {
  const valid = await dataFormRef.value?.validate().catch(() => false)
  if (!valid) return

  dataSaving.value = true
  try {
    if (dataEditingId.value) {
      const payload: DictDataUpdateRequest = {
        id: dataEditingId.value,
        dictTypeCode: dataForm.dictTypeCode,
        dictLabel: dataForm.dictLabel.trim(),
        dictValue: dataForm.dictValue.trim(),
        sort: dataForm.sort,
        status: dataForm.status,
      }
      await updateDictData(payload)
      ElMessage.success('字典数据更新成功')
    } else {
      const payload: DictDataCreateRequest = {
        dictTypeCode: dataForm.dictTypeCode,
        dictLabel: dataForm.dictLabel.trim(),
        dictValue: dataForm.dictValue.trim(),
        sort: dataForm.sort,
        status: dataForm.status,
      }
      await createDictData(payload)
      ElMessage.success('字典数据创建成功')
    }
    dataEditVisible.value = false
    await dataTable.load()
  } finally {
    dataSaving.value = false
  }
}

async function handleDataDelete(id: number) {
  await ElMessageBox.confirm('确认删除该字典数据吗？', '提示', { type: 'warning' })
  await deleteDictData(id)
  ElMessage.success('删除成功')
  await dataTable.load()
}

watch(
  () => activeTab.value,
  async (tab) => {
    if (tab === 'data' && !dataTable.records.value.length) {
      await dataTable.load()
    }
  },
)

onMounted(async () => {
  await Promise.all([typeTable.load(), loadTypeOptions()])
})
</script>

<template>
  <AdminPageShell title="字典管理" description="维护系统字典类型与字典数据。">
    <div class="liquid-glass rounded-3xl p-4 md:p-5">
      <el-tabs v-model="activeTab">
        <el-tab-pane label="字典类型" name="type">
          <div class="space-y-4">
            <el-form inline>
              <el-form-item>
                <el-input v-model="typeTable.query.keyword" placeholder="编码/名称关键词" clearable />
              </el-form-item>
              <el-form-item>
                <el-select v-model="typeTable.query.status" placeholder="状态" clearable style="width: 140px">
                  <el-option label="启用" :value="1" />
                  <el-option label="禁用" :value="0" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="typeTable.search">查询</el-button>
                <el-button @click="typeTable.resetQuery(); typeTable.search()">重置</el-button>
                <el-button v-if="can('sys:dict:type:create')" type="success" @click="openTypeCreate">新建字典类型</el-button>
              </el-form-item>
            </el-form>

            <el-alert v-if="typeTable.error.value" type="error" :closable="false" :title="typeTable.error.value" />

            <el-table :data="typeTable.records.value" v-loading="typeTable.loading.value" row-key="id" border class="admin-table">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="dictCode" label="字典编码" min-width="220" />
              <el-table-column prop="dictName" label="字典名称" min-width="220" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">{{ scope.row.status === 1 ? '启用' : '禁用' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" min-width="170" />
              <el-table-column label="操作" width="300" fixed="right">
                <template #default="scope">
                  <el-space wrap>
                    <el-button size="small" @click="openTypeDetail(scope.row.id)">详情</el-button>
                    <el-button v-if="can('sys:dict:type:update')" size="small" type="primary" @click="openTypeEdit(scope.row.id)">编辑</el-button>
                    <el-button v-if="can('sys:dict:type:delete')" size="small" type="danger" @click="handleTypeDelete(scope.row.id)">删除</el-button>
                  </el-space>
                </template>
              </el-table-column>
            </el-table>

            <AdminTablePager
              :current="typeTable.query.current"
              :size="typeTable.query.size"
              :total="typeTable.total.value"
              @update:current="typeTable.changePage"
              @update:size="(s) => { typeTable.query.size = s; typeTable.search() }"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="字典数据" name="data">
          <div class="space-y-4">
            <el-form inline>
              <el-form-item>
                <el-select v-model="dataTable.query.dictTypeCode" placeholder="字典类型" clearable filterable style="width: 220px">
                  <el-option v-for="item in typeOptions" :key="item.id" :label="`${item.dictCode} - ${item.dictName}`" :value="item.dictCode" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-input v-model="dataTable.query.keyword" placeholder="标签/值关键词" clearable />
              </el-form-item>
              <el-form-item>
                <el-select v-model="dataTable.query.status" placeholder="状态" clearable style="width: 140px">
                  <el-option label="启用" :value="1" />
                  <el-option label="禁用" :value="0" />
                </el-select>
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="dataTable.search">查询</el-button>
                <el-button @click="dataTable.resetQuery(); dataTable.search()">重置</el-button>
                <el-button v-if="can('sys:dict:data:create')" type="success" @click="openDataCreate">新建字典数据</el-button>
              </el-form-item>
            </el-form>

            <el-alert v-if="dataTable.error.value" type="error" :closable="false" :title="dataTable.error.value" />

            <el-table :data="dataTable.records.value" v-loading="dataTable.loading.value" row-key="id" border class="admin-table">
              <el-table-column prop="id" label="ID" width="80" />
              <el-table-column prop="dictTypeCode" label="字典类型" min-width="180" />
              <el-table-column prop="dictLabel" label="标签" min-width="180" />
              <el-table-column prop="dictValue" label="值" min-width="180" />
              <el-table-column prop="sort" label="排序" width="100" />
              <el-table-column prop="status" label="状态" width="100">
                <template #default="scope">
                  <el-tag :type="scope.row.status === 1 ? 'success' : 'danger'">{{ scope.row.status === 1 ? '启用' : '禁用' }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" min-width="170" />
              <el-table-column label="操作" width="300" fixed="right">
                <template #default="scope">
                  <el-space wrap>
                    <el-button size="small" @click="openDataDetail(scope.row.id)">详情</el-button>
                    <el-button v-if="can('sys:dict:data:update')" size="small" type="primary" @click="openDataEdit(scope.row.id)">编辑</el-button>
                    <el-button v-if="can('sys:dict:data:delete')" size="small" type="danger" @click="handleDataDelete(scope.row.id)">删除</el-button>
                  </el-space>
                </template>
              </el-table-column>
            </el-table>

            <AdminTablePager
              :current="dataTable.query.current"
              :size="dataTable.query.size"
              :total="dataTable.total.value"
              @update:current="dataTable.changePage"
              @update:size="(s) => { dataTable.query.size = s; dataTable.search() }"
            />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <el-drawer v-model="typeDetailVisible" title="字典类型详情" size="40%">
      <div v-if="typeDetail" class="space-y-2 text-sm">
        <p><strong>编码：</strong>{{ typeDetail.dictCode }}</p>
        <p><strong>名称：</strong>{{ typeDetail.dictName }}</p>
        <p><strong>状态：</strong>{{ typeDetail.status === 1 ? '启用' : '禁用' }}</p>
        <p><strong>创建时间：</strong>{{ typeDetail.createTime }}</p>
      </div>
    </el-drawer>

    <el-dialog v-model="typeEditVisible" :title="typeEditingId ? '编辑字典类型' : '新建字典类型'" width="620px" destroy-on-close>
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeFormRules" label-width="120px">
        <el-form-item label="字典编码" prop="dictCode">
          <el-input v-model="typeForm.dictCode" :disabled="Boolean(typeEditingId)" />
        </el-form-item>
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="typeForm.dictName" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="typeForm.status" style="width: 180px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeEditVisible = false">取消</el-button>
        <el-button type="primary" :loading="typeSaving" @click="submitType">保存</el-button>
      </template>
    </el-dialog>

    <el-drawer v-model="dataDetailVisible" title="字典数据详情" size="40%">
      <div v-if="dataDetail" class="space-y-2 text-sm">
        <p><strong>字典类型：</strong>{{ dataDetail.dictTypeCode }}</p>
        <p><strong>标签：</strong>{{ dataDetail.dictLabel }}</p>
        <p><strong>值：</strong>{{ dataDetail.dictValue }}</p>
        <p><strong>排序：</strong>{{ dataDetail.sort }}</p>
        <p><strong>状态：</strong>{{ dataDetail.status === 1 ? '启用' : '禁用' }}</p>
      </div>
    </el-drawer>

    <el-dialog v-model="dataEditVisible" :title="dataEditingId ? '编辑字典数据' : '新建字典数据'" width="680px" destroy-on-close>
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataFormRules" label-width="120px">
        <el-form-item label="字典类型" prop="dictTypeCode">
          <el-select v-model="dataForm.dictTypeCode" filterable style="width: 100%">
            <el-option v-for="item in typeOptions" :key="item.id" :label="`${item.dictCode} - ${item.dictName}`" :value="item.dictCode" />
          </el-select>
        </el-form-item>
        <el-form-item label="字典标签" prop="dictLabel">
          <el-input v-model="dataForm.dictLabel" />
        </el-form-item>
        <el-form-item label="字典值" prop="dictValue">
          <el-input v-model="dataForm.dictValue" />
        </el-form-item>
        <el-form-item label="排序" prop="sort">
          <el-input-number v-model="dataForm.sort" :min="0" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-select v-model="dataForm.status" style="width: 180px">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataEditVisible = false">取消</el-button>
        <el-button type="primary" :loading="dataSaving" @click="submitData">保存</el-button>
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
