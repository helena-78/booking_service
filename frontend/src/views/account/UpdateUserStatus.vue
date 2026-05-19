<template>
  <div class="form">
    <h2>Update User Status</h2>

    <label>User ID (UUID)</label>
    <input v-model="userId" placeholder="UUID of the user to update" />

    <label>New status</label>
    <select v-model="status">
      <option value="ACTIVE">ACTIVE</option>
      <option value="SUSPENDED">SUSPENDED</option>
      <option value="BANNED">BANNED</option>
    </select>

    <button @click="submit" :disabled="loading || !userId">
      <Spinner v-if="loading" :size="14" inline />
      <span>{{ loading ? 'Updating…' : 'Update status' }}</span>
    </button>

    <div v-if="error" class="error"><p><b>Error:</b> {{ error }}</p></div>

    <div v-if="result" class="success">
      <p>✓ Status updated to <b>{{ result.status }}</b></p>
      <p><small><b>User:</b> {{ result.userId }} ({{ result.name }})</small></p>
    </div>
  </div>
</template>

<script>
import { accountApi } from '../../api/client'
import { toast } from '../../store/toastStore'
import Spinner from '../../components/Spinner.vue'

export default {
  name: 'UpdateUserStatus',
  components: { Spinner },
  data() {
    return { userId: '', status: 'ACTIVE', loading: false, error: null, result: null }
  },
  methods: {
    async submit() {
      this.loading = true; this.error = null; this.result = null
      try {
        this.result = await accountApi.updateUserStatus(this.userId, this.status)
        toast.success(`User status set to ${this.result.status}.`)
      } catch (e) {
        this.error = e.message
        if (!e.silent) toast.error(e.message)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.form {
  max-width: 500px; margin: 0 auto; background: #fff;
  padding: 28px 30px; border-radius: 10px; text-align: left;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
h2 { color: #2c3e50; text-align: center; margin-top: 0; }
label {
  display: block; margin: 14px 0 6px; font-weight: bold;
  font-size: 0.85em; color: #555; text-transform: uppercase; letter-spacing: 0.5px;
}
input, select {
  display: block; width: 100%; padding: 10px; box-sizing: border-box;
  border: 1px solid #d5dde0; border-radius: 6px; font-size: 0.95em;
}
input:focus, select:focus { outline: none; border-color: #3498db; }
button {
  background: #3498db; color: white; border: none;
  padding: 11px 22px; margin-top: 20px; border-radius: 6px;
  font-size: 0.95em; font-weight: bold; cursor: pointer;
  display: inline-flex; align-items: center; justify-content: center; gap: 8px;
  min-width: 160px;
}
button:disabled { background: #aaa; cursor: not-allowed; }
.error { background: #fadbd8; color: #c0392b; padding: 10px 14px; margin-top: 16px; border-radius: 6px; font-size: 0.9em; }
.success { background: #d4efdf; color: #196f3d; padding: 10px 14px; margin-top: 16px; border-radius: 6px; }
</style>
