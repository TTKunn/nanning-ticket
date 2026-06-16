<template>
  <div>
    <div class="stat-grid" style="grid-template-columns:repeat(4,1fr);margin-bottom:12px;">
      <div class="stat-card" v-for="item in summaryStats" :key="item.label">
        <div class="stat-card-header">
          <span class="stat-card-label">{{ item.label }}</span>
        </div>
        <div class="stat-card-value">{{ item.value }}</div>
        <div style="font-size:12px;color:var(--color-text-muted);margin-top:4px;">{{ item.sub }}</div>
      </div>
    </div>

    <div class="card" style="margin-bottom:12px;">
      <div class="toolbar">
        <div class="form-item">
          <input class="form-input" placeholder="票据号/订单号/核销码..." style="width:220px;" />
        </div>
        <div class="form-item">
          <select class="form-select">
            <option>全部来源</option>
            <option>本地系统</option>
            <option>网售</option>
            <option>分销平台</option>
          </select>
        </div>
        <div class="form-item">
          <select class="form-select">
            <option>全部状态</option>
            <option>已出票</option>
            <option>待检票</option>
            <option>已检票</option>
            <option>已退款</option>
            <option>已过期</option>
          </select>
        </div>
        <button class="btn btn-default">查询</button>
        <div style="flex:1;"></div>
        <button class="btn btn-default">导出票据台账</button>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <span class="card-title">票据统一台账</span>
        <span style="font-size:12px;color:var(--color-text-muted);">系统直销和外部分销票据在同一页跟踪状态</span>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>票据号</th>
              <th>订单号</th>
              <th>票种</th>
              <th>票种分组</th>
              <th>来源渠道</th>
              <th>游客/手机号</th>
              <th>出票时间</th>
              <th>检票状态</th>
              <th>退款状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="voucher in vouchers" :key="voucher.code">
              <td style="font-family:monospace;font-size:12px;color:var(--color-blue);">{{ voucher.code }}</td>
              <td style="font-family:monospace;font-size:12px;color:var(--color-text-secondary);">{{ voucher.orderId }}</td>
              <td>
                <div style="font-weight:600;">{{ voucher.ticket }}</div>
                <div style="font-size:11px;color:var(--color-text-muted);">{{ voucher.scenic }}</div>
              </td>
              <td><span class="tag" :class="groupClass(voucher.group)">{{ voucher.group }}</span></td>
              <td>{{ voucher.source }}</td>
              <td style="font-size:12px;color:var(--color-text-secondary);">{{ voucher.visitor }}</td>
              <td>{{ voucher.issuedAt }}</td>
              <td><span class="tag" :class="voucher.verifyStatus === '已检票' ? 'tag-green' : voucher.verifyStatus === '待检票' ? 'tag-blue' : 'tag-gray'">{{ voucher.verifyStatus }}</span></td>
              <td><span class="tag" :class="voucher.refundStatus === '已退款' ? 'tag-red' : voucher.refundStatus === '可退款' ? 'tag-orange' : 'tag-gray'">{{ voucher.refundStatus }}</span></td>
              <td>
                <div style="display:flex;gap:8px;">
                  <span class="action-link" @click="openDetail(voucher)">详情</span>
                  <span class="action-link" v-if="voucher.verifyStatus === '待检票'">补打票据</span>
                  <span class="action-link danger" v-if="voucher.refundStatus === '可退款'">退票</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="modal-mask" v-if="showDetail && currentVoucher" @click.self="showDetail = false">
      <div class="modal-box" style="width:620px;">
        <div class="modal-header">
          <span class="modal-title">票据详情</span>
          <button class="modal-close" @click="showDetail = false">×</button>
        </div>
        <div class="modal-body">
          <div class="info-row"><span class="info-label">票据号</span><span class="info-value">{{ currentVoucher.code }}</span></div>
          <div class="info-row"><span class="info-label">订单号</span><span class="info-value">{{ currentVoucher.orderId }}</span></div>
          <div class="info-row"><span class="info-label">票种</span><span class="info-value">{{ currentVoucher.ticket }}</span></div>
          <div class="info-row"><span class="info-label">票种分组</span><span class="info-value">{{ currentVoucher.group }}</span></div>
          <div class="info-row"><span class="info-label">来源渠道</span><span class="info-value">{{ currentVoucher.source }}</span></div>
          <div class="info-row"><span class="info-label">检票状态</span><span class="info-value">{{ currentVoucher.verifyStatus }}</span></div>
          <div class="info-row"><span class="info-label">退款状态</span><span class="info-value">{{ currentVoucher.refundStatus }}</span></div>
          <div class="info-row"><span class="info-label">退票规则</span><span class="info-value">{{ currentVoucher.refundRule }}</span></div>
          <div class="divider"></div>
          <div style="font-size:13px;font-weight:600;color:var(--color-text-secondary);margin-bottom:8px;">状态链路</div>
          <table>
            <thead>
              <tr>
                <th>节点</th>
                <th>时间</th>
                <th>说明</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="step in currentVoucher.steps" :key="step.name">
                <td>{{ step.name }}</td>
                <td style="font-size:12px;color:var(--color-text-secondary);">{{ step.time }}</td>
                <td>{{ step.desc }}</td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="showDetail = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const showDetail = ref(false)
const currentVoucher = ref(null)

const summaryStats = [
  { label: '已出票票据', value: '2,184', sub: '含系统直销与分销渠道' },
  { label: '待检票票据', value: '624', sub: '尚未入园或体验' },
  { label: '可退款票据', value: '38', sub: '满足未使用可退规则' },
  { label: '异常票据', value: '4', sub: '渠道回传或核销状态不一致' },
]

const vouchers = [
  {
    code: 'VC202606070001',
    orderId: 'NN2026060700891',
    ticket: '青秀山成人门票',
    scenic: '青秀山风景区',
    group: '门票',
    source: '本地系统',
    visitor: '张** / 138****8888',
    issuedAt: '2026-06-07 09:12',
    verifyStatus: '待检票',
    refundStatus: '可退款',
    refundRule: '未使用可退',
    steps: [
      { name: '下单', time: '2026-06-07 09:10', desc: '窗口售票创建订单' },
      { name: '出票', time: '2026-06-07 09:12', desc: '本地系统出票成功' },
    ],
  },
  {
    code: 'VC202606070002',
    orderId: 'NN2026060700888',
    ticket: '邕江夜游全包票',
    scenic: '邕江景区',
    group: '全包票',
    source: '携程旅行',
    visitor: '李** / 139****6666',
    issuedAt: '2026-06-07 08:55',
    verifyStatus: '已检票',
    refundStatus: '不可退',
    refundRule: '未使用可退 + 过期自动退',
    steps: [
      { name: '渠道下单', time: '2026-06-07 08:48', desc: '携程下发订单' },
      { name: '出票', time: '2026-06-07 08:55', desc: '系统自动回传票据' },
      { name: '检票', time: '2026-06-07 09:30', desc: '入园核销成功' },
    ],
  },
  {
    code: 'VC202606070003',
    orderId: 'NN2026060700882',
    ticket: '青秀山观光车票',
    scenic: '青秀山风景区',
    group: '游玩票',
    source: '网售',
    visitor: '王** / 136****5555',
    issuedAt: '2026-06-07 08:20',
    verifyStatus: '待检票',
    refundStatus: '可退款',
    refundRule: '未使用可退',
    steps: [
      { name: '下单', time: '2026-06-07 08:18', desc: '网售渠道下单成功' },
      { name: '出票', time: '2026-06-07 08:20', desc: '电子票已发送' },
    ],
  },
]

function groupClass(group) {
  return {
    门票: 'tag-blue',
    游玩票: 'tag-orange',
    全包票: 'tag-green',
  }[group] || 'tag-gray'
}

function openDetail(voucher) {
  currentVoucher.value = voucher
  showDetail.value = true
}
</script>
