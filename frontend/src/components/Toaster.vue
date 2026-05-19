<template>
  <div class="toaster" aria-live="polite">
    <transition-group name="toast">
      <div
        v-for="t in toastState.toasts"
        :key="t.id"
        class="toast"
        :class="'toast-' + t.type"
        @click="dismiss(t.id)"
      >
        <span class="msg">{{ t.message }}</span>
        <button class="close" aria-label="Dismiss">×</button>
      </div>
    </transition-group>
  </div>
</template>

<script>
import { toastState, dismiss } from '../store/toastStore'

export default {
  name: 'AppToaster',
  data() {
    return { toastState }
  },
  methods: { dismiss }
}
</script>

<style scoped>
.toaster {
  position: fixed;
  top: 16px;
  right: 16px;
  z-index: 9999;
  display: flex;
  flex-direction: column;
  gap: 8px;
  pointer-events: none;
  max-width: 360px;
}
.toast {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  padding: 12px 14px;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.12);
  font-size: 0.9em;
  font-weight: 500;
  cursor: pointer;
  pointer-events: auto;
  border-left: 4px solid;
}
.toast-success { background: #eafaf1; color: #196f3d; border-left-color: #27ae60; }
.toast-error   { background: #fdedec; color: #a93226; border-left-color: #c0392b; }
.toast-info    { background: #ebf5fb; color: #1f618d; border-left-color: #3498db; }
.msg { flex: 1; line-height: 1.4; }
.close {
  background: none;
  border: none;
  color: inherit;
  cursor: pointer;
  font-size: 1.2em;
  line-height: 1;
  opacity: 0.6;
  padding: 0 4px;
}
.close:hover { opacity: 1; }
.toast-enter-active, .toast-leave-active { transition: all 0.25s ease; }
.toast-enter-from { opacity: 0; transform: translateX(20px); }
.toast-leave-to   { opacity: 0; transform: translateX(20px); }
</style>
