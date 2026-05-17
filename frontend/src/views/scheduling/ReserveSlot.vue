<template>
  <div class="form">
    <h2>Reserve a Time Slot</h2>

    <label>Start Time (ISO format)</label>
    <input v-model="form.startTime" placeholder="2026-05-20T14:00:00" />

    <label>End Time (ISO format)</label>
    <input v-model="form.endTime" placeholder="2026-05-20T15:30:00" />

    <label>Organizer ID (UUID)</label>
    <input v-model="form.organizerId" placeholder="11111111-1111-1111-1111-111111111111" />

    <label>Reserved For ID (UUID — activity or booking)</label>
    <input v-model="form.reservedForId" placeholder="aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa" />

    <button @click="submit" :disabled="loading">
      {{ loading ? 'Reserving…' : 'Reserve Slot' }}
    </button>

    <div v-if="error" class="error">
      <p><b>Error:</b> {{ error }}</p>
    </div>

    <div v-if="result" class="success">
      <p>✓ Slot reserved!</p>
      <p><small><b>Slot ID:</b> {{ result.slotId }}</small></p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'ReserveSlot',
  data() {
    return {
      form: {
        startTime: '',
        endTime: '',
        organizerId: '',
        reservedForId: ''
      },
      loading: false,
      error: null,
      result: null
    }
  },
  methods: {
    submit() {
      this.error = null
      this.result = null
      this.loading = true

      fetch('http://localhost:8089/api/timeslots/reserve', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(this.form)
      })
        .then(async response => {
          if (!response.ok) {
            const err = await response.json().catch(() => ({}))
            throw new Error(err.message || 'HTTP ' + response.status)
          }
          return response.json()
        })
        .then(data => {
          this.result = data
        })
        .catch(err => {
          this.error = err.message
        })
        .finally(() => {
          this.loading = false
        })
    }
  }
}
</script>

<style scoped>
.form {
  max-width: 500px;
  margin: 0 auto;
  background: #fff;
  padding: 30px;
  border-radius: 10px;
  text-align: left;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
h2 {
  color: #2c3e50;
  text-align: center;
}
label {
  display: block;
  margin: 18px 0 6px;
  font-weight: bold;
  font-size: 0.85em;
  color: #555;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
input {
  display: block;
  width: 100%;
  padding: 10px;
  box-sizing: border-box;
  border: 1px solid #d5dde0;
  border-radius: 6px;
  font-size: 0.95em;
}
input:focus {
  outline: none;
  border-color: #f39c12;
}
button {
  background: #f39c12;
  color: white;
  border: none;
  padding: 12px 24px;
  margin-top: 25px;
  border-radius: 6px;
  font-size: 1em;
  font-weight: bold;
  cursor: pointer;
  width: 100%;
}
button:disabled {
  background: #aaa;
  cursor: not-allowed;
}
.error {
  background: #fadbd8;
  color: #c0392b;
  padding: 10px 15px;
  margin-top: 20px;
  border-radius: 6px;
}
.success {
  background: #d4efdf;
  color: #196f3d;
  padding: 10px 15px;
  margin-top: 20px;
  border-radius: 6px;
}
</style>