<template>
  <div class="activity-suggestions">
    <h2>Activity Suggestions</h2>

    <form @submit.prevent="fetchCached" class="search-form">
      <label for="userId">User ID</label>
      <input
        id="userId"
        v-model="userId"
        placeholder="User UUID"
        type="text"
      />

      <label for="limit">Limit</label>
      <input
        id="limit"
        v-model.number="limit"
        type="number"
        min="1"
        max="100"
        style="max-width: 80px;"
      />

      <label for="minScore">Min Score</label>
      <input
        id="minScore"
        v-model.number="minScore"
        type="number"
        min="0"
        max="1"
        step="0.05"
        style="max-width: 80px;"
      />

      <button type="submit">Load Cached</button>
      <button type="button" class="recompute-button" @click="recompute">Recompute</button>
    </form>

    <div v-if="loading" class="status">Loading suggestions…</div>
    <div v-else-if="error" class="status error">{{ error }}</div>

    <div v-else-if="suggestion && suggestion.suggestedActivities.length === 0" class="status">
      <p>No activity suggestions found for the selected filters.</p>
    </div>

    <div v-else-if="suggestion" class="suggestions-list">
      <p class="count">
        Found <strong>{{ suggestion.suggestedActivities.length }}</strong> suggestion(s)
        <span class="meta"> — Computed: {{ formatDate(suggestion.createdAt) }}</span>
      </p>

      <div
        v-for="(activity, index) in suggestion.suggestedActivities"
        :key="activity.activityId"
        class="suggestion-card"
      >
        <div class="suggestion-header">
          <span class="suggestion-rank">#{{ index + 1 }}</span>
          <span class="suggestion-id">Activity ID: {{ activity.activityId }}</span>
          <span class="suggestion-score" :class="scoreClass(activity.relevanceScore)">
            {{ pct(activity.relevanceScore) }} match
          </span>
        </div>

        <div class="suggestion-row">
          <strong>Sport:</strong> {{ pct(activity.sportScore) }}
        </div>
        <div class="suggestion-row">
          <strong>Skill:</strong> {{ pct(activity.skillScore) }}
        </div>
        <div class="suggestion-row">
          <strong>Location:</strong> {{ pct(activity.locationScore) }}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
const BASE = 'http://localhost:8087/api/matching'

export default {
  name: 'ActivitySuggestions',
  data() {
    return {
      userId: '',
      limit: 20,
      minScore: 0.0,
      suggestion: null,
      loading: false,
      error: null
    }
  },
  methods: {
    fetchCached() {
      this.call('GET')
    },
    recompute() {
      this.call('POST')
    },
    call(method) {
      if (!this.userId.trim()) return

      this.loading = true
      this.error = null
      this.suggestion = null

      const params = new URLSearchParams({ limit: this.limit, minScore: this.minScore })
      const url = `${BASE}/activities/${this.userId.trim()}/suggestions?${params}`

      fetch(url, { method })
        .then(response => {
          if (!response.ok) {
            return response.json().then(data => {
              throw new Error(data.message || 'HTTP ' + response.status)
            })
          }
          return response.json()
        })
        .then(data => {
          this.suggestion = data
        })
        .catch(err => {
          this.error = err.message
          console.error(err)
        })
        .finally(() => {
          this.loading = false
        })
    },
    formatDate(value) {
      if (!value) return ''
      return new Date(value).toLocaleString()
    },
    pct(value) {
      return Math.round((value || 0) * 100) + '%'
    },
    scoreClass(score) {
      if (score >= 0.75) return 'score-high'
      if (score >= 0.45) return 'score-mid'
      return 'score-low'
    }
  }
}
</script>

<style scoped>
.activity-suggestions {
  max-width: 900px;
  margin: 0 auto;
  text-align: left;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  align-items: center;
  margin-bottom: 20px;
}

.search-form label {
  font-weight: bold;
}

.search-form input {
  flex: 1;
  min-width: 240px;
  padding: 8px 10px;
  border: 1px solid #ccc;
  border-radius: 6px;
}

.search-form button {
  background: #42b983;
  color: white;
  border: none;
  padding: 10px 18px;
  border-radius: 6px;
  cursor: pointer;
}

.recompute-button {
  background: #2d8fd4 !important;
}

.recompute-button:hover {
  background: #2178b5 !important;
}

.status {
  padding: 20px;
  background: #f9f9f9;
  border-radius: 10px;
  color: #333;
}

.status.error {
  background: #fdecea;
  color: #c0392b;
}

.suggestions-list {
  display: grid;
  gap: 16px;
}

.count {
  margin-bottom: 12px;
  color: #555;
}

.meta {
  font-size: 0.9em;
  color: #888;
}

.suggestion-card {
  border: 1px solid #dde2e6;
  border-radius: 10px;
  padding: 16px;
  background: #fff;
}

.suggestion-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
  font-size: 0.95em;
  color: #555;
}

.suggestion-rank {
  font-weight: bold;
  color: #333;
}

.suggestion-id {
  flex: 1;
  word-break: break-all;
}

.suggestion-score {
  font-weight: bold;
  white-space: nowrap;
}

.score-high { color: #27ae60; }
.score-mid  { color: #e67e22; }
.score-low  { color: #c0392b; }

.suggestion-row {
  margin-bottom: 8px;
}
</style>