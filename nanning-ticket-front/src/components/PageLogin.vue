<template>
  <div class="login-page">
    <div class="login-bg">
      <div class="login-bg-shape shape-1"></div>
      <div class="login-bg-shape shape-2"></div>
      <div class="login-bg-shape shape-3"></div>
    </div>

    <div class="login-container">
      <div class="login-brand">
        <div class="login-brand-icon">宁</div>
        <div class="login-brand-title">AI 南宁票务</div>
        <div class="login-brand-sub">后台管理系统</div>
        <div class="login-brand-tip">统一管理园区、票种、库存、销售、订单与渠道</div>
      </div>

      <div class="login-card">
        <div class="login-title">账号登录</div>
        <div class="login-subtitle">请使用后台账号登录系统</div>

        <form class="login-form" @submit.prevent="onSubmit">
          <div class="form-item" :class="{ 'has-error': errors.username }">
            <label class="form-label">登录账号</label>
            <div class="login-input-wrap">
              <svg width="14" height="14" viewBox="0 0 16 16" fill="currentColor" class="login-input-icon">
                <path d="M8 8a3 3 0 100-6 3 3 0 000 6zM2 14a6 6 0 1112 0H2z"/>
              </svg>
              <input
                class="login-input"
                v-model="form.username"
                type="text"
                placeholder="请输入登录账号"
                autocomplete="username"
                :disabled="submitting"
                @input="clearError('username')"
              />
            </div>
            <div v-if="errors.username" class="form-error">{{ errors.username }}</div>
          </div>

          <div class="form-item" :class="{ 'has-error': errors.password }">
            <label class="form-label">登录密码</label>
            <div class="login-input-wrap">
              <svg width="14" height="14" viewBox="0 0 16 16" fill="currentColor" class="login-input-icon">
                <path d="M3 7V5a3 3 0 016 0v2h1a1 1 0 011 1v6a1 1 0 01-1 1H2a1 1 0 01-1-1V8a1 1 0 011-1h1zm2 0h4V5a2 2 0 10-4 0v2z"/>
              </svg>
              <input
                class="login-input"
                v-model="form.password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="请输入密码"
                autocomplete="current-password"
                :disabled="submitting"
                @input="clearError('password')"
              />
              <button
                type="button"
                class="login-input-action"
                @click="showPassword = !showPassword"
                tabindex="-1"
              >
                {{ showPassword ? '隐藏' : '显示' }}
              </button>
            </div>
            <div v-if="errors.password" class="form-error">{{ errors.password }}</div>
          </div>

          <div v-if="serverError" class="alert alert-error login-server-error">
            <svg width="14" height="14" viewBox="0 0 16 16" fill="currentColor">
              <path d="M16 8A8 8 0 11.001 8 8 8 0 0116 8zM4.646 4.646a.5.5 0 01.708 0L8 7.293l2.646-2.647a.5.5 0 01.708.708L8.707 8l2.647 2.646a.5.5 0 01-.708.708L8 8.707l-2.646 2.647a.5.5 0 01-.708-.708L7.293 8 4.646 5.354a.5.5 0 010-.708z"/>
            </svg>
            <span>{{ serverError }}</span>
          </div>

          <button class="login-btn" type="submit" :disabled="submitting">
            <span v-if="submitting" class="login-btn-spinner"></span>
            {{ submitting ? '登录中...' : '登 录' }}
          </button>

          <div class="login-tips">
            <div class="login-tip-title">演示账号（启动后由 AuthBootstrap 自动生成，密码统一为 123456）：</div>
            <div class="login-tip-row"><b>admin</b> / 123456 （超级管理员）</div>
            <div class="login-tip-row"><b>manager</b> / 123456 （管理员）</div>
            <div class="login-tip-row"><b>seller</b> / 123456 （售票员）</div>
            <div class="login-tip-row"><b>verify</b> / 123456 （检票员）</div>
            <div class="login-tip-row"><b>finance</b> / 123456 （财务）</div>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from '../composables/useRouter'
import { login } from '../api/auth'
import { userStore } from '../store/user'
import { ElMessage } from './ui/Message'

const router = useRouter()
const submitting = ref(false)
const showPassword = ref(false)
const serverError = ref('')
const errors = reactive({ username: '', password: '' })

const form = reactive({
  username: localStorage.getItem('lastUsername') || '',
  password: '',
})

function clearError(field) {
  errors[field] = ''
  serverError.value = ''
}

function validate() {
  let ok = true
  errors.username = ''
  errors.password = ''
  if (!form.username || !form.username.trim()) {
    errors.username = '请输入登录账号'
    ok = false
  }
  if (!form.password) {
    errors.password = '请输入密码'
    ok = false
  } else if (form.password.length < 6) {
    errors.password = '密码长度至少 6 位'
    ok = false
  }
  return ok
}

async function onSubmit() {
  if (!validate() || submitting.value) return
  submitting.value = true
  serverError.value = ''
  try {
    const data = await login({
      username: form.username.trim(),
      password: form.password,
    })
    userStore.setSession(data)
    localStorage.setItem('lastUsername', form.username.trim())
    ElMessage({ type: 'success', message: '登录成功' })
    // 跳转回原页面或首页
    const next = sessionStorage.getItem('loginNext') || '#/'
    sessionStorage.removeItem('loginNext')
    location.hash = next.startsWith('#') ? next : '#/'
  } catch (e) {
    // request.js 已弹窗；登录页只接管 32xx 系列以便自定义文案
    const msg = e?.message || ''
    if (msg.includes('账号或密码') || msg.includes('已停用') || msg.includes('已被锁定')) {
      serverError.value = msg
    } else if (msg) {
      serverError.value = msg
    }
  } finally {
    submitting.value = false
  }
}
</script>

<style scoped>
.login-page {
  position: relative;
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1e3a8a 0%, #2563eb 50%, #0ea5e9 100%);
  overflow: hidden;
}

.login-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}
.login-bg-shape {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);
  filter: blur(2px);
}
.shape-1 { width: 320px; height: 320px; top: -80px; left: -80px; }
.shape-2 { width: 240px; height: 240px; bottom: -60px; right: -60px; background: rgba(255, 255, 255, 0.05); }
.shape-3 { width: 180px; height: 180px; top: 50%; right: 20%; background: rgba(255, 255, 255, 0.04); }

.login-container {
  position: relative;
  z-index: 1;
  width: 880px;
  max-width: 92vw;
  display: flex;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.25);
  overflow: hidden;
  min-height: 520px;
}

.login-brand {
  flex: 1;
  padding: 40px;
  color: #ffffff;
  background: linear-gradient(135deg, #1d4ed8 0%, #2563eb 100%);
  display: flex;
  flex-direction: column;
  justify-content: center;
}
.login-brand-icon {
  width: 56px;
  height: 56px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 24px;
}
.login-brand-title {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 6px;
}
.login-brand-sub {
  font-size: 14px;
  opacity: 0.85;
  margin-bottom: 24px;
}
.login-brand-tip {
  font-size: 13px;
  line-height: 1.7;
  opacity: 0.75;
}

.login-card {
  flex: 1;
  padding: 40px 44px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: #ffffff;
}
.login-title {
  font-size: 22px;
  font-weight: 600;
  color: var(--color-text-primary);
  margin-bottom: 6px;
}
.login-subtitle {
  font-size: 13px;
  color: var(--color-text-muted);
  margin-bottom: 28px;
}

.login-form .form-item {
  margin-bottom: 18px;
}
.login-input-wrap {
  position: relative;
  display: flex;
  align-items: center;
  border: 1px solid var(--color-border-dark);
  border-radius: var(--radius);
  background: #ffffff;
  transition: border-color 0.15s, box-shadow 0.15s;
  height: 40px;
  padding: 0 10px;
}
.login-input-wrap:focus-within {
  border-color: var(--color-blue);
  box-shadow: 0 0 0 2px rgba(37, 99, 235, 0.1);
}
.login-input-icon {
  flex-shrink: 0;
  color: var(--color-text-muted);
  margin-right: 8px;
}
.login-input {
  flex: 1;
  height: 100%;
  border: none;
  outline: none;
  background: transparent;
  font-size: 14px;
  color: var(--color-text-primary);
  min-width: 0;
}
.login-input::placeholder {
  color: var(--color-gray-400);
}
.login-input-action {
  background: transparent;
  border: none;
  color: var(--color-blue);
  font-size: 12px;
  cursor: pointer;
  padding: 0 4px;
  height: 24px;
}
.login-input-action:hover {
  color: var(--color-blue-dark);
}

.has-error .login-input-wrap {
  border-color: var(--color-red);
}
.form-error {
  font-size: 12px;
  color: var(--color-red);
  margin-top: 4px;
}
.login-server-error {
  margin-bottom: 12px;
}

.login-btn {
  width: 100%;
  height: 42px;
  background: var(--color-blue);
  color: #ffffff;
  border: none;
  border-radius: var(--radius);
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: background 0.15s;
  margin-top: 6px;
}
.login-btn:hover:not(:disabled) {
  background: var(--color-blue-dark);
}
.login-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}
.login-btn-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid rgba(255, 255, 255, 0.4);
  border-top-color: #ffffff;
  border-radius: 50%;
  animation: login-spin 0.8s linear infinite;
}
@keyframes login-spin {
  to { transform: rotate(360deg); }
}

.login-tips {
  margin-top: 22px;
  padding: 12px 14px;
  background: var(--color-blue-light);
  border: 1px solid var(--color-blue-border);
  border-radius: var(--radius);
  font-size: 12px;
  color: var(--color-text-secondary);
  line-height: 1.7;
}
.login-tip-title {
  color: var(--color-blue);
  font-weight: 500;
  margin-bottom: 4px;
}
.login-tip-row b {
  color: var(--color-blue-dark);
  font-weight: 600;
}

@media (max-width: 720px) {
  .login-container { flex-direction: column; }
  .login-brand { padding: 28px; }
}
</style>
