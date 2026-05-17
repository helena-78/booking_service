<template>
  <div class="form">
    <h2>Apply Account Sanction</h2>

    <label>Case ID (UUID)</label>
    <input v-model="caseId" placeholder="00000000-0000-0000-0000-000000000000" />

    <label>Target User ID (UUID)</label>
    <input v-model="targetUserId" placeholder="UUID of the user to sanction" />

    <label>Sanction Type</label>
    <select v-model="sanctionType">
      <option value="SUSPENSION">SUSPENSION → user status becomes SUSPENDED</option>
      <option value="BAN">BAN → user status becomes BANNED</option>
    </select>

    <button @click="applySanction" :disabled="loading">
      {{ loading ? 'Applying…' : 'Apply Sanction' }}
    </button>

    <div v-if="error" class="error">
      <p><b>Error:</b> {{ error }}</p>
    </div>

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
export default {
  name: 'ApplySanction',
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
    applySanction() {
      this.loading = true
      this.error = null
      this.result = null

      fetch('http://localhost:8090/api/sanctions', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          caseId: this.caseId,
          targetUserId: this.targetUserId,
          sanctionType: this.sanctionType
        })
      })
        .then(async response => {
          const body = await response.json().catch(() => null)
          if (!response.ok && response.status !== 202) {
            throw new Error((body && body.message) || 'HTTP ' + response.status)
          }
          return body
        })
        .then(data => {
          this.result = data
        })
        .catch(err => {
          this.error = err.message
        })
        .finally(() => {
          this.loading = false
        })
    }
  }
}
</script>

<style scoped>
.form {
  max-width: 560px;
  margin: 0 auto;
  background: #fff;
  padding: 30px;
  border-radius: 10px;
  text-align: left;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
h2 {
  color: #2c3e50;
  text-align: center;
}
.hint {
  background: #f5edf8;
  border-left: 3px solid #8e44ad;
  padding: 10px 12px;
  border-radius: 6px;
  font-size: 0.9em;
  color: #555;
  margin-bottom: 20px;
}
label {
  display: block;
  margin: 18px 0 6px;
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
input:focus, select:focus {
  outline: none;
  border-color: #8e44ad;
}
button {
  background: #8e44ad;
  color: white;
  border: none;
  padding: 12px 24px;
  margin-top: 25px;
  border-radius: 6px;
  font-size: 1em;
  font-weight: bold;
  cursor: pointer;
  width: 100%;
}
button:disabled {
  background: #aaa;
  cursor: not-allowed;
}
.error {
  background: #fadbd8;
  color: #c0392b;
  padding: 10px 15px;
  margin-top: 20px;
  border-radius: 6px;
}
.result {
  padding: 14px 18px;
  margin-top: 20px;
  border-radius: 8px;
  border-left: 4px solid #8e44ad;
}
.result.sync-synced         { background: #d4efdf; color: #196f3d; border-left-color: #27ae60; }
.result.sync-pending_retry  { background: #fff7e6; color: #7d5b00; border-left-color: #f39c12; }
.result.sync-failed         { background: #fadbd8; color: #c0392b; border-left-color: #c0392b; }
.badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 0.8em;
  font-weight: bold;
  color: white;
  margin-left: 6px;
}
.badge-synced         { background: #27ae60; }
.badge-pending_retry  { background: #f39c12; }
.badge-failed         { background: #c0392b; }
.sync-error {
  font-size: 0.9em;
  font-style: italic;
}
.note {
  font-size: 0.9em;
  margin-top: 10px;
  font-style: italic;
}
code {
  background: #fff;
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 0.9em;
}
</style>
