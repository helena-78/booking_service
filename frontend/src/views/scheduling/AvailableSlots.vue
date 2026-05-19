<template>
  <div class="slots">
    <h2>Time Slots</h2>

    <div class="filters">
      <label>
        <input type="radio" value="available" v-model="filter" />
        Available only
      </label>
      <label>
        <input type="radio" value="all" v-model="filter" />
        All slots
      </label>
    </div>

    <div v-if="loading" class="loading">Loading…</div>

    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="fetchSlots">Retry</button>
    </div>

    <div v-else-if="slots.length === 0" class="empty">
      <p>No slots to show.</p>
      <p>
        <router-link to="/slots/reserve">Reserve a new slot</router-link>
      </p>
    </div>

    <div v-else>
      <p class="count">{{ slots.length }} slot(s)</p>
      <div class="item" v-for="slot in slots" :key="slot.slotId" :class="'slot-' + slot.status.toLowerCase()">
        <div class="row">
          <div class="time">
            🕒 <b>{{ formatTime(slot.startTime) }}</b>
            → <b>{{ formatTime(slot.endTime) }}</b>
          </div>
          <div class="badge" :class="'badge-' + slot.status.toLowerCase()">
            {{ slot.status }}
          </div>
        </div>

        <div class="row meta-row">
          <small v-if="slot.organizerId" class="meta">
            <b>Organizer:</b> {{ organizerName(slot.organizerId) || slot.organizerId.slice(0,8) + '…' }}
          </small>
          <button
            v-if="slot.status === 'RESERVED'"
            class="btn-warning"
            @click="releaseSlot(slot.slotId)"
            :disabled="actionLoading">
            Release
          </button>
        </div>

        <div v-if="slot.reservedForId" class="activity-card">
          <div v-if="activityFor(slot.reservedForId)">
            <router-link
              :to="'/activities/' + slot.reservedForId"
              class="activity-link">
              📌 {{ activityFor(slot.reservedForId).title }}
            </router-link>
            <div class="activity-details">
              <span class="activity-status" :class="'status-' + activityFor(slot.reservedForId).status.toLowerCase()">
                {{ activityFor(slot.reservedForId).status }}
              </span>
              <span>🏃 {{ activityFor(slot.reservedForId).sportType }}</span>
              <span>
                👥 {{ activityFor(slot.reservedForId).participants.length }}
                / {{ activityFor(slot.reservedForId).maxParticipants }}
              </span>
            </div>
            <p v-if="activityFor(slot.reservedForId).description" class="activity-description">
              {{ activityFor(slot.reservedForId).description }}
            </p>
          </div>
          <div v-else class="muted">
            <small>Reserved for activity {{ slot.reservedForId.slice(0,8) }}… (details unavailable)</small>
          </div>
        </div>
      </div>
    </div>

    <div v-if="success" class="success msg">
      <p>✓ {{ success }}</p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AvailableSlots',
  data() {
    return {
      slots: [],
      profiles: {},     // userId -> profile
      activities: {},   // activityId -> activity
      loading: false,
      actionLoading: false,
      error: null,
      success: null,
      filter: 'available'
    }
  },
  watch: {
    filter() { this.fetchSlots() }
  },
  methods: {
    fetchSlots() {
      this.loading = true
      this.error = null
      const url = this.filter === 'available'
        ? 'http://localhost:8089/api/timeslots/available'
        : 'http://localhost:8089/api/timeslots'

      fetch(url)
        .then(async r => {
          if (!r.ok) {
            const err = await r.json().catch(() => ({}))
            throw new Error(err.message || `HTTP ${r.status}`)
          }
          return r.json()
        })
        .then(data => {
          this.slots = data
          // Pre-fetch profiles
          const uniqueOrganizers = [...new Set(
            data.filter(s => s.organizerId).map(s => s.organizerId)
          )]
          uniqueOrganizers.forEach(id => this.fetchProfile(id))

          // Pre-fetch activities (for RESERVED slots)
          const uniqueActivities = [...new Set(
            data.filter(s => s.reservedForId).map(s => s.reservedForId)
          )]
          uniqueActivities.forEach(id => this.fetchActivity(id))
        })
        .catch(err => {
          this.error = err.message
          console.error(err)
        })
        .finally(() => {
          this.loading = false
        })
    },

    fetchProfile(userId) {
      if (this.profiles[userId]) return
      fetch(`http://localhost:8081/api/users/${userId}`)
        .then(r => r.ok ? r.json() : null)
        .then(profile => {
          if (profile) {
            this.profiles = { ...this.profiles, [userId]: profile }
          }
        })
        .catch(() => { /* silently fall back */ })
    },

    fetchActivity(activityId) {
      if (this.activities[activityId]) return
      fetch(`http://localhost:8083/api/activities/${activityId}`)
        .then(r => r.ok ? r.json() : null)
        .then(activity => {
          if (activity) {
            this.activities = { ...this.activities, [activityId]: activity }
          }
        })
        .catch(() => { /* slot might reference deleted/unknown activity */ })
    },

    organizerName(userId) {
      return this.profiles[userId]?.name
    },

    activityFor(activityId) {
      return this.activities[activityId]
    },

    releaseSlot(slotId) {
      if (!confirm('Release this reserved slot?')) return
      this.actionLoading = true
      this.error = null
      this.success = null
      fetch(`http://localhost:8089/api/timeslots/${slotId}/release`, {
        method: 'DELETE'
      })
        .then(async r => {
          if (!r.ok && r.status !== 204) {
            const err = await r.json().catch(() => ({}))
            throw new Error(err.message || `HTTP ${r.status}`)
          }
        })
        .then(() => {
          this.success = 'Slot released.'
          this.fetchSlots()
        })
        .catch(err => { this.error = err.message })
        .finally(() => { this.actionLoading = false })
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
    this.fetchSlots()
  }
}
</script>

<style scoped>
.slots {
  text-align: left;
}
.filters {
  display: flex;
  gap: 20px;
  margin-bottom: 15px;
  padding: 10px 15px;
  background: #f4f4f4;
  border-radius: 8px;
  justify-content: center;
}
.filters label {
  cursor: pointer;
}
.count {
  text-align: center;
  color: #555;
}
.item {
  padding: 12px 18px;
  margin-bottom: 10px;
  border-radius: 8px;
}
.slot-available {
  background: #fff7e6;
  border-left: 4px solid #f39c12;
}
.slot-reserved {
  background: #eaeef9;
  border-left: 4px solid #3498db;
}
.slot-released {
  background: #f5f5f5;
  border-left: 4px solid #95a5a6;
}
.row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}
.meta-row {
  margin-top: 8px;
}
.time {
  font-size: 1.05em;
}
.badge {
  display: inline-block;
  color: white;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 0.8em;
  font-weight: bold;
}
.badge-available { background: #27ae60; }
.badge-reserved  { background: #3498db; }
.badge-released  { background: #95a5a6; }
.meta {
  color: #777;
}
button.btn-warning {
  background: #f39c12;
  color: white;
  border: none;
  padding: 6px 14px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.85em;
  font-weight: bold;
}
button.btn-warning:disabled { background: #aaa; cursor: not-allowed; }

.activity-card {
  background: white;
  border-radius: 6px;
  padding: 10px 14px;
  margin-top: 10px;
  border-left: 3px solid #42b983;
}
.activity-link {
  font-weight: bold;
  color: #2c3e50;
  text-decoration: none;
  font-size: 1em;
}
.activity-link:hover {
  color: #42b983;
  text-decoration: underline;
}
.activity-details {
  display: flex;
  gap: 12px;
  margin-top: 6px;
  font-size: 0.85em;
  color: #555;
  flex-wrap: wrap;
}
.activity-status {
  padding: 1px 8px;
  border-radius: 10px;
  color: white;
  font-weight: bold;
  font-size: 0.85em;
}
.status-planned   { background: #3498db; }
.status-active    { background: #27ae60; }
.status-completed { background: #95a5a6; }
.status-cancelled { background: #e74c3c; }
.activity-description {
  margin: 6px 0 0;
  color: #555;
  font-size: 0.9em;
  font-style: italic;
}
.muted {
  color: #999;
  font-style: italic;
}
.loading, .empty, .error {
  text-align: center;
  padding: 30px;
}
.error {
  color: #c0392b;
}
.msg {
  padding: 10px 15px;
  margin-top: 15px;
  border-radius: 6px;
}
.success {
  background: #d4efdf;
  color: #196f3d;
}
</style>