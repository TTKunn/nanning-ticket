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
      <button class="header-icon-btn" title="刷新">
        <svg width="15" height="15" viewBox="0 0 16 16" fill="currentColor">
          <path fill-rule="evenodd" d="M8 3a5 5 0 104.546 2.914.5.5 0 00-.908-.417A4 4 0 118 4v1.5l2-2-2-2V3z"/>
        </svg>
      </button>
      <div style="width:1px;height:20px;background:var(--color-border);"></div>
      <div class="header-avatar">管</div>
      <div class="header-user-info">
        <span class="header-user-name">管理员</span>
        <span class="header-user-role">超级管理员</span>
      </div>
    </div>
  </header>
</template>

<script setup>
defineProps({ title: { type: String, default: '数据概览' } })
</script>
