<template>
  <div class="detail">
    <router-link to="/activities" class="back">&larr; Back to all activities</router-link>

    <div v-if="loading" class="loading">Loading activity...</div>

    <div v-else-if="error && !activity" class="error">
      <p>{{ error }}</p>
      <button @click="fetchActivity">Retry</button>
    </div>

    <div v-else-if="activity">
      <h2>{{ activity.title }}</h2>

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

      <!-- ============ ORGANIZER ============ -->
      <section class="organizer-card">
        <h3>Organizer</h3>
        <div v-if="loadingOrganizer" class="muted">Loading organizer profile...</div>
        <div v-else-if="organizer">
          <div class="profile-row">
            <span class="profile-label">Name:</span>
            <span class="profile-value">{{ organizer.name || '—' }}</span>
          </div>
          <div class="profile-row">
            <span class="profile-label">Email:</span>
            <span class="profile-value">{{ organizer.email || organizer.contactInfo?.email || '—' }}</span>
          </div>
          <div class="profile-row" v-if="organizer.phone || organizer.contactInfo?.phone">
            <span class="profile-label">Phone:</span>
            <span class="profile-value">{{ organizer.phone || organizer.contactInfo?.phone }}</span>
          </div>
          <div class="profile-row" v-if="organizer.city">
            <span class="profile-label">City:</span>
            <span class="profile-value">{{ organizer.city }}</span>
          </div>
          <div class="profile-row" v-if="organizer.role">
            <span class="profile-label">Role:</span>
            <span class="profile-value">{{ organizer.role }}</span>
          </div>
        </div>
        <div v-else class="muted">
          Could not load organizer profile (Account Management Service unavailable).
        </div>
      </section>

      <!-- ============ EDIT ============ -->
      <section v-if="activity.status === 'PLANNED' || activity.status === 'ACTIVE'">
        <h3>Edit Activity</h3>
        <label>Title</label>
        <input v-model="editForm.title" />

        <label>Description</label>
        <input v-model="editForm.description" />

        <label>Max Participants</label>
        <input v-model.number="editForm.maxParticipants" type="number" min="1" />

        <label>Preferred Time Slot ID</label>
        <input v-model="editForm.preferredTimeSlotId" />

        <button @click="updateActivity" :disabled="actionLoading">
          {{ actionLoading ? 'Saving…' : 'Save Changes' }}
        </button>
      </section>

      <!-- ============ PARTICIPANTS ============ -->
      <section>
        <h3>Participants ({{ activity.participants.length }})</h3>
        <div v-if="activity.participants.length === 0">No participants yet.</div>
        <ul v-else class="participants-list">
          <li v-for="p in activity.participants" :key="p.participantId">
            <span class="role-badge" :class="'role-' + p.role.toLowerCase()">
              {{ p.role }}
            </span>
            <div class="participant-info">
              <div class="participant-name">
                {{ profileFor(p.userId)?.name || 'Loading…' }}
              </div>
              <div class="participant-email">
                {{ profileFor(p.userId)?.email
                   || profileFor(p.userId)?.contactInfo?.email
                   || '' }}
              </div>
            </div>
            <small class="joined-at">joined {{ formatDate(p.joinedAt) }}</small>
            <button
              v-if="p.role !== 'ORGANIZER'"
              class="btn-danger small"
              @click="removeParticipant(p.userId)">
              Remove
            </button>
          </li>
        </ul>
      </section>

      <!-- ============ JOIN/LEAVE ============ -->
      <section v-if="activity.status === 'PLANNED' || activity.status === 'ACTIVE'">
        <h3>Join / Leave</h3>
        <label>User ID</label>
        <input v-model="participantUserId" placeholder="UUID of user" />
        <div class="actions">
          <button class="btn-primary" @click="joinActivity" :disabled="actionLoading">
            Join as this user
          </button>
          <button class="btn-secondary" @click="leaveActivity" :disabled="actionLoading">
            Leave as this user
          </button>
        </div>
      </section>

      <!-- ============ CANCEL ============ -->
      <section v-if="activity.status !== 'CANCELLED' && activity.status !== 'COMPLETED'" class="danger-zone">
        <h3>Danger Zone</h3>
        <button class="btn-danger" @click="cancelActivity" :disabled="actionLoading">
          {{ actionLoading ? 'Cancelling…' : 'Cancel This Activity' }}
        </button>
      </section>

      <!-- ============ MESSAGES ============ -->
      <div v-if="error" class="error msg">
        <p><b>Error:</b> {{ error }}</p>
      </div>
      <div v-if="success" class="success msg">
        <p>✓ {{ success }}</p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ActivityDetail',
  data() {
    return {
      activityId: this.$route.params.id,
      activity: null,
      organizer: null,
      profiles: {},          // cache: userId -> profile
      loading: false,
      loadingOrganizer: false,
      actionLoading: false,
      error: null,
      success: null,
      participantUserId: '',
      editForm: {
        title: '',
        description: '',
        maxParticipants: 0,
        preferredTimeSlotId: ''
      }
    }
  },
  methods: {
    fetchActivity() {
      this.loading = true
      this.error = null
      fetch(`http://localhost:8083/api/activities/${this.activityId}`)
        .then(async r => {
          if (!r.ok) {
            const err = await r.json().catch(() => ({}))
            throw new Error(err.message || `HTTP ${r.status}`)
          }
          return r.json()
        })
        .then(data => {
          this.activity = data
          this.editForm.title = data.title || ''
          this.editForm.description = data.description || ''
          this.editForm.maxParticipants = data.maxParticipants || 1
          this.editForm.preferredTimeSlotId = data.preferredTimeSlotId || ''

          // Fetch profiles in parallel
          this.fetchOrganizer(data.organizerId)
          for (const p of data.participants) {
            this.fetchProfile(p.userId)
          }
        })
        .catch(err => {
          this.error = err.message
        })
        .finally(() => {
          this.loading = false
        })
    },

    fetchOrganizer(userId) {
      this.loadingOrganizer = true
      this.fetchProfile(userId)
        .then(profile => { this.organizer = profile })
        .finally(() => { this.loadingOrganizer = false })
    },

    // Cached fetch — never call Account Management twice for the same user
    fetchProfile(userId) {
      if (this.profiles[userId]) return Promise.resolve(this.profiles[userId])
      return fetch(`http://localhost:8081/api/users/${userId}`)
        .then(r => r.ok ? r.json() : null)
        .then(profile => {
          if (profile) {
            this.profiles = { ...this.profiles, [userId]: profile }
          }
          return profile
        })
        .catch(() => null)
    },

    profileFor(userId) {
      return this.profiles[userId]
    },

    updateActivity() {
      this.actionLoading = true
      this.error = null
      this.success = null
      const body = {
        title: this.editForm.title,
        description: this.editForm.description,
        maxParticipants: this.editForm.maxParticipants
      }
      if (this.editForm.preferredTimeSlotId) {
        body.preferredTimeSlotId = this.editForm.preferredTimeSlotId
      }
      fetch(`http://localhost:8083/api/activities/${this.activityId}`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
      })
        .then(async r => {
          if (!r.ok) {
            const err = await r.json().catch(() => ({}))
            throw new Error(err.message || `HTTP ${r.status}`)
          }
          return r.json()
        })
        .then(() => {
          this.success = 'Activity updated.'
          this.fetchActivity()
        })
        .catch(err => { this.error = err.message })
        .finally(() => { this.actionLoading = false })
    },

    cancelActivity() {
      if (!confirm('Are you sure you want to cancel this activity?')) return
      this.actionLoading = true
      this.error = null
      this.success = null
      fetch(`http://localhost:8083/api/activities/${this.activityId}`, {
        method: 'DELETE'
      })
        .then(async r => {
          if (!r.ok && r.status !== 204) {
            const err = await r.json().catch(() => ({}))
            throw new Error(err.message || `HTTP ${r.status}`)
          }
        })
        .then(() => {
          this.success = 'Activity cancelled. Reserved slot will be released via Kafka.'
          this.fetchActivity()
        })
        .catch(err => { this.error = err.message })
        .finally(() => { this.actionLoading = false })
    },

    joinActivity() {
      if (!this.participantUserId) {
        this.error = 'Please enter a user ID first.'
        return
      }
      this.actionLoading = true
      this.error = null
      this.success = null
      fetch(`http://localhost:8083/api/activities/${this.activityId}/join`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId: this.participantUserId })
      })
        .then(async r => {
          if (!r.ok) {
            const err = await r.json().catch(() => ({}))
            throw new Error(err.message || `HTTP ${r.status}`)
          }
          return r.json()
        })
        .then(() => {
          this.success = `User joined.`
          this.participantUserId = ''
          this.fetchActivity()
        })
        .catch(err => { this.error = err.message })
        .finally(() => { this.actionLoading = false })
    },

    leaveActivity() {
      if (!this.participantUserId) {
        this.error = 'Please enter a user ID first.'
        return
      }
      this.actionLoading = true
      this.error = null
      this.success = null
      fetch(`http://localhost:8083/api/activities/${this.activityId}/leave`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ userId: this.participantUserId })
      })
        .then(async r => {
          if (!r.ok && r.status !== 204) {
            const err = await r.json().catch(() => ({}))
            throw new Error(err.message || `HTTP ${r.status}`)
          }
        })
        .then(() => {
          this.success = `User left.`
          this.participantUserId = ''
          this.fetchActivity()
        })
        .catch(err => { this.error = err.message })
        .finally(() => { this.actionLoading = false })
    },

    removeParticipant(userId) {
      const name = this.profileFor(userId)?.name || userId
      if (!confirm(`Remove participant ${name}?`)) return
      this.actionLoading = true
      this.error = null
      this.success = null
      fetch(`http://localhost:8083/api/activities/${this.activityId}/participants/${userId}`, {
        method: 'DELETE'
      })
        .then(async r => {
          if (!r.ok && r.status !== 204) {
            const err = await r.json().catch(() => ({}))
            throw new Error(err.message || `HTTP ${r.status}`)
          }
        })
        .then(() => {
          this.success = `Participant removed.`
          this.fetchActivity()
        })
        .catch(err => { this.error = err.message })
        .finally(() => { this.actionLoading = false })
    },

    formatDate(iso) {
      if (!iso) return ''
      return new Date(iso).toLocaleString('en-GB', {
        day: '2-digit', month: 'short', hour: '2-digit', minute: '2-digit'
      })
    }
  },
  mounted() {
    this.fetchActivity()
  }
}
</script>

<style scoped>
.detail {
  text-align: left;
  max-width: 700px;
  margin: 0 auto;
}
.back {
  display: inline-block;
  margin-bottom: 20px;
  color: #42b983;
  text-decoration: none;
}
h2 { color: #2c3e50; }
.meta {
  display: flex;
  gap: 12px;
  margin-bottom: 15px;
  flex-wrap: wrap;
}
.badge, .role-badge {
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
.role-organizer { background: #e67e22; }
.role-player    { background: #16a085; }
.description {
  font-style: italic;
  color: #555;
  margin-bottom: 15px;
}
section {
  background: #f9f9f9;
  padding: 20px;
  border-radius: 10px;
  margin-top: 20px;
}
section.organizer-card {
  background: #ecf3ee;
  border-left: 4px solid #42b983;
}
section.danger-zone {
  background: #fef0ef;
  border-left: 4px solid #e74c3c;
}
h3 {
  margin-top: 0;
  color: #2c3e50;
}
.profile-row {
  display: flex;
  gap: 10px;
  padding: 4px 0;
}
.profile-label {
  font-weight: bold;
  color: #555;
  min-width: 70px;
}
.profile-value { color: #2c3e50; }
.muted { color: #999; font-style: italic; }
label {
  display: block;
  margin: 12px 0 4px;
  font-weight: bold;
  font-size: 0.8em;
  text-transform: uppercase;
  color: #555;
}
input {
  width: 100%;
  padding: 8px;
  box-sizing: border-box;
  border: 1px solid #d5dde0;
  border-radius: 6px;
}
.actions {
  display: flex;
  gap: 10px;
  margin-top: 12px;
}
button {
  background: #42b983;
  color: white;
  border: none;
  padding: 10px 18px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  margin-top: 12px;
}
button:disabled { background: #aaa; cursor: not-allowed; }
.btn-primary { background: #42b983; }
.btn-secondary { background: #95a5a6; }
.btn-danger { background: #e74c3c; }
button.small { padding: 4px 10px; font-size: 0.8em; margin-top: 0; }
.participants-list {
  list-style: none;
  padding: 0;
}
.participants-list li {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid #e0e0e0;
}
.participants-list li:last-child { border-bottom: none; }
.participant-info { flex: 1; }
.participant-name { font-weight: bold; color: #2c3e50; }
.participant-email { font-size: 0.85em; color: #777; }
.joined-at { color: #999; font-size: 0.8em; }
.msg { padding: 10px 15px; margin-top: 20px; border-radius: 6px; }
.error { background: #fadbd8; color: #c0392b; }
.success { background: #d4efdf; color: #196f3d; }
.loading { text-align: center; padding: 30px; }
</style>