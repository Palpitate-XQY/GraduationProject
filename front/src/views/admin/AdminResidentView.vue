<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import AdminPageShell from '@/components/admin/AdminPageShell.vue'
import AdminTablePager from '@/components/admin/AdminTablePager.vue'
import { useAdminTable } from '@/composables/useAdminTable'
import { useOrgOptions } from '@/composables/useOrgOptions'
import { getResidentProfileDetail, saveResidentProfileByAdmin } from '@/api/resident-admin'
import { userDetail, userPage } from '@/api/system'
import type { ResidentProfileVO } from '@/types/resident'
import type { UserVO } from '@/types/system'

const onlyResident = ref(true)
const quickUserId = ref<number | undefined>()
const { byType: orgByType, loadOrgOptions } = useOrgOptions()

const table = useAdminTable<UserVO, { current: number; size: number; keyword: string }>(
  { current: 1, size: 10, keyword: '' },
  (query) => userPage(query),
)

const displayRows = computed(() => {
  if (!onlyResident.value) return table.records.value
  return table.records.value.filter((row) => (row.roleCodes || []).includes('RESIDENT'))
})

const profileVisible = ref(false)
const profileLoading = ref(false)
const profile = ref<ResidentProfileVO | null>(null)
const profileUser = ref<UserVO | null>(null)

const editVisible = ref(false)
const saving = ref(false)
const editForm = reactive({
  userId: 0,
  complexOrgId: 0,
  roomNo: '',
  emergencyContact: '',
  emergencyPhone: '',
})

async function queryByUserId() {
  if (!quickUserId.value) return
  const res = await userDetail(quickUserId.value)
  table.records.value = [res.data]
  table.total.value = 1
  table.query.current = 1
}

async function openProfile(row: UserVO) {
  profileUser.value = row
  profileVisible.value = true
  profileLoading.value = true
  profile.value = null
  try {
    const res = await getResidentProfileDetail(row.id)
    profile.value = res.data
  } catch {
    profile.value = null
  } finally {
    profileLoading.value = false
  }
}

function openEditProfile(row: UserVO) {
  editForm.userId = row.id
  editForm.complexOrgId = profile.value?.complexOrgId || row.orgId
  editForm.roomNo = profile.value?.roomNo || ''
  editForm.emergencyContact = profile.value?.emergencyContact || ''
  editForm.emergencyPhone = profile.value?.emergencyPhone || ''
  editVisible.value = true
}

async function submitProfile() {
  if (!editForm.userId || !editForm.complexOrgId) {
    ElMessage.warning('请填写用户ID与小区组织')
    return
  }
  if (editForm.emergencyPhone && !/^[0-9+\-]{6,20}$/.test(editForm.emergencyPhone)) {
    ElMessage.warning('紧急联系电话格式不正确')
    return
  }
  saving.value = true
  try {
    await saveResidentProfileByAdmin({
      userId: editForm.userId,
      complexOrgId: editForm.complexOrgId,
      roomNo: editForm.roomNo || undefined,
      emergencyContact: editForm.emergencyContact || undefined,
      emergencyPhone: editForm.emergencyPhone || undefined,
    })
    ElMessage.success('档案保存成功')
    editVisible.value = false
    if (profileUser.value) {
      await openProfile(profileUser.value)
    }
  } finally {
    saving.value = false
  }
}

onMounted(async () => {
  await Promise.all([table.load(), loadOrgOptions()])
})
</script>

<template>
  <AdminPageShell title="居民档案" description="基于系统用户分页管理居民档案，支持仅居民过滤与 userId 快速检索。">
    <div class="liquid-glass rounded-3xl p-4 md:p-5 space-y-4">
      <el-form inline>
        <el-form-item>
          <el-input v-model="table.query.keyword" placeholder="用户名/昵称" clearable />
        </el-form-item>
        <el-form-item label="仅居民">
          <el-switch v-model="onlyResident" />
        </el-form-item>
        <el-form-item label="UserId 快速检索">
          <el-input-number v-model="quickUserId" :min="1" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="table.search">查询分页</el-button>
          <el-button @click="queryByUserId">按ID检索</el-button>
          <el-button @click="quickUserId=undefined; table.resetQuery(); table.search()">重置</el-button>
        </el-form-item>
      </el-form>

      <el-alert v-if="table.error.value" type="error" :closable="false" :title="table.error.value" />

      <el-table :data="displayRows" v-loading="table.loading.value" row-key="id" border class="admin-table">
        <el-table-column prop="id" label="用户ID" width="90" />
        <el-table-column prop="username" label="用户名" width="140" />
        <el-table-column prop="nickname" label="昵称" width="140" />
        <el-table-column prop="orgName" label="当前组织" min-width="180" />
        <el-table-column prop="roleCodes" label="角色" min-width="200" show-overflow-tooltip>
          <template #default="scope">{{ (scope.row.roleCodes || []).join(',') }}</template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-space>
              <el-button size="small" @click="openProfile(scope.row)">档案详情</el-button>
              <el-button size="small" type="primary" @click="openEditProfile(scope.row)">编辑档案</el-button>
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

    <el-drawer v-model="profileVisible" title="居民档案详情" size="42%">
      <div v-loading="profileLoading" class="space-y-2 text-sm">
        <template v-if="profileUser">
          <p><strong>用户：</strong>{{ profileUser.username }} ({{ profileUser.id }})</p>
          <p><strong>昵称：</strong>{{ profileUser.nickname }}</p>
          <p><strong>角色：</strong>{{ (profileUser.roleCodes || []).join(',') }}</p>
        </template>
        <template v-if="profile">
          <p><strong>社区：</strong>{{ profile.communityOrgName || profile.communityOrgId }}</p>
          <p><strong>小区：</strong>{{ profile.complexOrgName || profile.complexOrgId }}</p>
          <p><strong>房号：</strong>{{ profile.roomNo || '-' }}</p>
          <p><strong>紧急联系人：</strong>{{ profile.emergencyContact || '-' }}</p>
          <p><strong>紧急电话：</strong>{{ profile.emergencyPhone || '-' }}</p>
          <p><strong>更新时间：</strong>{{ profile.updateTime }}</p>
        </template>
        <p v-else-if="!profileLoading" class="text-white/65">该用户暂无居民档案，可直接点击“编辑档案”创建。</p>
      </div>
    </el-drawer>

    <el-dialog v-model="editVisible" title="编辑居民档案" width="620px" destroy-on-close>
      <el-form label-width="130px">
        <el-form-item label="用户ID">
          <el-input-number v-model="editForm.userId" :min="1" disabled />
        </el-form-item>
        <el-form-item label="小区组织ID">
          <el-select v-model="editForm.complexOrgId" filterable style="width: 100%">
            <el-option v-for="item in orgByType.COMPLEX || []" :key="item.value" :label="item.label" :value="item.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="房号">
          <el-input v-model="editForm.roomNo" />
        </el-form-item>
        <el-form-item label="紧急联系人">
          <el-input v-model="editForm.emergencyContact" />
        </el-form-item>
        <el-form-item label="紧急电话">
          <el-input v-model="editForm.emergencyPhone" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submitProfile">保存</el-button>
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


