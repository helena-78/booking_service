<template>
  <div class="user-ratings">
    <h2>Admin Ratings</h2>

    <form @submit.prevent="fetchRatings" class="search-form">
      <label for="reviewerId">Reviewer ID</label>
      <input
        id="reviewerId"
        v-model="reviewerId"
        placeholder="Reviewer UUID"
        type="text"
      />

      <label for="revieweeId">Reviewee ID</label>
      <input
        id="revieweeId"
        v-model="revieweeId"
        placeholder="Reviewee UUID"
        type="text"
      />

      <label for="activityId">Activity ID</label>
      <input
        id="activityId"
        v-model="activityId"
        placeholder="Activity UUID"
        type="text"
      />

      <button type="submit">Search Ratings</button>
    </form>

    <div v-if="loading" class="status">Loading ratings…</div>
    <div v-else-if="error" class="status error">{{ error }}</div>

    <div v-else-if="ratings.length === 0" class="status">
      <p>No ratings found for the selected filters.</p>
    </div>

    <div v-else class="ratings-list">
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
        <div class="rating-actions">
          <button type="button" class="delete-button" @click="confirmDelete(rating.ratingId)">Delete</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AdminRatings',
  data() {
    return {
      reviewerId: '',
      revieweeId: '',
      activityId: '',
      ratings: [],
      loading: false,
      error: null
    }
  },
  methods: {
    fetchRatings() {
      this.loading = true
      this.error = null
      this.ratings = []

      const params = {}
      if (this.reviewerId) params.reviewerId = this.reviewerId
      if (this.revieweeId) params.revieweeId = this.revieweeId
      if (this.activityId) params.activityId = this.activityId

      const query = new URLSearchParams(params).toString()
      fetch(`http://localhost:8082/api/ratings${query ? `?${query}` : ''}`)
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
    confirmDelete(ratingId) {
      if (!window.confirm('Delete rating ' + ratingId + '?')) {
        return
      }
      this.deleteRating(ratingId)
    },
    deleteRating(ratingId) {
      this.loading = true
      this.error = null

      fetch(`http://localhost:8082/api/ratings/${ratingId}`, {
        method: 'DELETE'
      })
        .then(response => {
          if (!response.ok) {
            throw new Error('Failed to delete rating (HTTP ' + response.status + ')')
          }
          return response.text()
        })
        .then(() => {
          this.fetchRatings()
        })
        .catch(err => {
          this.error = err.message
          console.error(err)
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

.rating-actions {
  margin-top: 16px;
}

.delete-button {
  background: #e74c3c;
  color: white;
  border: none;
  padding: 8px 14px;
  border-radius: 6px;
  cursor: pointer;
}

.delete-button:hover {
  background: #c0392b;
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
