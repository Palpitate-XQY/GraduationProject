<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import AdminPageShell from '@/components/admin/AdminPageShell.vue'
import AdminTablePager from '@/components/admin/AdminTablePager.vue'
import { useAdminTable } from '@/composables/useAdminTable'
import { useActionPermission } from '@/composables/useActionPermission'
import { useOrgOptions } from '@/composables/useOrgOptions'
import { useUserStore } from '@/stores/user'
import { REPAIR_STATUS_OPTIONS } from '@/constants/domain'
import { rolePage, userPage } from '@/api/system'
import {
  acceptRepair,
  assignRepair,
  closeRepair,
  confirmRepair,
  evaluateRepair,
  manageRepairDetail,
  manageRepairPage,
  processRepair,
  rejectRepair,
  reopenRepair,
  repairLogs,
  submitRepair,
  takeRepair,
  urgeRepair,
} from '@/api/repair'
import type { RepairOrderLogVO, RepairOrderVO } from '@/types/repair'
import type { RoleVO, UserVO } from '@/types/system'
import { toBackendQueryDateTime } from '@/utils/datetime'

const ASSIGN_ROLE_FETCH_SIZE = 100
const ASSIGN_USER_FETCH_SIZE = 100

interface AssignRoleGroup {
  key: string
  label: string
  roles: RoleVO[]
}

const { can } = useActionPermission()
const { byType: orgByType, loadOrgOptions } = useOrgOptions()
const userStore = useUserStore()

const table = useAdminTable<
  RepairOrderVO,
  {
    current: number
    size: number
    keyword: string
    status: string
    complexOrgId?: number
    propertyCompanyOrgId?: number
    maintainerUserId?: number
    residentUserId?: number
    createStartTime?: string
    createEndTime?: string
  }
>(
  {
    current: 1,
    size: 10,
    keyword: '',
    status: '',
    complexOrgId: undefined,
    propertyCompanyOrgId: undefined,
    maintainerUserId: undefined,
    residentUserId: undefined,
    createStartTime: '',
    createEndTime: '',
  },
  (query) =>
    manageRepairPage({
      ...query,
      createStartTime: toBackendQueryDateTime(query.createStartTime),
      createEndTime: toBackendQueryDateTime(query.createEndTime),
    }),
)

const detailVisible = ref(false)
const detailLoading = ref(false)
const detail = ref<RepairOrderVO | null>(null)
const logs = ref<RepairOrderLogVO[]>([])
const actionLoading = ref(false)

const assignVisible = ref(false)
const assignLoading = ref(false)
const assignSubmitting = ref(false)
const assignRolePool = ref<RoleVO[]>([])
const assignUserPool = ref<UserVO[]>([])
const assignError = ref('')
const assignForm = reactive({
  roleId: undefined as number | undefined,
  maintainerUserId: undefined as number | undefined,
  keyword: '',
  remark: '',
})

const canLoadAssignablePool = computed(() => can('sys:role:list') && can('sys:user:list'))
const currentRoleCodeSet = computed(
  () => new Set(Array.isArray(userStore.roleCodes) ? userStore.roleCodes : []),
)
const assignableChildRoles = computed(() => {
  const myRoleIds = assignRolePool.value
    .filter((role) => currentRoleCodeSet.value.has(role.roleCode))
    .map((role) => role.id)
  const myRoleIdSet = new Set(myRoleIds)
  return assignRolePool.value.filter(
    (role) => role.status === 1 && role.parentRoleId != null && myRoleIdSet.has(role.parentRoleId),
  )
})
const groupedAssignableChildRoles = computed<AssignRoleGroup[]>(() => {
  const roleMap = new Map(assignRolePool.value.map((item) => [item.id, item]))
  const groupMap = new Map<string, AssignRoleGroup>()

  assignableChildRoles.value.forEach((role) => {
    const parent = role.parentRoleId ? roleMap.get(role.parentRoleId) : null
    const key = parent ? `parent-${parent.id}` : 'parent-unknown'
    if (!groupMap.has(key)) {
      groupMap.set(key, {
        key,
        label: parent ? `${parent.roleName} (${parent.roleCode})` : '未识别父角色',
        roles: [],
      })
    }
    groupMap.get(key)!.roles.push(role)
  })

  return Array.from(groupMap.values()).map((group) => ({
    ...group,
    roles: group.roles.slice().sort((a, b) => a.sort - b.sort || a.id - b.id),
  }))
})
const assignableUsers = computed(() => {
  const keyword = assignForm.keyword.trim().toLowerCase()
  return assignUserPool.value
    .filter((item) => item.status === 1)
    .filter((item) => {
      if (!assignForm.roleId) return false
      return Array.isArray(item.roleIds) && item.roleIds.includes(assignForm.roleId)
    })
    .filter((item) => {
      if (!keyword) return true
      const nickname = (item.nickname || '').toLowerCase()
      const username = (item.username || '').toLowerCase()
      return nickname.includes(keyword) || username.includes(keyword)
    })
})

function parseIdList(input: string): number[] {
  return input
    .split(',')
    .map((item) => Number(item.trim()))
    .filter((item) => Number.isFinite(item) && item > 0)
}

async function askOptionalAttachmentIds() {
  try {
    const { value } = await ElMessageBox.prompt('附件文件ID（逗号分隔，可空）', '附件', {
      confirmButtonText: '确定',
      cancelButtonText: '跳过',
      inputPlaceholder: '例如: 12,15,20',
    })
    return parseIdList(value || '')
  } catch {
    return []
  }
}

async function openDetail(id: number) {
  detailVisible.value = true
  detailLoading.value = true
  try {
    const [d, l] = await Promise.all([manageRepairDetail(id), repairLogs(id)])
    detail.value = d.data
    logs.value = Array.isArray(l.data) ? l.data : []
  } finally {
    detailLoading.value = false
  }
}

async function refreshDetail() {
  if (!detail.value) return
  await openDetail(detail.value.id)
  await table.load()
}

async function runAction(label: string, executor: () => Promise<unknown>) {
  actionLoading.value = true
  try {
    await executor()
    ElMessage.success(`${label}成功`)
    await refreshDetail()
  } finally {
    actionLoading.value = false
  }
}

async function actionAccept() {
  if (!detail.value) return
  await runAction('受理', () => acceptRepair(detail.value!.id, {}))
}

async function actionReject() {
  if (!detail.value) return
  const result = await ElMessageBox.prompt('请输入驳回原因', '驳回工单', {
    confirmButtonText: '提交',
    inputValidator: (v) => (v ? true : '驳回原因不能为空'),
  }).catch(() => null)
  if (!result) return
  await runAction('驳回', () => rejectRepair(detail.value!.id, { reason: result.value }))
}

function resetAssignForm() {
  assignForm.roleId = undefined
  assignForm.maintainerUserId = undefined
  assignForm.keyword = ''
  assignForm.remark = ''
  assignError.value = ''
}

function handleAssignRoleChange() {
  assignForm.maintainerUserId = undefined
}

async function fetchAllRolesForAssign() {
  const result: RoleVO[] = []
  let current = 1
  let totalCount = 0

  while (true) {
    const res = await rolePage({ current, size: ASSIGN_ROLE_FETCH_SIZE, keyword: '' })
    const records = Array.isArray(res.data.records) ? res.data.records : []
    totalCount = Number(res.data.total || 0)
    result.push(...records)
    if (!records.length || result.length >= totalCount) break
    current += 1
  }

  const dedupe = new Map<number, RoleVO>()
  result.forEach((item) => dedupe.set(item.id, item))
  return Array.from(dedupe.values())
}

async function fetchAllUsersForAssign() {
  const result: UserVO[] = []
  let current = 1
  let totalCount = 0

  while (true) {
    const res = await userPage({ current, size: ASSIGN_USER_FETCH_SIZE, keyword: '', status: 1 })
    const records = Array.isArray(res.data.records) ? res.data.records : []
    totalCount = Number(res.data.total || 0)
    result.push(...records)
    if (!records.length || result.length >= totalCount) break
    current += 1
  }

  const dedupe = new Map<number, UserVO>()
  result.forEach((item) => dedupe.set(item.id, item))
  return Array.from(dedupe.values())
}

async function actionAssign() {
  if (!detail.value) return
  if (!canLoadAssignablePool.value) {
    await actionAssignByManualInput()
    return
  }

  resetAssignForm()
  assignVisible.value = true
  assignLoading.value = true
  try {
    const [roles, users] = await Promise.all([
      fetchAllRolesForAssign(),
      fetchAllUsersForAssign(),
    ])
    assignRolePool.value = roles
    assignUserPool.value = users

    if (assignableChildRoles.value.length === 1) {
      assignForm.roleId = assignableChildRoles.value[0].id
    }

    if (!assignableChildRoles.value.length) {
      assignError.value = '当前账号下暂无可转派的子角色，请先创建子角色并分配人员。'
    }
  } catch {
    assignVisible.value = false
    await actionAssignByManualInput()
  } finally {
    assignLoading.value = false
  }
}

async function actionAssignByManualInput() {
  if (!detail.value) return
  const result = await ElMessageBox.prompt('请输入转派目标用户ID', '转派工单', {
    confirmButtonText: '提交',
    inputValidator: (v) => (Number(v) > 0 ? true : '请输入有效用户ID'),
  }).catch(() => null)
  if (!result) return
  await runAction('转派', () => assignRepair(detail.value!.id, { maintainerUserId: Number(result.value) }))
}

async function submitAssign() {
  if (!detail.value) return
  if (!assignForm.roleId) {
    ElMessage.warning('请先选择子角色')
    return
  }
  if (!assignForm.maintainerUserId) {
    ElMessage.warning('请选择转派人员')
    return
  }
  if (!assignableUsers.value.some((item) => item.id === assignForm.maintainerUserId)) {
    ElMessage.warning('所选人员不在当前子角色下，请重新选择')
    return
  }

  assignSubmitting.value = true
  try {
    await runAction('转派', () =>
      assignRepair(detail.value!.id, {
        maintainerUserId: assignForm.maintainerUserId!,
        remark: assignForm.remark.trim() || undefined,
      }),
    )
    assignVisible.value = false
  } finally {
    assignSubmitting.value = false
  }
}

async function actionTake() {
  if (!detail.value) return
  await runAction('接单', () => takeRepair(detail.value!.id, {}))
}

async function actionProcess() {
  if (!detail.value) return
  const result = await ElMessageBox.prompt('请输入处理说明', '处理工单', {
    confirmButtonText: '提交',
    inputValidator: (v) => (v ? true : '处理说明不能为空'),
  }).catch(() => null)
  if (!result) return
  const attachmentFileIds = await askOptionalAttachmentIds()
  await runAction('处理', () => processRepair(detail.value!.id, { processDesc: result.value, attachmentFileIds }))
}

async function actionSubmit() {
  if (!detail.value) return
  const result = await ElMessageBox.prompt('请输入完工说明', '提交完工', {
    confirmButtonText: '提交',
    inputValidator: (v) => (v ? true : '完工说明不能为空'),
  }).catch(() => null)
  if (!result) return
  const attachmentFileIds = await askOptionalAttachmentIds()
  await runAction('提交', () => submitRepair(detail.value!.id, { finishDesc: result.value, attachmentFileIds }))
}

async function actionConfirm() {
  if (!detail.value) return
  await runAction('确认', () => confirmRepair(detail.value!.id, {}))
}

async function actionReopen() {
  if (!detail.value) return
  const result = await ElMessageBox.prompt('请输入重开反馈', '重开工单', {
    confirmButtonText: '提交',
    inputValidator: (v) => (v ? true : '反馈不能为空'),
  }).catch(() => null)
  if (!result) return
  const attachmentFileIds = await askOptionalAttachmentIds()
  await runAction('重开', () => reopenRepair(detail.value!.id, { feedback: result.value, attachmentFileIds }))
}

async function actionEvaluate() {
  if (!detail.value) return
  const scoreResult = await ElMessageBox.prompt('评分（1-5）', '评价工单', {
    confirmButtonText: '提交',
    inputValidator: (v) => {
      const n = Number(v)
      return n >= 1 && n <= 5 ? true : '评分范围 1-5'
    },
  }).catch(() => null)
  if (!scoreResult) return
  const { value: content } = await ElMessageBox.prompt('评价内容（可空）', '评价内容', {
    confirmButtonText: '提交',
    cancelButtonText: '跳过',
    inputPlaceholder: '输入评价内容',
  }).catch(() => ({ value: '' }))
  await runAction('评价', () =>
    evaluateRepair(detail.value!.id, {
      score: Number(scoreResult.value),
      content: content || undefined,
    }),
  )
}

async function actionUrge() {
  if (!detail.value) return
  const result = await ElMessageBox.prompt('请输入催单内容', '催单', {
    confirmButtonText: '提交',
    inputValidator: (v) => (v && v.trim() ? true : '催单内容不能为空'),
  }).catch(() => null)
  if (!result) return
  await runAction('催单', () => urgeRepair(detail.value!.id, { content: result.value.trim() }))
}

async function actionClose() {
  if (!detail.value) return
  await runAction('关闭', () => closeRepair(detail.value!.id, {}))
}

onMounted(async () => {
  await Promise.all([table.load(), loadOrgOptions()])
})
</script>

<template>
  <AdminPageShell title="报修工单" description="查询工单、查看日志并执行全流程状态动作。">
    <div class="liquid-glass rounded-3xl p-4 md:p-5 space-y-4">
      <el-form inline>
        <el-form-item>
          <el-input v-model="table.query.keyword" placeholder="关键词/工单号" clearable />
        </el-form-item>
        <el-form-item>
          <el-select v-model="table.query.status" placeholder="状态" clearable style="width: 160px">
            <el-option v-for="item in REPAIR_STATUS_OPTIONS" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="table.query.complexOrgId" filterable clearable placeholder="小区组织" style="width: 180px">
            <el-option v-for="item in orgByType.COMPLEX || []" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-select v-model="table.query.propertyCompanyOrgId" filterable clearable placeholder="物业公司组织" style="width: 200px">
            <el-option v-for="item in orgByType.PROPERTY_COMPANY || []" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input-number v-model="table.query.maintainerUserId" :min="1" placeholder="维修员ID" />
        </el-form-item>
        <el-form-item>
          <el-input-number v-model="table.query.residentUserId" :min="1" placeholder="居民ID" />
        </el-form-item>
        <el-form-item>
          <el-date-picker
            v-model="table.query.createStartTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="创建开始时间"
          />
        </el-form-item>
        <el-form-item>
          <el-date-picker
            v-model="table.query.createEndTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="创建结束时间"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="table.search">查询</el-button>
          <el-button @click="table.resetQuery(); table.search()">重置</el-button>
        </el-form-item>
      </el-form>

      <el-alert v-if="table.error.value" type="error" :closable="false" :title="table.error.value" />

      <el-table :data="table.records.value" v-loading="table.loading.value" row-key="id" border class="admin-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="orderNo" label="工单号" min-width="170" />
        <el-table-column prop="title" label="标题" min-width="220" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="130" />
        <el-table-column prop="residentUserId" label="居民ID" width="100" />
        <el-table-column prop="maintainerUserId" label="维修员ID" width="110" />
        <el-table-column prop="createTime" label="创建时间" min-width="170" />
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="scope">
            <el-button size="small" @click="openDetail(scope.row.id)">详情</el-button>
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

    <el-drawer v-model="detailVisible" title="工单详情与流程动作" size="62%">
      <div v-loading="detailLoading" class="space-y-4">
        <template v-if="detail">
          <div class="grid grid-cols-2 gap-3 text-sm">
            <p><strong>工单号：</strong>{{ detail.orderNo }}</p>
            <p><strong>状态：</strong>{{ detail.status }}</p>
            <p><strong>标题：</strong>{{ detail.title }}</p>
            <p><strong>紧急程度：</strong>{{ detail.emergencyLevel }}</p>
            <p><strong>居民ID：</strong>{{ detail.residentUserId }}</p>
            <p><strong>维修员ID：</strong>{{ detail.maintainerUserId || '-' }}</p>
            <p><strong>地址：</strong>{{ detail.repairAddress }}</p>
            <p><strong>催单次数：</strong>{{ detail.urgeCount }}</p>
          </div>

          <div class="liquid-glass rounded-2xl p-3">
            <p class="font-medium">工单描述</p>
            <p class="mt-2 text-sm whitespace-pre-wrap">{{ detail.description }}</p>
          </div>

          <div class="flex flex-wrap gap-2">
            <el-button v-if="can('repair:accept')" :loading="actionLoading" @click="actionAccept">受理</el-button>
            <el-button v-if="can('repair:reject')" :loading="actionLoading" type="warning" @click="actionReject">驳回</el-button>
            <el-button v-if="can('repair:assign')" :loading="actionLoading" @click="actionAssign">转派</el-button>
            <el-button v-if="can('repair:take')" :loading="actionLoading" @click="actionTake">接单</el-button>
            <el-button v-if="can('repair:process')" :loading="actionLoading" @click="actionProcess">处理</el-button>
            <el-button v-if="can('repair:submit')" :loading="actionLoading" @click="actionSubmit">提交</el-button>
            <el-button v-if="can('repair:confirm')" :loading="actionLoading" @click="actionConfirm">确认</el-button>
            <el-button v-if="can('repair:reopen')" :loading="actionLoading" type="warning" @click="actionReopen">重开</el-button>
            <el-button v-if="can('repair:evaluate')" :loading="actionLoading" @click="actionEvaluate">评价</el-button>
            <el-button v-if="can('repair:urge')" :loading="actionLoading" @click="actionUrge">催单</el-button>
            <el-button v-if="can('repair:close')" :loading="actionLoading" type="danger" @click="actionClose">关闭</el-button>
          </div>

          <div>
            <p class="font-medium mb-2">流程日志</p>
            <el-table :data="logs" size="small" border>
              <el-table-column prop="operationTime" label="操作时间" min-width="170" />
              <el-table-column prop="operationType" label="动作" width="130" />
              <el-table-column prop="fromStatus" label="原状态" width="120" />
              <el-table-column prop="toStatus" label="目标状态" width="120" />
              <el-table-column prop="operatorUserId" label="操作人" width="90" />
              <el-table-column prop="operationRemark" label="备注" min-width="220" show-overflow-tooltip />
            </el-table>
          </div>
        </template>
      </div>
    </el-drawer>

    <el-dialog v-model="assignVisible" title="转派工单（按子角色筛选）" width="680px" destroy-on-close>
      <div v-loading="assignLoading" class="space-y-4">
        <div v-if="detail" class="text-xs text-white/70">
          当前工单：{{ detail.orderNo }}
        </div>

        <el-alert v-if="assignError" type="warning" :closable="false" :title="assignError" />

        <el-form label-width="100px">
          <el-form-item label="子角色">
            <el-select
              v-model="assignForm.roleId"
              style="width: 100%"
              filterable
              clearable
              placeholder="选择子角色（如维修员/清扫员/调解员）"
              :disabled="!!assignError"
              @change="handleAssignRoleChange"
            >
              <el-option-group
                v-for="group in groupedAssignableChildRoles"
                :key="group.key"
                :label="group.label"
              >
                <el-option
                  v-for="role in group.roles"
                  :key="role.id"
                  :label="`${role.roleName} (${role.roleCode})`"
                  :value="role.id"
                />
              </el-option-group>
            </el-select>
          </el-form-item>

          <el-form-item label="人员筛选">
            <el-input
              v-model="assignForm.keyword"
              placeholder="输入昵称或用户名过滤候选人"
              clearable
              :disabled="!assignForm.roleId || !!assignError"
            />
          </el-form-item>

          <el-form-item label="转派对象">
            <el-select
              v-model="assignForm.maintainerUserId"
              style="width: 100%"
              filterable
              placeholder="请选择转派人员"
              :disabled="!assignForm.roleId || !!assignError"
            >
              <el-option
                v-for="user in assignableUsers"
                :key="user.id"
                :label="`${user.nickname || user.username} (${user.username}) #${user.id}`"
                :value="user.id"
              />
            </el-select>
            <div class="mt-1 text-xs text-white/60">候选人数：{{ assignableUsers.length }}</div>
          </el-form-item>

          <el-form-item label="备注">
            <el-input
              v-model="assignForm.remark"
              type="textarea"
              :rows="2"
              maxlength="200"
              show-word-limit
              placeholder="可选：补充转派说明"
            />
          </el-form-item>
        </el-form>

        <el-empty
          v-if="assignForm.roleId && !assignableUsers.length && !assignLoading"
          description="该子角色下暂无可转派人员"
        />
      </div>

      <template #footer>
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="assignSubmitting" @click="submitAssign">确认转派</el-button>
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
