<template>
  <div class="form">
    <h2>Sanction History</h2>

    <label>User ID (UUID)</label>
    <input v-model="userId" placeholder="UUID of the user to inspect" />
    <button @click="fetch" :disabled="loading || !userId" class="secondary">
      <Spinner v-if="loading" :size="14" inline />
      <span>Load history</span>
    </button>

    <div v-if="error" class="error"><p>{{ error }}</p></div>

    <Spinner v-if="loading && !sanctions" label="Loading…" />

    <div v-if="sanctions && sanctions.length === 0" class="empty">
      <p>No sanctions found for this user.</p>
    </div>

    <div v-else-if="sanctions" class="list">
      <p class="count">{{ sanctions.length }} sanction(s)</p>
      <div class="item" v-for="s in sanctions" :key="s.sanctionId">
        <div class="header-row">
          <span class="badge sanction-type">{{ s.sanctionType }}</span>
          <span class="badge" :class="'sync-' + s.accountSyncStatus.toLowerCase()">{{ s.accountSyncStatus }}</span>
        </div>
        <p class="ids">
          <small>
            <b>Sanction ID:</b> {{ s.sanctionId }}<br />
            <b>Case:</b> {{ s.caseId }}<br />
            <b>Sync attempts:</b> {{ s.syncAttempts }}<br />
            <b>Created:</b> {{ s.createdAt }}
            <span v-if="s.lastSyncError"><br /><b>Last sync error:</b> {{ s.lastSyncError }}</span>
          </small>
        </p>
      </div>
    </div>
  </div>
</template>

<script>
import { moderationApi } from '../../api/client'
import { toast } from '../../store/toastStore'
import Spinner from '../../components/Spinner.vue'

export default {
  name: 'SanctionHistory',
  components: { Spinner },
  data() {
    return { userId: '', sanctions: null, loading: false, error: null }
  },
  methods: {
    async fetch() {
      this.loading = true; this.error = null
      try {
        this.sanctions = await moderationApi.getSanctionHistory(this.userId)
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
  max-width: 600px; margin: 0 auto; background: #fff;
  padding: 28px 30px; border-radius: 10px; text-align: left;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
h2 { color: #2c3e50; text-align: center; margin-top: 0; }
label {
  display: block; margin: 14px 0 6px; font-weight: bold;
  font-size: 0.85em; color: #555; text-transform: uppercase; letter-spacing: 0.5px;
}
input {
  display: block; width: 100%; padding: 10px; box-sizing: border-box;
  border: 1px solid #d5dde0; border-radius: 6px; font-size: 0.95em;
}
input:focus { outline: none; border-color: #8e44ad; }
button {
  background: #8e44ad; color: white; border: none;
  padding: 11px 22px; margin-top: 14px; border-radius: 6px;
  font-size: 0.95em; font-weight: bold; cursor: pointer;
  display: inline-flex; align-items: center; justify-content: center; gap: 8px;
  min-width: 160px;
}
button.secondary { background: #ecf0f1; color: #2c3e50; }
button:disabled { background: #aaa; cursor: not-allowed; }
.list { margin-top: 20px; }
.count { color: #555; text-align: center; }
.item {
  background: #f5edf8; padding: 12px 18px; margin-bottom: 10px;
  border-radius: 8px; border-left: 4px solid #8e44ad;
}
.header-row { display: flex; gap: 12px; align-items: center; margin-bottom: 6px; }
.badge {
  display: inline-block; padding: 3px 10px; border-radius: 12px;
  font-size: 0.8em; font-weight: bold; color: white;
}
.sanction-type        { background: #8e44ad; }
.sync-synced          { background: #27ae60; }
.sync-pending_retry   { background: #f39c12; }
.sync-failed          { background: #c0392b; }
.ids { color: #777; margin: 0; }
.empty { text-align: center; padding: 20px; color: #777; }
.error { background: #fadbd8; color: #c0392b; padding: 10px 14px; margin-top: 14px; border-radius: 6px; font-size: 0.9em; }
</style>
