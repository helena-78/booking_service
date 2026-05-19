// Tiny global toast queue. Components push messages, the Toaster renders them.
// Three intents: 'success' (green), 'error' (red), 'info' (blue).

import { reactive } from 'vue'

let nextId = 1

export const toastState = reactive({
  toasts: []
})

function push(type, message, timeoutMs) {
  const id = nextId++
  toastState.toasts.push({ id, type, message })
  if (timeoutMs > 0) {
    setTimeout(() => dismiss(id), timeoutMs)
  }
  return id
}

export function dismiss(id) {
  const idx = toastState.toasts.findIndex(t => t.id === id)
  if (idx >= 0) toastState.toasts.splice(idx, 1)
}

export const toast = {
  success: (msg, ms = 4000) => push('success', msg, ms),
  error: (msg, ms = 6000) => push('error', msg, ms),
  info: (msg, ms = 4000) => push('info', msg, ms)
}
