<template>
  <div class="form">
    <h2>Account Login</h2>

    <label>Email</label>
    <input v-model="email" type="email" placeholder="demo@sportlink.test" />

    <label>Password</label>
    <input v-model="password" type="password" placeholder="demoPassword1" />

    <button @click="login" :disabled="loading">
      {{ loading ? 'Signing in…' : 'Sign in' }}
    </button>

    <div v-if="error" class="error">
      <p><b>Error:</b> {{ error }}</p>
    </div>

    <div v-if="result" class="success">
      <p>✓ Authenticated</p>
      <p><small>
        <b>Account type:</b> {{ result.accountType }}<br />
        <b>Role:</b> {{ result.role }}<br />
        <b>Account ID:</b> {{ result.accountId }}<br />
        <b>Token:</b> {{ result.token }}
      </small></p>
    </div>
  </div>
</template>

<script>
export default {
  name: 'AccountLogin',
  data() {
    return {
      email: '',
      password: '',
      loading: false,
      error: null,
      result: null
    }
  },
  methods: {
    login() {
      this.loading = true
      this.error = null
      this.result = null

      fetch('http://localhost:8081/api/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email: this.email, password: this.password })
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
.hint {
  background: #eaf3fa;
  border-left: 3px solid #3498db;
  padding: 10px 12px;
  border-radius: 6px;
  font-size: 0.9em;
  color: #555;
  margin-bottom: 20px;
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
  border-color: #3498db;
}
button {
  background: #3498db;
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
.note {
  font-size: 0.85em;
  color: #555;
  font-style: italic;
  margin-top: 8px;
}
code {
  background: #fff;
  padding: 1px 6px;
  border-radius: 4px;
  font-size: 0.9em;
}
</style>
