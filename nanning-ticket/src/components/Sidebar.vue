<template>
  <div class="layout-sidebar">
    <div class="sidebar-logo">
      <div class="sidebar-logo-icon">宁</div>
      <div>
        <div class="sidebar-logo-text">AI南宁票务</div>
        <div class="sidebar-logo-sub">后台管理系统</div>
      </div>
    </div>

    <nav class="sidebar-nav">
      <div class="sidebar-group-label">概览</div>
      <div
        v-for="item in navGroups[0].items"
        :key="item.key"
        class="sidebar-item"
        :class="{ active: current === item.key }"
        @click="$emit('navigate', item.key)"
      >
        <svg class="sidebar-item-icon" viewBox="0 0 16 16" fill="currentColor">
          <path :d="item.icon" />
        </svg>
        {{ item.label }}
      </div>

      <div class="sidebar-group-label">基础配置</div>
      <div
        v-for="item in navGroups[1].items"
        :key="item.key"
        class="sidebar-item"
        :class="{ active: current === item.key }"
        @click="$emit('navigate', item.key)"
      >
        <svg class="sidebar-item-icon" viewBox="0 0 16 16" fill="currentColor">
          <path :d="item.icon" />
        </svg>
        {{ item.label }}
      </div>

      <div class="sidebar-group-label">票务作业</div>
      <div
        v-for="item in navGroups[2].items"
        :key="item.key"
        class="sidebar-item"
        :class="{ active: current === item.key }"
        @click="$emit('navigate', item.key)"
      >
        <svg class="sidebar-item-icon" viewBox="0 0 16 16" fill="currentColor">
          <path :d="item.icon" />
        </svg>
        {{ item.label }}
        <span v-if="item.badge" class="sidebar-badge">{{ item.badge }}</span>
      </div>

      <div class="sidebar-group-label">运营分析</div>
      <div
        v-for="item in navGroups[3].items"
        :key="item.key"
        class="sidebar-item"
        :class="{ active: current === item.key }"
        @click="$emit('navigate', item.key)"
      >
        <svg class="sidebar-item-icon" viewBox="0 0 16 16" fill="currentColor">
          <path :d="item.icon" />
        </svg>
        {{ item.label }}
      </div>

      <div class="sidebar-group-label">系统</div>
      <div
        v-for="item in navGroups[4].items"
        :key="item.key"
        class="sidebar-item"
        :class="{ active: current === item.key }"
        @click="$emit('navigate', item.key)"
      >
        <svg class="sidebar-item-icon" viewBox="0 0 16 16" fill="currentColor">
          <path :d="item.icon" />
        </svg>
        {{ item.label }}
      </div>
    </nav>
  </div>
</template>

<script setup>
defineProps({ current: String })
defineEmits(['navigate'])

const navGroups = [
  {
    items: [
      { key: 'dashboard', label: '数据概览', icon: 'M2 2h5v5H2V2zm7 0h5v5H9V2zM2 9h5v5H2V9zm7 0h5v5H9V9z' },
    ],
  },
  {
    items: [
      { key: 'scenic', label: '园区管理', icon: 'M8 1a7 7 0 100 14A7 7 0 008 1zM0 8a8 8 0 1116 0A8 8 0 010 8zm5.5 0a2.5 2.5 0 115 0 2.5 2.5 0 01-5 0z' },
      { key: 'rules', label: '项目规则配置', icon: 'M2 2.5A1.5 1.5 0 013.5 1h3A1.5 1.5 0 018 2.5V4h4.5A1.5 1.5 0 0114 5.5v7a1.5 1.5 0 01-1.5 1.5h-9A1.5 1.5 0 012 12.5v-10zM9 4h3.5a.5.5 0 01.5.5V6H9V4zm-1 0V2.5a.5.5 0 00-.5-.5h-3a.5.5 0 00-.5.5V6h4V4zm-4 3v5.5a.5.5 0 00.5.5H8V7H4zm5 6h3.5a.5.5 0 00.5-.5V7H9v6z' },
      { key: 'tickets', label: '票种管理', icon: 'M1 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1 1 1 0 000 2 1 1 0 011 1v2a1 1 0 01-1 1H2a1 1 0 01-1-1v-2a1 1 0 011-1 1 1 0 000-2 1 1 0 01-1-1V4z' },
      { key: 'inventory', label: '库存管理', icon: 'M2 1a1 1 0 00-1 1v2a1 1 0 001 1h12a1 1 0 001-1V2a1 1 0 00-1-1H2zm0 5a1 1 0 00-1 1v2a1 1 0 001 1h12a1 1 0 001-1V7a1 1 0 00-1-1H2zm0 5a1 1 0 00-1 1v2a1 1 0 001 1h12a1 1 0 001-1v-2a1 1 0 00-1-1H2z' },
    ],
  },
  {
    items: [
      { key: 'sales', label: '门票售票', icon: 'M8 4a.5.5 0 01.5.5V7H11a.5.5 0 010 1H8.5v2.5a.5.5 0 01-1 0V8H5a.5.5 0 010-1h2.5V4.5A.5.5 0 018 4zM2 2h12a1 1 0 011 1v10a1 1 0 01-1 1H2a1 1 0 01-1-1V3a1 1 0 011-1z' },
      { key: 'verify', label: '门票检票', icon: 'M13.854 3.646a.5.5 0 010 .708l-7 7a.5.5 0 01-.708 0l-3.5-3.5a.5.5 0 11.708-.708L6.5 10.293l6.646-6.647a.5.5 0 01.708 0z' },
      { key: 'vouchers', label: '票据管理', icon: 'M3 2a1 1 0 011-1h8a1 1 0 011 1v12a1 1 0 01-1 1H4a1 1 0 01-1-1V2zm2 1v10h6V3H5zm1 2h4v1H6V5zm0 2h4v1H6V7zm0 2h3v1H6V9z' },
      { key: 'orders', label: '订单管理', icon: 'M2.5 2A1.5 1.5 0 001 3.5v9A1.5 1.5 0 002.5 14h11a1.5 1.5 0 001.5-1.5v-9A1.5 1.5 0 0013.5 2h-11zM2 4h12v1H2V4zm2 3h5v1H4V7zm0 2h7v1H4V9z', badge: '12' },
    ],
  },
  {
    items: [
      { key: 'channels', label: '渠道管理', icon: 'M8 0a8 8 0 100 16A8 8 0 008 0zM4.5 7.5a.5.5 0 000 1h5.793l-2.147 2.146a.5.5 0 00.708.708l3-3a.5.5 0 000-.708l-3-3a.5.5 0 10-.708.708L10.293 7.5H4.5z' },
      { key: 'reports', label: '数据报表', icon: 'M0 0h1v15h15v1H0V0zm10 3.5a.5.5 0 01.5-.5h4a.5.5 0 01.5.5v4a.5.5 0 01-1 0V4.9l-3.613 4.417a.5.5 0 01-.74.037L7.06 6.767l-3.656 5.027a.5.5 0 01-.808-.588l4-5.5a.5.5 0 01.758-.06l2.609 2.61L13.445 4H10.5a.5.5 0 01-.5-.5z' },
    ],
  },
  {
    items: [
      { key: 'settings', label: '系统设置', icon: 'M8 4.754a3.246 3.246 0 100 6.492 3.246 3.246 0 000-6.492zM5.754 8a2.246 2.246 0 114.492 0 2.246 2.246 0 01-4.492 0z M9.796 1.343c-.527-1.79-3.065-1.79-3.592 0l-.094.319a.873.873 0 01-1.255.52l-.292-.16c-1.64-.892-3.433.902-2.54 2.541l.159.292a.873.873 0 01-.52 1.255l-.319.094c-1.79.527-1.79 3.065 0 3.592l.319.094a.873.873 0 01.52 1.255l-.16.292c-.892 1.64.901 3.434 2.541 2.54l.292-.159a.873.873 0 011.255.52l.094.319c.527 1.79 3.065 1.79 3.592 0l.094-.319a.873.873 0 011.255-.52l.292.16c1.64.893 3.434-.902 2.54-2.541l-.159-.292a.873.873 0 01.52-1.255l.319-.094c1.79-.527 1.79-3.065 0-3.592l-.319-.094a.873.873 0 01-.52-1.255l.16-.292c.893-1.64-.902-3.433-2.541-2.54l-.292.159a.873.873 0 01-1.255-.52l-.094-.319z' },
    ],
  },
]
</script>
