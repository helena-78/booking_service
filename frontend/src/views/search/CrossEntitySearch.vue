<template>
  <div class="cross-search">
    <h2>Cross-Entity Search</h2>
    <p class="subtitle">
      GET <code>/api/search</code> &middot; searches across users, activities, and facilities in one call.
    </p>

    <form class="filters" @submit.prevent="runSearch">
      <div class="row">
        <label>
          Free-text (q)
          <input v-model="filters.q" type="text" placeholder="e.g. basketball" />
          <small class="hint">matches displayName + keywords</small>
        </label>
        <label>
          Entity type
          <select v-model="filters.entityType">
            <option value="">(any)</option>
            <option value="USER">USER</option>
            <option value="ACTIVITY">ACTIVITY</option>
            <option value="FACILITY">FACILITY</option>
          </select>
        </label>
        <label>
          Sport type
          <input v-model="filters.sportType" type="text" placeholder="e.g. BASKETBALL" />
        </label>
        <label>
          Location
          <input v-model="filters.location" type="text" placeholder="e.g. Tartu" />
        </label>
      </div>
      <div class="row">
        <label>
          Skill level
          <input v-model="filters.skillLevel" type="text" placeholder="e.g. INTERMEDIATE" />
        </label>
        <label>
          Activity status
          <input v-model="filters.activityStatus" type="text" placeholder="e.g. OPEN" />
        </label>
        <label>
          Min rating
          <input v-model.number="filters.minRating" type="number" step="0.1" min="0" max="5" placeholder="e.g. 4.5" />
        </label>
        <label>
          Facility name
          <input v-model="filters.facilityName" type="text" placeholder="e.g. Tartu Sport Hall" />
        </label>
      </div>
      <div class="actions">
        <button type="submit" :disabled="loading">
          {{ loading ? 'Searching…' : 'Search' }}
        </button>
        <button type="button" class="secondary" @click="clearFilters">Clear</button>
      </div>
    </form>

    <div v-if="lastUrl" class="last-url">
      <small>GET <code>{{ lastUrl }}</code></small>
    </div>

    <div v-if="loading" class="loading">Loading…</div>

    <div v-else-if="error" class="error">
      <p><b>Error:</b> {{ error }}</p>
      <button @click="runSearch">Retry</button>
    </div>

    <div v-else-if="results.length === 0 && hasSearched" class="empty">
      <p>No entries match the filters.</p>
    </div>

    <div v-else-if="results.length > 0">
      <p class="count">Found <b>{{ results.length }}</b> entry(ies)</p>
      <div class="item" v-for="r in results" :key="r.indexId || (r.entityType + '-' + r.entityId)">
        <div class="item-head">
          <h3>{{ r.displayName || '(no name)' }}</h3>
          <span class="badge" :class="'type-' + (r.entityType || '').toLowerCase()">
            {{ r.entityType }}
          </span>
        </div>
        <div class="meta">
          <span v-if="r.sportType">🏀 {{ r.sportType }}</span>
          <span v-if="r.location">📍 {{ r.location }}</span>
          <span v-if="r.skillLevel">🎯 {{ r.skillLevel }}</span>
          <span v-if="r.activityStatus">📋 {{ r.activityStatus }}</span>
          <span v-if="r.userRatingScore != null">⭐ {{ r.userRatingScore }}</span>
        </div>
        <div v-if="r.keywords && r.keywords.length" class="keywords">
          <span v-for="kw in r.keywords" :key="kw" class="keyword">{{ kw }}</span>
        </div>
        <p class="ids">
          <small>
            <b>Entity ID:</b> {{ r.entityId || '—' }}<br />
            <b>Index ID:</b> {{ r.indexId || '—' }}
            <span v-if="r.lastIndexedAt">
              <br /><b>Last indexed:</b> {{ formatTime(r.lastIndexedAt) }}
            </span>
          </small>
        </p>
      </div>
    </div>
  </div>
</template>

<script>
const SEARCH_BASE = 'http://localhost:8086'

export default {
  name: 'CrossEntitySearch',
  data() {
    return {
      filters: {
        q: '',
        entityType: '',
        sportType: '',
        location: '',
        skillLevel: '',
        activityStatus: '',
        minRating: null,
        facilityName: ''
      },
      results: [],
      loading: false,
      error: null,
      lastUrl: null,
      hasSearched: false
    }
  },
  methods: {
    runSearch() {
      this.loading = true
      this.error = null
      this.hasSearched = true

      const params = []
      const f = this.filters
      const addStr = (k, v) => {
        if (v != null && String(v).trim() !== '') {
          params.push(k + '=' + encodeURIComponent(String(v).trim()))
        }
      }
      addStr('q', f.q)
      addStr('entityType', f.entityType)
      addStr('sportType', f.sportType)
      addStr('location', f.location)
      addStr('skillLevel', f.skillLevel)
      addStr('activityStatus', f.activityStatus)
      if (f.minRating != null && f.minRating !== '') addStr('minRating', f.minRating)
      addStr('facilityName', f.facilityName)

      const query = params.length ? '?' + params.join('&') : ''
      const url = SEARCH_BASE + '/api/search' + query
      this.lastUrl = url

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
        .then(data => { this.results = data })
        .catch(err => { this.error = err.message })
        .finally(() => { this.loading = false })
    },
    clearFilters() {
      Object.keys(this.filters).forEach(k => {
        this.filters[k] = (k === 'minRating') ? null : ''
      })
      this.error = null
      this.results = []
      this.hasSearched = false
      this.lastUrl = null
    },
    formatTime(iso) {
      if (!iso) return ''
      const d = new Date(iso)
      return d.toLocaleString('en-GB', {
        day: '2-digit', month: 'short',
        hour: '2-digit', minute: '2-digit'
      })
    }
  },
  mounted() {
    this.runSearch()
  }
}
</script>

<style scoped>
.cross-search { text-align: left; }
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
}
.filters {
  background: #f4f4f4;
  padding: 15px 20px;
  border-radius: 10px;
  margin-bottom: 16px;
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
.hint {
  color: #666;
  font-size: 0.78em;
  margin-top: 3px;
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
button:disabled { opacity: 0.6; cursor: not-allowed; }
button.secondary {
  background: white;
  color: #2c3e50;
  border: 1px solid #ccc;
}
.last-url {
  background: #eaf3ff;
  border-left: 4px solid #3498db;
  padding: 6px 12px;
  border-radius: 0 6px 6px 0;
  margin-bottom: 10px;
  color: #2c3e50;
  word-break: break-all;
}
.last-url code { font-family: monospace; font-size: 0.85em; }
.count { text-align: center; color: #555; }
.item {
  background: #eaf3ff;
  padding: 15px 20px;
  margin-bottom: 12px;
  border-radius: 10px;
  border-left: 4px solid #3498db;
}
.item-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.item-head h3 { margin: 0; color: #2c3e50; }
.badge {
  color: white;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 0.78em;
  font-weight: bold;
}
.type-user { background: #8e44ad; }
.type-activity { background: #e67e22; }
.type-facility { background: #3498db; }
.meta {
  display: flex;
  gap: 12px;
  margin-bottom: 10px;
  flex-wrap: wrap;
  font-size: 0.93em;
}
.keywords { margin-bottom: 8px; }
.keyword {
  display: inline-block;
  background: #d6e9ff;
  color: #1f3a5f;
  padding: 2px 7px;
  border-radius: 3px;
  font-size: 0.8em;
  margin: 0 4px 4px 0;
  font-family: monospace;
}
.ids { color: #777; margin: 0; }
.loading, .empty, .error { text-align: center; padding: 30px; }
.error { color: #c0392b; }
</style>
