<template>
  <div class="activity-search">
    <h2>Activity Search</h2>
    <p class="subtitle">
      GET <code>/api/search/activities</code> &middot; search across indexed activities.
    </p>

    <form class="filters" @submit.prevent="runSearch">
      <div class="row">
        <label>
          Activity title
          <input v-model="filters.activityTitle" type="text" placeholder="e.g. Sunday Pickup" />
        </label>
        <label>
          Sport type
          <input v-model="filters.sportType" type="text" placeholder="e.g. BASKETBALL" />
        </label>
        <label>
          Status
          <select v-model="filters.activityStatus">
            <option value="">(any)</option>
            <option value="OPEN">OPEN</option>
            <option value="CLOSED">CLOSED</option>
            <option value="FULL">FULL</option>
            <option value="CANCELLED">CANCELLED</option>
          </select>
        </label>
        <label>
          Location
          <input v-model="filters.location" type="text" placeholder="e.g. Tartu" />
        </label>
      </div>
      <div class="row">
        <label>
          Max participants (min)
          <input v-model.number="filters.activityMaxParticipants" type="number" min="1" placeholder="e.g. 6" />
        </label>
        <label>
          Time slots (substring)
          <input v-model="filters.activityTimeSlots" type="text" placeholder="e.g. ts-300" />
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

    <div v-else-if="activities.length === 0 && hasSearched" class="empty">
      <p>No activities match the filters.</p>
    </div>

    <div v-else-if="activities.length > 0">
      <p class="count">Found <b>{{ activities.length }}</b> activity(ies)</p>
      <div class="item" v-for="a in activities" :key="a.indexId || a.entityId">
        <div class="item-head">
          <h3>{{ a.displayName || a.activityTitle || '(no title)' }}</h3>
          <span class="badge">ACTIVITY</span>
        </div>
        <div class="meta">
          <span v-if="a.sportType">🏀 {{ a.sportType }}</span>
          <span v-if="a.location">📍 {{ a.location }}</span>
          <span v-if="a.activityStatus" class="status" :class="'status-' + a.activityStatus.toLowerCase()">
            {{ a.activityStatus }}
          </span>
          <span v-if="a.activityMaxParticipants != null">
            👥 max {{ a.activityMaxParticipants }} ({{ participantCount(a) }} joined)
          </span>
        </div>
        <div v-if="a.activityTimeSlots" class="prefs">
          <small><b>Time slot:</b> {{ a.activityTimeSlots }}</small>
        </div>
        <div v-if="a.keywords && a.keywords.length" class="keywords">
          <span v-for="kw in a.keywords" :key="kw" class="keyword">{{ kw }}</span>
        </div>
        <p class="ids">
          <small>
            <b>Entity ID:</b> {{ a.entityId || '—' }}
            <span v-if="a.lastIndexedAt">
              <br /><b>Last indexed:</b> {{ formatTime(a.lastIndexedAt) }}
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
  name: 'ActivitySearch',
  data() {
    return {
      filters: {
        activityTitle: '',
        sportType: '',
        activityStatus: '',
        location: '',
        activityMaxParticipants: null,
        activityTimeSlots: ''
      },
      activities: [],
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
      addStr('activityTitle', f.activityTitle)
      addStr('sportType', f.sportType)
      addStr('activityStatus', f.activityStatus)
      addStr('location', f.location)
      if (f.activityMaxParticipants != null && f.activityMaxParticipants !== '') {
        addStr('activityMaxParticipants', f.activityMaxParticipants)
      }
      addStr('activityTimeSlots', f.activityTimeSlots)

      const query = params.length ? '?' + params.join('&') : ''
      const url = SEARCH_BASE + '/api/search/activities' + query
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
        .then(data => { this.activities = data })
        .catch(err => { this.error = err.message })
        .finally(() => { this.loading = false })
    },
    clearFilters() {
      Object.keys(this.filters).forEach(k => {
        this.filters[k] = (k === 'activityMaxParticipants') ? null : ''
      })
      this.error = null
      this.activities = []
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
    },
    participantCount(a) {
      // Participant count is tracked as synthetic "participant:<userId>" keywords
      // written by the ActivityEventConsumer when PARTICIPANT_JOINED / LEFT events arrive.
      if (!a.keywords) return 0
      return a.keywords.filter(k => k && k.startsWith('participant:')).length
    }
  },
  mounted() {
    this.runSearch()
  }
}
</script>

<style scoped>
.activity-search { text-align: left; }
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
  background: #fff1e0;
  padding: 15px 20px;
  margin-bottom: 12px;
  border-radius: 10px;
  border-left: 4px solid #e67e22;
}
.item-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.item-head h3 { margin: 0; color: #2c3e50; }
.badge {
  background: #e67e22;
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
.status {
  padding: 2px 8px;
  border-radius: 10px;
  font-weight: bold;
  font-size: 0.85em;
}
.status-open { background: #d4edda; color: #155724; }
.status-closed { background: #d6d8db; color: #383d41; }
.status-full { background: #fff3cd; color: #856404; }
.status-cancelled { background: #f8d7da; color: #721c24; }
.prefs { margin-bottom: 8px; color: #555; }
.keywords { margin-bottom: 8px; }
.keyword {
  display: inline-block;
  background: #f5dcc0;
  color: #5e3c10;
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
