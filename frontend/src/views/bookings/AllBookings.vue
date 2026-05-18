<template>
  <div class="bookings">
    <h2>Bookings</h2>
    <form class="filters" @submit.prevent="fetchBookings">
      <div class="row">
        <label>
          Activity ID
          <input v-model="filters.activityId" type="text" placeholder="e.g. act-200" />
        </label>
        <label>
          Sport Center ID
          <input v-model="filters.sportCenterId" type="text" placeholder="e.g. sc-100" />
        </label>
        <label>
          Booking status
          <select v-model="filters.bookingStatus">
            <option value="">(any)</option>
            <option value="PENDING">PENDING</option>
            <option value="CONFIRMED">CONFIRMED</option>
            <option value="CANCELLED">CANCELLED</option>
          </select>
        </label>
        <label>
          Booking ID (lookup)
          <input v-model="lookupId" type="text" placeholder="e.g. bk-001" />
          <small class="hint">fetches GET /api/bookings/{id}</small>
        </label>
      </div>
      <div class="actions">
        <button type="submit" :disabled="loading">
          {{ loading ? 'Loading…' : 'Search' }}
        </button>
        <button type="button" class="secondary" @click="lookupById" :disabled="!lookupId || loading">
          Lookup by ID
        </button>
        <button type="button" class="secondary" @click="clearFilters">Clear</button>
        <router-link class="primary-link" to="/bookings/new">+ New Booking</router-link>
      </div>
    </form>

    <div v-if="lastUrl" class="last-url">
      <small><b>{{ lastMethod }}</b> <code>{{ lastUrl }}</code></small>
    </div>

    <div v-if="loading" class="loading">Loading…</div>

    <div v-else-if="error" class="error">
      <p><b>Error:</b> {{ error }}</p>
      <button @click="fetchBookings">Retry</button>
    </div>

    <div v-else-if="bookings.length === 0" class="empty">
      <p>No bookings match the filters.</p>
    </div>

    <div v-else>
      <p class="count">Found <b>{{ bookings.length }}</b> booking(s)</p>
      <div class="item" v-for="b in bookings" :key="b.bookingId">
        <div class="item-head">
          <h3>Booking {{ b.bookingId }}</h3>
          <span class="badge" :class="'status-' + b.bookingStatus.toLowerCase()">
            {{ b.bookingStatus }}
          </span>
        </div>

        <div class="meta">
          <span>📍 Center: <b>{{ b.sportCenterId }}</b></span>
          <span>🏃 Activity: <b>{{ b.activityId }}</b></span>
          <span>🕒 Slot: <b>{{ b.timeSlotId }}</b></span>
          <span v-if="b.createdAt" class="created">
            Created {{ formatTime(b.createdAt) }}
          </span>
        </div>

        <div v-if="b.payment" class="payment">
          <b>Payment</b> &mdash;
          <span class="badge-small" :class="'pay-' + (b.payment.paymentStatus || '').toLowerCase()">
            {{ b.payment.paymentStatus }}
          </span>
          <span v-if="b.payment.money">
            {{ b.payment.money.amount }} {{ b.payment.money.currency }}
          </span>
          <span v-if="b.payment.paymentMethod">
            via {{ b.payment.paymentMethod }}
          </span>
          <span v-if="b.payment.paidAt"> &middot; paid {{ formatTime(b.payment.paidAt) }}</span>
          <span v-if="b.payment.refundedAt"> &middot; refunded {{ formatTime(b.payment.refundedAt) }}</span>
        </div>

        <!-- Per-booking action buttons -->
        <div class="row-actions">
          <button
            class="action"
            :disabled="b.bookingStatus !== 'PENDING' || busy[b.bookingId]"
            @click="confirmBooking(b)">
            Confirm
          </button>
          <button
            class="action danger"
            :disabled="b.bookingStatus === 'CANCELLED' || busy[b.bookingId]"
            @click="cancelBooking(b)">
            Cancel
          </button>
          <button
            class="action warn"
            :disabled="!canRefund(b) || busy[b.bookingId]"
            @click="refundBooking(b)">
            Refund
          </button>
        </div>

        <div v-if="actionMessages[b.bookingId]" class="action-msg"
             :class="{ ok: actionStatus[b.bookingId] === 'ok', err: actionStatus[b.bookingId] === 'err' }">
          {{ actionMessages[b.bookingId] }}
        </div>
      </div>
    </div>
  </div>
</template>

<script>
const BOOKING_BASE = 'http://localhost:8088'

export default {
  name: 'AllBookings',
  data() {
    return {
      filters: {
        activityId: '',
        sportCenterId: '',
        bookingStatus: ''
      },
      lookupId: '',
      bookings: [],
      loading: false,
      error: null,
      lastUrl: null,
      lastMethod: 'GET',
      busy: {},
      actionMessages: {},
      actionStatus: {}
    }
  },
  methods: {
    // ----- GET /api/bookings (+ optional filters) -----
    fetchBookings() {
      this.error = null
      this.loading = true

      const params = []
      const f = this.filters
      if (f.activityId && f.activityId.trim())
        params.push('activityId=' + encodeURIComponent(f.activityId.trim()))
      if (f.sportCenterId && f.sportCenterId.trim())
        params.push('sportCenterId=' + encodeURIComponent(f.sportCenterId.trim()))
      if (f.bookingStatus)
        params.push('bookingStatus=' + encodeURIComponent(f.bookingStatus))

      const url = BOOKING_BASE + '/api/bookings' + (params.length ? '?' + params.join('&') : '')
      this.lastUrl = url
      this.lastMethod = 'GET'

      fetch(url)
        .then(this._handleResponse)
        .then(data => {
          this.bookings = Array.isArray(data) ? data : [data]
        })
        .catch(err => {
          this.error = err.message
          console.error(err)
        })
        .finally(() => {
          this.loading = false
        })
    },

    // ----- GET /api/bookings/{id} -----
    lookupById() {
      if (!this.lookupId || !this.lookupId.trim()) return
      this.error = null
      this.loading = true

      const url = BOOKING_BASE + '/api/bookings/' + encodeURIComponent(this.lookupId.trim())
      this.lastUrl = url
      this.lastMethod = 'GET'

      fetch(url)
        .then(this._handleResponse)
        .then(data => {
          this.bookings = [data]
        })
        .catch(err => {
          this.bookings = []
          this.error = err.message
        })
        .finally(() => {
          this.loading = false
        })
    },

    // ----- PUT /api/bookings/{id}/confirm -----
    confirmBooking(b) {
      this._doAction(b, 'PUT', '/api/bookings/' + b.bookingId + '/confirm', 'Confirmed')
    },

    // ----- PUT /api/bookings/{id}/cancel -----
    cancelBooking(b) {
      this._doAction(b, 'PUT', '/api/bookings/' + b.bookingId + '/cancel', 'Cancelled')
    },

    // ----- POST /api/bookings/{id}/refund -----
    refundBooking(b) {
      this._doAction(b, 'POST', '/api/bookings/' + b.bookingId + '/refund', 'Refunded')
    },

    _doAction(b, method, path, successLabel) {
      this.busy = { ...this.busy, [b.bookingId]: true }
      this.actionMessages = { ...this.actionMessages, [b.bookingId]: '' }
      this.actionStatus = { ...this.actionStatus, [b.bookingId]: '' }

      const url = BOOKING_BASE + path
      this.lastUrl = url
      this.lastMethod = method

      fetch(url, { method })
        .then(this._handleResponse)
        .then(data => {
          // Replace booking in place with server response
          const idx = this.bookings.findIndex(x => x.bookingId === b.bookingId)
          if (idx !== -1) this.bookings.splice(idx, 1, data)
          this.actionMessages = { ...this.actionMessages, [b.bookingId]: '✓ ' + successLabel }
          this.actionStatus = { ...this.actionStatus, [b.bookingId]: 'ok' }
        })
        .catch(err => {
          this.actionMessages = { ...this.actionMessages, [b.bookingId]: '✗ ' + err.message }
          this.actionStatus = { ...this.actionStatus, [b.bookingId]: 'err' }
        })
        .finally(() => {
          this.busy = { ...this.busy, [b.bookingId]: false }
        })
    },

    // ----- helpers -----
    canRefund(b) {
      return b.bookingStatus === 'CANCELLED'
          && b.payment
          && b.payment.paymentStatus === 'PAID'
    },
    clearFilters() {
      this.filters.activityId = ''
      this.filters.sportCenterId = ''
      this.filters.bookingStatus = ''
      this.lookupId = ''
      this.fetchBookings()
    },
    formatTime(iso) {
      if (!iso) return ''
      const d = new Date(iso)
      return d.toLocaleString('en-GB', {
        day: '2-digit', month: 'short',
        hour: '2-digit', minute: '2-digit'
      })
    },
    async _handleResponse(response) {
      if (!response.ok) {
        let msg
        try {
          const body = await response.json()
          msg = body.message || body.error || ('HTTP ' + response.status)
        } catch (e) {
          msg = 'HTTP ' + response.status + ' ' + response.statusText
        }
        throw new Error(msg)
      }
      return response.json()
    }
  },
  mounted() {
    this.fetchBookings()
  }
}
</script>

<style scoped>
.bookings { text-align: left; }
.filters {
  background: #f4f4f4;
  padding: 15px 20px;
  border-radius: 10px;
  margin-bottom: 16px;
}
.row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 12px;
  margin-bottom: 12px;
}
.row label {
  display: flex;
  flex-direction: column;
  font-size: 0.9em;
  color: #2c3e50;
}
.row input, .row select {
  margin-top: 4px;
  padding: 6px 8px;
  border: 1px solid #ccc;
  border-radius: 6px;
  font-size: 0.95em;
}
.hint {
  color: #d68910;
  font-size: 0.8em;
  margin-top: 3px;
}
.actions {
  display: flex;
  gap: 8px;
  align-items: center;
  flex-wrap: wrap;
}
button {
  background: #42b983;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.95em;
}
button:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
button.secondary {
  background: white;
  color: #2c3e50;
  border: 1px solid #ccc;
}
.primary-link {
  background: #2c3e50;
  color: white;
  padding: 8px 16px;
  border-radius: 6px;
  text-decoration: none;
  font-size: 0.95em;
}
.last-url {
  background: #eaf3ff;
  border-left: 4px solid #3498db;
  padding: 6px 12px;
  border-radius: 0 6px 6px 0;
  margin-bottom: 10px;
  word-break: break-all;
}
.last-url code { font-family: monospace; font-size: 0.85em; }
.count { text-align: center; color: #555; }

.item {
  background: #ecf3ee;
  padding: 15px 20px;
  margin-bottom: 12px;
  border-radius: 10px;
  border-left: 4px solid #42b983;
}
.item-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}
.item-head h3 { margin: 0; color: #2c3e50; }
.badge {
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 0.8em;
  font-weight: bold;
  color: white;
}
.status-pending   { background: #f39c12; }
.status-confirmed { background: #27ae60; }
.status-cancelled { background: #e74c3c; }

.meta {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  font-size: 0.9em;
  color: #34495e;
  margin-bottom: 8px;
}
.created { color: #777; }

.payment {
  background: #fff;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 0.9em;
  margin-bottom: 10px;
}
.badge-small {
  padding: 2px 8px;
  border-radius: 10px;
  color: white;
  font-size: 0.75em;
  font-weight: bold;
}
.pay-pending  { background: #f39c12; }
.pay-paid     { background: #27ae60; }
.pay-refunded { background: #7f8c8d; }

.row-actions {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
.action {
  background: #3498db;
  padding: 6px 14px;
  font-size: 0.9em;
}
.action.danger { background: #e74c3c; }
.action.warn   { background: #d35400; }

.action-msg {
  margin-top: 8px;
  padding: 6px 10px;
  border-radius: 6px;
  font-size: 0.9em;
}
.action-msg.ok  { background: #d4efdf; color: #196f3d; }
.action-msg.err { background: #fadbd8; color: #c0392b; }

.loading, .empty, .error { text-align: center; padding: 30px; }
.error { color: #c0392b; }
</style>
