<template>
  <div id="app">
    <Sidebar :current="currentPage" @navigate="navigate" />
    <div class="layout-main">
      <AppHeader :title="pageTitle" />
      <div class="layout-content">
        <div class="page-header">
          <div>
            <div class="page-title">{{ pageTitle }}</div>
            <div class="page-subtitle">{{ pageSubtitle }}</div>
          </div>
        </div>
        <PageDashboard v-if="currentPage === 'dashboard'" @navigate="navigate" />
        <PageTickets v-else-if="currentPage === 'tickets'" />
        <PageInventory v-else-if="currentPage === 'inventory'" />
        <PageOrders v-else-if="currentPage === 'orders'" />
        <PageVerify v-else-if="currentPage === 'verify'" />
        <PageChannels v-else-if="currentPage === 'channels'" />
        <PageScenic v-else-if="currentPage === 'scenic'" />
        <PageReports v-else-if="currentPage === 'reports'" />
        <PageSettings v-else-if="currentPage === 'settings'" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import Sidebar from './components/Sidebar.vue'
import AppHeader from './components/AppHeader.vue'
import PageDashboard from './components/PageDashboard.vue'
import PageTickets from './components/PageTickets.vue'
import PageInventory from './components/PageInventory.vue'
import PageOrders from './components/PageOrders.vue'
import PageVerify from './components/PageVerify.vue'
import PageChannels from './components/PageChannels.vue'
import PageScenic from './components/PageScenic.vue'
import PageReports from './components/PageReports.vue'
import PageSettings from './components/PageSettings.vue'

const currentPage = ref('dashboard')

const pageMap = {
  dashboard: { title: '数据概览', subtitle: '实时掌握票务销售动态' },
  tickets: { title: '票种管理', subtitle: '管理景区票种信息、价格及上下架状态' },
  inventory: { title: '库存管理', subtitle: '监控各票种库存情况，及时补货' },
  orders: { title: '订单管理', subtitle: '查看和处理所有渠道的购票订单' },
  verify: { title: '核销管理', subtitle: '扫码核销及核销记录查询' },
  channels: { title: '渠道管理', subtitle: '管理分销渠道配置及对账结算' },
  scenic: { title: '景区管理', subtitle: '维护景区基本信息及票种配置' },
  reports: { title: '数据报表', subtitle: '多维度销售数据统计分析' },
  settings: { title: '系统设置', subtitle: '系统基本配置、账号及API管理' },
}

const pageTitle = computed(() => pageMap[currentPage.value]?.title || '')
const pageSubtitle = computed(() => pageMap[currentPage.value]?.subtitle || '')

function navigate(page) {
  currentPage.value = page
}
</script>
