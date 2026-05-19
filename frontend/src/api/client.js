// Thin fetch wrapper that injects the JWT, normalises errors (including
// Spring's `details` array on validation failures), and on 401 clears the
// session and bounces the user to the login screen with a toast.
//
// Exception: a 401 from /api/auth/login means "wrong credentials", not
// "expired session". The login screen displays its own error, so we skip
// the global toast/redirect for that path.

import { authState, clearAuth } from '../store/authStore'
import { toast } from '../store/toastStore'
import router from '../router'

const ACCOUNT_BASE = 'http://localhost:8081'
const MODERATION_BASE = 'http://localhost:8090'

async function request(baseUrl, path, options = {}) {
  const headers = { 'Content-Type': 'application/json', ...(options.headers || {}) }
  if (authState.token) {
    headers.Authorization = `Bearer ${authState.token}`
  }

  let response
  try {
    response = await fetch(baseUrl + path, { ...options, headers })
  } catch (networkErr) {
    throw new Error('Network error: ' + networkErr.message)
  }

  const isLoginAttempt = path.startsWith('/api/auth/login')

  if (response.status === 401 && !isLoginAttempt) {
    clearAuth()
    toast.error('Session expired. Please sign in again.')
    if (router.currentRoute.value.path !== '/account/login') {
      router.push('/account/login')
    }
    const err = new Error('Session expired. Please sign in again.')
    err.silent = true
    throw err
  }

  const text = await response.text()
  const body = text ? safeJson(text) : null

  if (!response.ok && response.status !== 202) {
    throw new Error(extractMessage(body, response.status))
  }
  return body
}

// Spring's error responses look like:
//   { status, error, message, details: ["field: must not be blank", ...] }
// We surface the most specific information we have.
function extractMessage(body, status) {
  if (!body) {
    if (status === 401) return 'Invalid email or password.'
    if (status === 403) return 'You do not have permission to perform this action.'
    return `HTTP ${status}`
  }
  if (Array.isArray(body.details) && body.details.length) {
    return body.details.join(' • ')
  }
  return body.message || body.error || `HTTP ${status}`
}

function safeJson(text) {
  try {
    return JSON.parse(text)
  } catch (e) {
    return null
  }
}

export const accountApi = {
  login: (email, password) => request(ACCOUNT_BASE, '/api/auth/login', {
    method: 'POST',
    body: JSON.stringify({ email, password })
  }),
  registerUser: (body) => request(ACCOUNT_BASE, '/api/users', {
    method: 'POST',
    body: JSON.stringify(body)
  }),
  registerFacility: (body) => request(ACCOUNT_BASE, '/api/sport-centers', {
    method: 'POST',
    body: JSON.stringify(body)
  }),
  getUser: (id) => request(ACCOUNT_BASE, `/api/users/${id}`),
  updateUser: (id, body) => request(ACCOUNT_BASE, `/api/users/${id}`, {
    method: 'PUT',
    body: JSON.stringify(body)
  }),
  updateUserStatus: (id, status) => request(ACCOUNT_BASE, `/api/users/${id}/status`, {
    method: 'PUT',
    body: JSON.stringify({ status })
  }),
  getFacility: (id) => request(ACCOUNT_BASE, `/api/sport-centers/${id}`),
  updateFacility: (id, body) => request(ACCOUNT_BASE, `/api/sport-centers/${id}`, {
    method: 'PUT',
    body: JSON.stringify(body)
  })
}

export const moderationApi = {
  listCases: (params = {}) => {
    const query = new URLSearchParams()
    if (params.contentType) query.append('contentType', params.contentType)
    if (params.verdict) query.append('verdict', params.verdict)
    query.append('size', params.size || '20')
    query.append('sort', 'createdAt,desc')
    return request(MODERATION_BASE, '/api/cases?' + query.toString())
  },
  getCase: (id) => request(MODERATION_BASE, `/api/cases/${id}`),
  createCase: (body) => request(MODERATION_BASE, '/api/cases', {
    method: 'POST',
    body: JSON.stringify(body)
  }),
  submitVerdict: (id, body) => request(MODERATION_BASE, `/api/cases/${id}/verdict`, {
    method: 'PUT',
    body: JSON.stringify(body)
  }),
  applySanction: (body) => request(MODERATION_BASE, '/api/sanctions', {
    method: 'POST',
    body: JSON.stringify(body)
  }),
  getSanctionHistory: (userId) => request(MODERATION_BASE, `/api/sanctions/${userId}`),
  removeContent: (contentType, contentId) => request(MODERATION_BASE, `/api/content/${contentType}/${contentId}`, {
    method: 'DELETE'
  })
}
