<template>
  <div class="form">
    <h2>Sport Center</h2>

    <label>Center ID (UUID)</label>
    <div class="row">
      <input v-model="centerId" placeholder="Enter facility UUID" />
      <button
        v-if="canUseMyId"
        @click="useMyId"
        type="button"
        class="ghost"
        title="Fill with the facility ID you're signed in as"
      >Use mine</button>
    </div>

    <div class="actions">
      <button @click="fetchFacility" :disabled="loading || !centerId" class="secondary">
        <Spinner v-if="loading && !facility" :size="14" inline />
        <span>Load facility</span>
      </button>
    </div>

    <div v-if="loadError" class="error"><p><b>Error:</b> {{ loadError }}</p></div>

    <template v-if="facility">
      <hr />
      <p class="readonly">
        <small>
          <b>Name:</b> {{ facility.name }}<br />
          <b>Email:</b> {{ facility.email }}<br />
          <b>Status:</b> {{ facility.status }}<br />
          <b>Sport types:</b> {{ facility.sportTypes }}
        </small>
      </p>

      <h3>Update fields</h3>

      <label>Name</label>
      <input v-model="updates.name" />

      <label>Sport types (comma separated)</label>
      <input v-model="updates.sportTypes" />

      <label>Contact name</label>
      <input v-model="updates.contactName" />

      <label>Contact phone</label>
      <input v-model="updates.contactPhone" />

      <label>Website</label>
      <input v-model="updates.website" />

      <label>City</label>
      <input v-model="updates.city" />

      <label>District</label>
      <input v-model="updates.district" />

      <label>Coordinates</label>
      <input v-model="updates.coordinates" />

      <button @click="saveFacility" :disabled="loading">
        <Spinner v-if="loading && facility" :size="14" inline />
        <span>{{ loading ? 'Saving…' : 'Save changes' }}</span>
      </button>

      <div v-if="saveError" class="error"><p><b>Error:</b> {{ saveError }}</p></div>
    </template>
  </div>
</template>

<script>
import { accountApi } from '../../api/client'
import { authState } from '../../store/authStore'
import { toast } from '../../store/toastStore'
import Spinner from '../../components/Spinner.vue'

export default {
  name: 'SportCenterView',
  components: { Spinner },
  data() {
    return {
      centerId: '', facility: null,
      updates: {
        name: '', sportTypes: '', contactName: '', contactPhone: '',
        website: '', city: '', district: '', coordinates: ''
      },
      loading: false, loadError: null, saveError: null,
      authState
    }
  },
  computed: {
    canUseMyId() {
      return authState.token && authState.accountType === 'FACILITY' && authState.accountId
    }
  },
  mounted() {
    if (this.canUseMyId) {
      this.centerId = authState.accountId
    }
  },
  methods: {
    useMyId() {
      this.centerId = authState.accountId
    },
    async fetchFacility() {
      this.loading = true; this.loadError = null; this.facility = null
      try {
        this.facility = await accountApi.getFacility(this.centerId)
        this.updates = {
          name: this.facility.name || '',
          sportTypes: this.facility.sportTypes || '',
          contactName: this.facility.contactName || '',
          contactPhone: this.facility.contactPhone || '',
          website: this.facility.website || '',
          city: this.facility.city || '',
          district: this.facility.district || '',
          coordinates: this.facility.coordinates || ''
        }
      } catch (e) {
        this.loadError = e.message
        if (!e.silent) toast.error(e.message)
      } finally {
        this.loading = false
      }
    },
    async saveFacility() {
      this.loading = true; this.saveError = null
      const body = Object.fromEntries(
        Object.entries(this.updates).filter(([, v]) => v !== '' && v !== null)
      )
      try {
        this.facility = await accountApi.updateFacility(this.centerId, body)
        toast.success('Facility updated.')
      } catch (e) {
        this.saveError = e.message
        if (!e.silent) toast.error(e.message)
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.form {
  max-width: 540px; margin: 0 auto; background: #fff;
  padding: 28px 30px; border-radius: 10px; text-align: left;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
h2, h3 { color: #2c3e50; }
h2 { text-align: center; margin-top: 0; }
h3 { margin-top: 24px; font-size: 1em; }
label {
  display: block; margin: 14px 0 6px; font-weight: bold;
  font-size: 0.85em; color: #555; text-transform: uppercase; letter-spacing: 0.5px;
}
.row { display: flex; gap: 8px; align-items: stretch; }
.row input { flex: 1; }
.actions { margin-top: 12px; }
input, select {
  display: block; width: 100%; padding: 10px; box-sizing: border-box;
  border: 1px solid #d5dde0; border-radius: 6px; font-size: 0.95em;
}
input:focus, select:focus { outline: none; border-color: #3498db; }
button {
  background: #3498db; color: white; border: none;
  padding: 11px 22px; margin-top: 8px; border-radius: 6px;
  font-size: 0.95em; font-weight: bold; cursor: pointer;
  display: inline-flex; align-items: center; justify-content: center; gap: 8px;
  min-width: 160px;
}
button.secondary { background: #ecf0f1; color: #2c3e50; }
button.ghost {
  background: transparent; color: #3498db; border: 1px solid #3498db;
  padding: 0 12px; min-width: 0; margin: 0; font-size: 0.85em;
}
button.ghost:hover { background: #eaf3fa; }
button:disabled { background: #aaa; cursor: not-allowed; }
.readonly { background: #eaf3fa; padding: 10px 14px; border-radius: 6px; }
.error { background: #fadbd8; color: #c0392b; padding: 10px 14px; margin-top: 14px; border-radius: 6px; font-size: 0.9em; }
hr { border: none; border-top: 1px solid #eee; margin: 20px 0; }
</style>
