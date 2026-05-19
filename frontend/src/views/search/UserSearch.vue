<template>
  <div class="user-search">
    <h2>User Search</h2>
    <p class="subtitle">
      GET <code>/api/search/users</code> &middot; used by the Matching Service to retrieve candidate users.
    </p>

    <form class="filters" @submit.prevent="runSearch">
      <div class="row">
        <label>
          User name
          <input v-model="filters.userName" type="text" placeholder="e.g. Olena" />
        </label>
        <label>
          Sport type
          <input v-model="filters.sportType" type="text" placeholder="e.g. BASKETBALL" />
        </label>
        <label>
          Skill level
          <input v-model="filters.skillLevel" type="text" placeholder="e.g. INTERMEDIATE" />
        </label>
        <label>
          Location
          <input v-model="filters.location" type="text" placeholder="e.g. Tartu" />
        </label>
      </div>
      <div class="row">
        <label>
          Min rating
          <input v-model.number="filters.userRatingScore" type="number" step="0.1" min="0" max="5" placeholder="e.g. 4.5" />
        </label>
        <label>
          Sport preferences
          <input v-model="filters.userSportPreferences" type="text" placeholder="e.g. basketball,football" />
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

    <div v-else-if="users.length === 0 && hasSearched" class="empty">
      <p>No users match the filters.</p>
    </div>

    <div v-else-if="users.length > 0">
      <p class="count">Found <b>{{ users.length }}</b> user(s)</p>
      <div class="item" v-for="u in users" :key="u.indexId || u.entityId">
        <div class="item-head">
          <h3>{{ u.displayName || u.userName || '(no name)' }}</h3>
          <span class="badge">USER</span>
        </div>
        <div class="meta">
          <span v-if="u.sportType">🏀 {{ u.sportType }}</span>
          <span v-if="u.skillLevel">🎯 {{ u.skillLevel }}</span>
          <span v-if="u.location">📍 {{ u.location }}</span>
          <span v-if="u.userRatingScore != null">⭐ {{ u.userRatingScore }}</span>
        </div>
        <div v-if="u.userSportPreferences" class="prefs">
          <small><b>Preferences:</b> {{ u.userSportPreferences }}</small>
        </div>
        <div v-if="u.keywords && u.keywords.length" class="keywords">
          <span v-for="kw in u.keywords" :key="kw" class="keyword">{{ kw }}</span>
        </div>
        <p class="ids">
          <small>
            <b>Entity ID:</b> {{ u.entityId || '—' }}
            <span v-if="u.lastIndexedAt">
              <br /><b>Last indexed:</b> {{ formatTime(u.lastIndexedAt) }}
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
  name: 'UserSearch',
  data() {
    return {
      filters: {
        userName: '',
        sportType: '',
        skillLevel: '',
        location: '',
        userRatingScore: null,
        userSportPreferences: ''
      },
      users: [],
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
      addStr('userName', f.userName)
      addStr('sportType', f.sportType)
      addStr('skillLevel', f.skillLevel)
      addStr('location', f.location)
      if (f.userRatingScore != null && f.userRatingScore !== '') addStr('userRatingScore', f.userRatingScore)
      addStr('userSportPreferences', f.userSportPreferences)

      const query = params.length ? '?' + params.join('&') : ''
      const url = SEARCH_BASE + '/api/search/users' + query
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
        .then(data => { this.users = data })
        .catch(err => { this.error = err.message })
        .finally(() => { this.loading = false })
    },
    clearFilters() {
      Object.keys(this.filters).forEach(k => {
        this.filters[k] = (k === 'userRatingScore') ? null : ''
      })
      this.error = null
      this.users = []
      this.hasSearched = false
      this.lastUrl = null
      this.runSearch()
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
.user-search { text-align: left; }
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
.row input {
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
  background: #f3eaff;
  padding: 15px 20px;
  margin-bottom: 12px;
  border-radius: 10px;
  border-left: 4px solid #8e44ad;
}
.item-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.item-head h3 { margin: 0; color: #2c3e50; }
.badge {
  background: #8e44ad;
  color: white;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 0.78em;
  font-weight: bold;
}
.meta {
  display: flex;
  gap: 12px;
  margin-bottom: 10px;
  flex-wrap: wrap;
  font-size: 0.93em;
}
.prefs { margin-bottom: 8px; color: #555; }
.keywords { margin-bottom: 8px; }
.keyword {
  display: inline-block;
  background: #e0d0f0;
  color: #4a2569;
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
