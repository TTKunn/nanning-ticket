<template>
  <div>
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
      <!-- 今日销售趋势 -->
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
            <!-- 简易折线图 SVG -->
            <svg width="100%" height="160" viewBox="0 0 400 160" preserveAspectRatio="none">
              <defs>
                <linearGradient id="grad1" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="0%" stop-color="#2563eb" stop-opacity="0.15"/>
                  <stop offset="100%" stop-color="#2563eb" stop-opacity="0"/>
                </linearGradient>
              </defs>
              <!-- 网格线 -->
              <line x1="0" y1="40" x2="400" y2="40" stroke="#e5e7eb" stroke-width="1"/>
              <line x1="0" y1="80" x2="400" y2="80" stroke="#e5e7eb" stroke-width="1"/>
              <line x1="0" y1="120" x2="400" y2="120" stroke="#e5e7eb" stroke-width="1"/>
              <!-- 昨日线 -->
              <polyline points="0,100 50,90 100,95 150,80 200,85 250,75 300,80 350,70 400,75"
                fill="none" stroke="#d1d5db" stroke-width="1.5" stroke-dasharray="4,3"/>
              <!-- 今日面积 -->
              <polygon points="0,110 50,95 100,85 150,70 200,60 250,50 300,55 350,40 400,45 400,160 0,160"
                fill="url(#grad1)"/>
              <!-- 今日线 -->
              <polyline points="0,110 50,95 100,85 150,70 200,60 250,50 300,55 350,40 400,45"
                fill="none" stroke="#2563eb" stroke-width="2"/>
              <!-- 数据点 -->
              <circle cx="350" cy="40" r="4" fill="#2563eb"/>
              <circle cx="400" cy="45" r="4" fill="#2563eb"/>
            </svg>
          </div>
          <div style="display:flex;justify-content:space-between;margin-top:8px;font-size:11px;color:var(--color-text-muted);">
            <span v-for="h in ['00:00','03:00','06:00','09:00','12:00','15:00','18:00','21:00','24:00']" :key="h">{{ h }}</span>
          </div>
        </div>
      </div>

      <!-- 渠道销售占比 -->
      <div class="card">
        <div class="card-header">
          <span class="card-title">渠道销售占比</span>
          <span style="font-size:12px;color:var(--color-text-muted);">本月</span>
        </div>
        <div class="card-body">
          <div style="display:flex;align-items:center;gap:24px;">
            <!-- 饼图占位 -->
            <svg width="120" height="120" viewBox="0 0 120 120" style="flex-shrink:0;">
              <circle cx="60" cy="60" r="50" fill="none" stroke="#e5e7eb" stroke-width="20"/>
              <circle cx="60" cy="60" r="50" fill="none" stroke="#2563eb" stroke-width="20"
                stroke-dasharray="157 157" stroke-dashoffset="0" transform="rotate(-90 60 60)"/>
              <circle cx="60" cy="60" r="50" fill="none" stroke="#374151" stroke-width="20"
                stroke-dasharray="63 251" stroke-dashoffset="-157" transform="rotate(-90 60 60)"/>
              <circle cx="60" cy="60" r="50" fill="none" stroke="#9ca3af" stroke-width="20"
                stroke-dasharray="47 267" stroke-dashoffset="-220" transform="rotate(-90 60 60)"/>
              <circle cx="60" cy="60" r="50" fill="none" stroke="#d1d5db" stroke-width="20"
                stroke-dasharray="47 267" stroke-dashoffset="-267" transform="rotate(-90 60 60)"/>
              <text x="60" y="64" text-anchor="middle" font-size="13" font-weight="700" fill="#1a1a1a">100%</text>
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
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 下方两列 -->
    <div class="grid-2">
      <!-- 库存预警 -->
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
              <tr v-for="item in lowStockItems" :key="item.name">
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
            </tbody>
          </table>
        </div>
      </div>

      <!-- 最新订单 -->
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
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
defineEmits(['navigate'])

const stats = [
  {
    label: '今日销售额',
    value: '¥28,460',
    trend: '较昨日 +12.5%',
    trendClass: 'trend-up',
    trendIcon: 'M7.646 4.646a.5.5 0 01.708 0l6 6a.5.5 0 01-.708.708L8 5.707l-5.646 5.647a.5.5 0 01-.708-.708l6-6z',
    icon: 'M4 10.781c.148 1.667 1.513 2.85 3.591 3.003V15h1.043v-1.216c2.27-.179 3.678-1.438 3.678-3.3 0-1.59-.947-2.51-2.956-3.028l-.722-.187V3.467c1.122.11 1.879.714 2.07 1.616h1.47c-.166-1.6-1.54-2.748-3.54-2.875V1H7.591v1.233c-1.939.23-3.27 1.472-3.27 3.156 0 1.454.966 2.483 2.661 2.917l.61.162v4.031c-1.149-.17-1.94-.8-2.131-1.718H4zm3.391-3.836c-1.043-.263-1.6-.825-1.6-1.616 0-.944.704-1.641 1.8-1.828v3.495l-.2-.051zm1.591 1.872c1.287.323 1.852.859 1.852 1.769 0 1.097-.826 1.828-2.2 1.939V8.73l.348.086z',
    iconBg: '#eff6ff', iconColor: '#2563eb'
  },
  {
    label: '今日出票量',
    value: '1,284',
    trend: '较昨日 +8.3%',
    trendClass: 'trend-up',
    trendIcon: 'M7.646 4.646a.5.5 0 01.708 0l6 6a.5.5 0 01-.708.708L8 5.707l-5.646 5.647a.5.5 0 01-.708-.708l6-6z',
    icon: 'M1 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1 1 1 0 000 2 1 1 0 011 1v2a1 1 0 01-1 1H2a1 1 0 01-1-1v-2a1 1 0 011-1 1 1 0 000-2 1 1 0 01-1-1V4z',
    iconBg: '#f0fdf4', iconColor: '#16a34a'
  },
  {
    label: '待处理订单',
    value: '12',
    trend: '需及时处理',
    trendClass: 'trend-down',
    trendIcon: 'M8.354 11.354a.5.5 0 01-.708 0l-6-6a.5.5 0 11.708-.708L8 10.293l5.646-5.647a.5.5 0 01.708.708l-6 6z',
    icon: 'M3 2a1 1 0 011-1h8a1 1 0 011 1v12a1 1 0 01-1 1H4a1 1 0 01-1-1V2zm2 1v10h6V3H5zm1 2h4v1H6V5zm0 2h4v1H6V7zm0 2h3v1H6V9z',
    iconBg: '#fef2f2', iconColor: '#dc2626'
  },
  {
    label: '活跃渠道数',
    value: '6',
    trend: '本月新增 1 个',
    trendClass: 'trend-neutral',
    trendIcon: 'M8 9.5a1.5 1.5 0 100-3 1.5 1.5 0 000 3z',
    icon: 'M8 0a8 8 0 100 16A8 8 0 008 0zM4.5 7.5a.5.5 0 000 1h5.793l-2.147 2.146a.5.5 0 00.708.708l3-3a.5.5 0 000-.708l-3-3a.5.5 0 10-.708.708L10.293 7.5H4.5z',
    iconBg: '#f9fafb', iconColor: '#374151'
  }
]

const channels = [
  { name: '直销（AI南宁）', pct: 40, color: '#2563eb' },
  { name: '美团旅游', pct: 25, color: '#374151' },
  { name: '携程', pct: 19, color: '#9ca3af' },
  { name: '其他平台', pct: 16, color: '#d1d5db' },
]

const lowStockItems = [
  { name: '青秀山成人票', scenic: '青秀山风景区', stock: 8 },
  { name: '南湖公园联票', scenic: '南湖公园', stock: 15 },
  { name: '广西民族博物馆', scenic: '民族博物馆', stock: 32 },
  { name: '邕江游船夜游', scenic: '邕江景区', stock: 45 },
]

const recentOrders = [
  { id: 'NN2026052400123', ticket: '青秀山成人票', channel: '美团', amount: '60.00', status: '已支付', statusClass: 'tag-green' },
  { id: 'NN2026052400122', ticket: '南湖公园联票', channel: '携程', amount: '120.00', status: '待核销', statusClass: 'tag-blue' },
  { id: 'NN2026052400121', ticket: '邕江游船夜游', channel: '直销', amount: '88.00', status: '已核销', statusClass: 'tag-gray' },
  { id: 'NN2026052400120', ticket: '民族博物馆票', channel: '飞猪', amount: '30.00', status: '已退款', statusClass: 'tag-red' },
  { id: 'NN2026052400119', ticket: '青秀山成人票', channel: '直销', amount: '60.00', status: '已支付', statusClass: 'tag-green' },
]
</script>
