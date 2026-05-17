<template>
  <div class="facilities">
    <h2>Facility Search</h2>

    <form class="filters" @submit.prevent="searchFacilities">
      <div class="row">
        <label>
          Facility name
          <input v-model="filters.facilityName" type="text" placeholder="e.g. Tartu" />
        </label>
        <label>
          Sport type
          <input v-model="filters.sportType" type="text" placeholder="e.g. BASKETBALL" />
        </label>
        <label>
          Location
          <input v-model="filters.location" type="text" placeholder="e.g. Tartu" />
        </label>
        <label>
          Sport Center ID
          <input v-model="filters.sportCenterId" type="text" placeholder="e.g. sc-100" />
          <small class="hint">triggers Booking Service call</small>
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

    <div v-if="bookingCallTriggered" class="integration-note">
      <b>Cross-service call active.</b>
      The Search Service is calling
      <code>GET /api/bookings?sportCenterId={{ filters.sportCenterId }}</code>
      on the Booking Service for this request.
    </div>

    <div v-if="loading" class="loading">Loading…</div>

    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="searchFacilities">Retry</button>
    </div>

    <div v-else-if="facilities.length === 0" class="empty">
      <p>No facilities match the filters.</p>
    </div>

    <div v-else>
      <p class="count">Found <b>{{ facilities.length }}</b> facility(ies)</p>
      <div class="item" v-for="facility in facilities" :key="facility.indexId || facility.entityId">
        <h3>{{ facility.displayName || facility.facilityName || '(no name)' }}</h3>
        <div class="meta">
          <span class="badge">FACILITY</span>
          <span v-if="facility.sportType" class="sport">🏀 {{ facility.sportType }}</span>
          <span v-if="facility.location" class="location">📍 {{ facility.location }}</span>
        </div>
        <div v-if="facility.keywords && facility.keywords.length" class="keywords">
          <span
            v-for="kw in facility.keywords"
            :key="kw"
            class="keyword"
          >{{ kw }}</span>
        </div>
        <p class="ids">
          <small>
            <b>Entity ID:</b> {{ facility.entityId || '—' }}<br />
            <b>Index ID:</b> {{ facility.indexId || '—' }}
            <span v-if="facility.lastIndexedAt">
              <br /><b>Last indexed:</b> {{ formatTime(facility.lastIndexedAt) }}
            </span>
          </small>
        </p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'FacilitySearch',
  data() {
    return {
      filters: {
        facilityName: '',
        sportType: '',
        location: '',
        sportCenterId: ''
      },
      facilities: [],
      loading: false,
      error: null,
      lastUrl: null,
      bookingCallTriggered: false
    }
  },
  methods: {
    searchFacilities() {
      this.loading = true
      this.error = null
      this.bookingCallTriggered = Boolean(
        this.filters.sportCenterId && this.filters.sportCenterId.trim()
      )

      const params = []
      const f = this.filters
      if (f.facilityName && f.facilityName.trim())
        params.push('facilityName=' + encodeURIComponent(f.facilityName.trim()))
      if (f.sportType && f.sportType.trim())
        params.push('sportType=' + encodeURIComponent(f.sportType.trim()))
      if (f.location && f.location.trim())
        params.push('location=' + encodeURIComponent(f.location.trim()))
      if (f.sportCenterId && f.sportCenterId.trim())
        params.push('sportCenterId=' + encodeURIComponent(f.sportCenterId.trim()))

      const query = params.length ? '?' + params.join('&') : ''
      const url = 'http://localhost:8086/api/search/facilities' + query
      this.lastUrl = url

      fetch(url)
        .then(response => {
          if (!response.ok) {
            throw new Error('Failed to fetch facilities (HTTP ' + response.status + ')')
          }
          return response.json()
        })
        .then(data => {
          this.facilities = data
        })
        .catch(err => {
          this.error = err.message
          console.error(err)
        })
        .finally(() => {
          this.loading = false
        })
    },
    clearFilters() {
      this.filters.facilityName = ''
      this.filters.sportType = ''
      this.filters.location = ''
      this.filters.sportCenterId = ''
      this.error = null
      this.bookingCallTriggered = false
      this.searchFacilities()
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
    this.searchFacilities()
  }
}
</script>

<style scoped>
.facilities {
  text-align: left;
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
.hint {
  color: #d68910;
  font-size: 0.8em;
  margin-top: 3px;
}
.actions {
  display: flex;
  gap: 8px;
}
button {
  background: #42b983;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.95em;
}
button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
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
.last-url code {
  font-family: monospace;
  font-size: 0.85em;
}
.integration-note {
  background: #fff7e6;
  border-left: 4px solid #f39c12;
  padding: 10px 14px;
  border-radius: 0 8px 8px 0;
  margin-bottom: 14px;
  color: #7a4a00;
  font-size: 0.9em;
}
.integration-note code {
  background: #fdebc8;
  padding: 1px 5px;
  border-radius: 3px;
  font-family: monospace;
}
.count {
  text-align: center;
  color: #555;
}
.item {
  background: #eaf3ff;
  padding: 15px 20px;
  margin-bottom: 12px;
  border-radius: 10px;
  border-left: 4px solid #3498db;
}
.item h3 {
  margin: 0 0 8px;
  color: #2c3e50;
}
.meta {
  display: flex;
  gap: 12px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}
.badge {
  background: #3498db;
  color: white;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 0.8em;
  font-weight: bold;
}
.sport, .location {
  font-size: 0.95em;
}
.keywords {
  margin-bottom: 8px;
}
.keyword {
  display: inline-block;
  background: #d6e9ff;
  color: #1f3a5f;
  padding: 2px 7px;
  border-radius: 3px;
  font-size: 0.8em;
  margin: 0 4px 4px 0;
}
.ids {
  color: #777;
  margin: 0;
}
.loading, .empty, .error {
  text-align: center;
  padding: 30px;
}
.error {
  color: #c0392b;
}
</style>
