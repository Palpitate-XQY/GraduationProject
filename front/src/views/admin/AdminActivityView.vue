<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'
import AdminPageShell from '@/components/admin/AdminPageShell.vue'
import AdminTablePager from '@/components/admin/AdminTablePager.vue'
import AdminFilePicker from '@/components/admin/AdminFilePicker.vue'
import { useAdminTable } from '@/composables/useAdminTable'
import { useActionPermission } from '@/composables/useActionPermission'
import { useOrgOptions } from '@/composables/useOrgOptions'
import { ACTIVITY_STATUS_OPTIONS } from '@/constants/domain'
import {
  activitySignupList,
  activityStats,
  createActivity,
  deleteActivity,
  manageActivityDetail,
  manageActivityPage,
  publishActivity,
  recallActivity,
  updateActivity,
} from '@/api/activity'
import type {
  ActivityCreateRequest,
  ActivityScopeItem,
  ActivitySignupVO,
  ActivityStatsVO,
  ActivityUpdateRequest,
  ActivityVO,
} from '@/types/activity'
import { parseTimestamp, toIsoLocalDateTime } from '@/utils/datetime'

interface ScopeFormItem {
  scopeType: string
  scopeRefId?: number
}

const ACTIVITY_SCOPE_TYPE_OPTIONS = [
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

const table = useAdminTable<ActivityVO, { current: number; size: number; keyword: string; status: string }>(
  { current: 1, size: 10, keyword: '', status: '' },
  (query) => manageActivityPage(query),
)

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<ActivityVO | null>(null)
const stats = ref<ActivityStatsVO | null>(null)
const signups = ref<ActivitySignupVO[]>([])

const editVisible = ref(false)
const saving = ref(false)
const editingId = ref<number | null>(null)
const coverIds = ref<number[]>([])
const attachmentIds = ref<number[]>([])
const scopeItems = ref<ScopeFormItem[]>([])
const scopeError = ref('')

const formRef = ref<FormInstance>()
const form = reactive({
  title: '',
  content: '',
  activityStartTime: '',
  activityEndTime: '',
  signupStartTime: '',
  signupEndTime: '',
  location: '',
  maxParticipants: 100,
})

const formRules: FormRules = {
  title: [{ required: true, message: '请输入活动标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入活动内容', trigger: 'blur' }],
  activityStartTime: [{ required: true, message: '请选择活动开始时间', trigger: 'change' }],
  activityEndTime: [{ required: true, message: '请选择活动结束时间', trigger: 'change' }],
  signupStartTime: [{ required: true, message: '请选择报名开始时间', trigger: 'change' }],
  signupEndTime: [{ required: true, message: '请选择报名结束时间', trigger: 'change' }],
  location: [{ required: true, message: '请输入活动地点', trigger: 'blur' }],
  maxParticipants: [{ required: true, message: '请输入人数上限', trigger: 'change' }],
}

function newScopeItem(): ScopeFormItem {
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
  scopeItems.value.push(newScopeItem())
}

function removeScopeRow(index: number) {
  scopeItems.value.splice(index, 1)
}

function resetForm() {
  editingId.value = null
  form.title = ''
  form.content = ''
  form.activityStartTime = ''
  form.activityEndTime = ''
  form.signupStartTime = ''
  form.signupEndTime = ''
  form.location = ''
  form.maxParticipants = 100
  scopeItems.value = [newScopeItem()]
  scopeError.value = ''
  coverIds.value = []
  attachmentIds.value = []
}

async function openDetail(id: number) {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const [detailRes, statsRes, signupRes] = await Promise.all([
      manageActivityDetail(id),
      activityStats(id),
      activitySignupList(id),
    ])
    detail.value = detailRes.data
    stats.value = statsRes.data
    signups.value = Array.isArray(signupRes.data) ? signupRes.data : []
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
  const res = await manageActivityDetail(id)
  const row = res.data
  editingId.value = row.id
  form.title = row.title
  form.content = row.content
  form.activityStartTime = toIsoLocalDateTime(row.activityStartTime) || ''
  form.activityEndTime = toIsoLocalDateTime(row.activityEndTime) || ''
  form.signupStartTime = toIsoLocalDateTime(row.signupStartTime) || ''
  form.signupEndTime = toIsoLocalDateTime(row.signupEndTime) || ''
  form.location = row.location
  form.maxParticipants = row.maxParticipants
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

function validateTimeRange() {
  const signupStart = parseTimestamp(form.signupStartTime)
  const signupEnd = parseTimestamp(form.signupEndTime)
  const activityStart = parseTimestamp(form.activityStartTime)
  const activityEnd = parseTimestamp(form.activityEndTime)

  if (Number.isNaN(signupStart) || Number.isNaN(signupEnd) || Number.isNaN(activityStart) || Number.isNaN(activityEnd)) {
    ElMessage.warning('时间格式不正确，请重新选择')
    return false
  }
  if (signupStart > signupEnd) {
    ElMessage.warning('报名开始时间不能晚于报名结束时间')
    return false
  }
  if (activityStart > activityEnd) {
    ElMessage.warning('活动开始时间不能晚于活动结束时间')
    return false
  }
  if (signupEnd > activityEnd) {
    ElMessage.warning('报名结束时间不能晚于活动结束时间')
    return false
  }
  return true
}

function buildScopePayload() {
  if (!scopeItems.value.length) {
    scopeError.value = '请至少配置一个可见范围'
    return null
  }
  const payload: ActivityScopeItem[] = []
  for (const item of scopeItems.value) {
    if (!item.scopeType) {
      scopeError.value = '可见范围类型不能为空'
      return null
    }
    if (item.scopeType === 'SELF') {
      scopeError.value = '活动范围不支持 SELF 类型'
      return null
    }
    if (!item.scopeRefId) {
      scopeError.value = '可见范围组织不能为空'
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
  if (!validateTimeRange()) return

  const scopePayload = buildScopePayload()
  if (!scopePayload) return

  const payloadBase: ActivityCreateRequest = {
    title: form.title.trim(),
    content: form.content.trim(),
    coverFileId: coverIds.value[0] || null,
    attachmentJson: attachmentIds.value.length ? JSON.stringify(attachmentIds.value) : null,
    activityStartTime: toIsoLocalDateTime(form.activityStartTime)!,
    activityEndTime: toIsoLocalDateTime(form.activityEndTime)!,
    signupStartTime: toIsoLocalDateTime(form.signupStartTime)!,
    signupEndTime: toIsoLocalDateTime(form.signupEndTime)!,
    location: form.location.trim(),
    maxParticipants: form.maxParticipants,
    scopeItems: scopePayload,
  }

  saving.value = true
  try {
    if (editingId.value) {
      const payload: ActivityUpdateRequest = {
        id: editingId.value,
        ...payloadBase,
      }
      await updateActivity(payload)
      ElMessage.success('活动更新成功')
    } else {
      await createActivity(payloadBase)
      ElMessage.success('活动创建成功')
    }
    editVisible.value = false
    await table.load()
  } finally {
    saving.value = false
  }
}

async function handlePublish(id: number) {
  await publishActivity(id)
  ElMessage.success('已发布')
  await table.load()
}

async function handleRecall(id: number) {
  await recallActivity(id)
  ElMessage.success('已撤回')
  await table.load()
}

async function handleDelete(id: number) {
  await ElMessageBox.confirm('确认删除该活动吗？', '提示', { type: 'warning' })
  await deleteActivity(id)
  ElMessage.success('已删除')
  await table.load()
}

onMounted(async () => {
  await Promise.all([table.load(), loadOrgOptions()])
})
</script>

<template>
  <AdminPageShell title="活动管理" description="管理活动发布、报名统计与参与人名单。">
    <div class="liquid-glass rounded-3xl p-4 md:p-5 space-y-4">
      <el-form inline>
        <el-form-item>
          <el-input v-model="table.query.keyword" placeholder="关键词" clearable />
        </el-form-item>
        <el-form-item>
          <el-select v-model="table.query.status" placeholder="状态" clearable style="width: 160px">
            <el-option v-for="item in ACTIVITY_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="table.search">查询</el-button>
          <el-button @click="table.resetQuery(); table.search()">重置</el-button>
          <el-button v-if="can('activity:create')" type="success" @click="openCreate">新建活动</el-button>
        </el-form-item>
      </el-form>

      <el-alert v-if="table.error.value" type="error" :closable="false" :title="table.error.value" />

      <el-table :data="table.records.value" v-loading="table.loading.value" row-key="id" border class="admin-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="title" label="活动标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="120" />
        <el-table-column prop="location" label="地点" min-width="160" show-overflow-tooltip />
        <el-table-column prop="activityStartTime" label="开始时间" min-width="165" />
        <el-table-column prop="activityEndTime" label="结束时间" min-width="165" />
        <el-table-column label="操作" width="390" fixed="right">
          <template #default="scope">
            <el-space wrap>
              <el-button size="small" @click="openDetail(scope.row.id)">详情</el-button>
              <el-button v-if="can('activity:update')" size="small" type="primary" @click="openEdit(scope.row.id)">编辑</el-button>
              <el-button v-if="can('activity:publish')" size="small" type="success" @click="handlePublish(scope.row.id)">发布</el-button>
              <el-button v-if="can('activity:recall')" size="small" type="warning" @click="handleRecall(scope.row.id)">撤回</el-button>
              <el-button v-if="can('activity:delete')" size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
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

    <el-drawer v-model="detailVisible" title="活动详情" size="58%">
      <div v-loading="detailLoading" class="space-y-4">
        <template v-if="detail">
          <div class="grid grid-cols-2 gap-3 text-sm">
            <p><strong>标题：</strong>{{ detail.title }}</p>
            <p><strong>状态：</strong>{{ detail.status }}</p>
            <p><strong>报名窗口：</strong>{{ detail.signupStartTime }} ~ {{ detail.signupEndTime }}</p>
            <p><strong>活动时间：</strong>{{ detail.activityStartTime }} ~ {{ detail.activityEndTime }}</p>
            <p><strong>地点：</strong>{{ detail.location }}</p>
            <p><strong>人数上限：</strong>{{ detail.maxParticipants }}</p>
          </div>
          <div>
            <p class="font-medium mb-1">内容</p>
            <div class="rounded-xl border border-white/15 p-3 whitespace-pre-wrap text-sm">{{ detail.content }}</div>
          </div>
          <div class="grid grid-cols-3 gap-3">
            <div class="liquid-glass rounded-2xl p-3">
              <p class="text-xs text-white/60">报名人数</p>
              <p class="text-2xl font-heading italic text-white">{{ stats?.signupCount ?? 0 }}</p>
            </div>
            <div class="liquid-glass rounded-2xl p-3">
              <p class="text-xs text-white/60">最大人数</p>
              <p class="text-2xl font-heading italic text-white">{{ stats?.maxParticipants ?? detail.maxParticipants }}</p>
            </div>
            <div class="liquid-glass rounded-2xl p-3">
              <p class="text-xs text-white/60">剩余名额</p>
              <p class="text-2xl font-heading italic text-white">{{ stats?.remainingSlots ?? 0 }}</p>
            </div>
          </div>
          <div>
            <p class="font-medium mb-2">报名名单</p>
            <el-table :data="signups" size="small" border>
              <el-table-column prop="id" label="记录ID" width="90" />
              <el-table-column prop="userId" label="用户ID" width="90" />
              <el-table-column prop="signupStatus" label="状态" width="100" />
              <el-table-column prop="signupTime" label="报名时间" min-width="160" />
              <el-table-column prop="cancelTime" label="取消时间" min-width="160" />
            </el-table>
          </div>
        </template>
      </div>
    </el-drawer>

    <el-dialog v-model="editVisible" :title="editingId ? '编辑活动' : '新建活动'" width="920px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="120px">
        <el-form-item label="活动标题" prop="title">
          <el-input v-model="form.title" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="活动内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="5" />
        </el-form-item>
        <el-form-item label="报名开始" prop="signupStartTime">
          <el-date-picker v-model="form.signupStartTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="报名结束" prop="signupEndTime">
          <el-date-picker v-model="form.signupEndTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="活动开始" prop="activityStartTime">
          <el-date-picker v-model="form.activityStartTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="活动结束" prop="activityEndTime">
          <el-date-picker v-model="form.activityEndTime" type="datetime" value-format="YYYY-MM-DDTHH:mm:ss" style="width: 100%" />
        </el-form-item>
        <el-form-item label="活动地点" prop="location">
          <el-input v-model="form.location" />
        </el-form-item>
        <el-form-item label="最大人数" prop="maxParticipants">
          <el-input-number v-model="form.maxParticipants" :min="1" :max="100000" />
        </el-form-item>
        <el-form-item label="封面文件">
          <AdminFilePicker v-model="coverIds" :multiple="false" biz-type="ACTIVITY" />
        </el-form-item>
        <el-form-item label="附件文件">
          <AdminFilePicker v-model="attachmentIds" :multiple="true" biz-type="ACTIVITY" />
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
                  <el-option v-for="option in ACTIVITY_SCOPE_TYPE_OPTIONS" :key="option.value" :label="option.label" :value="option.value" />
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
