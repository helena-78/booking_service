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
        <h3>{{ activity.title }}</h3>
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
        <p class="ids">
          <small>
            <b>Organizer:</b> {{ activity.organizerId }}<br />
            <b>Activity ID:</b> {{ activity.activityId }}
            <span v-if="activity.preferredTimeSlotId">
              <br /><b>Reserved slot:</b> {{ activity.preferredTimeSlotId }}
            </span>
          </small>
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
      loading: false,
      error: null
    }
  },
  methods: {
    fetchActivities() {
      this.loading = true
      this.error = null
      fetch('http://localhost:8083/api/activities')
        .then(response => {
          if (!response.ok) {
            throw new Error('Failed to fetch activities (HTTP ' + response.status + ')')
          }
          return response.json()
        })
        .then(data => {
          this.activities = data
        })
        .catch(err => {
          this.error = err.message
          console.error(err)
        })
        .finally(() => {
          this.loading = false
        })
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
button {
  background: #42b983;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
}
</style>