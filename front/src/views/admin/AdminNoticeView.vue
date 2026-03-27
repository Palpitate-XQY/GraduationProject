<script setup lang="ts">
import { onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import AdminPageShell from '@/components/admin/AdminPageShell.vue'
import AdminTablePager from '@/components/admin/AdminTablePager.vue'
import AdminFilePicker from '@/components/admin/AdminFilePicker.vue'
import { useAdminTable } from '@/composables/useAdminTable'
import { useActionPermission } from '@/composables/useActionPermission'
import { useOrgOptions } from '@/composables/useOrgOptions'
import { NOTICE_STATUS_OPTIONS, NOTICE_TYPE_OPTIONS } from '@/constants/domain'
import {
  createNotice,
  deleteNotice,
  manageNoticeDetail,
  manageNoticePage,
  publishNotice,
  recallNotice,
  updateNotice,
} from '@/api/notice'
import type { NoticeCreateRequest, NoticeScopeItem, NoticeType, NoticeUpdateRequest, NoticeVO } from '@/types/notice'

interface ScopeFormItem {
  scopeType: string
  scopeRefId?: number
}

const NOTICE_SCOPE_TYPE_OPTIONS = [
  { label: '街道范围', value: 'STREET' },
  { label: '社区范围', value: 'COMMUNITY' },
  { label: '小区范围', value: 'COMPLEX' },
  { label: '物业公司范围', value: 'PROPERTY_COMPANY' },
  { label: '自定义范围', value: 'CUSTOM' },
] as const

const SCOPE_ORG_TYPE_MAP: Record<string, string | undefined> = {
  STREET: 'STREET',
  COMMUNITY: 'COMMUNITY',
  COMPLEX: 'COMPLEX',
  PROPERTY_COMPANY: 'PROPERTY_COMPANY',
}

const { can } = useActionPermission()
const { options: orgOptions, loadOrgOptions } = useOrgOptions()

const table = useAdminTable<NoticeVO, { current: number; size: number; keyword: string; noticeType: string; status: string }>(
  {
    current: 1,
    size: 10,
    keyword: '',
    noticeType: '',
    status: '',
  },
  (query) => manageNoticePage(query),
)

const detailLoading = ref(false)
const detailVisible = ref(false)
const detail = ref<NoticeVO | null>(null)

const editVisible = ref(false)
const saving = ref(false)
const editingId = ref<number | null>(null)
const coverIds = ref<number[]>([])
const attachmentIds = ref<number[]>([])
const scopeItems = ref<ScopeFormItem[]>([])
const scopeError = ref('')

const formRef = ref<FormInstance>()
const form = reactive({
  noticeType: 'STREET_COMMUNITY' as NoticeType,
  title: '',
  content: '',
  topFlag: 0,
})

const formRules: FormRules = {
  noticeType: [{ required: true, message: '请选择公告类型', trigger: 'change' }],
  title: [{ required: true, message: '请输入公告标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入公告内容', trigger: 'blur' }],
}

watch(
  () => form.noticeType,
  (type) => {
    if (type === 'PROPERTY') {
      scopeItems.value = scopeItems.value.map((item) => ({
        ...item,
        scopeType: 'COMPLEX',
      }))
    }
  },
)

function newScopeItem(): ScopeFormItem {
  return {
    scopeType: form.noticeType === 'PROPERTY' ? 'COMPLEX' : 'COMMUNITY',
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
  scopeItems.value.push(newScopeItem())
}

function removeScopeRow(index: number) {
  scopeItems.value.splice(index, 1)
}

function resetForm() {
  editingId.value = null
  form.noticeType = 'STREET_COMMUNITY'
  form.title = ''
  form.content = ''
  form.topFlag = 0
  scopeItems.value = [newScopeItem()]
  scopeError.value = ''
  coverIds.value = []
  attachmentIds.value = []
}

async function openDetail(id: number) {
  detailLoading.value = true
  detailVisible.value = true
  try {
    const res = await manageNoticeDetail(id)
    detail.value = res.data
  } finally {
    detailLoading.value = false
  }
}

function openCreate() {
  resetForm()
  editVisible.value = true
}

async function openEdit(id: number) {
  resetForm()
  const res = await manageNoticeDetail(id)
  const row = res.data
  editingId.value = row.id
  form.noticeType = (row.noticeType as NoticeType) || 'STREET_COMMUNITY'
  form.title = row.title
  form.content = row.content
  form.topFlag = row.topFlag || 0
  scopeItems.value = (row.scopeItems || []).map((item) => ({
    scopeType: item.scopeType,
    scopeRefId: item.scopeRefId,
  }))
  if (!scopeItems.value.length) {
    scopeItems.value = [newScopeItem()]
  }
  if (row.coverFileId) {
    coverIds.value = [row.coverFileId]
  }
  attachmentIds.value = (row.attachments || []).map((item) => item.fileId)
  editVisible.value = true
}

function buildScopePayload() {
  if (!scopeItems.value.length) {
    scopeError.value = '请至少配置一个可见范围'
    return null
  }
  const payload: NoticeScopeItem[] = []
  for (const item of scopeItems.value) {
    if (!item.scopeType) {
      scopeError.value = '可见范围类型不能为空'
      return null
    }
    if (!item.scopeRefId) {
      scopeError.value = '可见范围组织不能为空'
      return null
    }
    if (form.noticeType === 'PROPERTY' && item.scopeType !== 'COMPLEX') {
      scopeError.value = '物业公告仅允许选择小区范围'
      return null
    }
    payload.push({
      scopeType: item.scopeType,
      scopeRefId: item.scopeRefId,
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

  const payloadBase: NoticeCreateRequest = {
    noticeType: form.noticeType,
    title: form.title.trim(),
    content: form.content.trim(),
    coverFileId: coverIds.value[0] || null,
    attachmentJson: attachmentIds.value.length ? JSON.stringify(attachmentIds.value) : null,
    topFlag: form.topFlag,
    scopeItems: scopePayload,
  }

  saving.value = true
  try {
    if (editingId.value) {
      const payload: NoticeUpdateRequest = {
        id: editingId.value,
        ...payloadBase,
      }
      await updateNotice(payload)
      ElMessage.success('公告更新成功')
    } else {
      await createNotice(payloadBase)
      ElMessage.success('公告创建成功')
    }
    editVisible.value = false
    await table.load()
  } finally {
    saving.value = false
  }
}

async function handlePublish(id: number) {
  await publishNotice(id)
  ElMessage.success('已发布')
  await table.load()
}

async function handleRecall(id: number) {
  await recallNotice(id)
  ElMessage.success('已撤回')
  await table.load()
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确认删除该公告吗？', '提示', { type: 'warning' })
  await deleteNotice(id)
  ElMessage.success('已删除')
  await table.load()
}

onMounted(async () => {
  await Promise.all([table.load(), loadOrgOptions()])
})
</script>

<template>
  <AdminPageShell title="公告管理" description="管理公告草稿、发布范围与发布状态。">
    <div class="liquid-glass rounded-3xl p-4 md:p-5 space-y-4">
      <el-form inline>
        <el-form-item>
          <el-input v-model="table.query.keyword" placeholder="关键词" clearable />
        </el-form-item>
        <el-form-item>
          <el-select v-model="table.query.noticeType" placeholder="类型" clearable style="width: 180px">
            <el-option v-for="item in NOTICE_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="table.query.status" placeholder="状态" clearable style="width: 150px">
            <el-option v-for="item in NOTICE_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="table.search">查询</el-button>
          <el-button @click="table.resetQuery(); table.search()">重置</el-button>
          <el-button v-if="can('notice:create')" type="success" @click="openCreate">新建公告</el-button>
        </el-form-item>
      </el-form>

      <el-alert v-if="table.error.value" type="error" :closable="false" :title="table.error.value" />

      <el-table :data="table.records.value" v-loading="table.loading.value" row-key="id" class="admin-table" border>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="noticeType" label="类型" width="140" />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="publisherOrgId" label="发布组织" width="120" />
        <el-table-column prop="publishTime" label="发布时间" min-width="170" />
        <el-table-column label="操作" width="360" fixed="right">
          <template #default="scope">
            <el-space wrap>
              <el-button size="small" @click="openDetail(scope.row.id)">详情</el-button>
              <el-button v-if="can('notice:update')" size="small" type="primary" @click="openEdit(scope.row.id)">编辑</el-button>
              <el-button v-if="can('notice:publish')" size="small" type="success" @click="handlePublish(scope.row.id)">发布</el-button>
              <el-button v-if="can('notice:recall')" size="small" type="warning" @click="handleRecall(scope.row.id)">撤回</el-button>
              <el-button v-if="can('notice:delete')" size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
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

    <el-drawer v-model="detailVisible" title="公告详情" size="45%">
      <div v-loading="detailLoading" class="space-y-3 text-sm">
        <template v-if="detail">
          <p><strong>标题：</strong>{{ detail.title }}</p>
          <p><strong>类型：</strong>{{ detail.noticeType }}</p>
          <p><strong>状态：</strong>{{ detail.status }}</p>
          <p><strong>发布时间：</strong>{{ detail.publishTime || '-' }}</p>
          <p><strong>范围：</strong>{{ JSON.stringify(detail.scopeItems) }}</p>
          <p><strong>内容：</strong></p>
          <div class="rounded-xl border border-white/15 p-3 whitespace-pre-wrap">{{ detail.content }}</div>
        </template>
      </div>
    </el-drawer>

    <el-dialog v-model="editVisible" :title="editingId ? '编辑公告' : '新建公告'" width="860px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px">
        <el-form-item label="公告类型" prop="noticeType">
          <el-select v-model="form.noticeType" style="width: 280px">
            <el-option v-for="item in NOTICE_TYPE_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="6" />
        </el-form-item>
        <el-form-item label="置顶标记">
          <el-switch v-model="form.topFlag" :active-value="1" :inactive-value="0" />
        </el-form-item>
        <el-form-item label="封面文件">
          <AdminFilePicker v-model="coverIds" :multiple="false" biz-type="NOTICE" />
        </el-form-item>
        <el-form-item label="附件文件">
          <AdminFilePicker v-model="attachmentIds" :multiple="true" biz-type="NOTICE" />
        </el-form-item>
        <el-form-item label="可见范围">
          <div class="w-full space-y-3">
            <div
              v-for="(item, index) in scopeItems"
              :key="index"
              class="grid grid-cols-12 gap-2 items-center rounded-2xl border border-white/15 p-3 bg-white/5"
            >
              <div class="col-span-4">
                <el-select v-model="item.scopeType" style="width: 100%">
                  <el-option v-for="option in NOTICE_SCOPE_TYPE_OPTIONS" :key="option.value" :label="option.label" :value="option.value" />
                </el-select>
              </div>
              <div class="col-span-7">
                <el-select v-model="item.scopeRefId" filterable placeholder="选择组织" style="width: 100%">
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
