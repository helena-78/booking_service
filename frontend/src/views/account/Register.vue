<template>
  <div class="form">
    <h2>Register</h2>

    <div class="tabs">
      <button :class="{ active: kind === 'user' }" @click="kind = 'user'">User</button>
      <button :class="{ active: kind === 'facility' }" @click="kind = 'facility'">Sport Center</button>
    </div>

    <!-- User registration -->
    <template v-if="kind === 'user'">
      <label>Name</label>
      <input v-model="user.name" placeholder="Jane Doe" />

      <label>Email</label>
      <input v-model="user.email" type="email" placeholder="jane@example.com" autocomplete="email" />

      <label>Password (min 8 characters)</label>
      <input v-model="user.password" type="password" autocomplete="new-password" />

      <label>Role</label>
      <select v-model="user.role">
        <option value="USER">USER</option>
        <option value="ORGANIZER">ORGANIZER</option>
        <option value="MODERATOR">MODERATOR</option>
      </select>

      <label>Skill level</label>
      <select v-model="user.skillLevel">
        <option :value="null">— None —</option>
        <option value="BEGINNER">BEGINNER</option>
        <option value="INTERMEDIATE">INTERMEDIATE</option>
        <option value="ADVANCED">ADVANCED</option>
        <option value="PROFESSIONAL">PROFESSIONAL</option>
      </select>

      <label>City</label>
      <input v-model="user.city" />

      <label>District (optional)</label>
      <input v-model="user.district" />

      <label>Sport preferences (comma separated, optional)</label>
      <input v-model="user.sportPreferences" placeholder="badminton,tennis" />

      <button @click="submitUser" :disabled="loading || !user.email || !user.password">
        <Spinner v-if="loading" :size="14" inline />
        <span>{{ loading ? 'Creating…' : 'Register User' }}</span>
      </button>
    </template>

    <!-- Facility registration -->
    <template v-else>
      <label>Facility name</label>
      <input v-model="facility.name" placeholder="Tartu Sports Centre" />

      <label>Email</label>
      <input v-model="facility.email" type="email" placeholder="contact@tartu-sports.test" autocomplete="email" />

      <label>Password (min 8 characters)</label>
      <input v-model="facility.password" type="password" autocomplete="new-password" />

      <label>Sport types (comma separated)</label>
      <input v-model="facility.sportTypes" placeholder="badminton,tennis,futsal" />

      <label>Contact name (optional)</label>
      <input v-model="facility.contactName" />

      <label>Contact phone (optional)</label>
      <input v-model="facility.contactPhone" placeholder="+372 555 0100" />

      <label>Website (optional)</label>
      <input v-model="facility.website" placeholder="https://example.com" />

      <label>City</label>
      <input v-model="facility.city" />

      <label>District (optional)</label>
      <input v-model="facility.district" />

      <label>Coordinates (lat,lng — optional)</label>
      <input v-model="facility.coordinates" placeholder="58.3776,26.7290" />

      <button @click="submitFacility" :disabled="loading || !facility.email || !facility.password">
        <Spinner v-if="loading" :size="14" inline />
        <span>{{ loading ? 'Creating…' : 'Register Sport Center' }}</span>
      </button>
    </template>

    <div v-if="error" class="error"><p><b>Error:</b> {{ error }}</p></div>

    <div v-if="result" class="success">
      <p>✓ Registered</p>
      <p><small>
        <b>ID:</b> {{ result.userId || result.centerId }}<br />
        <b>Name:</b> {{ result.name }}<br />
        <b>Email:</b> {{ result.email }}
      </small></p>
    </div>
  </div>
</template>

<script>
import { accountApi } from '../../api/client'
import { toast } from '../../store/toastStore'
import Spinner from '../../components/Spinner.vue'

function emptyUser() {
  return {
    name: '', email: '', password: '', role: 'USER',
    skillLevel: null, city: '', district: '', sportPreferences: ''
  }
}
function emptyFacility() {
  return {
    name: '', email: '', password: '', sportTypes: '',
    contactName: '', contactPhone: '', website: '',
    city: '', district: '', coordinates: ''
  }
}

export default {
  name: 'AccountRegister',
  components: { Spinner },
  data() {
    return {
      kind: 'user',
      user: emptyUser(),
      facility: emptyFacility(),
      loading: false,
      error: null,
      result: null
    }
  },
  watch: {
    kind() {
      this.error = null
      this.result = null
    }
  },
  methods: {
    async submitUser() {
      this.loading = true; this.error = null; this.result = null
      try {
        this.result = await accountApi.registerUser(this.user)
        toast.success(`User "${this.result.name}" registered.`)
        this.user = emptyUser()
      } catch (e) {
        this.error = e.message
        if (!e.silent) toast.error(e.message)
      } finally {
        this.loading = false
      }
    },
    async submitFacility() {
      this.loading = true; this.error = null; this.result = null
      try {
        this.result = await accountApi.registerFacility(this.facility)
        toast.success(`Sport center "${this.result.name}" registered.`)
        this.facility = emptyFacility()
      } catch (e) {
        this.error = e.message
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
  max-width: 540px;
  margin: 0 auto;
  background: #fff;
  padding: 28px 30px;
  border-radius: 10px;
  text-align: left;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
h2 { color: #2c3e50; text-align: center; margin-top: 0; }
.tabs { display: flex; gap: 8px; margin-bottom: 20px; }
.tabs button {
  flex: 1; padding: 10px; background: #ecf0f1; color: #2c3e50;
  border: none; border-radius: 6px; cursor: pointer; font-weight: bold;
}
.tabs button.active { background: #3498db; color: white; }
label {
  display: block; margin: 14px 0 6px; font-weight: bold;
  font-size: 0.85em; color: #555; text-transform: uppercase; letter-spacing: 0.5px;
}
input, select {
  display: block; width: 100%; padding: 10px; box-sizing: border-box;
  border: 1px solid #d5dde0; border-radius: 6px; font-size: 0.95em;
}
input:focus, select:focus { outline: none; border-color: #3498db; }
button:not(.tabs button) {
  background: #3498db; color: white; border: none;
  padding: 11px 24px; margin-top: 20px; border-radius: 6px;
  font-size: 0.95em; font-weight: bold; cursor: pointer;
  display: inline-flex; align-items: center; justify-content: center; gap: 8px;
  min-width: 200px;
}
button:disabled { background: #aaa; cursor: not-allowed; }
.error { background: #fadbd8; color: #c0392b; padding: 10px 14px; margin-top: 16px; border-radius: 6px; font-size: 0.9em; }
.success { background: #d4efdf; color: #196f3d; padding: 10px 14px; margin-top: 16px; border-radius: 6px; }
</style>
