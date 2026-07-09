<template>
  <div class="page">
        <nav class="page-nav">
      <a href="#" class="back-link" @click.prevent="$router.back()">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
        返回
      </a>
      <h1>问诊社区</h1>
      <button class="new-btn" @click="showForm = true">
        <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5"><path d="M12 5v14M5 12h14"/></svg>
        发帖
      </button>
    </nav>


    <div class="page-content">
      <!-- 科室筛选 -->
      <div class="dept-filter">
        <button class="dept-btn" :class="{ active: !selectedDept }" @click="selectedDept = null">全部</button>
        <button v-for="dept in departments" :key="dept.id"
          class="dept-btn" :class="{ active: selectedDept === dept.id }"
          @click="selectedDept = dept.id">
          {{ dept.name }}
        </button>
      </div>

      <!-- 帖子列表 -->
      <div class="post-list">
        <div v-for="post in posts" :key="post.id" class="post-card" @click="viewPost(post.id)">
          <div class="post-header">
            <span class="post-status" :class="post.status.toLowerCase()">{{ statusText(post.status) }}</span>
            <span class="post-dept">{{ getDeptName(post.departmentId) }}</span>
          </div>
          <h3 class="post-title">{{ post.title }}</h3>
          <p class="post-preview">{{ (post.content || '').substring(0, 100) }}{{ post.content && post.content.length > 100 ? '...' : '' }}</p>
          <div class="post-footer">
            <span class="post-time">{{ formatTime(post.createdAt) }}</span>
          </div>
        </div>
        <div v-if="posts.length === 0" class="empty">暂无问诊帖子</div>
      </div>

      <!-- 发帖表单弹窗 -->
      <div v-if="showForm" class="modal-overlay" @click.self="showForm = false">
        <div class="modal">
          <div class="modal-header">
            <h2>发布问诊帖</h2>
            <button class="close-btn" @click="showForm = false">×</button>
          </div>
          <form @submit.prevent="submitPost">
            <div class="form-group">
              <label>选择科室 *</label>
              <select v-model="postForm.departmentId" required>
                <option value="">请选择科室</option>
                <option v-for="dept in departments" :key="dept.id" :value="dept.id">{{ dept.name }}</option>
              </select>
            </div>
            <div class="form-group">
              <label>标题 *</label>
              <input v-model="postForm.title" type="text" placeholder="简述您的问题" required />
            </div>
            <div class="form-group">
              <label>详细描述 *</label>
              <textarea v-model="postForm.content" placeholder="请详细描述您的症状、持续时间等信息..." rows="6" required></textarea>
            </div>
            <button type="submit" class="submit-btn" :disabled="submitting">
              {{ submitting ? '发布中...' : '发布' }}
            </button>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import api from '../utils/api'
import { toast } from '../composables/useToast'

const router = useRouter()
const departments = ref([])
const posts = ref([])
const selectedDept = ref(null)
const showForm = ref(false)
const submitting = ref(false)

const postForm = ref({
  departmentId: '',
  title: '',
  content: ''
})

onMounted(async () => {
  await loadDepartments()
  await loadPosts()
})

watch(selectedDept, () => loadPosts())

async function loadDepartments() {
  try {
    const { data } = await api.get('/api/admin/departments')
    if (data.code === 200) {
      departments.value = data.data || []
    }
  } catch (e) {
    console.error('Failed to load departments:', e)
  }
}

async function loadPosts() {
  try {
    const params = selectedDept.value ? { departmentId: selectedDept.value } : {}
    const { data } = await api.get('/api/consultation/posts', { params })
    if (data.code === 200) {
      posts.value = data.data || []
    }
  } catch (e) {
    toast.error('加载帖子失败')
  }
}

async function submitPost() {
  if (!postForm.value.departmentId || !postForm.value.title.trim() || !postForm.value.content.trim()) {
    toast.warning('请填写完整信息')
    return
  }

  submitting.value = true
  try {
    const { data } = await api.post('/api/consultation/posts', postForm.value)
    if (data.code === 200) {
      showForm.value = false
      postForm.value = { departmentId: '', title: '', content: '' }
      await loadPosts()
      toast.success('发布成功！')
    } else {
      toast.error(data.message || '发布失败，请重试')
    }
  } catch (e) {
    toast.error('发布失败，请重试')
  } finally {
    submitting.value = false
  }
}

function viewPost(id) {
  router.push('/consultation/' + id)
}

function getDeptName(deptId) {
  return departments.value.find(d => d.id === deptId)?.name || '未知科室'
}

function statusText(status) {
  const map = { PENDING: '待回复', REPLIED: '已回复', CLOSED: '已关闭' }
  return map[status] || status
}

function formatTime(timestamp) {
  if (!timestamp) return ''
  return new Date(timestamp).toLocaleString('zh-CN')
}
</script>

<style scoped>
.page { min-height: 100vh; background: var(--bg-primary); }

.page-nav {
  background: var(--color-white);
  border-bottom: 1px solid var(--border-light);
  padding: 0 32px;
  height: 60px;
  position: sticky;
  top: 0;
  z-index: 10;
  display: flex;
  align-items: center;
}

.page-nav h1 {
  flex: 1;
  text-align: center;
}



.back-link {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 14px;
  color: var(--text-muted);
  transition: color var(--transition-fast);
}

.back-link:hover { color: var(--color-forest); }

.nav-spacer {
  min-width: 80px;
}

.page-nav h1 {
  font-size: 18px;
  font-family: var(--font-body);
  font-weight: 600;
  flex: 1;
}

.new-btn {
  padding: 8px 20px;
  background: var(--color-forest);
  color: var(--color-white);
  border: none;
  border-radius: var(--radius-sm);
  font-size: 14px;
  font-weight: 500;
  transition: all var(--transition-fast);
}

.new-btn:hover { background: var(--color-forest-dark); }

.page-content {
  max-width: 800px;
  margin: 32px auto;
  padding: 0 24px;
}

.dept-filter {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 24px;
}

.dept-btn {
  padding: 8px 16px;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-sm);
  background: var(--color-white);
  font-size: 13px;
  font-weight: 500;
  color: var(--text-secondary);
  transition: all var(--transition-fast);
}

.dept-btn:hover {
  border-color: var(--color-forest);
  color: var(--color-forest);
}

.dept-btn.active {
  background: var(--color-forest);
  color: var(--color-white);
  border-color: var(--color-forest);
}

.post-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.post-card {
  background: var(--color-white);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
  padding: 20px;
  cursor: pointer;
  transition: all var(--transition-base);
}

.post-card:hover {
  border-color: var(--color-forest);
  transform: translateY(-2px);
  box-shadow: var(--shadow-md);
}

.post-header {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
}

.post-status {
  padding: 3px 10px;
  border-radius: var(--radius-xs);
  font-size: 12px;
  font-weight: 500;
}

.post-status.pending {
  background: #fef3c7;
  color: #92400e;
}

.post-status.replied {
  background: #d1fae5;
  color: #065f46;
}

.post-status.closed {
  background: var(--color-paper);
  color: var(--text-muted);
}

.post-dept {
  padding: 3px 10px;
  border-radius: var(--radius-xs);
  font-size: 12px;
  background: var(--color-paper);
  color: var(--color-forest);
}

.post-title {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
  color: var(--text-primary);
}

.post-preview {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 10px;
}

.post-footer {
  font-size: 12px;
  color: var(--text-muted);
}

.empty {
  text-align: center;
  color: var(--text-muted);
  padding: 48px;
  background: var(--color-white);
  border: 1px solid var(--border-light);
  border-radius: var(--radius-lg);
}

/* Modal */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  padding: 20px;
}

.modal {
  background: var(--color-white);
  border-radius: var(--radius-lg);
  padding: 32px;
  width: 100%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.modal-header h2 {
  font-size: 20px;
  font-weight: 600;
}

.close-btn {
  width: 32px;
  height: 32px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-paper);
  border: none;
  border-radius: var(--radius-sm);
  font-size: 20px;
  color: var(--text-muted);
  cursor: pointer;
}

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 6px;
  text-transform: uppercase;
  letter-spacing: 0.05em;
}

.form-group input,
.form-group textarea,
.form-group select {
  width: 100%;
  padding: 12px 14px;
  border: 1px solid var(--border-light);
  border-radius: var(--radius-sm);
  font-size: 15px;
  color: var(--text-primary);
  outline: none;
  transition: border-color var(--transition-fast);
  background: var(--color-white);
}

.form-group input:focus,
.form-group textarea:focus,
.form-group select:focus {
  border-color: var(--color-forest);
}

.submit-btn {
  width: 100%;
  padding: 13px;
  background: var(--color-forest);
  color: var(--color-white);
  border: none;
  border-radius: var(--radius-sm);
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.submit-btn:hover:not(:disabled) {
  background: var(--color-forest-dark);
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}
</style>
