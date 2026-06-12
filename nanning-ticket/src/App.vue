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
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
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

const currentPage = ref('dashboard')

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
}

const pageTitle = computed(() => pageMap[currentPage.value]?.title || '')
const pageSubtitle = computed(() => pageMap[currentPage.value]?.subtitle || '')

function navigate(page) {
  currentPage.value = page
}
</script>
