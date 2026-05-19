// Reactive auth state backed by localStorage so the session survives reloads.
// Email is stored client-side at login time (not in the JWT) just so the UI
// can show "Logged in as you@example.com" without an extra API call.

import { reactive } from 'vue'

const STORAGE_KEY = 'sportlink.auth'

function load() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch (e) {
    return null
  }
}

const initial = load()

export const authState = reactive({
  accountId: initial ? initial.accountId : null,
  accountType: initial ? initial.accountType : null,
  role: initial ? initial.role : null,
  email: initial ? initial.email : null,
  token: initial ? initial.token : null
})

function persist() {
  if (authState.token) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify({
      accountId: authState.accountId,
      accountType: authState.accountType,
      role: authState.role,
      email: authState.email,
      token: authState.token
    }))
  } else {
    localStorage.removeItem(STORAGE_KEY)
  }
}

export function setAuth(payload) {
  authState.accountId = payload.accountId
  authState.accountType = payload.accountType
  authState.role = payload.role
  authState.email = payload.email || null
  authState.token = payload.token
  persist()
}

export function clearAuth() {
  authState.accountId = null
  authState.accountType = null
  authState.role = null
  authState.email = null
  authState.token = null
  persist()
}

export function isAuthenticated() {
  return !!authState.token
}

export function hasRole(role) {
  return authState.role === role
}
