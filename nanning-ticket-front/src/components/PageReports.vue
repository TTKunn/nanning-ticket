<template>
  <div>
    <!-- 筛选条件 -->
    <div class="card" style="margin-bottom:12px;">
      <div class="toolbar">
        <div class="form-item">
          <label class="form-label">统计周期</label>
          <select class="form-select">
            <option>本月（2026年05月）</option>
            <option>上月（2026年04月）</option>
            <option>近3个月</option>
            <option>近6个月</option>
            <option>本年</option>
            <option>自定义</option>
          </select>
        </div>
        <div class="form-item">
          <label class="form-label">景区</label>
          <select class="form-select">
            <option>全部景区</option>
            <option v-for="s in scenics" :key="s">{{ s }}</option>
          </select>
        </div>
        <div class="form-item">
          <label class="form-label">渠道</label>
          <select class="form-select">
            <option>全部渠道</option>
            <option>AI南宁直销</option>
            <option>美团旅游</option>
            <option>携程旅行</option>
          </select>
        </div>
        <button class="btn btn-primary" style="margin-top:18px;">查询</button>
        <div style="flex:1;"></div>
        <button class="btn btn-default" style="margin-top:18px;">
          <svg width="13" height="13" viewBox="0 0 16 16" fill="currentColor"><path d="M.5 9.9a.5.5 0 01.5.5v2.5a1 1 0 001 1h12a1 1 0 001-1v-2.5a.5.5 0 011 0v2.5a2 2 0 01-2 2H2a2 2 0 01-2-2v-2.5a.5.5 0 01.5-.5z"/><path d="M7.646 11.854a.5.5 0 00.708 0l3-3a.5.5 0 00-.708-.708L8.5 10.293V1.5a.5.5 0 00-1 0v8.793L5.354 8.146a.5.5 0 10-.708.708l3 3z"/></svg>
          导出报表
        </button>
      </div>
    </div>

    <!-- 核心指标 -->
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

    <!-- 销售趋势 + 景区分布 -->
    <div class="grid-2" style="margin-bottom:12px;">
      <div class="card">
        <div class="card-header">
          <span class="card-title">销售额趋势（近30天）</span>
        </div>
        <div class="card-body">
          <svg width="100%" height="180" viewBox="0 0 500 180" preserveAspectRatio="none">
            <defs>
              <linearGradient id="areaGrad" x1="0" y1="0" x2="0" y2="1">
                <stop offset="0%" stop-color="#2563eb" stop-opacity="0.12"/>
                <stop offset="100%" stop-color="#2563eb" stop-opacity="0"/>
              </linearGradient>
            </defs>
            <line x1="0" y1="45" x2="500" y2="45" stroke="#f3f4f6" stroke-width="1"/>
            <line x1="0" y1="90" x2="500" y2="90" stroke="#f3f4f6" stroke-width="1"/>
            <line x1="0" y1="135" x2="500" y2="135" stroke="#f3f4f6" stroke-width="1"/>
            <polygon
              points="0,140 17,130 33,125 50,120 67,115 83,100 100,95 117,90 133,85 150,80 167,75 183,70 200,65 217,60 233,55 250,50 267,55 283,60 300,50 317,45 333,40 350,45 367,50 383,45 400,40 417,35 433,40 450,38 467,42 483,38 500,35 500,180 0,180"
              fill="url(#areaGrad)"/>
            <polyline
              points="0,140 17,130 33,125 50,120 67,115 83,100 100,95 117,90 133,85 150,80 167,75 183,70 200,65 217,60 233,55 250,50 267,55 283,60 300,50 317,45 333,40 350,45 367,50 383,45 400,40 417,35 433,40 450,38 467,42 483,38 500,35"
              fill="none" stroke="#2563eb" stroke-width="2"/>
          </svg>
          <div style="display:flex;justify-content:space-between;font-size:11px;color:var(--color-text-muted);margin-top:4px;">
            <span>05/01</span><span>05/08</span><span>05/15</span><span>05/22</span><span>05/24</span>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <span class="card-title">景区销售占比</span>
        </div>
        <div class="card-body">
          <div v-for="s in scenicSales" :key="s.name" style="margin-bottom:12px;">
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

    <!-- 渠道销售明细 -->
    <div class="card" style="margin-bottom:12px;">
      <div class="card-header">
        <span class="card-title">渠道销售明细</span>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>渠道名称</th>
              <th>出票数量</th>
              <th>销售总额</th>
              <th>占比</th>
              <th>退款金额</th>
              <th>退款率</th>
              <th>净收入</th>
              <th>环比</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in channelReport" :key="r.channel">
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
              <td style="font-size:12px;" :style="{ color: parseFloat(r.refundRate) > 3 ? 'var(--color-red)' : 'var(--color-text-secondary)' }">
                {{ r.refundRate }}
              </td>
              <td style="font-weight:600;">¥{{ r.net }}</td>
              <td>
                <span :class="r.trend > 0 ? 'trend-up' : 'trend-down'" style="font-size:12px;">
                  {{ r.trend > 0 ? '↑' : '↓' }} {{ Math.abs(r.trend) }}%
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 票种销售排行 -->
    <div class="card">
      <div class="card-header">
        <span class="card-title">票种销售排行 TOP 10</span>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th style="width:50px;">排名</th>
              <th>票种名称</th>
              <th>所属景区</th>
              <th>出票数量</th>
              <th>销售额</th>
              <th>核销率</th>
              <th>退款率</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="(t, i) in ticketRank" :key="t.name">
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
              <td>
                <span :style="{ color: parseFloat(t.verifyRate) > 90 ? 'var(--color-green)' : 'var(--color-text-primary)' }">
                  {{ t.verifyRate }}
                </span>
              </td>
              <td :style="{ color: parseFloat(t.refundRate) > 3 ? 'var(--color-red)' : 'var(--color-text-secondary)' }">
                {{ t.refundRate }}
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<script setup>
const scenics = ['青秀山风景区', '南湖公园', '广西民族博物馆', '邕江景区', '良凤江国家森林公园']

const kpis = [
  { label: '本月销售总额', value: '¥87,200', trend: '较上月 +18.4%', trendClass: 'trend-up', icon: 'M4 10.781c.148 1.667 1.513 2.85 3.591 3.003V15h1.043v-1.216c2.27-.179 3.678-1.438 3.678-3.3 0-1.59-.947-2.51-2.956-3.028l-.722-.187V3.467c1.122.11 1.879.714 2.07 1.616h1.47c-.166-1.6-1.54-2.748-3.54-2.875V1H7.591v1.233c-1.939.23-3.27 1.472-3.27 3.156 0 1.454.966 2.483 2.661 2.917l.61.162v4.031c-1.149-.17-1.94-.8-2.131-1.718H4z', iconBg: '#eff6ff', iconColor: '#2563eb' },
  { label: '本月出票总量', value: '2,180', trend: '较上月 +12.1%', trendClass: 'trend-up', icon: 'M1 4a1 1 0 011-1h12a1 1 0 011 1v2a1 1 0 01-1 1 1 1 0 000 2 1 1 0 011 1v2a1 1 0 01-1 1H2a1 1 0 01-1-1v-2a1 1 0 011-1 1 1 0 000-2 1 1 0 01-1-1V4z', iconBg: '#f0fdf4', iconColor: '#16a34a' },
  { label: '本月退款金额', value: '¥1,240', trend: '退款率 1.42%', trendClass: 'trend-neutral', icon: 'M8.354 11.354a.5.5 0 01-.708 0l-6-6a.5.5 0 11.708-.708L8 10.293l5.646-5.647a.5.5 0 01.708.708l-6 6z', iconBg: '#fef2f2', iconColor: '#dc2626' },
  { label: '本月净收入', value: '¥85,960', trend: '扣除退款后', trendClass: 'trend-neutral', icon: 'M0 0h1v15h15v1H0V0zm10 3.5a.5.5 0 01.5-.5h4a.5.5 0 01.5.5v4a.5.5 0 01-1 0V4.9l-3.613 4.417a.5.5 0 01-.74.037L7.06 6.767l-3.656 5.027a.5.5 0 01-.808-.588l4-5.5a.5.5 0 01.758-.06l2.609 2.61L13.445 4H10.5a.5.5 0 01-.5-.5z', iconBg: '#f9fafb', iconColor: '#374151' },
]

const scenicSales = [
  { name: '广西民族博物馆', amount: '29,040', pct: 33, color: '#2563eb' },
  { name: '青秀山风景区', amount: '24,720', pct: 28, color: '#374151' },
  { name: '南湖公园', amount: '14,400', pct: 17, color: '#6b7280' },
  { name: '邕江景区', amount: '13,640', pct: 16, color: '#9ca3af' },
  { name: '良凤江国家森林公园', amount: '5,400', pct: 6, color: '#d1d5db' },
]

const channelReport = [
  { channel: 'AI南宁直销', tickets: 545, sales: '34,840', pct: 40, refund: '240', refundRate: '0.69%', net: '34,600', trend: 22 },
  { channel: '美团旅游', tickets: 340, sales: '21,760', pct: 25, refund: '480', refundRate: '2.21%', net: '21,280', trend: 15 },
  { channel: '携程旅行', tickets: 262, sales: '16,768', pct: 19, refund: '320', refundRate: '1.91%', net: '16,448', trend: 8 },
  { channel: '飞猪旅行', tickets: 139, sales: '8,896', pct: 10, refund: '120', refundRate: '1.35%', net: '8,776', trend: -3 },
  { channel: '同程旅行', tickets: 87, sales: '5,568', pct: 6, refund: '80', refundRate: '1.44%', net: '5,488', trend: 5 },
]

const ticketRank = [
  { name: '广西民族博物馆票', scenic: '广西民族博物馆', qty: 968, sales: '29,040', verifyRate: '92.9%', refundRate: '0.8%' },
  { name: '青秀山成人票', scenic: '青秀山风景区', qty: 412, sales: '24,720', verifyRate: '91.3%', refundRate: '1.2%' },
  { name: '南湖公园联票', scenic: '南湖公园', qty: 120, sales: '14,400', verifyRate: '88.3%', refundRate: '2.5%' },
  { name: '邕江游船夜游', scenic: '邕江景区', qty: 155, sales: '13,640', verifyRate: '90.3%', refundRate: '1.9%' },
  { name: '青秀山儿童票', scenic: '青秀山风景区', qty: 98, sales: '2,940', verifyRate: '91.8%', refundRate: '0.0%' },
]
</script>
