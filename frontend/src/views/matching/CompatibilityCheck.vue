<template>
  <div class="compatibility-check">
    <h2>Compatibility Check</h2>

    <form @submit.prevent="checkCompatibility" class="search-form">
      <label for="userAId">User A ID</label>
      <input
        id="userAId"
        v-model="userAId"
        placeholder="User A UUID"
        type="text"
      />

      <label for="userBId">User B ID</label>
      <input
        id="userBId"
        v-model="userBId"
        placeholder="User B UUID"
        type="text"
      />

      <button type="submit" :disabled="loading">Check Compatibility</button>
    </form>

    <div v-if="loading" class="status">Computing compatibility…</div>
    <div v-else-if="error" class="status error">{{ error }}</div>

    <div v-else-if="result" class="result-card">
      <div class="total-score-row">
        <span class="total-label">Overall Compatibility</span>
        <span class="total-value" :class="scoreClass(result.compatibilityScore)">
          {{ pct(result.compatibilityScore) }}
        </span>
      </div>

      <div class="total-bar-track">
        <div
          class="total-bar-fill"
          :class="scoreClass(result.compatibilityScore)"
          :style="{ width: pct(result.compatibilityScore) }"
        ></div>
      </div>

      <div class="pair-row">
        <span class="pair-label">User A:</span>
        <span class="pair-id">{{ result.userAId }}</span>
      </div>
      <div class="pair-row">
        <span class="pair-label">User B:</span>
        <span class="pair-id">{{ result.userBId }}</span>
      </div>

      <div class="dimensions">
        <div
          v-for="dim in dimensions"
          :key="dim.key"
          class="dimension-row"
        >
          <span class="dim-label">{{ dim.label }}</span>
          <div class="bar-track">
            <div
              class="bar-fill"
              :class="scoreClass(result[dim.key])"
              :style="{ width: pct(result[dim.key]) }"
            ></div>
          </div>
          <span class="dim-value" :class="scoreClass(result[dim.key])">
            {{ pct(result[dim.key]) }}
          </span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
const BASE = 'http://localhost:8087/api/matching'

export default {
  name: 'CompatibilityCheck',
  data() {
    return {
      userAId: '',
      userBId: '',
      result: null,
      loading: false,
      error: null,
      dimensions: [
        { key: 'sportScore',    label: 'Sport'    },
        { key: 'skillScore',    label: 'Skill'    },
        { key: 'locationScore', label: 'Location' },
        { key: 'ratingScore',   label: 'Rating'   },
        { key: 'languageScore', label: 'Language' }
      ]
    }
  },
  methods: {
    checkCompatibility() {
      if (!this.userAId.trim() || !this.userBId.trim()) return

      this.loading = true
      this.error = null
      this.result = null

      const params = new URLSearchParams({
        userAId: this.userAId.trim(),
        userBId: this.userBId.trim()
      })

      fetch(`${BASE}/compatibility?${params}`)
        .then(response => {
          if (!response.ok) {
            return response.json().then(data => {
              throw new Error(data.message || 'HTTP ' + response.status)
            })
          }
          return response.json()
        })
        .then(data => {
          this.result = data
        })
        .catch(err => {
          this.error = err.message
          console.error(err)
        })
        .finally(() => {
          this.loading = false
        })
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
.compatibility-check {
  max-width: 700px;
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

.search-form button:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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

.result-card {
  border: 1px solid #dde2e6;
  border-radius: 10px;
  padding: 20px;
  background: #fff;
}

.total-score-row {
  display: flex;
  justify-content: space-between;
  align-items: baseline;
  margin-bottom: 8px;
}

.total-label {
  font-size: 1.05em;
  font-weight: bold;
  color: #333;
}

.total-value {
  font-size: 1.4em;
  font-weight: bold;
}

.total-bar-track {
  height: 10px;
  background: #eee;
  border-radius: 5px;
  overflow: hidden;
  margin-bottom: 20px;
}

.total-bar-fill {
  height: 100%;
  border-radius: 5px;
  transition: width 0.4s ease;
}

.pair-row {
  display: flex;
  gap: 8px;
  margin-bottom: 6px;
  font-size: 0.9em;
  color: #555;
}

.pair-label {
  font-weight: bold;
  min-width: 60px;
}

.pair-id {
  word-break: break-all;
  color: #777;
}

.dimensions {
  margin-top: 20px;
  display: grid;
  gap: 12px;
}

.dimension-row {
  display: grid;
  grid-template-columns: 80px 1fr 48px;
  align-items: center;
  gap: 12px;
}

.dim-label {
  font-weight: bold;
  color: #444;
  font-size: 0.95em;
}

.bar-track {
  height: 8px;
  background: #eee;
  border-radius: 4px;
  overflow: hidden;
}

.bar-fill {
  height: 100%;
  border-radius: 4px;
  transition: width 0.4s ease;
}

.dim-value {
  font-weight: bold;
  font-size: 0.9em;
  text-align: right;
}

/* Score colour classes applied to both text and bar fills */
.score-high { color: #27ae60; background-color: #27ae60; }
.score-mid  { color: #e67e22; background-color: #e67e22; }
.score-low  { color: #c0392b; background-color: #c0392b; }

/* text-only overrides so bar-fill bg doesn't bleed into text colour */
.dim-value.score-high,
.total-value.score-high { background-color: transparent; }
.dim-value.score-mid,
.total-value.score-mid  { background-color: transparent; }
.dim-value.score-low,
.total-value.score-low  { background-color: transparent; }
</style>