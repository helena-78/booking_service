<template>
  <div class="leave-rating">
    <div class="rating-header">
      <div class="header-accent"></div>
      <h3>Rate Your Experience</h3>
      <p class="subtitle">Your feedback helps build a trustworthy community</p>
    </div>

    <!-- Success state -->
    <div v-if="submitted" class="success-panel">
      <div class="success-icon">✓</div>
      <p class="success-title">Rating Submitted</p>
      <p class="success-sub">{{ revieweeId }} has been rated.</p>
    </div>

    <!-- Already rated -->
    <div v-else-if="alreadyRated" class="info-panel">
      <span class="info-icon">i</span>
      <p>You've already rated this participant for this activity.</p>
    </div>

    <!-- Form -->
    <form v-else @submit.prevent="submitRating" class="rating-form" novalidate>

      <div class="score-section">
        <div class="score-block">
          <label>Behavior</label>
          <div class="stars">
            <button
              v-for="n in 5"
              :key="'b' + n"
              type="button"
              class="star"
              :class="{ active: n <= form.behaviorValue, hovered: n <= behaviorHover }"
              @mouseenter="behaviorHover = n"
              @mouseleave="behaviorHover = 0"
              @click="form.behaviorValue = n"
            >★</button>
          </div>
          <select v-model="form.behaviorLabel" class="label-select" required>
            <option value="" disabled>Select label…</option>
            <option value="FAIR_PLAY">Fair Play</option>
            <option value="RESPECTFUL">Respectful</option>
            <option value="UNSPORTSMANLIKE">Unsportsmanlike</option>
            <option value="AGGRESSIVE">Aggressive</option>
          </select>
        </div>

        <div class="divider-v"></div>

        <div class="score-block">
          <label>Skill</label>
          <div class="stars">
            <button
              v-for="n in 5"
              :key="'s' + n"
              type="button"
              class="star"
              :class="{ active: n <= form.skillValue, hovered: n <= skillHover }"
              @mouseenter="skillHover = n"
              @mouseleave="skillHover = 0"
              @click="form.skillValue = n"
            >★</button>
          </div>
          <select v-model="form.skillLabel" class="label-select" required>
            <option value="" disabled>Select label…</option>
            <option value="BEGINNER">Beginner</option>
            <option value="INTERMEDIATE">Intermediate</option>
            <option value="ADVANCED">Advanced</option>
            <option value="EXPERT">Expert</option>
          </select>
        </div>
      </div>

      <div v-if="validationError" class="error-msg">{{ validationError }}</div>
      <div v-if="serverError" class="error-msg">{{ serverError }}</div>

      <button type="submit" class="submit-btn" :class="{ loading }" :disabled="loading">
        <span v-if="loading" class="spinner"></span>
        <span v-else>Submit Rating</span>
      </button>
    </form>
  </div>
</template>

<script>
export default {
  name: 'LeaveRating',

  props: {
    // All three must be provided by the parent event page
    activityId: {
      type: String,
      required: true
    },
    reviewerId: {
      type: String,
      required: true
    },
    revieweeId: {
      type: String,
      required: true
    }
  },

  data() {
    return {
      form: {
        behaviorValue: 0,
        behaviorLabel: '',
        skillValue: 0,
        skillLabel: ''
      },
      behaviorHover: 0,
      skillHover: 0,
      loading: false,
      submitted: false,
      alreadyRated: false,
      validationError: null,
      serverError: null
    }
  },

  methods: {
    validate() {
      if (this.form.behaviorValue === 0) return 'Please rate behavior.'
      if (!this.form.behaviorLabel) return 'Please select a behavior label.'
      if (this.form.skillValue === 0) return 'Please rate skill.'
      if (!this.form.skillLabel) return 'Please select a skill label.'
      return null
    },

    async submitRating() {
      this.validationError = this.validate()
      if (this.validationError) return

      this.loading = true
      this.serverError = null

      const payload = {
        reviewerId: this.reviewerId,
        revieweeId: this.revieweeId,
        activityId: this.activityId,
        behaviorValue: this.form.behaviorValue,
        behaviorLabel: this.form.behaviorLabel,
        skillValue: this.form.skillValue,
        skillLabel: this.form.skillLabel
      }

      try {
        const res = await fetch('http://localhost:8082/api/ratings', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify(payload)
        })

        if (res.status === 201) {
          this.submitted = true
          this.$emit('rating-submitted')
          return
        }

        const body = await res.json().catch(() => ({}))

        if (res.status === 400 && body.message?.includes('already')) {
          this.alreadyRated = true
          return
        }

        this.serverError = body.message || `Unexpected error (HTTP ${res.status})`
      } catch (err) {
        this.serverError = 'Could not reach the rating service. Please try again.'
        console.error(err)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=DM+Serif+Display&family=DM+Sans:wght@400;500;600&display=swap');

.leave-rating {
  font-family: 'DM Sans', sans-serif;
  background: #0f1117;
  border: 1px solid #1e2330;
  border-radius: 16px;
  padding: 32px;
  max-width: 540px;
  color: #e8eaf0;
}

/* Header */
.rating-header {
  position: relative;
  margin-bottom: 28px;
}

.header-accent {
  width: 36px;
  height: 3px;
  background: #42e693;
  border-radius: 2px;
  margin-bottom: 12px;
}

.rating-header h3 {
  font-family: 'DM Serif Display', serif;
  font-size: 1.5rem;
  font-weight: 400;
  margin: 0 0 4px;
  color: #fff;
  letter-spacing: -0.01em;
}

.subtitle {
  font-size: 0.85rem;
  color: #6b7280;
  margin: 0;
}

/* Score section */
.score-section {
  display: flex;
  gap: 0;
  margin-bottom: 24px;
  background: #161a24;
  border: 1px solid #1e2330;
  border-radius: 12px;
  overflow: hidden;
}

.score-block {
  flex: 1;
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.score-block label {
  font-size: 0.72rem;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.1em;
  color: #6b7280;
}

.divider-v {
  width: 1px;
  background: #1e2330;
  margin: 12px 0;
}

/* Stars */
.stars {
  display: flex;
  gap: 4px;
}

.star {
  background: none;
  border: none;
  font-size: 1.6rem;
  color: #2a2f3d;
  cursor: pointer;
  padding: 0;
  line-height: 1;
  transition: color 0.1s, transform 0.1s;
}

.star.active,
.star.hovered {
  color: #42e693;
}

.star:hover {
  transform: scale(1.15);
}

/* Select */
.label-select {
  background: #0f1117;
  border: 1px solid #1e2330;
  border-radius: 8px;
  color: #e8eaf0;
  font-family: 'DM Sans', sans-serif;
  font-size: 0.82rem;
  padding: 8px 10px;
  width: 100%;
  appearance: none;
  cursor: pointer;
  transition: border-color 0.2s;
}

.label-select:focus {
  outline: none;
  border-color: #42e693;
}

.label-select option {
  background: #161a24;
}

/* Submit */
.submit-btn {
  width: 100%;
  background: #42e693;
  color: #0a0d12;
  border: none;
  border-radius: 10px;
  font-family: 'DM Sans', sans-serif;
  font-size: 0.9rem;
  font-weight: 600;
  padding: 14px;
  cursor: pointer;
  transition: background 0.2s, transform 0.1s;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 48px;
}

.submit-btn:hover:not(:disabled) {
  background: #2fcc7a;
  transform: translateY(-1px);
}

.submit-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

/* Spinner */
.spinner {
  width: 18px;
  height: 18px;
  border: 2px solid rgba(0,0,0,0.2);
  border-top-color: #0a0d12;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

/* Error */
.error-msg {
  font-size: 0.83rem;
  color: #f87171;
  background: rgba(248, 113, 113, 0.08);
  border: 1px solid rgba(248, 113, 113, 0.2);
  border-radius: 8px;
  padding: 10px 14px;
  margin-bottom: 16px;
}

/* Success */
.success-panel {
  text-align: center;
  padding: 24px 0 8px;
}

.success-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(66, 230, 147, 0.12);
  color: #42e693;
  font-size: 1.4rem;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
}

.success-title {
  font-family: 'DM Serif Display', serif;
  font-size: 1.2rem;
  color: #fff;
  margin: 0 0 6px;
}

.success-sub {
  font-size: 0.83rem;
  color: #6b7280;
  margin: 0;
  word-break: break-all;
}

/* Info */
.info-panel {
  display: flex;
  align-items: center;
  gap: 12px;
  background: rgba(99, 179, 237, 0.08);
  border: 1px solid rgba(99, 179, 237, 0.2);
  border-radius: 10px;
  padding: 16px;
  font-size: 0.85rem;
  color: #93c5fd;
}

.info-icon {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  border: 1px solid #93c5fd;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 700;
  flex-shrink: 0;
}
</style>