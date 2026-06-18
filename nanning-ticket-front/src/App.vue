<template>
  <div id="app">
    <!-- 登录页（独立路由） -->
    <PageLogin v-if="isLoginRoute" />

    <!-- 主壳（侧边栏 + 头部 + 业务页） -->
    <template v-else>
      <Sidebar :current="currentPage" @navigate="navigate" />
      <div class="layout-main">
        <AppHeader
          :title="pageTitle"
          :user="userStore.user"
          :role-label="roleLabelOf(userStore.user)"
          @command="onUserCommand"
        />
        <div class="layout-content">
          <div class="page-header">
            <div>
              <div class="page-title">{{ pageTitle }}</div>
              <div class="page-subtitle">{{ pageSubtitle }}</div>
            </div>
          </div>
          <PageDashboard v-if="currentPage === 'dashboard'" @navigate="navigate" />
          <PageScenic v-else-if="currentPage === 'scenic'" />
          <PageRules v-else-if="currentPage === 'rules'" />
          <PageTickets v-else-if="currentPage === 'tickets'" />
          <PageInventory v-else-if="currentPage === 'inventory'" />
          <PageSales v-else-if="currentPage === 'sales'" />
          <PageVerify v-else-if="currentPage === 'verify'" />
          <PageVouchers v-else-if="currentPage === 'vouchers'" />
          <PageOrders v-else-if="currentPage === 'orders'" />
          <PageChannels v-else-if="currentPage === 'channels'" />
          <PageReports v-else-if="currentPage === 'reports'" />
          <PageSettings v-else-if="currentPage === 'settings'" />
          <PageAccounts v-else-if="currentPage === 'accounts'" />
        </div>
      </div>
    </template>

    <!-- 修改密码弹窗 -->
    <div v-if="showPasswordModal" class="modal-mask" @click.self="closePasswordModal">
      <div class="modal-box" style="width: 460px;">
        <div class="modal-header">
          <div class="modal-title">修改密码</div>
          <button class="modal-close" @click="closePasswordModal">×</button>
        </div>
        <div class="modal-body form-vertical">
          <div class="form-item" :class="{ 'has-error': pwdErrors.oldPassword }">
            <label class="form-label">原密码</label>
            <input class="form-input" type="password" v-model="pwdForm.oldPassword" placeholder="请输入原密码" />
            <div v-if="pwdErrors.oldPassword" class="form-error">{{ pwdErrors.oldPassword }}</div>
          </div>
          <div class="form-item" :class="{ 'has-error': pwdErrors.newPassword }">
            <label class="form-label">新密码</label>
            <input class="form-input" type="password" v-model="pwdForm.newPassword" placeholder="6-64 位字母数字" />
            <div v-if="pwdErrors.newPassword" class="form-error">{{ pwdErrors.newPassword }}</div>
          </div>
          <div class="form-item" :class="{ 'has-error': pwdErrors.confirmPassword }">
            <label class="form-label">确认新密码</label>
            <input class="form-input" type="password" v-model="pwdForm.confirmPassword" placeholder="请再次输入新密码" />
            <div v-if="pwdErrors.confirmPassword" class="form-error">{{ pwdErrors.confirmPassword }}</div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="closePasswordModal">取消</button>
          <button class="btn btn-primary" :disabled="pwdSubmitting" @click="submitChangePassword">
            {{ pwdSubmitting ? '提交中...' : '确认修改' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import Sidebar from './components/Sidebar.vue'
import AppHeader from './components/AppHeader.vue'
import PageDashboard from './components/PageDashboard.vue'
import PageScenic from './components/PageScenic.vue'
import PageRules from './components/PageRules.vue'
import PageTickets from './components/PageTickets.vue'
import PageInventory from './components/PageInventory.vue'
import PageSales from './components/PageSales.vue'
import PageVerify from './components/PageVerify.vue'
import PageVouchers from './components/PageVouchers.vue'
import PageOrders from './components/PageOrders.vue'
import PageChannels from './components/PageChannels.vue'
import PageReports from './components/PageReports.vue'
import PageSettings from './components/PageSettings.vue'
import PageAccounts from './components/PageAccounts.vue'
import PageLogin from './components/PageLogin.vue'
import { userStore, roleLabel } from './store/user'
import { useRouter } from './composables/useRouter'
import { logout as apiLogout, changePassword } from './api/auth'
import { fetchMe } from './api/auth'
import { ElMessage } from './components/ui/Message'

const router = useRouter()

// 路由判断：/login 显示登录页
const isLoginRoute = computed(() => {
  return router.currentRoute.value === '/login'
})

// 主壳内的 currentPage
const currentPage = ref(parseRouteToPage(router.currentRoute.value))

function parseRouteToPage(route) {
  if (!route || route === '/' || route === '') return 'dashboard'
  const path = route.replace(/^\//, '')
  if (!path || path === 'dashboard') return 'dashboard'
  return path
}

watch(
  () => router.currentRoute.value,
  (val) => {
    if (val === '/login') return
    currentPage.value = parseRouteToPage(val)
  },
)

// 路由守卫：未登录访问主壳 → 跳登录
onMounted(async () => {
  // 首次加载：若处于 /login 不必校验；否则校验登录态
  if (router.currentRoute.value === '/login') {
    // 已登录用户访问 /login → 直接跳到首页
    if (userStore.isLoggedIn) {
      location.hash = '#/'
    }
    return
  }
  if (!userStore.isLoggedIn) {
    sessionStorage.setItem('loginNext', '#' + router.currentRoute.value)
    location.hash = '#/login'
    return
  }
  // 已登录 → 拉一次最新用户信息（可选失败容错）
  try {
    const me = await fetchMe()
    if (me) userStore.setUser(me)
  } catch (e) {
    // 已被 request.js 统一处理
  }
})

// 监听路由变化：主壳跳 /login 时清空状态
watch(
  () => router.currentRoute.value,
  (val) => {
    if (val === '/login' && userStore.isLoggedIn) {
      // 已登录却进入 /login，回首页
      location.hash = '#/'
    }
  },
)

const pageMap = {
  dashboard: { title: '数据概览', subtitle: '查看园区票务经营总览与预警信息' },
  scenic: { title: '园区管理', subtitle: '维护园区基础信息、收费项目与规则入口' },
  rules: { title: '项目规则配置', subtitle: '配置门票、游玩票、全包票等收费规则与检退票逻辑' },
  tickets: { title: '票种管理', subtitle: '维护票种分组、库存方式、购票限制与渠道发布' },
  inventory: { title: '库存管理', subtitle: '按总库存和日历库存双维度管理可售数量' },
  sales: { title: '门票售票', subtitle: '供业务人员进行窗口售票、出票与现场收款操作' },
  verify: { title: '门票检票', subtitle: '扫码检票、手工检票与检票记录查询' },
  vouchers: { title: '票据管理', subtitle: '统一管理系统直销与分销平台出票票据状态' },
  orders: { title: '订单管理', subtitle: '查看订单、退款状态与各渠道购票记录' },
  channels: { title: '渠道管理', subtitle: '维护分销渠道、票种分发和结算关系' },
  reports: { title: '数据报表', subtitle: '统计销售、出票、退款、核销等经营数据' },
  settings: { title: '系统设置', subtitle: '维护全局参数、账号、通知与接口配置' },
  accounts: { title: '账号管理', subtitle: '管理系统后台用户、角色与启停状态' },
}

const pageTitle = computed(() => pageMap[currentPage.value]?.title || '')
const pageSubtitle = computed(() => pageMap[currentPage.value]?.subtitle || '')

function navigate(page) {
  currentPage.value = page
  location.hash = '#/' + (page === 'dashboard' ? '' : page)
}

function roleLabelOf(user) {
  if (!user) return ''
  // 主角色在前；若主角色不在映射里再用第一个
  return roleLabel(user.role) || roleLabel((user.roleList || [])[0])
}

// 用户菜单
async function onUserCommand(cmd) {
  if (cmd === 'logout') {
    try {
      await apiLogout()
    } catch (e) {
      // 忽略服务端错误
    }
    userStore.clear()
    ElMessage({ type: 'success', message: '已退出登录' })
    location.hash = '#/login'
  } else if (cmd === 'changePassword') {
    openPasswordModal()
  } else if (cmd === 'accounts') {
    navigate('accounts')
  }
}

// 修改密码弹窗
const showPasswordModal = ref(false)
const pwdSubmitting = ref(false)
const pwdForm = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwdErrors = ref({ oldPassword: '', newPassword: '', confirmPassword: '' })

function openPasswordModal() {
  pwdForm.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  pwdErrors.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
  showPasswordModal.value = true
}
function closePasswordModal() {
  showPasswordModal.value = false
}

function validatePassword() {
  const e = { oldPassword: '', newPassword: '', confirmPassword: '' }
  if (!pwdForm.value.oldPassword) e.oldPassword = '请输入原密码'
  const np = pwdForm.value.newPassword
  if (!np) {
    e.newPassword = '请输入新密码'
  } else if (np.length < 6 || np.length > 64) {
    e.newPassword = '新密码长度需 6-64 位'
  }
  if (pwdForm.value.newPassword !== pwdForm.value.confirmPassword) {
    e.confirmPassword = '两次输入的新密码不一致'
  }
  pwdErrors.value = e
  return !e.oldPassword && !e.newPassword && !e.confirmPassword
}

async function submitChangePassword() {
  if (!validatePassword() || pwdSubmitting.value) return
  pwdSubmitting.value = true
  try {
    await changePassword({
      oldPassword: pwdForm.value.oldPassword,
      newPassword: pwdForm.value.newPassword,
    })
    ElMessage({ type: 'success', message: '密码修改成功，请使用新密码重新登录' })
    closePasswordModal()
    // 修改密码后建议重新登录
    setTimeout(async () => {
      try { await apiLogout() } catch (e) {}
      userStore.clear()
      location.hash = '#/login'
    }, 800)
  } catch (e) {
    // request.js 已弹窗
  } finally {
    pwdSubmitting.value = false
  }
}
</script>
