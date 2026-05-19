<template>
  <div class="form">
    <h2>Case Detail</h2>

    <Spinner v-if="loading" label="Loading case…" />

    <div v-else-if="loadError" class="error"><p>{{ loadError }}</p></div>

    <template v-else-if="caseInfo">
      <div class="item">
        <div class="header-row">
          <span class="badge" :class="'verdict-' + caseInfo.verdict.toLowerCase()">{{ caseInfo.verdict }}</span>
          <span class="type">{{ caseInfo.contentType }}</span>
        </div>
        <p class="content">{{ caseInfo.content }}</p>
        <p class="ids">
          <small>
            <b>Case ID:</b> {{ caseInfo.caseId }}<br />
            <b>Reported content:</b> {{ caseInfo.reportedContentId }}<br />
            <b>Target user:</b> {{ caseInfo.targetUserId }}<br />
            <b>Reported by:</b> {{ caseInfo.reportedByUserId }}<br />
            <b>Created:</b> {{ caseInfo.createdAt }}
            <span v-if="caseInfo.resolvedAt"><br /><b>Resolved:</b> {{ caseInfo.resolvedAt }}</span>
            <span v-if="caseInfo.moderatorId"><br /><b>Moderator:</b> {{ caseInfo.moderatorId }}</span>
          </small>
        </p>
      </div>

      <template v-if="caseInfo.verdict === 'PENDING'">
        <h3>Submit verdict</h3>

        <label>Verdict</label>
        <select v-model="verdict">
          <option value="REMOVE_CONTENT">REMOVE_CONTENT</option>
          <option value="SANCTION_USER">SANCTION_USER</option>
          <option value="DISMISSED">DISMISSED</option>
        </select>

        <label>Moderator ID (your user ID)</label>
        <div class="row">
          <input v-model="moderatorId" placeholder="Your moderator user UUID" />
          <button
            v-if="canUseMyId"
            @click="useMyId"
            type="button"
            class="ghost"
            title="Fill with your signed-in user ID"
          >Use mine</button>
        </div>

        <button @click="submitVerdict" :disabled="submitting || !moderatorId">
          <Spinner v-if="submitting" :size="14" inline />
          <span>{{ submitting ? 'Submitting…' : 'Submit verdict' }}</span>
        </button>

        <div v-if="submitError" class="error"><p>{{ submitError }}</p></div>
      </template>

      <template v-else>
        <p class="note">This case is already resolved.</p>

        <!-- Allow moderators to delete content after a REMOVE_CONTENT verdict. -->
        <div v-if="caseInfo.verdict === 'REMOVE_CONTENT' && !contentRemoved" class="content-actions">
          <button @click="removeContent" :disabled="removing" class="danger">
            <Spinner v-if="removing" :size="14" inline />
            <span>{{ removing ? 'Removing…' : 'Remove the content now' }}</span>
          </button>
          <p class="hint">
            Sends <code>DELETE /api/content/{{ caseInfo.contentType }}/{{ caseInfo.reportedContentId }}</code>
            to the owning service.
          </p>
        </div>

        <div v-if="removeError" class="error"><p>{{ removeError }}</p></div>

        <div v-if="contentRemoved" class="success">
          <p>✓ Content removed.</p>
          <p><small>{{ caseInfo.contentType }} <code>{{ caseInfo.reportedContentId }}</code></small></p>
        </div>
      </template>
    </template>
  </div>
</template>

<script>
import { moderationApi } from '../../api/client'
import { authState } from '../../store/authStore'
import { toast } from '../../store/toastStore'
import Spinner from '../../components/Spinner.vue'

export default {
  name: 'CaseDetail',
  components: { Spinner },
  data() {
    return {
      caseInfo: null,
      verdict: 'REMOVE_CONTENT',
      moderatorId: '',
      loading: false, loadError: null,
      submitting: false, submitError: null,
      removing: false, removeError: null, contentRemoved: false,
      authState
    }
  },
  computed: {
    canUseMyId() {
      return authState.token && authState.accountId
    }
  },
  async mounted() {
    if (!authState.token) {
      toast.info('Please sign in to view case details.')
      this.$router.push('/account/login')
      return
    }
    if (this.canUseMyId) {
      this.moderatorId = authState.accountId
    }
    await this.fetch()
  },
  methods: {
    useMyId() {
      this.moderatorId = authState.accountId
    },
    async fetch() {
      this.loading = true; this.loadError = null
      try {
        this.caseInfo = await moderationApi.getCase(this.$route.params.id)
      } catch (e) {
        this.loadError = e.message
        if (!e.silent) toast.error(e.message)
      } finally {
        this.loading = false
      }
    },
    async submitVerdict() {
      this.submitting = true; this.submitError = null
      try {
        this.caseInfo = await moderationApi.submitVerdict(this.$route.params.id, {
          verdict: this.verdict,
          moderatorId: this.moderatorId
        })
        toast.success(`Verdict ${this.caseInfo.verdict} recorded.`)
      } catch (e) {
        this.submitError = e.message
        if (!e.silent) toast.error(e.message)
      } finally {
        this.submitting = false
      }
    },
    async removeContent() {
      this.removing = true; this.removeError = null
      try {
        await moderationApi.removeContent(this.caseInfo.contentType, this.caseInfo.reportedContentId)
        this.contentRemoved = true
        toast.success('Content removed.')
      } catch (e) {
        this.removeError = e.message
        if (!e.silent) toast.error(e.message)
      } finally {
        this.removing = false
      }
    }
  }
}
</script>

<style scoped>
.form {
  max-width: 600px; margin: 0 auto; background: #fff;
  padding: 28px 30px; border-radius: 10px; text-align: left;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
h2, h3 { color: #2c3e50; }
h2 { text-align: center; margin-top: 0; }
h3 { margin-top: 24px; font-size: 1em; }
.role-badge {
  background: #fdebd0;
  color: #7e5109;
  border-left: 3px solid #f39c12;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 0.85em;
  margin-bottom: 16px;
}
.role-badge.ok {
  background: #d4efdf;
  color: #196f3d;
  border-left-color: #27ae60;
}
.item {
  background: #f5edf8; padding: 12px 18px; margin-bottom: 10px;
  border-radius: 8px; border-left: 4px solid #8e44ad;
}
.header-row { display: flex; gap: 12px; align-items: center; margin-bottom: 6px; }
.badge {
  display: inline-block; padding: 3px 10px; border-radius: 12px;
  font-size: 0.8em; font-weight: bold; color: white;
}
.verdict-pending        { background: #f39c12; }
.verdict-remove_content { background: #c0392b; }
.verdict-sanction_user  { background: #8e44ad; }
.verdict-dismissed      { background: #95a5a6; }
.type { color: #555; font-size: 0.9em; font-weight: bold; }
.content { margin: 6px 0; color: #333; }
.ids { color: #777; margin: 0; }
label {
  display: block; margin: 14px 0 6px; font-weight: bold;
  font-size: 0.85em; color: #555; text-transform: uppercase; letter-spacing: 0.5px;
}
.row { display: flex; gap: 8px; align-items: stretch; }
.row input { flex: 1; }
input, select {
  display: block; width: 100%; padding: 10px; box-sizing: border-box;
  border: 1px solid #d5dde0; border-radius: 6px; font-size: 0.95em;
}
input:focus, select:focus { outline: none; border-color: #8e44ad; }
button {
  background: #8e44ad; color: white; border: none;
  padding: 11px 22px; margin-top: 16px; border-radius: 6px;
  font-size: 0.95em; font-weight: bold; cursor: pointer;
  display: inline-flex; align-items: center; justify-content: center; gap: 8px;
  min-width: 180px;
}
button.ghost {
  background: transparent; color: #8e44ad; border: 1px solid #8e44ad;
  padding: 0 12px; min-width: 0; margin: 0; font-size: 0.85em;
}
button.ghost:hover { background: #f5edf8; }
button.danger { background: #c0392b; }
button.danger:hover { background: #a93226; }
button:disabled { background: #aaa; cursor: not-allowed; }
.error { background: #fadbd8; color: #c0392b; padding: 10px 14px; margin-top: 14px; border-radius: 6px; font-size: 0.9em; }
.success { background: #d4efdf; color: #196f3d; padding: 10px 14px; margin-top: 14px; border-radius: 6px; }
.note { color: #777; font-style: italic; }
.content-actions { margin-top: 14px; }
.hint { color: #888; font-size: 0.8em; margin: 8px 0 0; }
.hint code, .success code {
  background: #ecf0f1; padding: 1px 5px; border-radius: 4px; font-size: 0.95em;
}
</style>
