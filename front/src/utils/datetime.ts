/**
 * Datetime helpers for query/body contract alignment.
 */
export function toBackendQueryDateTime(input?: string | null) {
  if (!input) return undefined
  return input.replace('T', ' ').trim()
}

export function toIsoLocalDateTime(input?: string | null) {
  if (!input) return undefined
  return input.includes('T') ? input : input.replace(' ', 'T')
}

export function parseTimestamp(input?: string | null) {
  if (!input) return NaN
  const safe = input.includes('T') ? input : input.replace(' ', 'T')
  return new Date(safe).getTime()
}
