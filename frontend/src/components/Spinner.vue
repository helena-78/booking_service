<template>
  <div class="spinner-wrap" :class="{ inline }">
    <span class="spinner" :style="{ width: size + 'px', height: size + 'px' }"></span>
    <span v-if="label" class="label">{{ label }}</span>
  </div>
</template>

<script>
export default {
  name: 'AppSpinner',
  props: {
    size: { type: Number, default: 18 },
    label: { type: String, default: '' },
    inline: { type: Boolean, default: false }
  }
}
</script>

<style scoped>
.spinner-wrap {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 20px;
  color: #555;
}
.spinner-wrap.inline { padding: 0; display: inline-flex; }
.spinner {
  display: inline-block;
  border: 2px solid #d5dde0;
  border-top-color: #3498db;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}
.label { font-size: 0.9em; }
@keyframes spin {
  to { transform: rotate(360deg); }
}
</style>
