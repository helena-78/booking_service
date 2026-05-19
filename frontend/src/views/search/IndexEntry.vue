<template>
  <div class="index-entry">
    <h2>Search Index Maintenance</h2>
    <p class="subtitle">
      <code>POST /api/search/index</code>,
      <code>GET /api/search/index/{entityType}/{entityId}</code>,
      <code>DELETE /api/search/index/{entityType}/{entityId}</code>
    </p>

    <!-- =========================================================== -->
    <!-- LOOKUP / DELETE  (GET + DELETE)                              -->
    <!-- =========================================================== -->
    <section class="card">
      <h3>Look up / delete a single entry</h3>
      <form class="filters" @submit.prevent="lookup">
        <div class="row">
          <label>
            Entity type
            <select v-model="lookupForm.entityType">
              <option value="USER">USER</option>
              <option value="ACTIVITY">ACTIVITY</option>
              <option value="FACILITY">FACILITY</option>
            </select>
          </label>
          <label>
            Entity ID
            <input v-model="lookupForm.entityId" type="text" placeholder="e.g. sc-100" />
          </label>
        </div>
        <div class="actions">
          <button type="submit" :disabled="lookupLoading || !lookupForm.entityId">
            {{ lookupLoading ? 'Fetching…' : 'GET entry' }}
          </button>
          <button type="button" class="danger" :disabled="lookupLoading || !lookupForm.entityId" @click="remove">
            DELETE entry
          </button>
        </div>
      </form>

      <div v-if="lookupUrl" class="last-url">
        <small><b>{{ lookupMethod }}</b> <code>{{ lookupUrl }}</code></small>
      </div>

      <div v-if="lookupError" class="error inline">
        <p><b>Error:</b> {{ lookupError }}</p>
      </div>

      <div v-if="lookupResult" class="result">
        <h4>Entry</h4>
        <pre>{{ JSON.stringify(lookupResult, null, 2) }}</pre>
      </div>

      <div v-if="deleteMessage" class="success">
        ✓ {{ deleteMessage }}
      </div>
    </section>

    <!-- =========================================================== -->
    <!-- UPSERT  (POST)                                               -->
    <!-- =========================================================== -->
    <section class="card">
      <h3>Index / re-index an entry (POST)</h3>
      <form class="filters" @submit.prevent="upsert">
        <div class="row">
          <label>
            Entity type
            <select v-model="upsertForm.entityType">
              <option value="USER">USER</option>
              <option value="ACTIVITY">ACTIVITY</option>
              <option value="FACILITY">FACILITY</option>
            </select>
          </label>
          <label>
            Entity ID
            <input v-model="upsertForm.entityId" type="text" placeholder="e.g. sc-300" />
          </label>
          <label>
            Display name
            <input v-model="upsertForm.displayName" type="text" placeholder="e.g. New Volleyball Hall" />
          </label>
        </div>
        <div class="row">
          <label>
            Keywords (comma-separated)
            <input v-model="upsertForm.keywordsRaw" type="text" placeholder="e.g. volleyball, tartu, indoor" />
          </label>
        </div>
        <h4>Filters</h4>
        <div class="row">
          <label>
            sportType
            <input v-model="upsertForm.filters.sportType" type="text" placeholder="e.g. VOLLEYBALL" />
          </label>
          <label>
            location
            <input v-model="upsertForm.filters.location" type="text" placeholder="e.g. Tartu" />
          </label>
          <label>
            skillLevel
            <input v-model="upsertForm.filters.skillLevel" type="text" placeholder="e.g. INTERMEDIATE" />
          </label>
          <label>
            facilityName
            <input v-model="upsertForm.filters.facilityName" type="text" placeholder="e.g. New Hall" />
          </label>
        </div>
        <div class="row">
          <label>
            activityTitle
            <input v-model="upsertForm.filters.activityTitle" type="text" />
          </label>
          <label>
            activityStatus
            <input v-model="upsertForm.filters.activityStatus" type="text" placeholder="e.g. OPEN" />
          </label>
          <label>
            activityMaxParticipants
            <input v-model.number="upsertForm.filters.activityMaxParticipants" type="number" min="0" />
          </label>
          <label>
            activityTimeSlots
            <input v-model="upsertForm.filters.activityTimeSlots" type="text" />
          </label>
        </div>
        <div class="row">
          <label>
            userName
            <input v-model="upsertForm.filters.userName" type="text" />
          </label>
          <label>
            userRatingScore
            <input v-model.number="upsertForm.filters.userRatingScore" type="number" step="0.1" min="0" max="5" />
          </label>
          <label>
            userSportPreferences
            <input v-model="upsertForm.filters.userSportPreferences" type="text" />
          </label>
        </div>
        <div class="actions">
          <button type="submit" :disabled="upsertLoading || !upsertForm.entityId">
            {{ upsertLoading ? 'Submitting…' : 'POST index entry' }}
          </button>
          <button type="button" class="secondary" @click="clearUpsert">Clear</button>
        </div>
      </form>

      <div v-if="upsertUrl" class="last-url">
        <small><b>POST</b> <code>{{ upsertUrl }}</code></small>
      </div>

      <div v-if="upsertError" class="error inline">
        <p><b>Error:</b> {{ upsertError }}</p>
      </div>

      <div v-if="upsertResult" class="result">
        <h4>Created / updated entry</h4>
        <pre>{{ JSON.stringify(upsertResult, null, 2) }}</pre>
      </div>
    </section>
  </div>
</template>

<script>
const SEARCH_BASE = 'http://localhost:8086'

function emptyUpsertForm() {
  return {
    entityType: 'FACILITY',
    entityId: '',
    displayName: '',
    keywordsRaw: '',
    filters: {
      sportType: '',
      location: '',
      skillLevel: '',
      facilityName: '',
      activityTitle: '',
      activityStatus: '',
      activityMaxParticipants: null,
      activityTimeSlots: '',
      userName: '',
      userRatingScore: null,
      userSportPreferences: ''
    }
  }
}

export default {
  name: 'IndexEntry',
  data() {
    return {
      lookupForm: { entityType: 'FACILITY', entityId: '' },
      lookupResult: null,
      lookupError: null,
      lookupUrl: null,
      lookupMethod: 'GET',
      lookupLoading: false,
      deleteMessage: null,

      upsertForm: emptyUpsertForm(),
      upsertResult: null,
      upsertError: null,
      upsertUrl: null,
      upsertLoading: false
    }
  },
  methods: {
    lookup() {
      this.lookupResult = null
      this.lookupError = null
      this.deleteMessage = null
      this.lookupMethod = 'GET'
      this.lookupLoading = true

      const url = SEARCH_BASE + '/api/search/index/'
        + encodeURIComponent(this.lookupForm.entityType) + '/'
        + encodeURIComponent(this.lookupForm.entityId.trim())
      this.lookupUrl = url

      fetch(url)
        .then(async response => {
          if (!response.ok) {
            let msg
            try {
              const body = await response.json()
              msg = body.message || body.error || ('HTTP ' + response.status)
            } catch (e) {
              msg = 'HTTP ' + response.status + ' ' + response.statusText
            }
            throw new Error(msg)
          }
          return response.json()
        })
        .then(data => { this.lookupResult = data })
        .catch(err => { this.lookupError = err.message })
        .finally(() => { this.lookupLoading = false })
    },
    remove() {
      if (!confirm('Delete index entry '
          + this.lookupForm.entityType + '/' + this.lookupForm.entityId + '?')) return
      this.lookupResult = null
      this.lookupError = null
      this.deleteMessage = null
      this.lookupMethod = 'DELETE'
      this.lookupLoading = true

      const url = SEARCH_BASE + '/api/search/index/'
        + encodeURIComponent(this.lookupForm.entityType) + '/'
        + encodeURIComponent(this.lookupForm.entityId.trim())
      this.lookupUrl = url

      fetch(url, { method: 'DELETE' })
        .then(async response => {
          if (!response.ok) {
            let msg
            try {
              const body = await response.json()
              msg = body.message || body.error || ('HTTP ' + response.status)
            } catch (e) {
              msg = 'HTTP ' + response.status + ' ' + response.statusText
            }
            throw new Error(msg)
          }
          this.deleteMessage = 'Deleted '
            + this.lookupForm.entityType + '/' + this.lookupForm.entityId
        })
        .catch(err => { this.lookupError = err.message })
        .finally(() => { this.lookupLoading = false })
    },
    upsert() {
      this.upsertResult = null
      this.upsertError = null
      this.upsertLoading = true

      const url = SEARCH_BASE + '/api/search/index'
      this.upsertUrl = url

      // Strip empty filter fields so the upstream service gets a tidy payload.
      const rawFilters = this.upsertForm.filters
      const filters = {}
      Object.keys(rawFilters).forEach(k => {
        const v = rawFilters[k]
        if (v !== null && v !== undefined && String(v).trim() !== '') {
          filters[k] = v
        }
      })

      const keywords = (this.upsertForm.keywordsRaw || '')
        .split(',')
        .map(s => s.trim())
        .filter(s => s.length > 0)

      const payload = {
        entityType: this.upsertForm.entityType,
        entityId: this.upsertForm.entityId.trim(),
        displayName: this.upsertForm.displayName || null,
        keywords,
        filters
      }

      fetch(url, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      })
        .then(async response => {
          if (!response.ok) {
            let msg
            try {
              const body = await response.json()
              msg = body.message || body.error || ('HTTP ' + response.status)
            } catch (e) {
              msg = 'HTTP ' + response.status + ' ' + response.statusText
            }
            throw new Error(msg)
          }
          return response.json()
        })
        .then(data => { this.upsertResult = data })
        .catch(err => { this.upsertError = err.message })
        .finally(() => { this.upsertLoading = false })
    },
    clearUpsert() {
      this.upsertForm = emptyUpsertForm()
      this.upsertResult = null
      this.upsertError = null
      this.upsertUrl = null
    }
  }
}
</script>

<style scoped>
.index-entry { text-align: left; }
.subtitle {
  color: #555;
  font-size: 0.9em;
  margin-top: 0;
}
.subtitle code {
  background: #eaf3ff;
  padding: 1px 6px;
  border-radius: 3px;
  font-family: monospace;
  margin: 0 4px 4px 0;
  display: inline-block;
}
.card {
  background: #fbfbfb;
  border: 1px solid #e1e1e1;
  border-radius: 12px;
  padding: 16px 20px;
  margin-bottom: 18px;
}
.card h3 { margin-top: 0; color: #2c3e50; }
.card h4 {
  margin: 16px 0 8px;
  color: #555;
  font-size: 1em;
}
.filters {
  background: transparent;
  padding: 0;
}
.row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}
.row label {
  display: flex;
  flex-direction: column;
  font-size: 0.9em;
  color: #2c3e50;
}
.row input, .row select {
  margin-top: 4px;
  padding: 6px 8px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 0.95em;
}
.actions { display: flex; gap: 8px; }
button {
  background: #42b983;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.95em;
}
button:disabled { opacity: 0.5; cursor: not-allowed; }
button.secondary {
  background: white;
  color: #2c3e50;
  border: 1px solid #ccc;
}
button.danger {
  background: #c0392b;
}
.last-url {
  background: #eaf3ff;
  border-left: 4px solid #3498db;
  padding: 6px 12px;
  border-radius: 0 6px 6px 0;
  margin: 12px 0 10px;
  color: #2c3e50;
  word-break: break-all;
}
.last-url code { font-family: monospace; font-size: 0.85em; }
.result {
  background: #f4fbf7;
  border-left: 4px solid #42b983;
  border-radius: 0 6px 6px 0;
  padding: 10px 14px;
  margin-top: 12px;
}
.result h4 {
  margin: 0 0 6px;
  color: #1f5132;
  font-size: 0.95em;
}
.result pre {
  margin: 0;
  font-family: monospace;
  font-size: 0.85em;
  overflow-x: auto;
  background: white;
  padding: 10px;
  border-radius: 6px;
  border: 1px solid #d6ebde;
}
.success {
  background: #f4fbf7;
  border-left: 4px solid #42b983;
  border-radius: 0 6px 6px 0;
  padding: 10px 14px;
  color: #1f5132;
  margin-top: 12px;
}
.error.inline {
  background: #fdeceb;
  border-left: 4px solid #c0392b;
  border-radius: 0 6px 6px 0;
  padding: 10px 14px;
  color: #722f2a;
  margin-top: 12px;
}
.error.inline p { margin: 0; }
</style>
