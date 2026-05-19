<template>
  <div class="user-ratings">
    <h2>User Ratings</h2>

    <div v-if="userId" class="user-info">
      <p>Showing ratings for user: <strong>{{ userId }}</strong></p>
    </div>

    <div v-else class="status error">
      <p>No user selected. Open this page from a user profile to view that user's ratings.</p>
    </div>

    <div v-if="loading" class="status">Loading ratings…</div>
    <div v-else-if="error" class="status error">{{ error }}</div>

    <div v-else-if="ratings.length === 0 && userId" class="status">
      <p>No ratings found for this user.</p>
    </div>

    <div v-else-if="ratings.length > 0" class="ratings-list">
      <p class="count">Found <strong>{{ ratings.length }}</strong> rating(s)</p>
      <div v-for="rating in ratings" :key="rating.ratingId" class="rating-card">
        <div class="rating-header">
          <span class="rating-id">Rating ID: {{ rating.ratingId }}</span>
          <span class="created-at">{{ formatDate(rating.createdAt) }}</span>
        </div>
        <div class="rating-row">
          <strong>Reviewer:</strong> {{ rating.reviewerName || rating.reviewerId }}
        </div>
        <div class="rating-row">
          <strong>Reviewee:</strong> {{ rating.revieweeName || rating.revieweeId }}
        </div>
        <div class="rating-row">
          <strong>Activity:</strong> {{ rating.activityId }}
        </div>
        <div class="rating-row">
          <strong>Behavior:</strong> {{ rating.behaviorValue }} / 5
          <span class="label">({{ rating.behaviorLabel }})</span>
        </div>
        <div class="rating-row">
          <strong>Skill:</strong> {{ rating.skillValue }} / 5
          <span class="label">({{ rating.skillLabel }})</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'UserRatings',
  data() {
    return {
      userId: '',
      ratings: [],
      loading: false,
      error: null
    }
  },
  watch: {
    '$route.params.userId': {
      immediate: true,
      handler(value) {
        this.userId = value || this.$route.query.revieweeId || ''
        this.ratings = []
        this.error = null
        if (this.userId) {
          this.fetchRatings()
        }
      }
    }
  },
  methods: {
    fetchRatings() {
      if (!this.userId) {
        this.error = 'No user selected.'
        this.ratings = []
        return
      }

      this.loading = true
      this.error = null
      this.ratings = []

      const query = new URLSearchParams({ revieweeId: this.userId }).toString()
      fetch(`http://localhost:8082/api/ratings?${query}`)
        .then(response => {
          if (!response.ok) {
            throw new Error('Failed to load ratings (HTTP ' + response.status + ')')
          }
          return response.json()
        })
        .then(data => {
          this.ratings = data
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
    }
  }
}
</script>

<style scoped>
.user-ratings {
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

.ratings-list {
  display: grid;
  gap: 16px;
}

.rating-card {
  border: 1px solid #dde2e6;
  border-radius: 10px;
  padding: 16px;
  background: #fff;
}

.rating-header {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 12px;
  font-size: 0.95em;
  color: #555;
}

.rating-row {
  margin-bottom: 8px;
}

.label {
  color: #777;
  margin-left: 6px;
}

.count {
  margin-bottom: 12px;
  color: #555;
}
</style>
