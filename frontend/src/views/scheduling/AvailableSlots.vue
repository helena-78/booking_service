<template>
  <div class="slots">
    <h2>Available Time Slots</h2>

    <div v-if="loading" class="loading">Loading…</div>

    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="fetchSlots">Retry</button>
    </div>

    <div v-else-if="slots.length === 0" class="empty">
      <p>No available slots right now.</p>
      <p>
        <router-link to="/slots/reserve">Reserve a new slot</router-link>
      </p>
    </div>

    <div v-else>
      <p class="count">{{ slots.length }} slot(s) available</p>
      <div class="item" v-for="slot in slots" :key="slot.slotId">
        <div class="time">
          🕒 <b>{{ formatTime(slot.startTime) }}</b>
          → <b>{{ formatTime(slot.endTime) }}</b>
        </div>
        <div class="badge">{{ slot.status }}</div>
        <p class="ids">
          <small>
            <b>Slot ID:</b> {{ slot.slotId }}<br />
            <b>Organizer:</b> {{ slot.organizerId || '—' }}
          </small>
        </p>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AvailableSlots',
  data() {
    return {
      slots: [],
      loading: false,
      error: null
    }
  },
  methods: {
    fetchSlots() {
      this.loading = true
      this.error = null
      fetch('http://localhost:8089/api/timeslots/available')
        .then(response => {
          if (!response.ok) {
            throw new Error('Failed to fetch slots (HTTP ' + response.status + ')')
          }
          return response.json()
        })
        .then(data => {
          this.slots = data
        })
        .catch(err => {
          this.error = err.message
          console.error(err)
        })
        .finally(() => {
          this.loading = false
        })
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
.count {
  text-align: center;
  color: #555;
}
.item {
  background: #fff7e6;
  padding: 12px 18px;
  margin-bottom: 10px;
  border-radius: 8px;
  border-left: 4px solid #f39c12;
}
.time {
  font-size: 1.05em;
  margin-bottom: 6px;
}
.badge {
  display: inline-block;
  background: #27ae60;
  color: white;
  padding: 2px 10px;
  border-radius: 12px;
  font-size: 0.8em;
  font-weight: bold;
  margin-bottom: 6px;
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