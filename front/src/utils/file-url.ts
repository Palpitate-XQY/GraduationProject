/**
 * Build file URL that can carry access token for native <img>/<a> requests.
 * Only appends token for same-origin /api/files links to avoid leaking token to third-party domains.
 */
export function withFileAccessToken(rawUrl?: string | null): string {
  if (!rawUrl) return ''

  const token = localStorage.getItem('access_token')
  if (!token) return rawUrl

  try {
    const normalized = new URL(rawUrl, window.location.origin)
    const sameOrigin = normalized.origin === window.location.origin
    const isFileApiPath = normalized.pathname.startsWith('/api/files/')
    if (!sameOrigin || !isFileApiPath) {
      return rawUrl
    }

    normalized.searchParams.set('access_token', token)
    return `${normalized.pathname}${normalized.search}${normalized.hash}`
  } catch {
    return rawUrl
  }
}

/**
 * Build file preview URL from fileId and append token when needed.
 */
export function buildFilePreviewUrl(fileId?: number | null): string {
  if (!fileId) return ''
  return withFileAccessToken(`/api/files/${fileId}/preview`)
}

/**
 * Build file download URL from fileId and append token when needed.
 */
export function buildFileDownloadUrl(fileId?: number | null): string {
  if (!fileId) return ''
  return withFileAccessToken(`/api/files/${fileId}/download`)
}
