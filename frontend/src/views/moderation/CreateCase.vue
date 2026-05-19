<template>
  <div class="form">
    <h2>Report Content</h2>
    <p class="subtitle">Open a new moderation case against a piece of content.</p>

    <label>Reported content ID (UUID)</label>
    <input v-model="reportedContentId" placeholder="UUID of the offending content" />

    <label>Content type</label>
    <select v-model="contentType">
      <option value="COMMENT">COMMENT</option>
      <option value="ACTIVITY_DESCRIPTION">ACTIVITY_DESCRIPTION</option>
      <option value="USER_PROFILE">USER_PROFILE</option>
    </select>

    <label>Reported by (your user ID)</label>
    <div class="row">
      <input v-model="reportedByUserId" placeholder="Your user UUID" />
      <button
        v-if="canUseMyId"
        @click="useMyId"
        type="button"
        class="ghost"
        title="Fill with your signed-in user ID"
      >Use mine</button>
    </div>

    <label>Target user (whose content)</label>
    <input v-model="targetUserId" placeholder="UUID of the user being reported" />

    <label>Description of the violation</label>
    <textarea v-model="content" rows="4" placeholder="What rule does this content break?"></textarea>

    <button @click="submit" :disabled="loading || !canSubmit">
      <Spinner v-if="loading" :size="14" inline />
      <span>{{ loading ? 'Submitting…' : 'Submit case' }}</span>
    </button>

    <div v-if="error" class="error"><p><b>Error:</b> {{ error }}</p></div>

    <div v-if="result" class="success">
      <p>✓ Case submitted (verdict: <b>{{ result.verdict }}</b>)</p>
      <p><small><b>Case ID:</b> {{ result.caseId }}</small></p>
    </div>
  </div>
</template>

<script>
import { moderationApi } from '../../api/client'
import { authState } from '../../store/authStore'
import { toast } from '../../store/toastStore'
import Spinner from '../../components/Spinner.vue'

export default {
  name: 'CreateCase',
  components: { Spinner },
  data() {
    return {
      reportedContentId: '', contentType: 'COMMENT',
      reportedByUserId: '', targetUserId: '', content: '',
      loading: false, error: null, result: null,
      authState
    }
  },
  computed: {
    canUseMyId() {
      return authState.token && authState.accountType === 'USER' && authState.accountId
    },
    canSubmit() {
      return this.reportedContentId && this.reportedByUserId &&
             this.targetUserId && this.content.trim().length > 0
    }
  },
  mounted() {
    if (this.canUseMyId) {
      this.reportedByUserId = authState.accountId
    }
  },
  methods: {
    useMyId() {
      this.reportedByUserId = authState.accountId
    },
    async submit() {
      this.loading = true; this.error = null; this.result = null
      try {
        this.result = await moderationApi.createCase({
          reportedContentId: this.reportedContentId,
          contentType: this.contentType,
          reportedByUserId: this.reportedByUserId,
          targetUserId: this.targetUserId,
          content: this.content
        })
        toast.success(`Case submitted (${this.result.caseId.slice(0, 8)}…).`)
        this.reportedContentId = ''
        this.targetUserId = ''
        this.content = ''
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
  max-width: 560px; margin: 0 auto; background: #fff;
  padding: 28px 30px; border-radius: 10px; text-align: left;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
h2 { color: #2c3e50; text-align: center; margin: 0; }
.subtitle { text-align: center; color: #888; font-size: 0.9em; margin: 4px 0 16px; }
label {
  display: block; margin: 14px 0 6px; font-weight: bold;
  font-size: 0.85em; color: #555; text-transform: uppercase; letter-spacing: 0.5px;
}
.row { display: flex; gap: 8px; align-items: stretch; }
.row input { flex: 1; }
input, select, textarea {
  display: block; width: 100%; padding: 10px; box-sizing: border-box;
  border: 1px solid #d5dde0; border-radius: 6px; font-size: 0.95em;
  font-family: inherit;
}
input:focus, select:focus, textarea:focus { outline: none; border-color: #8e44ad; }
button {
  background: #8e44ad; color: white; border: none;
  padding: 11px 22px; margin-top: 20px; border-radius: 6px;
  font-size: 0.95em; font-weight: bold; cursor: pointer;
  display: inline-flex; align-items: center; justify-content: center; gap: 8px;
  min-width: 180px;
}
button.ghost {
  background: transparent; color: #8e44ad; border: 1px solid #8e44ad;
  padding: 0 12px; min-width: 0; margin: 0; font-size: 0.85em;
}
button.ghost:hover { background: #f5edf8; }
button:disabled { background: #aaa; cursor: not-allowed; }
.error { background: #fadbd8; color: #c0392b; padding: 10px 14px; margin-top: 14px; border-radius: 6px; font-size: 0.9em; }
.success { background: #d4efdf; color: #196f3d; padding: 10px 14px; margin-top: 14px; border-radius: 6px; }
</style>
