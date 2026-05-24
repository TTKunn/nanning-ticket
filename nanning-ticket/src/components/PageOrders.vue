<template>
  <div>
    <!-- 工具栏 -->
    <div class="card" style="margin-bottom:12px;">
      <div class="toolbar">
        <div class="form-item">
          <input class="form-input" placeholder="订单号/手机号..." style="width:180px;" />
        </div>
        <div class="form-item">
          <select class="form-select">
            <option>全部渠道</option>
            <option>AI南宁直销</option>
            <option>美团旅游</option>
            <option>携程旅行</option>
            <option>飞猪旅行</option>
          </select>
        </div>
        <div class="form-item">
          <select class="form-select" v-model="filterStatus">
            <option value="">全部状态</option>
            <option value="paid">已支付</option>
            <option value="pending">待核销</option>
            <option value="verified">已核销</option>
            <option value="refunded">已退款</option>
            <option value="cancelled">已取消</option>
          </select>
        </div>
        <div class="form-item">
          <input class="form-input" type="date" style="width:140px;" />
        </div>
        <div class="form-item">
          <input class="form-input" type="date" style="width:140px;" />
        </div>
        <button class="btn btn-default">查询</button>
        <button class="btn btn-default">重置</button>
        <div style="flex:1;"></div>
        <button class="btn btn-default">
          <svg width="13" height="13" viewBox="0 0 16 16" fill="currentColor"><path d="M.5 9.9a.5.5 0 01.5.5v2.5a1 1 0 001 1h12a1 1 0 001-1v-2.5a.5.5 0 011 0v2.5a2 2 0 01-2 2H2a2 2 0 01-2-2v-2.5a.5.5 0 01.5-.5z"/><path d="M7.646 11.854a.5.5 0 00.708 0l3-3a.5.5 0 00-.708-.708L8.5 10.293V1.5a.5.5 0 00-1 0v8.793L5.354 8.146a.5.5 0 10-.708.708l3 3z"/></svg>
          导出
        </button>
      </div>
    </div>

    <!-- 状态快捷筛选 -->
    <div class="tab-bar" style="margin-bottom:12px;border-radius:var(--radius-md) var(--radius-md) 0 0;">
      <div class="tab-item" :class="{ active: filterStatus === '' }" @click="filterStatus = ''">
        全部 <span class="tag tag-gray" style="margin-left:4px;">{{ orders.length }}</span>
      </div>
      <div class="tab-item" :class="{ active: filterStatus === 'paid' }" @click="filterStatus = 'paid'">
        已支付 <span class="tag tag-green" style="margin-left:4px;">{{ countByStatus('已支付') }}</span>
      </div>
      <div class="tab-item" :class="{ active: filterStatus === 'pending' }" @click="filterStatus = 'pending'">
        待核销 <span class="tag tag-blue" style="margin-left:4px;">{{ countByStatus('待核销') }}</span>
      </div>
      <div class="tab-item" :class="{ active: filterStatus === 'verified' }" @click="filterStatus = 'verified'">
        已核销 <span class="tag tag-gray" style="margin-left:4px;">{{ countByStatus('已核销') }}</span>
      </div>
      <div class="tab-item" :class="{ active: filterStatus === 'refunded' }" @click="filterStatus = 'refunded'">
        退款 <span class="tag tag-red" style="margin-left:4px;">{{ countByStatus('已退款') }}</span>
      </div>
    </div>

    <!-- 订单表格 -->
    <div class="card">
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th style="width:40px;"><input type="checkbox" /></th>
              <th>订单号</th>
              <th>票种名称</th>
              <th>购买人</th>
              <th>手机号</th>
              <th>数量</th>
              <th>实付金额</th>
              <th>渠道</th>
              <th>下单时间</th>
              <th>游览日期</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="o in filteredOrders" :key="o.id">
              <td><input type="checkbox" /></td>
              <td style="font-family:monospace;font-size:12px;color:var(--color-text-secondary);">{{ o.id }}</td>
              <td>
                <div style="font-weight:500;">{{ o.ticket }}</div>
                <div style="font-size:11px;color:var(--color-text-muted);">{{ o.scenic }}</div>
              </td>
              <td>{{ o.buyer }}</td>
              <td style="font-family:monospace;font-size:12px;">{{ o.phone }}</td>
              <td>{{ o.qty }}</td>
              <td style="font-weight:600;">¥{{ o.amount }}</td>
              <td><span class="tag tag-gray">{{ o.channel }}</span></td>
              <td style="font-size:12px;color:var(--color-text-secondary);">{{ o.orderTime }}</td>
              <td style="font-size:12px;">{{ o.visitDate }}</td>
              <td>
                <span class="tag" :class="statusClass(o.status)">{{ o.status }}</span>
              </td>
              <td>
                <div style="display:flex;gap:8px;">
                  <span class="action-link" @click="viewOrder(o)">详情</span>
                  <span class="action-link" v-if="o.status === '待核销'">核销</span>
                  <span class="action-link danger" v-if="o.status === '已支付' || o.status === '待核销'">退款</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="pagination">
        <span class="pagination-info">共 {{ filteredOrders.length }} 条</span>
        <button class="page-btn" disabled>«</button>
        <button class="page-btn active">1</button>
        <button class="page-btn">2</button>
        <button class="page-btn">3</button>
        <button class="page-btn">»</button>
      </div>
    </div>

    <!-- 订单详情弹窗 -->
    <div class="modal-mask" v-if="showDetail && currentOrder" @click.self="showDetail = false">
      <div class="modal-box" style="width:560px;">
        <div class="modal-header">
          <span class="modal-title">订单详情</span>
          <button class="modal-close" @click="showDetail = false">×</button>
        </div>
        <div class="modal-body">
          <div style="display:flex;justify-content:space-between;align-items:center;margin-bottom:16px;">
            <span style="font-family:monospace;font-size:13px;color:var(--color-text-secondary);">{{ currentOrder.id }}</span>
            <span class="tag" :class="statusClass(currentOrder.status)">{{ currentOrder.status }}</span>
          </div>
          <div class="divider"></div>
          <div style="font-size:13px;font-weight:600;margin-bottom:10px;color:var(--color-text-secondary);">票务信息</div>
          <div class="info-row"><span class="info-label">票种名称</span><span class="info-value">{{ currentOrder.ticket }}</span></div>
          <div class="info-row"><span class="info-label">所属景区</span><span class="info-value">{{ currentOrder.scenic }}</span></div>
          <div class="info-row"><span class="info-label">购买数量</span><span class="info-value">{{ currentOrder.qty }} 张</span></div>
          <div class="info-row"><span class="info-label">游览日期</span><span class="info-value">{{ currentOrder.visitDate }}</span></div>
          <div class="divider"></div>
          <div style="font-size:13px;font-weight:600;margin-bottom:10px;color:var(--color-text-secondary);">购买人信息</div>
          <div class="info-row"><span class="info-label">购买人</span><span class="info-value">{{ currentOrder.buyer }}</span></div>
          <div class="info-row"><span class="info-label">手机号</span><span class="info-value">{{ currentOrder.phone }}</span></div>
          <div class="divider"></div>
          <div style="font-size:13px;font-weight:600;margin-bottom:10px;color:var(--color-text-secondary);">支付信息</div>
          <div class="info-row"><span class="info-label">销售渠道</span><span class="info-value">{{ currentOrder.channel }}</span></div>
          <div class="info-row"><span class="info-label">实付金额</span><span class="info-value" style="font-weight:600;color:var(--color-red);">¥{{ currentOrder.amount }}</span></div>
          <div class="info-row"><span class="info-label">下单时间</span><span class="info-value">{{ currentOrder.orderTime }}</span></div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="showDetail = false">关闭</button>
          <button class="btn btn-primary" v-if="currentOrder.status === '待核销'">立即核销</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const filterStatus = ref('')
const showDetail = ref(false)
const currentOrder = ref(null)

const orders = [
  { id: 'NN2026052400123', ticket: '青秀山成人票', scenic: '青秀山风景区', buyer: '张**', phone: '138****8888', qty: 2, amount: '120.00', channel: '美团', orderTime: '2026-05-24 09:12', visitDate: '2026-05-25', status: '已支付' },
  { id: 'NN2026052400122', ticket: '南湖公园联票', scenic: '南湖公园', buyer: '李**', phone: '139****6666', qty: 1, amount: '120.00', channel: '携程', orderTime: '2026-05-24 08:55', visitDate: '2026-05-24', status: '待核销' },
  { id: 'NN2026052400121', ticket: '邕江游船夜游', scenic: '邕江景区', buyer: '王**', phone: '136****5555', qty: 3, amount: '264.00', channel: '直销', orderTime: '2026-05-24 08:30', visitDate: '2026-05-24', status: '已核销' },
  { id: 'NN2026052400120', ticket: '广西民族博物馆票', scenic: '广西民族博物馆', buyer: '赵**', phone: '137****4444', qty: 1, amount: '30.00', channel: '飞猪', orderTime: '2026-05-23 20:10', visitDate: '2026-05-24', status: '已退款' },
  { id: 'NN2026052400119', ticket: '青秀山成人票', scenic: '青秀山风景区', buyer: '陈**', phone: '135****3333', qty: 2, amount: '120.00', channel: '直销', orderTime: '2026-05-23 18:45', visitDate: '2026-05-26', status: '已支付' },
  { id: 'NN2026052400118', ticket: '良凤江森林公园票', scenic: '良凤江国家森林公园', buyer: '刘**', phone: '133****2222', qty: 4, amount: '180.00', channel: '同程', orderTime: '2026-05-23 16:20', visitDate: '2026-05-25', status: '待核销' },
  { id: 'NN2026052400117', ticket: '青秀山儿童票', scenic: '青秀山风景区', buyer: '孙**', phone: '132****1111', qty: 2, amount: '60.00', channel: '美团', orderTime: '2026-05-23 14:05', visitDate: '2026-05-24', status: '已核销' },
  { id: 'NN2026052400116', ticket: '南湖公园联票', scenic: '南湖公园', buyer: '周**', phone: '131****0000', qty: 1, amount: '120.00', channel: '携程', orderTime: '2026-05-23 11:30', visitDate: '2026-05-23', status: '已取消' },
]

const statusMap = { '已支付': 'paid', '待核销': 'pending', '已核销': 'verified', '已退款': 'refunded', '已取消': 'cancelled' }

const filteredOrders = computed(() => {
  if (!filterStatus.value) return orders
  return orders.filter(o => statusMap[o.status] === filterStatus.value)
})

function countByStatus(s) {
  return orders.filter(o => o.status === s).length
}

function statusClass(s) {
  const map = { '已支付': 'tag-green', '待核销': 'tag-blue', '已核销': 'tag-gray', '已退款': 'tag-red', '已取消': 'tag-gray' }
  return map[s] || 'tag-gray'
}

function viewOrder(o) {
  currentOrder.value = o
  showDetail.value = true
}
</script>
