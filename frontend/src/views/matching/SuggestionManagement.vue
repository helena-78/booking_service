<template>
  <div class="suggestion-management">
    <h2>Suggestion Management</h2>
    <p class="description">
      Manually invalidate a stale suggestion cache entry by its ID.
      Works for both user and activity suggestions.
    </p>

    <form @submit.prevent="invalidate" class="search-form">
      <label for="suggestionId">Suggestion ID</label>
      <input
        id="suggestionId"
        v-model="suggestionId"
        placeholder="Suggestion UUID"
        type="text"
      />
      <button type="submit" class="delete-button" :disabled="loading || !suggestionId.trim()">
        Invalidate
      </button>
    </form>

    <div v-if="loading" class="status">Processing…</div>

    <div v-else-if="error" class="status error">{{ error }}</div>

    <div v-else-if="successMessage" class="status success">
      {{ successMessage }}
    </div>

    <div v-if="history.length > 0" class="history">
      <h3>Recent Invalidations</h3>
      <div v-for="entry in history" :key="entry.id" class="history-row">
        <span class="history-id">{{ entry.suggestionId }}</span>
        <span class="history-time">{{ entry.time }}</span>
        <span class="history-badge" :class="entry.ok ? 'badge-ok' : 'badge-fail'">
          {{ entry.ok ? 'Deleted' : 'Failed' }}
        </span>
      </div>
    </div>
  </div>
</template>

<script>
const BASE = 'http://localhost:8087/api/matching'

export default {
  name: 'SuggestionManagement',
  data() {
    return {
      suggestionId: '',
      loading: false,
      error: null,
      successMessage: null,
      history: []
    }
  },
  methods: {
    invalidate() {
      const id = this.suggestionId.trim()
      if (!id) return

      this.loading = true
      this.error = null
      this.successMessage = null

      fetch(`${BASE}/suggestions/${id}`, { method: 'DELETE' })
        .then(response => {
          if (!response.ok) {
            return response.json().then(data => {
              throw new Error(data.message || 'HTTP ' + response.status)
            })
          }
          this.successMessage = `Suggestion ${id} successfully invalidated.`
          this.history.unshift({
            id: Date.now(),
            suggestionId: id,
            time: new Date().toLocaleTimeString(),
            ok: true
          })
          this.suggestionId = ''
        })
        .catch(err => {
          this.error = err.message
          this.history.unshift({
            id: Date.now(),
            suggestionId: id,
            time: new Date().toLocaleTimeString(),
            ok: false
          })
          console.error(err)
        })
        .finally(() => {
          this.loading = false
        })
    }
  }
}
</script>

<style scoped>
.suggestion-management {
  max-width: 700px;
  margin: 0 auto;
  text-align: left;
}

.description {
  color: #666;
  margin-bottom: 20px;
  font-size: 0.95em;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  margin-bottom: 20px;
}

.search-form label {
  font-weight: bold;
}

.search-form input {
  flex: 1;
  min-width: 240px;
  padding: 8px 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
}

.delete-button {
  background: #e74c3c;
  color: white;
  border: none;
  padding: 10px 18px;
  border-radius: 6px;
  cursor: pointer;
}

.delete-button:hover:not(:disabled) {
  background: #c0392b;
}

.delete-button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.status {
  padding: 16px 20px;
  border-radius: 10px;
  margin-bottom: 20px;
  color: #333;
  background: #f9f9f9;
}

.status.error {
  background: #fdecea;
  color: #c0392b;
}

.status.success {
  background: #eafaf1;
  color: #1e8449;
}

.history {
  margin-top: 8px;
}

.history h3 {
  font-size: 0.95em;
  color: #555;
  margin-bottom: 10px;
}

.history-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 14px;
  border: 1px solid #eee;
  border-radius: 8px;
  margin-bottom: 8px;
  background: #fff;
  font-size: 0.9em;
}

.history-id {
  flex: 1;
  word-break: break-all;
  color: #555;
  font-family: monospace;
}

.history-time {
  color: #999;
  white-space: nowrap;
}

.history-badge {
  padding: 3px 10px;
  border-radius: 12px;
  font-weight: bold;
  font-size: 0.85em;
  white-space: nowrap;
}

.badge-ok   { background: #eafaf1; color: #1e8449; }
.badge-fail { background: #fdecea; color: #c0392b; }
</style>