<template>
  <div class="create-booking">
    <h2>Create a Booking</h2>
    <p class="subtitle">POST <code>/api/bookings</code> on the Booking Service</p>

    <form class="form" @submit.prevent="submit">
      <label>
        Sport Center ID
        <input v-model="form.sportCenterId" type="text" required placeholder="e.g. sc-100" />
      </label>
      <label>
        Activity ID
        <input v-model="form.activityId" type="text" required placeholder="e.g. act-500" />
      </label>
      <label>
        Time Slot ID
        <input v-model="form.timeSlotId" type="text" required placeholder="e.g. ts-900" />
        <small class="hint">slot is reserved via Scheduling Service synchronously</small>
      </label>

      <fieldset>
        <legend>Payment</legend>
        <label>
          Amount
          <input v-model.number="form.payment.amount" type="number" min="0" step="0.01" placeholder="60.00" />
        </label>
        <label>
          Currency
          <input v-model="form.payment.currency" type="text" placeholder="EUR" maxlength="3" />
        </label>
        <label>
          Payment method
          <select v-model="form.payment.paymentMethod">
            <option value="">(none)</option>
            <option value="CARD">CARD</option>
            <option value="PAYPAL">PAYPAL</option>
            <option value="CASH">CASH</option>
          </select>
        </label>
      </fieldset>

      <button type="submit" :disabled="loading">
        {{ loading ? 'Creating…' : 'Create Booking' }}
      </button>
    </form>

    <div v-if="error" class="error">
      <p><b>Error:</b> {{ error }}</p>
      <p v-if="errorHint" class="hint-text">{{ errorHint }}</p>
    </div>

    <div v-if="result" class="success">
      <p>✓ Booking created</p>
      <pre>{{ JSON.stringify(result, null, 2) }}</pre>
      <p>
        <router-link to="/bookings">→ View all bookings</router-link>
      </p>
    </div>
  </div>
</template>

<script>
const BOOKING_BASE = 'http://localhost:8088'

export default {
  name: 'CreateBooking',
  data() {
    return {
      form: {
        sportCenterId: '',
        activityId: '',
        timeSlotId: '',
        payment: {
          amount: null,
          currency: 'EUR',
          paymentMethod: 'CARD'
        }
      },
      loading: false,
      error: null,
      errorHint: null,
      result: null
    }
  },
  methods: {
    submit() {
      this.error = null
      this.errorHint = null
      this.result = null
      this.loading = true

      // Build payload: drop the whole payment object if completely empty
      const p = this.form.payment
      const hasPayment = p.amount || p.currency || p.paymentMethod
      const payload = {
        sportCenterId: this.form.sportCenterId.trim(),
        activityId: this.form.activityId.trim(),
        timeSlotId: this.form.timeSlotId.trim()
      }
      if (hasPayment) payload.payment = { ...p }

      fetch(BOOKING_BASE + '/api/bookings', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      })
        .then(async response => {
          if (!response.ok) {
            let msg
            try {
              const body = await response.json()
              msg = body.message || body.error || ('HTTP ' + response.status)
            } catch (e) {
              msg = 'HTTP ' + response.status + ' ' + response.statusText
            }
            if (response.status === 503) {
              this.errorHint = 'The Scheduling Service is unavailable. Booking creation '
                + 'needs it up at http://localhost:8089 to reserve the time slot.'
            }
            throw new Error(msg)
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
.create-booking { text-align: left; max-width: 600px; margin: 0 auto; }
h2 { color: #2c3e50; text-align: center; }
.subtitle {
  text-align: center;
  color: #777;
  font-size: 0.9em;
  margin-top: -8px;
}
.subtitle code {
  background: #eaf3ff;
  padding: 1px 6px;
  border-radius: 4px;
  font-family: monospace;
}
.form {
  background: #fff;
  padding: 25px 30px;
  border-radius: 10px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.08);
}
label {
  display: block;
  margin: 12px 0 6px;
  font-weight: bold;
  font-size: 0.85em;
  color: #555;
  text-transform: uppercase;
  letter-spacing: 0.4px;
}
input, select {
  display: block;
  width: 100%;
  padding: 9px 10px;
  border: 1px solid #d5dde0;
  border-radius: 6px;
  box-sizing: border-box;
  font-size: 0.95em;
}
input:focus, select:focus {
  outline: none;
  border-color: #42b983;
}
fieldset {
  margin-top: 14px;
  border: 1px solid #e3e7ea;
  border-radius: 8px;
  padding: 10px 15px 15px;
}
fieldset legend {
  font-weight: bold;
  color: #2c3e50;
  padding: 0 6px;
}
.hint, .hint-text {
  font-weight: normal;
  font-size: 0.78em;
  color: #d68910;
  text-transform: none;
  letter-spacing: 0;
  margin-top: 3px;
}
button {
  background: #42b983;
  color: white;
  border: none;
  padding: 12px 24px;
  margin-top: 20px;
  border-radius: 6px;
  font-size: 1em;
  font-weight: bold;
  cursor: pointer;
  width: 100%;
}
button:disabled { opacity: 0.5; cursor: not-allowed; }

.error {
  background: #fadbd8;
  color: #c0392b;
  padding: 12px 15px;
  margin-top: 20px;
  border-radius: 6px;
}
.success {
  background: #d4efdf;
  color: #196f3d;
  padding: 12px 15px;
  margin-top: 20px;
  border-radius: 6px;
}
.success pre {
  background: #fff;
  padding: 10px;
  border-radius: 6px;
  overflow-x: auto;
  font-size: 0.85em;
  color: #2c3e50;
}
</style>
