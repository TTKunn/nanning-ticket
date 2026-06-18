<template>
  <div>
    <div class="card" style="margin-bottom: 12px;">
      <div class="toolbar">
        <div class="form-item">
          <input
            class="form-input"
            v-model="filter.keyword"
            placeholder="账号 / 姓名 / 手机号"
            style="width: 220px;"
            @keyup.enter="load(1)"
          />
        </div>
        <div class="form-item">
          <select class="form-select" v-model="filter.status" @change="load(1)">
            <option value="">全部状态</option>
            <option value="启用">启用</option>
            <option value="停用">停用</option>
          </select>
        </div>
        <div class="form-item">
          <select class="form-select" v-model="filter.role" @change="load(1)">
            <option value="">全部角色</option>
            <option value="SUPER_ADMIN">超级管理员</option>
            <option value="ADMIN">管理员</option>
            <option value="OPERATOR">运营</option>
            <option value="SELLER">售票员</option>
            <option value="VERIFIER">检票员</option>
            <option value="FINANCE">财务</option>
            <option value="STAFF">普通员工</option>
          </select>
        </div>
        <button class="btn btn-default" @click="load(1)">查询</button>
        <button class="btn btn-default" @click="resetFilter">重置</button>
        <div style="flex: 1;"></div>
        <button class="btn btn-primary" @click="openCreate">
          <svg width="13" height="13" viewBox="0 0 16 16" fill="currentColor"><path d="M8 4a.5.5 0 01.5.5v3h3a.5.5 0 010 1h-3v3a.5.5 0 01-1 0v-3h-3a.5.5 0 010-1h3v-3A.5.5 0 018 4z"/></svg>
          新增账号
        </button>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <span class="card-title">后台账号列表</span>
        <span class="card-subtitle">共 {{ total }} 个账号</span>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th style="width: 60px;">ID</th>
              <th>登录账号</th>
              <th>姓名</th>
              <th>手机</th>
              <th>邮箱</th>
              <th>主角色</th>
              <th>管辖园区</th>
              <th>状态</th>
              <th>最近登录</th>
              <th style="width: 220px;">操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="10" class="empty-state">加载中...</td>
            </tr>
            <tr v-else-if="!records.length">
              <td colspan="10" class="empty-state">暂无账号数据</td>
            </tr>
            <tr v-for="u in records" v-else :key="u.id">
              <td>{{ u.id }}</td>
              <td style="font-weight: 500;">{{ u.username }}</td>
              <td>{{ u.realName }}</td>
              <td>{{ u.phone || '—' }}</td>
              <td>{{ u.email || '—' }}</td>
              <td>
                <span class="tag" :class="roleTagClass(u.role)">{{ roleLabelOf(u.role) }}</span>
                <span
                  v-if="u.roleList && u.roleList.length > 1"
                  class="role-extra"
                  :title="extraRoleTooltip(u)"
                >
                  +{{ u.roleList.length - 1 }}
                </span>
              </td>
              <td>
                <span v-if="!u.scenicIdList || u.scenicIdList.length === 0" class="tag tag-blue">全部</span>
                <span v-else class="tag tag-gray">{{ u.scenicIdList.join(' / ') }}</span>
              </td>
              <td>
                <span class="tag" :class="u.status === '启用' ? 'tag-green' : 'tag-gray'">{{ u.status }}</span>
              </td>
              <td class="text-muted">
                <div v-if="u.lastLoginAt">{{ formatDateTime(u.lastLoginAt) }}</div>
                <div v-if="u.lastLoginIp" style="font-size: 11px; color: var(--color-gray-400);">IP {{ u.lastLoginIp }}</div>
                <div v-else>—</div>
              </td>
              <td>
                <div class="row-actions">
                  <span class="action-link" @click="openEdit(u)">编辑</span>
                  <span class="action-link" @click="openResetPwd(u)">重置密码</span>
                  <span
                    v-if="u.status === '启用'"
                    class="action-link danger"
                    @click="toggleStatus(u, '停用')"
                  >停用</span>
                  <span
                    v-else
                    class="action-link"
                    @click="toggleStatus(u, '启用')"
                  >启用</span>
                  <span class="action-link danger" @click="remove(u)">删除</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div v-if="total > filter.pageSize" class="pagination">
        <span class="pagination-info">共 {{ total }} 条 / 第 {{ filter.pageNum }} 页</span>
        <button class="page-btn" :disabled="filter.pageNum <= 1" @click="load(filter.pageNum - 1)">上一页</button>
        <span class="page-btn active">{{ filter.pageNum }}</span>
        <button class="page-btn" :disabled="filter.pageNum * filter.pageSize >= total" @click="load(filter.pageNum + 1)">下一页</button>
      </div>
    </div>

    <!-- 新增 / 编辑弹窗 -->
    <div v-if="showModal" class="modal-mask" @click.self="closeModal">
      <div class="modal-box" style="width: 560px;">
        <div class="modal-header">
          <div class="modal-title">{{ form.id ? '编辑账号' : '新增账号' }}</div>
          <button class="modal-close" @click="closeModal">×</button>
        </div>
        <div class="modal-body form-vertical">
          <div class="form-item" :class="{ 'has-error': formErrors.username }">
            <label class="form-label">登录账号 *</label>
            <input
              class="form-input"
              v-model="form.username"
              :disabled="!!form.id"
              placeholder="字母开头，3-32 位字母数字下划线"
            />
            <div v-if="formErrors.username" class="form-error">{{ formErrors.username }}</div>
          </div>
          <div class="form-item" :class="{ 'has-error': formErrors.password }">
            <label class="form-label">密码 {{ form.id ? '（留空表示不修改）' : '*' }}</label>
            <input
              class="form-input"
              type="password"
              v-model="form.password"
              :placeholder="form.id ? '留空表示不修改' : '6-64 位字母数字'"
            />
            <div v-if="formErrors.password" class="form-error">{{ formErrors.password }}</div>
          </div>
          <div class="form-item">
            <label class="form-label">姓名 *</label>
            <input class="form-input" v-model="form.realName" placeholder="真实姓名" />
          </div>
          <div class="form-row">
            <div class="form-item" style="flex: 1;">
              <label class="form-label">手机号</label>
              <input class="form-input" v-model="form.phone" placeholder="选填" />
            </div>
            <div class="form-item" style="flex: 1;">
              <label class="form-label">邮箱</label>
              <input class="form-input" v-model="form.email" placeholder="选填" />
            </div>
          </div>
          <div class="form-item">
            <label class="form-label">主角色 *</label>
            <select class="form-select" v-model="form.role">
              <option value="SUPER_ADMIN">超级管理员</option>
              <option value="ADMIN">管理员</option>
              <option value="OPERATOR">运营</option>
              <option value="SELLER">售票员</option>
              <option value="VERIFIER">检票员</option>
              <option value="FINANCE">财务</option>
              <option value="STAFF">普通员工</option>
            </select>
          </div>
          <div class="form-item">
            <label class="form-label">额外角色（可多选）</label>
            <div class="checkbox-group">
              <label
                v-for="r in extraRoleOptions"
                :key="r.value"
                class="checkbox-item"
              >
                <input
                  type="checkbox"
                  :value="r.value"
                  v-model="form.extraRolesList"
                  :disabled="r.value === form.role"
                />
                {{ r.label }}
              </label>
            </div>
          </div>
          <div class="form-item">
            <label class="form-label">管辖园区</label>
            <input
              class="form-input"
              v-model="form.scenicIds"
              placeholder="逗号分隔的园区 ID（留空 = 全部）"
            />
            <div style="font-size: 11px; color: var(--color-text-muted); margin-top: 4px;">
              示例：1,2,3。留空表示可访问所有园区。
            </div>
          </div>
          <div class="form-item">
            <label class="form-label">状态</label>
            <select class="form-select" v-model="form.status">
              <option value="启用">启用</option>
              <option value="停用">停用</option>
            </select>
          </div>
          <div class="form-item">
            <label class="form-label">备注</label>
            <textarea class="form-textarea" v-model="form.remark" placeholder="选填" rows="2"></textarea>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="closeModal">取消</button>
          <button class="btn btn-primary" :disabled="submitting" @click="submitForm">
            {{ submitting ? '保存中...' : '保 存' }}
          </button>
        </div>
      </div>
    </div>

    <!-- 重置密码弹窗 -->
    <div v-if="showResetModal" class="modal-mask" @click.self="showResetModal = false">
      <div class="modal-box" style="width: 420px;">
        <div class="modal-header">
          <div class="modal-title">重置密码</div>
          <button class="modal-close" @click="showResetModal = false">×</button>
        </div>
        <div class="modal-body form-vertical">
          <div style="margin-bottom: 12px; color: var(--color-text-secondary); font-size: 13px;">
            即将重置账号 <b>{{ resetTarget?.username }}</b> ({{ resetTarget?.realName }}) 的登录密码。
          </div>
          <div class="form-item" :class="{ 'has-error': resetError }">
            <label class="form-label">新密码 *</label>
            <input class="form-input" type="text" v-model="resetNewPwd" placeholder="6-64 位字母数字" />
            <div v-if="resetError" class="form-error">{{ resetError }}</div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="showResetModal = false">取消</button>
          <button class="btn btn-primary" :disabled="resetSubmitting" @click="submitResetPwd">
            {{ resetSubmitting ? '提交中...' : '确认重置' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import {
  listAccounts,
  createAccount,
  updateAccount,
  deleteAccount,
  toggleAccountStatus,
  resetAccountPassword,
} from '../api/account'
import { userStore } from '../store/user'
import { roleLabel } from '../store/user'
import { ElMessage } from './ui/Message'

const loading = ref(false)
const submitting = ref(false)
const records = ref([])
const total = ref(0)

const filter = reactive({
  keyword: '',
  status: '',
  role: '',
  pageNum: 1,
  pageSize: 10,
})

function resetFilter() {
  filter.keyword = ''
  filter.status = ''
  filter.role = ''
  load(1)
}

async function load(page) {
  if (page) filter.pageNum = page
  loading.value = true
  try {
    const data = await listAccounts({
      keyword: filter.keyword || undefined,
      status: filter.status || undefined,
      role: filter.role || undefined,
      pageNum: filter.pageNum,
      pageSize: filter.pageSize,
    })
    records.value = data?.records || []
    total.value = data?.total || 0
  } catch (e) {
    // request.js 已弹窗
  } finally {
    loading.value = false
  }
}

onMounted(() => load(1))

function roleLabelOf(code) {
  return roleLabel(code)
}

function roleTagClass(code) {
  return {
    SUPER_ADMIN: 'tag-red',
    ADMIN: 'tag-blue',
    OPERATOR: 'tag-blue',
    SELLER: 'tag-green',
    VERIFIER: 'tag-orange',
    FINANCE: 'tag-yellow',
    STAFF: 'tag-gray',
  }[code] || 'tag-gray'
}

/** 主角色右侧 +N 的悬浮提示：列出所有额外角色 */
function extraRoleTooltip(u) {
  if (!u.roleList || u.roleList.length <= 1) return ''
  const extras = u.roleList
    .filter((r) => r !== u.role)
    .map((r) => roleLabel(r))
    .join('、')
  return `额外角色：${extras}`
}

function formatDateTime(s) {
  if (!s) return '—'
  return String(s).replace('T', ' ').slice(0, 19)
}

// ===== 新增 / 编辑弹窗 =====
const showModal = ref(false)
const form = ref({})
const formErrors = ref({})
const extraRoleOptions = [
  { value: 'SUPER_ADMIN', label: '超级管理员' },
  { value: 'ADMIN', label: '管理员' },
  { value: 'OPERATOR', label: '运营' },
  { value: 'SELLER', label: '售票员' },
  { value: 'VERIFIER', label: '检票员' },
  { value: 'FINANCE', label: '财务' },
  { value: 'STAFF', label: '普通员工' },
]

function openCreate() {
  form.value = {
    id: null,
    username: '',
    password: '',
    realName: '',
    phone: '',
    email: '',
    role: 'STAFF',
    extraRolesList: [],
    scenicIds: '',
    status: '启用',
    remark: '',
  }
  formErrors.value = {}
  showModal.value = true
}

function openEdit(u) {
  // 解析 roleCodes 中除主角色外的额外角色
  const allRoles = u.roleList || []
  const extra = allRoles.filter((r) => r !== u.role)
  form.value = {
    id: u.id,
    username: u.username,
    password: '',
    realName: u.realName,
    phone: u.phone || '',
    email: u.email || '',
    role: u.role,
    extraRolesList: extra,
    scenicIds: (u.scenicIdList || []).join(','),
    status: u.status,
    remark: u.remark || '',
  }
  formErrors.value = {}
  showModal.value = true
}

function closeModal() {
  showModal.value = false
}

function validateForm() {
  const err = {}
  const f = form.value
  if (!f.username || !/^[A-Za-z][A-Za-z0-9_]{2,31}$/.test(f.username)) {
    err.username = '账号必须以字母开头，仅含字母数字下划线，3-32 位'
  }
  if (!f.id) {
    if (!f.password) err.password = '请输入密码'
    else if (f.password.length < 6 || f.password.length > 64) err.password = '密码长度需 6-64 位'
  } else if (f.password) {
    if (f.password.length < 6 || f.password.length > 64) err.password = '密码长度需 6-64 位'
  }
  if (!f.realName) err.realName = '请输入姓名'
  formErrors.value = err
  return Object.keys(err).length === 0
}

async function submitForm() {
  if (!validateForm() || submitting.value) return
  submitting.value = true
  try {
    const payload = {
      username: form.value.username.trim(),
      realName: form.value.realName.trim(),
      phone: form.value.phone?.trim() || '',
      email: form.value.email?.trim() || '',
      role: form.value.role,
      extraRoles: (form.value.extraRolesList || []).join(','),
      scenicIds: form.value.scenicIds?.trim() || '',
      status: form.value.status,
      remark: form.value.remark || '',
    }
    if (form.value.password) payload.password = form.value.password
    if (form.value.id) {
      await updateAccount(form.value.id, payload)
      ElMessage({ type: 'success', message: '更新成功' })
    } else {
      payload.password = form.value.password
      await createAccount(payload)
      ElMessage({ type: 'success', message: '新建成功' })
    }
    closeModal()
    load(1)
  } catch (e) {
    // request.js 已弹窗
  } finally {
    submitting.value = false
  }
}

// ===== 启停 =====
async function toggleStatus(u, target) {
  if (u.id === userStore.user?.id && target === '停用') {
    ElMessage({ type: 'warning', message: '不能停用自己的账号' })
    return
  }
  try {
    await toggleAccountStatus(u.id, target)
    ElMessage({ type: 'success', message: '状态已更新' })
    u.status = target
  } catch (e) {}
}

// ===== 删除 =====
async function remove(u) {
  if (u.id === userStore.user?.id) {
    ElMessage({ type: 'warning', message: '不能删除自己的账号' })
    return
  }
  if (u.role === 'SUPER_ADMIN') {
    ElMessage({ type: 'warning', message: '超级管理员账号不可删除' })
    return
  }
  if (!confirm(`确定删除账号 ${u.username} (${u.realName}) 吗？该操作不可恢复。`)) return
  try {
    await deleteAccount(u.id)
    ElMessage({ type: 'success', message: '已删除' })
    load()
  } catch (e) {}
}

// ===== 重置密码 =====
const showResetModal = ref(false)
const resetTarget = ref(null)
const resetNewPwd = ref('')
const resetError = ref('')
const resetSubmitting = ref(false)

function openResetPwd(u) {
  resetTarget.value = u
  resetNewPwd.value = ''
  resetError.value = ''
  showResetModal.value = true
}

async function submitResetPwd() {
  const p = resetNewPwd.value
  if (!p) {
    resetError.value = '请输入新密码'
    return
  }
  if (p.length < 6 || p.length > 64) {
    resetError.value = '密码长度需 6-64 位'
    return
  }
  resetError.value = ''
  resetSubmitting.value = true
  try {
    await resetAccountPassword(resetTarget.value.id, p)
    ElMessage({ type: 'success', message: '密码已重置' })
    showResetModal.value = false
  } catch (e) {} finally {
    resetSubmitting.value = false
  }
}
</script>

<style scoped>
.card-subtitle {
  font-size: 12px;
  color: var(--color-text-muted);
  font-weight: 400;
}
.text-muted { color: var(--color-text-muted); font-size: 12px; }

/* 表格行内操作列：水平排列 + 间距 + 分隔线，避免挤在一起 */
.row-actions {
  display: inline-flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px 0;
  white-space: nowrap;
}
.row-actions .action-link {
  padding: 0 10px;
  line-height: 14px;
}
.row-actions .action-link + .action-link {
  border-left: 1px solid var(--color-border);
}
.row-actions .action-link:first-child { padding-left: 0; }
.row-actions .action-link:last-child { padding-right: 0; }

.role-extra {
  display: inline-block;
  margin-left: 4px;
  padding: 0 5px;
  background: var(--color-gray-100);
  color: var(--color-text-secondary);
  font-size: 11px;
  border-radius: 3px;
  font-weight: 500;
}
.has-error .form-input,
.has-error .form-select,
.has-error .form-textarea { border-color: var(--color-red); }
.form-error {
  font-size: 12px;
  color: var(--color-red);
  margin-top: 4px;
}
.checkbox-group {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  padding: 4px 0;
}
.checkbox-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: var(--color-text-primary);
  cursor: pointer;
  user-select: none;
}
.checkbox-item input { cursor: pointer; }
.checkbox-item input:disabled { cursor: not-allowed; }
</style>
