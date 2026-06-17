<template>
  <div>
    <div class="card" style="margin-bottom:12px;">
      <div class="toolbar">
        <div class="form-item">
          <label class="form-label">统计周期</label>
          <select class="form-select" v-model="period" @change="onPeriodChange">
            <option value="month">本月</option>
            <option value="lastMonth">上月</option>
            <option value="last3">近 3 个月</option>
            <option value="last6">近 6 个月</option>
            <option value="year">本年</option>
            <option value="custom">自定义</option>
          </select>
        </div>
        <div class="form-item" v-if="period === 'custom'">
          <label class="form-label">起</label>
          <input class="form-input" type="date" v-model="dateFrom" />
        </div>
        <div class="form-item" v-if="period === 'custom'">
          <label class="form-label">止</label>
          <input class="form-input" type="date" v-model="dateTo" />
        </div>
        <div class="form-item">
          <label class="form-label">景区</label>
          <select class="form-select" v-model="filterScenicId">
            <option value="">全部景区</option>
            <option v-for="s in scenics" :key="s.id" :value="s.id">{{ s.name }}</option>
          </select>
        </div>
        <button class="btn btn-primary" style="margin-top:18px;" @click="loadAll">查询</button>
        <div style="flex:1;"></div>
      </div>
    </div>

    <div v-if="loading" class="empty-state" style="padding:30px;">报表数据加载中...</div>

    <template v-else>
      <div class="stat-grid" style="margin-bottom:12px;">
        <div class="stat-card" v-for="s in kpis" :key="s.label">
          <div class="stat-card-header">
            <span class="stat-card-label">{{ s.label }}</span>
            <div class="stat-card-icon" :style="{ background: s.iconBg }">
              <svg width="15" height="15" viewBox="0 0 16 16" fill="currentColor" :style="{ color: s.iconColor }">
                <path :d="s.icon" />
              </svg>
            </div>
          </div>
          <div class="stat-card-value">{{ s.value }}</div>
          <div class="stat-card-trend" :class="s.trendClass">
            {{ s.trend }}
          </div>
        </div>
      </div>

      <div class="grid-2" style="margin-bottom:12px;">
        <div class="card">
          <div class="card-header">
            <span class="card-title">销售趋势</span>
          </div>
          <div class="card-body">
            <div v-if="!trendPoints.length" class="empty-state">暂无趋势数据</div>
            <svg v-else width="100%" height="180" viewBox="0 0 500 180" preserveAspectRatio="none">
              <defs>
                <linearGradient id="areaGrad" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stop-color="#2563eb" stop-opacity="0.12"/>
                  <stop offset="100%" stop-color="#2563eb" stop-opacity="0"/>
                </linearGradient>
              </defs>
              <line x1="0" y1="45" x2="500" y2="45" stroke="#f3f4f6" stroke-width="1"/>
              <line x1="0" y1="90" x2="500" y2="90" stroke="#f3f4f6" stroke-width="1"/>
              <line x1="0" y1="135" x2="500" y2="135" stroke="#f3f4f6" stroke-width="1"/>
              <polygon :points="trendArea" fill="url(#areaGrad)"/>
              <polyline :points="trendLine" fill="none" stroke="#2563eb" stroke-width="2"/>
            </svg>
          </div>
        </div>

        <div class="card">
          <div class="card-header">
            <span class="card-title">景区销售占比</span>
          </div>
          <div class="card-body">
            <div v-if="!scenicSales.length" class="empty-state">暂无数据</div>
            <div v-for="s in scenicSales" v-else :key="s.name" style="margin-bottom:12px;">
              <div style="display:flex;justify-content:space-between;font-size:12px;margin-bottom:4px;">
                <span>{{ s.name }}</span>
                <span style="font-weight:600;">¥{{ s.amount }} <span style="color:var(--color-text-muted);font-weight:400;">({{ s.pct }}%)</span></span>
              </div>
              <div class="progress-bar">
                <div class="progress-fill" :style="{ width: s.pct + '%', background: s.color }"></div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="card" style="margin-bottom:12px;">
        <div class="card-header"><span class="card-title">渠道销售明细</span></div>
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>渠道名称</th>
                <th>出票数量</th>
                <th>销售总额</th>
                <th>占比</th>
                <th>退款金额</th>
                <th>净收入</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="!channelReport.length">
                <td colspan="6" class="empty-state">暂无数据</td>
              </tr>
              <tr v-for="r in channelReport" v-else :key="r.channel">
                <td style="font-weight:500;">{{ r.channel }}</td>
                <td>{{ r.tickets }}</td>
                <td>¥{{ r.sales }}</td>
                <td>
                  <div style="display:flex;align-items:center;gap:8px;">
                    <div class="progress-bar" style="width:60px;">
                      <div class="progress-fill" :style="{ width: r.pct + '%' }"></div>
                    </div>
                    <span style="font-size:12px;">{{ r.pct }}%</span>
                  </div>
                </td>
                <td style="color:var(--color-red);">¥{{ r.refund }}</td>
                <td style="font-weight:600;">¥{{ r.net }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="card">
        <div class="card-header"><span class="card-title">票种销售排行</span></div>
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th style="width:50px;">排名</th>
                <th>票种名称</th>
                <th>所属景区</th>
                <th>出票数量</th>
                <th>销售额</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="!ticketRank.length">
                <td colspan="5" class="empty-state">暂无数据</td>
              </tr>
              <tr v-for="(t, i) in ticketRank" v-else :key="t.name">
                <td>
                  <span v-if="i < 3" :style="{ fontWeight:700, color: ['#dc2626','#ea580c','#ca8a04'][i], fontSize:'15px' }">
                    {{ i + 1 }}
                  </span>
                  <span v-else style="color:var(--color-text-muted);">{{ i + 1 }}</span>
                </td>
                <td style="font-weight:500;">{{ t.name }}</td>
                <td style="color:var(--color-text-secondary);">{{ t.scenic }}</td>
                <td>{{ t.qty }}</td>
                <td style="font-weight:600;">¥{{ t.sales }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, ref, watch } from 'vue'
import { getReportOverview, getReportTrend, getReportRanking } from '../api/report'
import { listScenicOptions } from '../api/scenic'

const loading = ref(false)
const scenics = ref([])

const period = ref('month')
const dateFrom = ref('')
const dateTo = ref('')
const filterScenicId = ref('')

const kpis = ref([
  { label: '销售总额', value: '¥0', trend: '—', trendClass: 'trend-neutral', icon: 'M4 10.781c.148 1.667 1.513 2.85 3.591 3.003V15h1.043v-1.216c2.27-.179 3.678-1.438 3.678-3.3 0-1.59-.947-2.51-2.956-3.028l-.722-.187V3.467c1.122.11 1.879.714 2.07 1.616h1.47c-.166-1.6-1.54-2.748-3.54-2.875V1H7.591v1.233c-1.939.23-3.27 1.472-3.27 3.156 0 1.454.966 2.483 2.661 2.917l.61.162v4.031c-1.149-.17-1.94-.8-2.131-1.718H4z', iconBg: '#eff6ff', iconColor: '#2563eb' },
  { label: '出票总量', value: '0', trend: '—', trendClass: 'trend-neutral', icon: 'M1 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1 1 1 0 000 2 1 1 0 011 1v2a1 1 0 01-1 1H2a1 1 0 01-1-1v-2a1 1 0 011-1 1 1 0 000-2 1 1 0 01-1-1V4z', iconBg: '#f0fdf4', iconColor: '#16a34a' },
  { label: '退款金额', value: '¥0', trend: '—', trendClass: 'trend-neutral', icon: 'M8.354 11.354a.5.5 0 01-.708 0l-6-6a.5.5 0 11.708-.708L8 10.293l5.646-5.647a.5.5 0 01.708.708l-6 6z', iconBg: '#fef2f2', iconColor: '#dc2626' },
  { label: '净收入', value: '¥0', trend: '—', trendClass: 'trend-neutral', icon: 'M0 0h1v15h15v1H0V0zm10 3.5a.5.5 0 01.5-.5h4a.5.5 0 01.5.5v4a.5.5 0 01-1 0V4.9l-3.613 4.417a.5.5 0 01-.74.037L7.06 6.767l-3.656 5.027a.5.5 0 01-.808-.588l4-5.5a.5.5 0 01.758-.06l2.609 2.61L13.445 4H10.5a.5.5 0 01-.5-.5z', iconBg: '#f9fafb', iconColor: '#374151' },
])

const trendPoints = ref([])
const scenicSales = ref([])
const channelReport = ref([])
const ticketRank = ref([])

// 趋势折线图坐标
const trendLine = computed(() => trendPoints.value.map(p => `${p.x},${p.y}`).join(' '))
const trendArea = computed(() => {
  if (!trendPoints.value.length) return ''
  const line = trendLine.value
  const lastX = trendPoints.value[trendPoints.value.length - 1].x
  const firstX = trendPoints.value[0].x
  return `${line} ${lastX},180 ${firstX},180`
})

function rangeOf(p) {
  const d = new Date()
  const fmt = (dt) => dt.toISOString().slice(0, 10)
  const start = new Date(d.getFullYear(), d.getMonth(), 1)
  const end = new Date(d.getFullYear(), d.getMonth() + 1, 0)
  if (p === 'lastMonth') {
    return { dateFrom: fmt(new Date(d.getFullYear(), d.getMonth() - 1, 1)), dateTo: fmt(new Date(d.getFullYear(), d.getMonth(), 0)) }
  }
  if (p === 'last3') {
    return { dateFrom: fmt(new Date(d.getFullYear(), d.getMonth() - 2, 1)), dateTo: fmt(end) }
  }
  if (p === 'last6') {
    return { dateFrom: fmt(new Date(d.getFullYear(), d.getMonth() - 5, 1)), dateTo: fmt(end) }
  }
  if (p === 'year') {
    return { dateFrom: fmt(new Date(d.getFullYear(), 0, 1)), dateTo: fmt(end) }
  }
  return { dateFrom: fmt(start), dateTo: fmt(end) }
}

function onPeriodChange() {
  if (period.value === 'custom') return
  const r = rangeOf(period.value)
  dateFrom.value = r.dateFrom
  dateTo.value = r.dateTo
  loadAll()
}

async function loadScenics() {
  try { scenics.value = await listScenicOptions() } catch (e) { /* handled */ }
}

async function loadAll() {
  if (period.value !== 'custom') {
    const r = rangeOf(period.value)
    dateFrom.value = r.dateFrom
    dateTo.value = r.dateTo
  }
  loading.value = true
  try {
    const baseParams = { dateFrom: dateFrom.value, dateTo: dateTo.value }
    if (filterScenicId.value) baseParams.scenicId = filterScenicId.value
    // 概览
    const overview = await getReportOverview(baseParams).catch(() => null)
    if (overview) {
      kpis.value[0].value = `¥${Number(overview.totalGmv || 0).toLocaleString()}`
      kpis.value[0].trend = `销售 ${overview.saleCount || 0} 笔 / 订单 ${overview.orderCount || 0} 笔`
      kpis.value[1].value = String(overview.totalTicketCount || 0)
      kpis.value[1].trend = `核销率 ${overview.useRate || 0}%`
      kpis.value[2].value = `¥${Number(overview.refundAmount || 0).toLocaleString()}`
      kpis.value[3].value = `¥${Number(overview.netRevenue || 0).toLocaleString()}`
    }

    // 趋势（自动按时间跨度选择 interval）
    const days = (new Date(dateTo.value) - new Date(dateFrom.value)) / 86400000
    const interval = days > 60 ? 'MONTH' : days > 14 ? 'WEEK' : 'DAY'
    const trend = await getReportTrend({ ...baseParams, interval }).catch(() => null)
    if (trend?.points) {
      const max = Math.max(...trend.points.map(p => Number(p.amount || 0)), 1)
      trendPoints.value = trend.points.map((p, i) => ({
        x: (i / Math.max(1, trend.points.length - 1)) * 500,
        y: 150 - (Number(p.amount || 0) / max) * 120,
      }))
    } else {
      trendPoints.value = []
    }

    // 景区销售
    const scenicRank = await getReportRanking({ ...baseParams, groupBy: 'SCENIC' }).catch(() => [])
    const total = scenicRank.reduce((s, r) => s + Number(r.amount || 0), 0) || 1
    scenicSales.value = scenicRank.slice(0, 6).map((r, i) => ({
      name: r.scenicName || r.name || '—',
      amount: Number(r.amount || 0).toLocaleString(),
      pct: Math.round((Number(r.amount || 0) / total) * 100),
      color: ['#2563eb', '#374151', '#6b7280', '#9ca3af', '#d1d5db', '#f3f4f6'][i] || '#d1d5db',
    }))

    // 渠道销售
    const channelRank = await getReportRanking({ ...baseParams, groupBy: 'CHANNEL' }).catch(() => [])
    const cTotal = channelRank.reduce((s, r) => s + Number(r.amount || 0), 0) || 1
    channelReport.value = channelRank.map(r => ({
      channel: r.channelName || r.name,
      tickets: r.ticketCount || 0,
      sales: Number(r.amount || 0).toFixed(2),
      pct: Math.round((Number(r.amount || 0) / cTotal) * 100),
      refund: Number(r.refundAmount || 0).toFixed(2),
      net: (Number(r.amount || 0) - Number(r.refundAmount || 0)).toFixed(2),
    }))

    // 票种销售
    const ticketRankData = await getReportRanking({ ...baseParams, groupBy: 'TICKET' }).catch(() => [])
    ticketRank.value = ticketRankData.slice(0, 10).map(r => ({
      name: r.ticketName || r.name,
      scenic: r.scenicName,
      qty: r.ticketCount || 0,
      sales: Number(r.amount || 0).toFixed(2),
    }))
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

onMounted(async () => {
  onPeriodChange()
  await loadScenics()
  await loadAll()
})
</script>
