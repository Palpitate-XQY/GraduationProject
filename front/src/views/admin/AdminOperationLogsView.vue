<script setup lang="ts">
import { onMounted } from 'vue'
import AdminPageShell from '@/components/admin/AdminPageShell.vue'
import AdminTablePager from '@/components/admin/AdminTablePager.vue'
import { useAdminTable } from '@/composables/useAdminTable'
import { operationLogPage } from '@/api/log'
import type { OperationLogVO } from '@/types/log'

const table = useAdminTable<
  OperationLogVO,
  {
    current: number
    size: number
    username: string
    operationModule: string
    operationType: string
    successFlag?: number
    traceId: string
    startTime: string
    endTime: string
  }
>(
  {
    current: 1,
    size: 10,
    username: '',
    operationModule: '',
    operationType: '',
    successFlag: undefined,
    traceId: '',
    startTime: '',
    endTime: '',
  },
  (query) => operationLogPage(query),
)

onMounted(() => table.load())
</script>

<template>
  <AdminPageShell title="操作日志" description="审计系统关键操作与失败堆栈线索。">
    <div class="liquid-glass rounded-3xl p-4 md:p-5 space-y-4">
      <el-form inline>
        <el-form-item>
          <el-input v-model="table.query.username" placeholder="用户名" clearable />
        </el-form-item>
        <el-form-item>
          <el-input v-model="table.query.operationModule" placeholder="模块" clearable />
        </el-form-item>
        <el-form-item>
          <el-input v-model="table.query.operationType" placeholder="动作" clearable />
        </el-form-item>
        <el-form-item>
          <el-select v-model="table.query.successFlag" placeholder="结果" clearable style="width: 140px">
            <el-option label="成功" :value="1" />
            <el-option label="失败" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-input v-model="table.query.traceId" placeholder="TraceId" clearable />
        </el-form-item>
        <el-form-item>
          <el-date-picker v-model="table.query.startTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="开始时间" />
        </el-form-item>
        <el-form-item>
          <el-date-picker v-model="table.query.endTime" type="datetime" value-format="YYYY-MM-DD HH:mm:ss" placeholder="结束时间" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="table.search">查询</el-button>
          <el-button @click="table.resetQuery(); table.search()">重置</el-button>
        </el-form-item>
      </el-form>

      <el-alert v-if="table.error.value" type="error" :closable="false" :title="table.error.value" />

      <el-table :data="table.records.value" v-loading="table.loading.value" row-key="id" border class="admin-table">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" width="120" />
        <el-table-column prop="operationModule" label="模块" width="130" />
        <el-table-column prop="operationType" label="动作" width="120" />
        <el-table-column prop="requestMethod" label="方法" width="90" />
        <el-table-column prop="requestUri" label="请求URI" min-width="200" show-overflow-tooltip />
        <el-table-column prop="successFlag" label="结果" width="90">
          <template #default="scope">
            <el-tag :type="scope.row.successFlag === 1 ? 'success' : 'danger'">
              {{ scope.row.successFlag === 1 ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="errorMessage" label="错误信息" min-width="220" show-overflow-tooltip />
        <el-table-column prop="traceId" label="TraceId" min-width="180" show-overflow-tooltip />
        <el-table-column prop="operationTime" label="操作时间" min-width="170" />
      </el-table>

      <AdminTablePager
        :current="table.query.current"
        :size="table.query.size"
        :total="table.total.value"
        @update:current="table.changePage"
        @update:size="(s) => { table.query.size = s; table.search() }"
      />
    </div>
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


