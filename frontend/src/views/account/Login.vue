<template>
  <div class="form">
    <h2>Account Login</h2>

    <label>Email</label>
    <input v-model="email" type="email" placeholder="you@example.com" autocomplete="username" />

    <label>Password</label>
    <input v-model="password" type="password" placeholder="At least 8 characters" autocomplete="current-password" />

    <button @click="login" :disabled="loading || !email || !password">
      <Spinner v-if="loading" :size="14" inline />
      <span>{{ loading ? 'Logging in…' : 'Login' }}</span>
    </button>

    <div v-if="error" class="error"><p><b>Error:</b> {{ error }}</p></div>

    <div v-if="authState.token" class="success">
      <p>✓ Signed in</p>
      <p><small>
        <b>Account ID:</b> {{ authState.accountId }}<br />
        <b>Email:</b> {{ authState.email }}<br />
        <b>Account type:</b> {{ authState.accountType }} &middot;
        <b>Role:</b> {{ authState.role }}
      </small></p>
    </div>

    <button v-if="authState.token" @click="signOut" class="secondary">Sign out</button>
  </div>
</template>

<script>
import { accountApi } from '../../api/client'
import { authState, setAuth, clearAuth } from '../../store/authStore'
import { toast } from '../../store/toastStore'
import Spinner from '../../components/Spinner.vue'

export default {
  name: 'AccountLogin',
  components: { Spinner },
  data() {
    return {
      email: '',
      password: '',
      loading: false,
      error: null,
      authState
    }
  },
  methods: {
    async login() {
      this.loading = true
      this.error = null
      try {
        const result = await accountApi.login(this.email, this.password)
        setAuth({ ...result, email: this.email })
        toast.success(`Welcome back, ${this.email}`)
        this.password = ''
      } catch (err) {
        this.error = err.message
        if (!err.silent) toast.error(err.message)
      } finally {
        this.loading = false
      }
    },
    signOut() {
      clearAuth()
      this.email = ''
      this.password = ''
      toast.info('Signed out.')
    }
  }
}
</script>

<style scoped>
.form {
  max-width: 480px;
  margin: 0 auto;
  background: #fff;
  padding: 28px 30px;
  border-radius: 10px;
  text-align: left;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
h2 { color: #2c3e50; text-align: center; margin-top: 0; }
label {
  display: block;
  margin: 16px 0 6px;
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
input:focus { outline: none; border-color: #3498db; }
button {
  background: #3498db;
  color: white;
  border: none;
  padding: 11px 24px;
  margin-top: 20px;
  border-radius: 6px;
  font-size: 0.95em;
  font-weight: bold;
  cursor: pointer;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  min-width: 160px;
}
button.secondary {
  background: #ecf0f1;
  color: #2c3e50;
  margin-top: 12px;
}
button:disabled { background: #aaa; cursor: not-allowed; }
.error {
  background: #fadbd8;
  color: #c0392b;
  padding: 10px 14px;
  margin-top: 16px;
  border-radius: 6px;
  font-size: 0.9em;
}
.success {
  background: #d4efdf;
  color: #196f3d;
  padding: 10px 14px;
  margin-top: 16px;
  border-radius: 6px;
}
</style>
