import { computed, ref } from 'vue'
import { orgTree } from '@/api/org'
import type { OrgTreeVO } from '@/types/org'

export interface OrgOption {
  value: number
  label: string
  orgType: string
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

/**
 * Organization option list for form selectors.
 */
export function useOrgOptions() {
  const loading = ref(false)
  const options = ref<OrgOption[]>([])

  const byType = computed(() => {
    const map: Record<string, OrgOption[]> = {}
    options.value.forEach((item) => {
      if (!map[item.orgType]) {
        map[item.orgType] = []
      }
      map[item.orgType].push(item)
    })
    return map
  })

  async function loadOrgOptions() {
    loading.value = true
    try {
      const res = await orgTree({})
      const flattened: OrgOption[] = []
      flattenOrgTree(Array.isArray(res.data) ? res.data : [], flattened)
      options.value = flattened
    } catch {
      options.value = []
    } finally {
      loading.value = false
    }
  }

  return {
    loading,
    options,
    byType,
    loadOrgOptions,
  }
}
