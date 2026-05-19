<template>
  <div class="cases">
    <h2>Moderation Cases</h2>

    <div class="filters">
      <label>
        Content type
        <select v-model="contentType" @change="fetchCases">
          <option :value="null">All</option>
          <option value="COMMENT">COMMENT</option>
          <option value="ACTIVITY_DESCRIPTION">ACTIVITY_DESCRIPTION</option>
          <option value="USER_PROFILE">USER_PROFILE</option>
        </select>
      </label>
      <label>
        Verdict
        <select v-model="verdict" @change="fetchCases">
          <option :value="null">All</option>
          <option value="PENDING">PENDING</option>
          <option value="REMOVE_CONTENT">REMOVE_CONTENT</option>
          <option value="SANCTION_USER">SANCTION_USER</option>
          <option value="DISMISSED">DISMISSED</option>
        </select>
      </label>
      <button @click="fetchCases" :disabled="loading">
        <Spinner v-if="loading" :size="14" inline />
        <span>Refresh</span>
      </button>
    </div>

    <Spinner v-if="loading" label="Loading cases…" />

    <div v-else-if="error" class="error">
      <p>{{ error }}</p>
      <button @click="fetchCases">Retry</button>
    </div>

    <div v-else-if="cases.length === 0" class="empty">
      <p>No moderation cases match the current filters.</p>
    </div>

    <div v-else>
      <p class="count">Showing <b>{{ cases.length }}</b> of <b>{{ totalElements }}</b> case(s)</p>
      <div class="item" v-for="c in cases" :key="c.caseId">
        <div class="header-row">
          <span class="badge" :class="'verdict-' + c.verdict.toLowerCase()">{{ c.verdict }}</span>
          <span class="type">{{ c.contentType }}</span>
          <router-link :to="`/moderation/cases/${c.caseId}`" class="open-link">Open →</router-link>
        </div>
        <p class="content">{{ c.content }}</p>
        <p class="ids">
          <small>
            <b>Case ID:</b> {{ c.caseId }}<br />
            <b>Target user:</b> {{ c.targetUserId }}<br />
            <b>Reported by:</b> {{ c.reportedByUserId }}<br />
            <b>Created:</b> {{ c.createdAt }}
            <span v-if="c.resolvedAt"><br /><b>Resolved:</b> {{ c.resolvedAt }}</span>
            <span v-if="c.moderatorId"><br /><b>Moderator:</b> {{ c.moderatorId }}</span>
          </small>
        </p>
      </div>
    </div>
  </div>
</template>

<script>
import { moderationApi } from '../../api/client'
import { authState } from '../../store/authStore'
import { toast } from '../../store/toastStore'
import Spinner from '../../components/Spinner.vue'

export default {
  name: 'ModerationCases',
  components: { Spinner },
  data() {
    return {
      cases: [],
      totalElements: 0,
      contentType: null,
      verdict: null,
      loading: false,
      error: null
    }
  },
  methods: {
    async fetchCases() {
      this.loading = true
      this.error = null
      try {
        const data = await moderationApi.listCases({
          contentType: this.contentType,
          verdict: this.verdict
        })
        this.cases = data.content || []
        this.totalElements = data.totalElements || this.cases.length
      } catch (err) {
        this.error = err.message
        if (!err.silent) toast.error(err.message)
      } finally {
        this.loading = false
      }
    }
  },
  mounted() {
    if (!authState.token) {
      toast.info('Please sign in to view moderation cases.')
      this.$router.push('/account/login')
      return
    }
    this.fetchCases()
  }
}
</script>

<style scoped>
.cases { text-align: left; max-width: 760px; margin: 0 auto; }
h2 { color: #2c3e50; text-align: center; }
.role-badge {
  background: #fdebd0;
  color: #7e5109;
  border-left: 3px solid #f39c12;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 0.85em;
  margin-bottom: 18px;
}
.role-badge.ok {
  background: #d4efdf;
  color: #196f3d;
  border-left-color: #27ae60;
}
.filters {
  display: flex;
  gap: 16px;
  align-items: flex-end;
  background: #f4f4f4;
  padding: 12px;
  border-radius: 8px;
  margin-bottom: 20px;
}
.filters label {
  display: flex;
  flex-direction: column;
  font-weight: bold;
  font-size: 0.85em;
  color: #555;
  text-transform: uppercase;
  letter-spacing: 0.5px;
}
.filters select {
  margin-top: 4px;
  padding: 6px 8px;
  border-radius: 6px;
  border: 1px solid #d5dde0;
}
.filters select:focus { outline: none; border-color: #8e44ad; }
.filters button {
  background: #8e44ad;
  color: white;
  border: none;
  padding: 8px 14px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: bold;
  display: inline-flex;
  align-items: center;
  gap: 8px;
}
.filters button:disabled { background: #aaa; cursor: not-allowed; }
.count { text-align: center; color: #555; }
.item {
  background: #f5edf8;
  padding: 12px 18px;
  margin-bottom: 10px;
  border-radius: 8px;
  border-left: 4px solid #8e44ad;
}
.header-row {
  display: flex;
  gap: 12px;
  align-items: center;
  margin-bottom: 6px;
}
.open-link {
  margin-left: auto;
  font-size: 0.85em;
  color: #8e44ad;
  text-decoration: none;
  font-weight: bold;
}
.badge {
  display: inline-block;
  padding: 3px 10px;
  border-radius: 12px;
  font-size: 0.8em;
  font-weight: bold;
  color: white;
}
.verdict-pending        { background: #f39c12; }
.verdict-remove_content { background: #c0392b; }
.verdict-sanction_user  { background: #8e44ad; }
.verdict-dismissed      { background: #95a5a6; }
.type { color: #555; font-size: 0.9em; font-weight: bold; }
.content { margin: 6px 0; color: #333; }
.ids { color: #777; margin: 0; }
.empty, .error { text-align: center; padding: 30px; }
.error { color: #c0392b; }
.error button {
  background: #8e44ad;
  color: white;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  margin-top: 10px;
}
</style>
