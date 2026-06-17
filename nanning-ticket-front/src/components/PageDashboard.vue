<template>
  <div>
    <!-- 加载遮罩（仅本页内容区域） -->
    <div v-if="loading" class="page-loading">
      <div class="app-loading-spinner" />
      <span>正在加载概览数据...</span>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-grid">
      <div class="stat-card" v-for="s in stats" :key="s.label">
        <div class="stat-card-header">
          <span class="stat-card-label">{{ s.label }}</span>
          <div class="stat-card-icon" :style="{ background: s.iconBg }">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="currentColor" :style="{ color: s.iconColor }">
              <path :d="s.icon" />
            </svg>
          </div>
        </div>
        <div class="stat-card-value">{{ s.value }}</div>
        <div class="stat-card-trend" :class="s.trendClass">
          <svg width="12" height="12" viewBox="0 0 16 16" fill="currentColor">
            <path :d="s.trendIcon" />
          </svg>
          {{ s.trend }}
        </div>
      </div>
    </div>

    <!-- 中间两列 -->
    <div class="grid-2" style="margin-bottom:12px;">
      <div class="card">
        <div class="card-header">
          <span class="card-title">今日销售趋势</span>
          <div style="display:flex;gap:8px;">
            <span class="tag tag-blue">今日</span>
            <span class="tag tag-gray">昨日</span>
          </div>
        </div>
        <div class="card-body">
          <div class="chart-placeholder" style="height:180px;">
            <svg width="100%" height="160" viewBox="0 0 400 160" preserveAspectRatio="none">
              <defs>
                <linearGradient id="grad1" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stop-color="#2563eb" stop-opacity="0.15"/>
                  <stop offset="100%" stop-color="#2563eb" stop-opacity="0"/>
                </linearGradient>
              </defs>
              <line x1="0" y1="40" x2="400" y2="40" stroke="#e5e7eb" stroke-width="1"/>
              <line x1="0" y1="80" x2="400" y2="80" stroke="#e5e7eb" stroke-width="1"/>
              <line x1="0" y1="120" x2="400" y2="120" stroke="#e5e7eb" stroke-width="1"/>
              <polyline points="0,100 50,90 100,95 150,80 200,85 250,75 300,80 350,70 400,75"
                fill="none" stroke="#d1d5db" stroke-width="1.5" stroke-dasharray="4,3"/>
              <polygon points="0,110 50,95 100,85 150,70 200,60 250,50 300,55 350,40 400,45 400,160 0,160"
                fill="url(#grad1)"/>
              <polyline points="0,110 50,95 100,85 150,70 200,60 250,50 300,55 350,40 400,45"
                fill="none" stroke="#2563eb" stroke-width="2"/>
              <circle cx="350" cy="40" r="4" fill="#2563eb"/>
              <circle cx="400" cy="45" r="4" fill="#2563eb"/>
            </svg>
          </div>
          <div style="display:flex;justify-content:space-between;margin-top:8px;font-size:11px;color:var(--color-text-muted);">
            <span v-for="h in ['00:00','03:00','06:00','09:00','12:00','15:00','18:00','21:00','24:00']" :key="h">{{ h }}</span>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <span class="card-title">渠道销售占比</span>
          <span style="font-size:12px;color:var(--color-text-muted);">本月</span>
        </div>
        <div class="card-body">
          <div style="display:flex;align-items:center;gap:24px;">
            <svg width="120" height="120" viewBox="0 0 120 120" style="flex-shrink:0;">
              <circle cx="60" cy="60" r="50" fill="none" stroke="#e5e7eb" stroke-width="20"/>
              <circle v-for="(seg, i) in channelSegments" :key="i"
                cx="60" cy="60" r="50" fill="none" :stroke="seg.color" stroke-width="20"
                :stroke-dasharray="`${seg.dash} 314`" :stroke-dashoffset="seg.offset" transform="rotate(-90 60 60)"/>
              <text x="60" y="64" text-anchor="middle" font-size="13" font-weight="700" fill="#1a1a1a">{{ totalChannelPct }}%</text>
            </svg>
            <div style="flex:1;">
              <div v-for="ch in channels" :key="ch.name" style="margin-bottom:10px;">
                <div style="display:flex;justify-content:space-between;font-size:12px;margin-bottom:3px;">
                  <span style="display:flex;align-items:center;gap:6px;">
                    <span :style="{ width:'8px', height:'8px', borderRadius:'2px', background:ch.color, display:'inline-block' }"></span>
                    {{ ch.name }}
                  </span>
                  <span style="font-weight:600;">{{ ch.pct }}%</span>
                </div>
                <div class="progress-bar">
                  <div class="progress-fill" :style="{ width: ch.pct + '%', background: ch.color }"></div>
                </div>
              </div>
              <div v-if="!channels.length" class="empty-state" style="padding:20px;">暂无渠道数据</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 下方两列 -->
    <div class="grid-2">
      <div class="card">
        <div class="card-header">
          <span class="card-title">库存预警</span>
          <span class="tag tag-red">{{ lowStockItems.length }} 项预警</span>
        </div>
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>票种名称</th>
                <th>景区</th>
                <th>剩余库存</th>
                <th>状态</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in lowStockItems" :key="item.id">
                <td>{{ item.name }}</td>
                <td style="color:var(--color-text-secondary);">{{ item.scenic }}</td>
                <td>
                  <span :style="{ color: item.stock < 20 ? 'var(--color-red)' : 'var(--color-orange)', fontWeight: 600 }">
                    {{ item.stock }}
                  </span>
                </td>
                <td>
                  <span class="tag" :class="item.stock < 20 ? 'tag-red' : 'tag-orange'">
                    {{ item.stock < 20 ? '紧急补货' : '库存偏低' }}
                  </span>
                </td>
              </tr>
              <tr v-if="!lowStockItems.length && !loading">
                <td colspan="4" class="empty-state">暂无库存预警</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <span class="card-title">最新订单</span>
          <span class="action-link" @click="$emit('navigate', 'orders')">查看全部 →</span>
        </div>
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>订单号</th>
                <th>票种</th>
                <th>渠道</th>
                <th>金额</th>
                <th>状态</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="o in recentOrders" :key="o.id">
                <td style="font-family:monospace;font-size:12px;color:var(--color-text-secondary);">{{ o.id }}</td>
                <td>{{ o.ticket }}</td>
                <td>
                  <span class="tag tag-gray">{{ o.channel }}</span>
                </td>
                <td style="font-weight:600;">¥{{ o.amount }}</td>
                <td>
                  <span class="tag" :class="o.statusClass">{{ o.status }}</span>
                </td>
              </tr>
              <tr v-if="!recentOrders.length && !loading">
                <td colspan="5" class="empty-state">暂无订单数据</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { getReportOverview, getReportRanking, getReportInventory } from '../api/report'
import { getOrderStats, listOrders } from '../api/order'

defineEmits(['navigate'])

const loading = ref(false)

const stats = ref([
  {
    label: '今日销售额',
    value: '¥0',
    trend: '—',
    trendClass: 'trend-neutral',
    trendIcon: 'M8 9.5a1.5 1.5 0 100-3 1.5 1.5 0 000 3z',
    icon: 'M4 10.781c.148 1.667 1.513 2.85 3.591 3.003V15h1.043v-1.216c2.27-.179 3.678-1.438 3.678-3.3 0-1.59-.947-2.51-2.956-3.028l-.722-.187V3.467c1.122.11 1.879.714 2.07 1.616h1.47c-.166-1.6-1.54-2.748-3.54-2.875V1H7.591v1.233c-1.939.23-3.27 1.472-3.27 3.156 0 1.454.966 2.483 2.661 2.917l.61.162v4.031c-1.149-.17-1.94-.8-2.131-1.718H4z',
    iconBg: '#eff6ff', iconColor: '#2563eb'
  },
  {
    label: '今日出票量',
    value: '0',
    trend: '—',
    trendClass: 'trend-neutral',
    trendIcon: 'M8 9.5a1.5 1.5 0 100-3 1.5 1.5 0 000 3z',
    icon: 'M1 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1 1 1 0 000 2 1 1 0 011 1v2a1 1 0 01-1 1H2a1 1 0 01-1-1v-2a1 1 0 011-1 1 1 0 000-2 1 1 0 01-1-1V4z',
    iconBg: '#f0fdf4', iconColor: '#16a34a'
  },
  {
    label: '待处理订单',
    value: '0',
    trend: '需及时处理',
    trendClass: 'trend-down',
    trendIcon: 'M8.354 11.354a.5.5 0 01-.708 0l-6-6a.5.5 0 11.708-.708L8 10.293l5.646-5.647a.5.5 0 01.708.708l-6 6z',
    icon: 'M3 2a1 1 0 011-1h8a1 1 0 011 1v12a1 1 0 01-1 1H4a1 1 0 01-1-1V2z',
    iconBg: '#fef2f2', iconColor: '#dc2626'
  },
  {
    label: '活跃渠道数',
    value: '0',
    trend: '—',
    trendClass: 'trend-neutral',
    trendIcon: 'M8 9.5a1.5 1.5 0 100-3 1.5 1.5 0 000 3z',
    icon: 'M8 0a8 8 0 100 16A8 8 0 008 0zM4.5 7.5a.5.5 0 000 1h5.793l-2.147 2.146a.5.5 0 00.708.708l3-3a.5.5 0 000-.708l-3-3a.5.5 0 10-.708.708L10.293 7.5H4.5z',
    iconBg: '#f9fafb', iconColor: '#374151'
  },
])

const channels = ref([])
const lowStockItems = ref([])
const recentOrders = ref([])

// 渠道饼图片段
const channelSegments = computed(() => {
  const total = 314 // 2πr ≈ 314
  let acc = 0
  return channels.value.map((c) => {
    const seg = (c.pct / 100) * total
    const piece = { color: c.color, dash: seg.toFixed(2), offset: -acc }
    acc += seg
    return piece
  })
})
const totalChannelPct = computed(() => {
  const sum = channels.value.reduce((s, c) => s + (Number(c.pct) || 0), 0)
  return sum ? sum : 0
})

// 状态 → tag 样式映射
const statusTagMap = {
  '已支付': 'tag-green', '待支付': 'tag-orange',
  '已核销': 'tag-gray', '待核销': 'tag-blue',
  '已退款': 'tag-red', '已取消': 'tag-gray',
}

function todayRange() {
  const d = new Date()
  const fmt = (dt) => dt.toISOString().slice(0, 10)
  return { dateFrom: fmt(d), dateTo: fmt(d) }
}
function monthRange() {
  const d = new Date()
  const start = new Date(d.getFullYear(), d.getMonth(), 1)
  const end = new Date(d.getFullYear(), d.getMonth() + 1, 0)
  const fmt = (dt) => dt.toISOString().slice(0, 10)
  return { dateFrom: fmt(start), dateTo: fmt(end) }
}

async function loadOverview() {
  loading.value = true
  try {
    const range = todayRange()
    // 并发拉取：概览指标、渠道维度排名、订单统计、最新订单
    const [overview, channelRank, orderStats, orderList] = await Promise.all([
      getReportOverview(range),
      getReportRanking({ ...range, groupBy: 'CHANNEL' }).catch(() => []),
      getOrderStats().catch(() => null),
      listOrders({ pageNum: 1, pageSize: 5, ...range }).catch(() => ({ records: [] })),
    ])

    if (overview) {
      stats.value[0].value = `¥${Number(overview.totalGmv || 0).toLocaleString()}`
      stats.value[1].value = String(overview.totalTicketCount || 0)
    }
    if (orderStats) {
      stats.value[2].value = String(orderStats.pendingCount || 0)
      stats.value[3].value = String((orderStats.totalCount || 0))
    }

    // 渠道占比（取金额前 4 个，其他合并）
    if (Array.isArray(channelRank) && channelRank.length) {
      const top = channelRank.slice(0, 4).map((r, i) => ({
        name: r.channelName || r.name || `渠道${i + 1}`,
        pct: Number(r.pct || r.amountPct || 0),
        color: ['#2563eb', '#374151', '#9ca3af', '#d1d5db'][i] || '#d1d5db',
      }))
      const sum = top.reduce((s, x) => s + x.pct, 0)
      if (sum < 100 && top.length) {
        top.push({ name: '其他渠道', pct: Math.max(0, 100 - sum), color: '#d1d5db' })
      }
      channels.value = top
    }

    // 库存预警：取库存日报前 5 项（remaining 升序）
    const inv = await getReportInventory({ ...todayRange() }).catch(() => [])
    if (Array.isArray(inv)) {
      lowStockItems.value = inv
        .filter((it) => Number(it.remaining || 0) <= Number(it.warning || 9999))
        .slice(0, 5)
        .map((it) => ({
          id: it.ticketId || it.id,
          name: it.ticketName || it.name,
          scenic: it.scenicName || it.scenic,
          stock: Number(it.remaining || 0),
        }))
    }

    // 最新订单
    if (orderList && orderList.records) {
      recentOrders.value = orderList.records.map((o) => ({
        id: o.orderNo || o.id,
        ticket: o.ticketName || o.items?.[0]?.ticketName || '—',
        channel: o.channelName || o.channelCode || '—',
        amount: Number(o.totalAmount || 0).toFixed(2),
        status: o.status || '—',
        statusClass: statusTagMap[o.status] || 'tag-gray',
      }))
    }
  } finally {
    loading.value = false
  }
}

onMounted(loadOverview)
</script>

<style scoped>
.page-loading {
  padding: 8px 0 16px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: var(--color-text-muted);
}
.app-loading-spinner {
  width: 14px;
  height: 14px;
  border: 2px solid var(--color-gray-200);
  border-top-color: var(--color-blue);
  border-radius: 50%;
  animation: dash-spin 0.8s linear infinite;
}
@keyframes dash-spin { to { transform: rotate(360deg); } }
</style>
