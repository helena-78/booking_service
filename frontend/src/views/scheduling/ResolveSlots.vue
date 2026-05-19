<template>
  <div class="resolve">
    <h2>Find Common Availability</h2>
    <p class="hint">
      Enter UUIDs of multiple users to find time windows when they're all
      available simultaneously. Used by Matching Service to suggest meeting
      times for compatible users.
    </p>

    <section class="user-list">
      <h3>Users to check</h3>

      <div class="user-row" v-for="(u, idx) in userIds" :key="idx">
        <input
          v-model="userIds[idx]"
          :placeholder="'User ' + (idx + 1) + ' UUID'"
          @blur="lookupProfile(userIds[idx])" />
        <span class="user-label" v-if="profileFor(userIds[idx])">
          {{ profileFor(userIds[idx]).name }}
        </span>
        <button
          v-if="userIds.length > 1"
          class="btn-remove"
          @click="removeUser(idx)">
          Remove
        </button>
      </div>

      <button class="btn-secondary" @click="addUser">+ Add user</button>
    </section>

    <div class="actions">
      <button class="btn-primary" @click="resolve" :disabled="loading || !canResolve">
        {{ loading ? 'Searching…' : 'Find Common Slots' }}
      </button>
    </div>

    <div v-if="error" class="error msg">
      <p><b>Error:</b> {{ error }}</p>
    </div>

    <section v-if="resolved !== null" class="results">
      <h3>Results</h3>
      <div v-if="resolved.length === 0" class="empty">
        <p>No common availability found.</p>
        <small>
          Either the users have no overlapping windows, or some of them have
          no schedule set up yet. You can set schedules in
          <router-link to="/slots/availability">User Availability</router-link>.
        </small>
      </div>
      <div v-else>
        <p class="count">{{ resolved.length }} overlapping window(s) found</p>
        <div class="window" v-for="w in resolved" :key="w.windowId">
          <div class="day">{{ dayLabel(w.dayOfWeek) }}</div>
          <div class="time">
            🕒 {{ formatTime(w.timeRangeFrom) }}
            → {{ formatTime(w.timeRangeTo) }}
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script>
export default {
  name: 'ResolveSlots',
  data() {
    return {
      userIds: ['', ''],   // start with 2 empty rows
      profiles: {},
      resolved: null,      // null until first search; [] if none; [...] if found
      loading: false,
      error: null,
      dayNames: {
        MON: 'Monday', TUE: 'Tuesday', WED: 'Wednesday',
        THU: 'Thursday', FRI: 'Friday', SAT: 'Saturday', SUN: 'Sunday'
      }
    }
  },
  computed: {
    canResolve() {
      return this.userIds.filter(id => id && id.trim()).length >= 1
    }
  },
  methods: {
    addUser() {
      this.userIds.push('')
    },
    removeUser(idx) {
      this.userIds.splice(idx, 1)
    },
    lookupProfile(userId) {
      if (!userId || !userId.trim() || this.profiles[userId]) return
      fetch(`http://localhost:8081/api/users/${userId.trim()}`)
        .then(r => r.ok ? r.json() : null)
        .then(p => {
          if (p) this.profiles = { ...this.profiles, [userId.trim()]: p }
        })
        .catch(() => { /* unknown user — silent fallback */ })
    },
    profileFor(userId) {
      return userId ? this.profiles[userId.trim()] : null
    },
    resolve() {
      const cleanIds = this.userIds.map(id => id.trim()).filter(Boolean)
      if (cleanIds.length === 0) {
        this.error = 'Please enter at least one user ID.'
        return
      }
      this.loading = true
      this.error = null
      this.resolved = null
      fetch('http://localhost:8089/api/timeslots/resolve', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userIds: cleanIds })
      })
        .then(async r => {
          if (!r.ok) {
            const err = await r.json().catch(() => ({}))
            throw new Error(err.message || `HTTP ${r.status}`)
          }
          return r.json()
        })
        .then(data => {
          this.resolved = data
        })
        .catch(err => { this.error = err.message })
        .finally(() => { this.loading = false })
    },
    dayLabel(dow) {
      return this.dayNames[dow] || dow
    },
    formatTime(iso) {
      if (!iso) return ''
      const d = new Date(iso)
      return d.toLocaleString('en-GB', {
        hour: '2-digit', minute: '2-digit'
      })
    }
  }
}
</script>

<style scoped>
.resolve {
  text-align: left;
  max-width: 700px;
  margin: 0 auto;
}
h2 { color: #2c3e50; }
.hint {
  color: #777;
  font-size: 0.9em;
  margin-bottom: 20px;
}
section {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 10px;
  margin-bottom: 20px;
}
.user-list { background: #ecf3ee; border-left: 4px solid #42b983; }
h3 {
  margin-top: 0;
  color: #2c3e50;
}
.user-row {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 10px;
}
.user-row input {
  flex: 1;
  padding: 8px;
  border: 1px solid #d5dde0;
  border-radius: 6px;
  font-size: 0.95em;
}
.user-label {
  font-weight: bold;
  color: #2c3e50;
  white-space: nowrap;
  font-size: 0.9em;
}
.btn-remove {
  background: transparent;
  color: #e74c3c;
  border: 1px solid #e74c3c;
  padding: 4px 10px;
  border-radius: 4px;
  cursor: pointer;
  font-size: 0.8em;
  margin: 0;
}
button {
  background: #42b983;
  color: white;
  border: none;
  padding: 10px 18px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  margin-top: 10px;
}
button:disabled { background: #aaa; cursor: not-allowed; }
.btn-primary { background: #42b983; }
.btn-secondary { background: #95a5a6; padding: 6px 14px; }
.actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
}
.results { background: #fff7e6; border-left: 4px solid #f39c12; }
.empty {
  color: #777;
  text-align: center;
  padding: 20px 0;
}
.empty small {
  display: block;
  margin-top: 10px;
}
.count {
  font-weight: bold;
  color: #2c3e50;
  margin: 0 0 12px;
}
.window {
  background: white;
  border-radius: 6px;
  padding: 12px 15px;
  margin-bottom: 8px;
  border-left: 3px solid #3498db;
  display: flex;
  align-items: center;
  gap: 15px;
}
.day {
  font-weight: bold;
  color: #3498db;
  min-width: 100px;
}
.time {
  color: #2c3e50;
}
.msg {
  padding: 10px 15px;
  margin-top: 15px;
  border-radius: 6px;
}
.error { background: #fadbd8; color: #c0392b; }
</style>