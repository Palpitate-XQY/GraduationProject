<script setup lang="ts">
interface Props {
  current: number
  size: number
  total: number
}

const props = defineProps<Props>()
const emit = defineEmits<{
  'update:current': [value: number]
  'update:size': [value: number]
}>()

function handleCurrentChange(value: number) {
  emit('update:current', value)
}

function handleSizeChange(value: number) {
  emit('update:size', value)
}
</script>

<template>
  <div class="admin-pager-wrap flex justify-end pt-4">
    <el-pagination
      background
      layout="total, sizes, prev, pager, next"
      :current-page="props.current"
      :page-size="props.size"
      :total="props.total"
      :page-sizes="[10, 20, 50]"
      @current-change="handleCurrentChange"
      @size-change="handleSizeChange"
    />
  </div>
</template>
