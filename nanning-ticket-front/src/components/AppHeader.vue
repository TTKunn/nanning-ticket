<template>
  <header class="layout-header">
    <div class="header-breadcrumb">
      <span>AI南宁票务</span>
      <span class="header-breadcrumb-sep">/</span>
      <span class="header-breadcrumb-current">{{ title }}</span>
    </div>
    <div class="header-spacer"></div>
    <div class="header-actions">
      <!-- 通知 -->
      <button class="header-icon-btn" title="通知">
        <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor">
          <path d="M8 16a2 2 0 002-2H6a2 2 0 002 2zm.995-14.901a1 1 0 10-1.99 0A5.002 5.002 0 003 6c0 1.098-.5 6-2 7h14c-1.5-1-2-5.902-2-7 0-2.42-1.72-4.44-4.005-4.901z"/>
        </svg>
        <span class="dot"></span>
      </button>
      <!-- 刷新 -->
      <button class="header-icon-btn" title="刷新" @click="$emit('refresh')">
        <svg width="15" height="15" viewBox="0 0 16 16" fill="currentColor">
          <path fill-rule="evenodd" d="M8 3a5 5 0 104.546 2.914.5.5 0 00-.908-.417A4 4 0 118 4v1.5l2-2-2-2V3z"/>
        </svg>
      </button>
      <div style="width:1px;height:20px;background:var(--color-border);"></div>

      <!-- 用户信息 + 下拉菜单 -->
      <div class="header-user-block" @click="toggleMenu" v-click-outside="closeMenu">
        <div class="header-avatar">{{ avatarText }}</div>
        <div class="header-user-info">
          <span class="header-user-name">{{ user?.realName || user?.username || '未登录' }}</span>
          <span class="header-user-role">{{ roleLabel || '—' }}</span>
        </div>
        <svg width="10" height="10" viewBox="0 0 16 16" fill="currentColor" class="header-user-caret">
          <path d="M3.204 5h9.592L8 10.481 3.204 5zm-.753.659l4.796 5.48a1 1 0 001.506 0l4.796-5.48c.566-.647.106-1.659-.753-1.659H3.204a1 1 0 00-.753 1.659z"/>
        </svg>
        <div v-if="menuOpen" class="header-user-menu" @click.stop>
          <div class="header-user-menu-header">
            <div class="header-user-menu-name">{{ user?.realName || '—' }}</div>
            <div class="header-user-menu-sub">@{{ user?.username }}</div>
            <div v-if="user?.scenicIdList && user.scenicIdList.length" class="header-user-menu-tip">
              管辖园区：{{ user.scenicIdList.join(' / ') }}
            </div>
            <div v-else class="header-user-menu-tip">管辖园区：全部</div>
          </div>
          <div class="header-user-menu-divider"></div>
          <div class="header-user-menu-item" @click="emitCmd('changePassword')">
            <svg width="13" height="13" viewBox="0 0 16 16" fill="currentColor">
              <path d="M3 7V5a3 3 0 016 0v2h1a1 1 0 011 1v6a1 1 0 01-1 1H2a1 1 0 01-1-1V8a1 1 0 011-1h1zm2 0h4V5a2 2 0 10-4 0v2z"/>
            </svg>
            修改密码
          </div>
          <div v-if="canManageAccounts" class="header-user-menu-item" @click="emitCmd('accounts')">
            <svg width="13" height="13" viewBox="0 0 16 16" fill="currentColor">
              <path d="M7 14s-1 0-1-1 1-4 5-4 5 3 5 4-1 1-1 1H7zm4-6a3 3 0 100-6 3 3 0 000 6z"/>
            </svg>
            账号管理
          </div>
          <div class="header-user-menu-divider"></div>
          <div class="header-user-menu-item danger" @click="emitCmd('logout')">
            <svg width="13" height="13" viewBox="0 0 16 16" fill="currentColor">
              <path d="M10 12.5l1.5-1.5L9.5 9H14V7H9.5l2-2L10 3.5 6 7.5 10 11.5zM3 1h6v2H3v10h6v2H1V1h2z"/>
            </svg>
            退出登录
          </div>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed, ref } from 'vue'
import { userStore } from '../store/user'

const props = defineProps({
  title: { type: String, default: '数据概览' },
  user: { type: Object, default: null },
  roleLabel: { type: String, default: '' },
})
const emit = defineEmits(['command', 'refresh'])

const menuOpen = ref(false)
function toggleMenu() { menuOpen.value = !menuOpen.value }
function closeMenu() { menuOpen.value = false }
function emitCmd(cmd) {
  menuOpen.value = false
  emit('command', cmd)
}

const user = computed(() => userStore.user)
const roleLabel = computed(() => props.roleLabel)
const canManageAccounts = computed(() => userStore.hasAnyRole(['SUPER_ADMIN', 'ADMIN']))

const avatarText = computed(() => {
  const u = userStore.user
  if (!u) return '?'
  // 中文姓名取最后一个字；英文取首字母
  const name = u.realName || u.username || '?'
  const trimmed = name.trim()
  if (/[一-龥]/.test(trimmed)) {
    return trimmed.slice(-1)
  }
  return trimmed.slice(0, 1).toUpperCase()
})

// v-click-outside 指令（局部）
const vClickOutside = {
  mounted(el, binding) {
    el.__clickOutside__ = (event) => {
      if (!(el === event.target || el.contains(event.target))) {
        binding.value(event)
      }
    }
    document.addEventListener('click', el.__clickOutside__)
  },
  unmounted(el) {
    document.removeEventListener('click', el.__clickOutside__)
  },
}
</script>

<style scoped>
.header-user-block {
  position: relative;
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 4px 8px;
  border-radius: var(--radius);
  cursor: pointer;
  transition: background 0.15s;
}
.header-user-block:hover { background: var(--color-gray-100); }
.header-user-caret { color: var(--color-text-muted); }

.header-user-menu {
  position: absolute;
  top: calc(100% + 6px);
  right: 0;
  min-width: 220px;
  background: #ffffff;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  box-shadow: var(--shadow-md);
  z-index: 100;
  padding: 4px 0;
  font-size: 13px;
}
.header-user-menu-header {
  padding: 10px 14px 8px;
}
.header-user-menu-name {
  font-weight: 600;
  color: var(--color-text-primary);
  font-size: 13px;
}
.header-user-menu-sub {
  color: var(--color-text-muted);
  font-size: 12px;
  margin-top: 1px;
}
.header-user-menu-tip {
  color: var(--color-text-muted);
  font-size: 11px;
  margin-top: 6px;
}
.header-user-menu-divider {
  height: 1px;
  background: var(--color-border);
  margin: 4px 0;
}
.header-user-menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 14px;
  color: var(--color-text-primary);
  cursor: pointer;
  transition: background 0.1s;
}
.header-user-menu-item:hover { background: var(--color-gray-50); }
.header-user-menu-item.danger { color: var(--color-red); }
.header-user-menu-item.danger:hover { background: var(--color-red-light); }
.header-user-menu-item svg { color: inherit; opacity: 0.7; flex-shrink: 0; }
</style>
