<template>
  <div id="app">
    <Toaster />

    <header>
      <h1>🏆 SportLink</h1>

      <div class="auth-strip">
        <template v-if="authState.token">
          <span class="auth-pill">
            <b>{{ authState.email || authState.accountId }}</b>
            <span class="role-tag" :class="'role-' + (authState.role || '').toLowerCase()">
              {{ authState.role }}
            </span>
          </span>
          <button class="signout-btn" @click="signOut">Sign out</button>
        </template>
        <template v-else>
          <span class="auth-pill muted">Not signed in</span>
          <router-link class="signout-btn link-btn" to="/account/login">Sign in</router-link>
        </template>
      </div>

      <nav class="grouped">
        <div class="group">
          <span class="group-label">Activities</span>
          <router-link to="/activities">All</router-link>
          <router-link to="/activities/new">Create</router-link>
        </div>
        <div class="group">
          <span class="group-label">Scheduling</span>
          <router-link to="/slots">Slots</router-link>
          <router-link to="/slots/reserve">Reserve</router-link>
        </div>
        <div class="group account">
          <span class="group-label">Account</span>
          <router-link to="/account/login">Login</router-link>
          <router-link to="/account/register">Register</router-link>
          <router-link to="/account/profile">Profile</router-link>
          <router-link to="/account/status">Status</router-link>
          <router-link to="/account/sport-center">Facility</router-link>
        </div>
        <div class="group moderation">
          <span class="group-label">Moderation</span>
          <router-link to="/moderation/cases">Cases</router-link>
          <router-link to="/moderation/cases/new">Report</router-link>
          <router-link to="/moderation/sanctions/new">Sanction</router-link>
          <router-link to="/moderation/sanctions/history">History</router-link>
        </div>
        <div class="group">
          <span class="group-label">Other</span>
          <router-link to="/ratings">Ratings</router-link>
          <router-link to="/search/facilities">Search</router-link>
        </div>
      </nav>
    </header>

    <main>
      <router-view />
    </main>
  </div>
</template>

<script>
import Toaster from './components/Toaster.vue'
import { authState, clearAuth } from './store/authStore'
import { toast } from './store/toastStore'

export default {
  name: 'App',
  components: { Toaster },
  data() {
    return { authState }
  },
  methods: {
    signOut() {
      clearAuth()
      toast.info('Signed out.')
      if (this.$route.path !== '/account/login') {
        this.$router.push('/account/login')
      }
    }
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  max-width: 1100px;
  margin: 0 auto;
  padding: 20px;
}

header h1 {
  color: #42b983;
  margin-bottom: 10px;
}

.auth-strip {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
  font-size: 0.9em;
}
.auth-pill {
  background: #eaf3fa;
  color: #1f618d;
  padding: 5px 12px;
  border-radius: 999px;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.auth-pill.muted { background: #f4f4f4; color: #888; }
.role-tag {
  background: #3498db;
  color: white;
  padding: 1px 8px;
  border-radius: 999px;
  font-size: 0.75em;
  font-weight: bold;
  letter-spacing: 0.4px;
}
.role-tag.role-moderator { background: #8e44ad; }
.role-tag.role-organizer { background: #16a085; }
.role-tag.role-user      { background: #3498db; }
.signout-btn {
  background: #ecf0f1;
  color: #2c3e50;
  border: none;
  padding: 5px 12px;
  border-radius: 999px;
  font-weight: bold;
  font-size: 0.85em;
  cursor: pointer;
}
.signout-btn:hover { background: #d6dbdf; }
.signout-btn.link-btn { text-decoration: none; }

nav.grouped {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 14px;
  padding: 16px;
  background: #f4f4f4;
  border-radius: 8px;
  margin-bottom: 14px;
  line-height: 2;
}

nav.search-nav {
  background: #eaf3ff;
  border: 1px solid #d6e9ff;
  margin-bottom: 30px;
  padding: 12px 20px;
}

nav.search-nav .nav-label {
  font-weight: bold;
  color: #1f3a5f;
  margin-right: 8px;
}

nav .group {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  padding: 8px 12px;
  background: #fff;
  border-radius: 6px;
  border-top: 3px solid #95a5a6;
  min-width: 110px;
}

nav .group.account    { border-top-color: #3498db; }
nav .group.moderation { border-top-color: #8e44ad; }

nav .group-label {
  font-size: 0.7em;
  text-transform: uppercase;
  letter-spacing: 0.6px;
  color: #888;
  font-weight: bold;
  margin-bottom: 4px;
}

nav a {
  font-weight: bold;
  color: #2c3e50;
  text-decoration: none;
  font-size: 0.9em;
}

nav a.router-link-exact-active {
  color: #42b983;
}

nav .group.account a.router-link-exact-active    { color: #3498db; }
nav .group.moderation a.router-link-exact-active { color: #8e44ad; }
</style>
