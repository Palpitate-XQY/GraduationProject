import { computed, reactive, ref } from 'vue'
import type { PageQuery, PageResult } from '@/types/common'

type Fetcher<T, Q extends PageQuery> = (query: Q) => Promise<{ data: PageResult<T> }>

const MIN_PAGE_SIZE = 1
const MAX_PAGE_SIZE = 100

function ensureArray<T>(value: unknown): T[] {
  return Array.isArray(value) ? (value as T[]) : []
}

function normalizePageQuery<Q extends PageQuery>(query: Q) {
  const current = Number(query.current)
  const size = Number(query.size)
  query.current = Number.isFinite(current) && current > 0 ? Math.floor(current) : 1
  if (!Number.isFinite(size)) {
    query.size = 10
    return
  }
  query.size = Math.min(MAX_PAGE_SIZE, Math.max(MIN_PAGE_SIZE, Math.floor(size)))
}

/**
 * Shared table state for admin list pages.
 */
export function useAdminTable<T, Q extends PageQuery>(initialQuery: Q, fetcher: Fetcher<T, Q>) {
  const query = reactive({ ...initialQuery }) as Q
  const records = ref<T[]>([])
  const total = ref(0)
  const loading = ref(false)
  const error = ref('')

  const totalPages = computed(() => Math.max(1, Math.ceil(total.value / (query.size || 10))))

  async function load() {
    normalizePageQuery(query)
    loading.value = true
    error.value = ''
    try {
      const res = await fetcher(query)
      records.value = ensureArray<T>(res.data?.records)
      total.value = Number(res.data?.total || 0)
    } catch (e) {
      error.value = e instanceof Error ? e.message : '加载失败'
    } finally {
      loading.value = false
    }
  }

  async function search() {
    query.current = 1
    await load()
  }

  async function changePage(page: number) {
    query.current = page
    await load()
  }

  function resetQuery() {
    Object.assign(query, { ...initialQuery })
  }

  return {
    query,
    records,
    total,
    totalPages,
    loading,
    error,
    load,
    search,
    changePage,
    resetQuery,
  }
}