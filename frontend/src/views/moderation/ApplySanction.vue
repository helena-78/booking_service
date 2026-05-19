<template>
  <div class="form">
    <h2>Apply Account Sanction</h2>

    <label>Case ID (UUID)</label>
    <input v-model="caseId" placeholder="The moderation case this sanction belongs to" />

    <label>Target User ID (UUID)</label>
    <input v-model="targetUserId" placeholder="UUID of the user being sanctioned" />

    <label>Sanction type</label>
    <select v-model="sanctionType">
      <option value="SUSPENSION">SUSPENSION</option>
      <option value="BAN">BAN</option>
    </select>

    <button @click="applySanction" :disabled="loading || !caseId || !targetUserId">
      <Spinner v-if="loading" :size="14" inline />
      <span>{{ loading ? 'Applying…' : 'Apply Sanction' }}</span>
    </button>

    <div v-if="error" class="error"><p><b>Error:</b> {{ error }}</p></div>

    <div
      v-if="result"
      class="result"
      :class="'sync-' + result.accountSyncStatus.toLowerCase()"
    >
      <p>✓ Sanction recorded</p>
      <p><small>
        <b>Sanction ID:</b> {{ result.sanctionId }}<br />
        <b>Sanction type:</b> {{ result.sanctionType }}<br />
        <b>Sync attempts:</b> {{ result.syncAttempts }}
      </small></p>
      <p>
        <b>Account sync status:</b>
        <span class="badge" :class="'badge-' + result.accountSyncStatus.toLowerCase()">
          {{ result.accountSyncStatus }}
        </span>
      </p>
      <p v-if="result.lastSyncError" class="sync-error">
        <b>Last sync error:</b> {{ result.lastSyncError }}
      </p>
    </div>
  </div>
</template>

<script>
import { moderationApi } from '../../api/client'
import { toast } from '../../store/toastStore'
import Spinner from '../../components/Spinner.vue'

export default {
  name: 'ApplySanction',
  components: { Spinner },
  data() {
    return {
      caseId: '',
      targetUserId: '',
      sanctionType: 'SUSPENSION',
      loading: false,
      error: null,
      result: null
    }
  },
  methods: {
    async applySanction() {
      this.loading = true
      this.error = null
      this.result = null
      try {
        const body = await moderationApi.applySanction({
          caseId: this.caseId,
          targetUserId: this.targetUserId,
          sanctionType: this.sanctionType
        })
        this.result = body
        toast.success(`Sanction applied (sync: ${body.accountSyncStatus}).`)
      } catch (err) {
        this.error = err.message
        if (!err.silent) toast.error(err.message)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.form {
  max-width: 560px;
  margin: 0 auto;
  background: #fff;
  padding: 28px 30px;
  border-radius: 10px;
  text-align: left;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
h2 { color: #2c3e50; text-align: center; margin-top: 0; }
label {
  display: block;
  margin: 16px 0 6px;
  font-weight: bold;
  font-size: 0.85em;
  color: #555;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
input, select {
  display: block;
  width: 100%;
  padding: 10px;
  box-sizing: border-box;
  border: 1px solid #d5dde0;
  border-radius: 6px;
  font-size: 0.95em;
}
input:focus, select:focus { outline: none; border-color: #8e44ad; }

button {
  background: #8e44ad;
  color: white;
  border: none;
  padding: 11px 22px;
  margin-top: 22px;
  border-radius: 6px;
  font-size: 0.95em;
  font-weight: bold;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-width: 180px;
}
button:disabled { background: #aaa; cursor: not-allowed; }
.error {
  background: #fadbd8;
  color: #c0392b;
  padding: 10px 14px;
  margin-top: 14px;
  border-radius: 6px;
  font-size: 0.9em;
}
.result {
  padding: 14px 18px;
  margin-top: 18px;
  border-radius: 8px;
  border-left: 4px solid #8e44ad;
}
.result.sync-synced        { background: #d4efdf; color: #196f3d; border-left-color: #27ae60; }
.result.sync-pending_retry { background: #fff7e6; color: #7d5b00; border-left-color: #f39c12; }
.result.sync-failed        { background: #fadbd8; color: #c0392b; border-left-color: #c0392b; }
.badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 0.8em;
  font-weight: bold;
  color: white;
  margin-left: 6px;
}
.badge-synced        { background: #27ae60; }
.badge-pending_retry { background: #f39c12; }
.badge-failed        { background: #c0392b; }
.sync-error { font-size: 0.9em; font-style: italic; }
</style>
