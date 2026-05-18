<template>
  <div class="booking-facilities">
    <h2>Find a Facility (via Booking Service)</h2>
    <p class="subtitle">
      GET <code>/api/bookings/facilities/search</code> &middot; the Booking Service
      proxies the query synchronously to the Search Service.
    </p>

    <form class="filters" @submit.prevent="searchFacilities">
      <div class="row">
        <label>
          Sport type
          <input v-model="filters.sportType" type="text" placeholder="e.g. BASKETBALL" />
        </label>
        <label>
          Location
          <input v-model="filters.location" type="text" placeholder="e.g. Tartu" />
        </label>
        <label>
          Time Slot ID
          <input v-model="filters.timeSlotId" type="text" placeholder="e.g. ts-300" />
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
      <p v-if="error.includes('503') || error.toLowerCase().includes('search')" class="hint-text">
        The Search Service may be down. The Booking Service requires it at
        <code>http://localhost:8086</code> for this endpoint.
      </p>
    </div>

    <div v-else-if="facilities.length === 0 && hasSearched" class="empty">
      <p>No facilities match the filters.</p>
    </div>

    <div v-else-if="facilities.length > 0">
      <p class="count">Found <b>{{ facilities.length }}</b> facility(ies)</p>
      <div class="item" v-for="(f, i) in facilities" :key="(f.entityId || f.facilityId) + '-' + i">
        <h3>{{ f.displayName || f.facilityName || '(no name)' }}</h3>
        <div class="meta">
          <span class="badge">FACILITY</span>
          <span v-if="f.sportType" class="sport">🏀 {{ f.sportType }}</span>
          <span v-if="f.location" class="location">📍 {{ f.location }}</span>
        </div>
        <p class="ids">
          <small>
            <b>Entity ID:</b> {{ f.entityId || f.facilityId || '—' }}
          </small>
        </p>
      </div>
    </div>
  </div>
</template>

<script>
const BOOKING_BASE = 'http://localhost:8088'

export default {
  name: 'BookingFacilitySearch',
  data() {
    return {
      filters: {
        sportType: '',
        location: '',
        timeSlotId: ''
      },
      facilities: [],
      loading: false,
      error: null,
      lastUrl: null,
      hasSearched: false
    }
  },
  methods: {
    searchFacilities() {
      this.loading = true
      this.error = null
      this.hasSearched = true

      const params = []
      const f = this.filters
      if (f.sportType && f.sportType.trim())
        params.push('sportType=' + encodeURIComponent(f.sportType.trim()))
      if (f.location && f.location.trim())
        params.push('location=' + encodeURIComponent(f.location.trim()))
      if (f.timeSlotId && f.timeSlotId.trim())
        params.push('timeSlotId=' + encodeURIComponent(f.timeSlotId.trim()))

      const query = params.length ? '?' + params.join('&') : ''
      const url = BOOKING_BASE + '/api/bookings/facilities/search' + query
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
        .then(data => {
          this.facilities = data
        })
        .catch(err => {
          this.error = err.message
        })
        .finally(() => {
          this.loading = false
        })
    },
    clearFilters() {
      this.filters.sportType = ''
      this.filters.location = ''
      this.filters.timeSlotId = ''
      this.error = null
      this.facilities = []
      this.hasSearched = false
    }
  }
}
</script>

<style scoped>
.booking-facilities { text-align: left; }
h2 { color: #2c3e50; text-align: center; }
.subtitle {
  text-align: center; color: #777; font-size: 0.9em; margin-top: -8px;
}
.subtitle code {
  background: #eaf3ff; padding: 1px 6px; border-radius: 4px; font-family: monospace;
}
.filters {
  background: #f4f4f4; padding: 15px 20px; border-radius: 10px; margin-bottom: 16px;
}
.row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px; margin-bottom: 12px;
}
.row label {
  display: flex; flex-direction: column;
  font-size: 0.9em; color: #2c3e50;
}
.row input {
  margin-top: 4px; padding: 6px 8px;
  border: 1px solid #ccc; border-radius: 6px; font-size: 0.95em;
}
.actions { display: flex; gap: 8px; }
button {
  background: #42b983; color: white; border: none;
  padding: 8px 16px; border-radius: 6px; cursor: pointer; font-size: 0.95em;
}
button:disabled { opacity: 0.5; cursor: not-allowed; }
button.secondary { background: white; color: #2c3e50; border: 1px solid #ccc; }
.last-url {
  background: #eaf3ff; border-left: 4px solid #3498db;
  padding: 6px 12px; border-radius: 0 6px 6px 0; margin-bottom: 10px; word-break: break-all;
}
.last-url code { font-family: monospace; font-size: 0.85em; }
.count { text-align: center; color: #555; }
.hint-text {
  font-size: 0.85em; color: #c0392b; margin-top: 6px;
}
.hint-text code {
  background: #fff; padding: 1px 5px; border-radius: 3px; font-family: monospace;
}
.item {
  background: #eaf3ff; padding: 15px 20px;
  margin-bottom: 12px; border-radius: 10px; border-left: 4px solid #3498db;
}
.item h3 { margin: 0 0 8px; color: #2c3e50; }
.meta { display: flex; gap: 12px; margin-bottom: 8px; flex-wrap: wrap; }
.badge {
  background: #3498db; color: white;
  padding: 3px 10px; border-radius: 12px;
  font-size: 0.8em; font-weight: bold;
}
.sport, .location { font-size: 0.95em; }
.ids { color: #777; margin: 0; }
.loading, .empty, .error { text-align: center; padding: 30px; }
.error { color: #c0392b; }
</style>
