// 轻量级 user store：基于 reactive 单例，替代 Pinia
// 提供：登录态、当前用户、token、scenic 范围、role 列表
// 持久化：localStorage（key=auth.session）
import { reactive } from 'vue'

const STORAGE_KEY = 'auth.session'

function readFromStorage() {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return null
    const data = JSON.parse(raw)
    // 简单过期校验（仅前端粗粒度校验；后端会以 JWT 过期为准）
    if (data.tokenExpireAt && data.tokenExpireAt < Date.now()) {
      return null
    }
    return data
  } catch (e) {
    return null
  }
}

function writeToStorage(data) {
  if (data) {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(data))
  } else {
    localStorage.removeItem(STORAGE_KEY)
  }
}

const initial = readFromStorage() || {}

export const userStore = reactive({
  token: initial.token || '',
  tokenExpireAt: initial.tokenExpireAt || 0,
  user: initial.user || null,
  // 可管辖园区 ID 列表
  scenicIdList: initial.scenicIdList || null,
  // 当前用户所有角色编码
  roleList: initial.roleList || [],

  /** 是否已登录 */
  get isLoggedIn() {
    return !!this.token && (!this.tokenExpireAt || this.tokenExpireAt > Date.now())
  },

  /** 是否超级管理员 */
  get isSuperAdmin() {
    return (this.roleList || []).includes('SUPER_ADMIN')
  },

  /** 是否拥有任一角色（超级管理员始终为 true） */
  hasAnyRole(roles) {
    if (this.isSuperAdmin) return true
    if (!roles || !roles.length) return true
    return roles.some((r) => (this.roleList || []).includes(r))
  },

  /** 是否能访问全部园区（scenicIdList 为空/null 即表示全部） */
  get isAllScenic() {
    return !this.scenicIdList || this.scenicIdList.length === 0
  },

  /** 是否能访问指定园区 */
  canAccessScenic(scenicId) {
    if (this.isAllScenic) return true
    return this.scenicIdList.includes(Number(scenicId))
  },

  /** 写入登录会话（来自 /api/auth/login 响应） */
  setSession(payload) {
    this.token = payload.token
    this.tokenExpireAt = payload.tokenExpireAt || 0
    this.user = payload.user || null
    this.scenicIdList = payload.scenicIdList || null
    this.roleList = payload.roleList || []
    writeToStorage({
      token: this.token,
      tokenExpireAt: this.tokenExpireAt,
      user: this.user,
      scenicIdList: this.scenicIdList,
      roleList: this.roleList,
    })
  },

  /** 更新当前用户（来自 /api/auth/me） */
  setUser(user) {
    this.user = user
    this.roleList = user?.roleList || []
    this.scenicIdList = user?.scenicIdList || null
    writeToStorage({
      token: this.token,
      tokenExpireAt: this.tokenExpireAt,
      user: this.user,
      scenicIdList: this.scenicIdList,
      roleList: this.roleList,
    })
  },

  /** 清除会话 */
  clear() {
    this.token = ''
    this.tokenExpireAt = 0
    this.user = null
    this.scenicIdList = null
    this.roleList = []
    writeToStorage(null)
  },
})

/** 角色 / 状态等枚举的友好映射（前端展示用） */
export const ROLE_LABELS = {
  SUPER_ADMIN: '超级管理员',
  ADMIN: '管理员',
  OPERATOR: '运营',
  SELLER: '售票员',
  VERIFIER: '检票员',
  FINANCE: '财务',
  STAFF: '普通员工',
}

export function roleLabel(code) {
  return ROLE_LABELS[code] || code || '-'
}

export const STATUS_LABELS = {
  启用: 'tag-green',
  停用: 'tag-gray',
}
