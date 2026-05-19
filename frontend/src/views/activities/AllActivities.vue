<template>
  <div class="activities">
    <h2>All Activities</h2>

    <div v-if="loading" class="loading">Loading...</div>

    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="fetchActivities">Retry</button>
    </div>

    <div v-else-if="activities.length === 0" class="empty">
      <p>No activities yet. <router-link to="/activities/new">Create the first one</router-link>!</p>
    </div>

    <div v-else>
      <p class="count">Found <b>{{ activities.length }}</b> activity(ies)</p>
      <div class="item" v-for="activity in activities" :key="activity.activityId">
        <h3>
          <router-link :to="'/activities/' + activity.activityId" class="title-link">
            {{ activity.title }}
          </router-link>
        </h3>
        <div class="meta">
          <span class="badge" :class="'status-' + activity.status.toLowerCase()">
            {{ activity.status }}
          </span>
          <span class="sport">🏃 {{ activity.sportType }}</span>
          <span class="participants">
            👥 {{ activity.participants.length }} / {{ activity.maxParticipants }}
          </span>
        </div>
        <p v-if="activity.description" class="description">{{ activity.description }}</p>
        <p class="organizer-line">
          <b>Organized by:</b>
          {{ organizerName(activity.organizerId) || 'Loading…' }}
        </p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AllActivities',
  data() {
    return {
      activities: [],
      profiles: {},     // userId → profile
      loading: false,
      error: null
    }
  },
  methods: {
    fetchActivities() {
      this.loading = true
      this.error = null
      fetch('http://localhost:8083/api/activities')
        .then(async r => {
          if (!r.ok) {
            const err = await r.json().catch(() => ({}))
            throw new Error(err.message || `HTTP ${r.status}`)
          }
          return r.json()
        })
        .then(data => {
          this.activities = data
          // Fetch organizer profiles in parallel
          const uniqueOrganizers = [...new Set(data.map(a => a.organizerId))]
          uniqueOrganizers.forEach(id => this.fetchProfile(id))
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
        .catch(() => { /* ignore — show fallback */ })
    },

    organizerName(userId) {
      return this.profiles[userId]?.name
    }
  },
  mounted() {
    this.fetchActivities()
  }
}
</script>

<style scoped>
.activities {
  text-align: left;
}
.count {
  text-align: center;
  color: #555;
}
.item {
  background: #ecf3ee;
  padding: 15px 20px;
  margin-bottom: 12px;
  border-radius: 10px;
  border-left: 4px solid #42b983;
}
.item h3 {
  margin: 0 0 8px;
  color: #2c3e50;
}
.title-link {
  color: #2c3e50;
  text-decoration: none;
}
.title-link:hover {
  color: #42b983;
  text-decoration: underline;
}
.meta {
  display: flex;
  gap: 12px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}
.badge {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 0.8em;
  font-weight: bold;
  color: white;
}
.status-planned   { background: #3498db; }
.status-active    { background: #27ae60; }
.status-completed { background: #95a5a6; }
.status-cancelled { background: #e74c3c; }
.sport, .participants {
  font-size: 0.95em;
}
.description {
  font-style: italic;
  color: #555;
  margin: 8px 0;
}
.organizer-line {
  color: #555;
  margin: 8px 0 0;
  font-size: 0.95em;
}
.loading, .empty, .error {
  text-align: center;
  padding: 30px;
}
.error {
  color: #c0392b;
}
button {
  background: #42b983;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
}
</style>