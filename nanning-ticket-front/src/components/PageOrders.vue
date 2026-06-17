<template>
  <div>
    <div class="card" style="margin-bottom:12px;">
      <div class="toolbar">
        <div class="form-item">
          <input class="form-input" v-model="filterKeyword" placeholder="订单号/手机号..." style="width:180px;" />
        </div>
        <div class="form-item">
          <select class="form-select" v-model="filterScenicId">
            <option value="">全部园区</option>
            <option v-for="s in scenics" :key="s.id" :value="s.id">{{ s.name }}</option>
          </select>
        </div>
        <div class="form-item">
          <select class="form-select" v-model="filterChannelCode">
            <option value="">全部渠道</option>
            <option value="DIRECT">直销</option>
            <option value="MEITUAN">美团</option>
            <option value="CTRIP">携程</option>
            <option value="FLIGGY">飞猪</option>
          </select>
        </div>
        <div class="form-item">
          <input class="form-input" type="date" v-model="filterDateFrom" style="width:140px;" />
        </div>
        <div class="form-item">
          <input class="form-input" type="date" v-model="filterDateTo" style="width:140px;" />
        </div>
        <button class="btn btn-default" @click="loadOrders">查询</button>
        <button class="btn btn-default" @click="resetFilters">重置</button>
        <div style="flex:1;"></div>
      </div>
    </div>

    <!-- 状态快捷筛选 -->
    <div class="tab-bar" style="margin-bottom:12px;border-radius:var(--radius-md) var(--radius-md) 0 0;">
      <div class="tab-item" :class="{ active: filterStatus === '' }" @click="filterStatus = ''; loadOrders()">
        全部 <span class="tag tag-gray" style="margin-left:4px;">{{ total }}</span>
      </div>
      <div class="tab-item" :class="{ active: filterStatus === '已支付' }" @click="filterStatus = '已支付'; loadOrders()">
        已支付 <span class="tag tag-green" style="margin-left:4px;">{{ statusCount('已支付') }}</span>
      </div>
      <div class="tab-item" :class="{ active: filterStatus === '已出票' }" @click="filterStatus = '已出票'; loadOrders()">
        已出票 <span class="tag tag-blue" style="margin-left:4px;">{{ statusCount('已出票') }}</span>
      </div>
      <div class="tab-item" :class="{ active: filterStatus === '已核销' }" @click="filterStatus = '已核销'; loadOrders()">
        已核销 <span class="tag tag-gray" style="margin-left:4px;">{{ statusCount('已核销') }}</span>
      </div>
      <div class="tab-item" :class="{ active: filterStatus === '已退款' }" @click="filterStatus = '已退款'; loadOrders()">
        退款 <span class="tag tag-red" style="margin-left:4px;">{{ statusCount('已退款') }}</span>
      </div>
    </div>

    <div class="card">
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>订单号</th>
              <th>票种</th>
              <th>购买人</th>
              <th>手机号</th>
              <th>数量</th>
              <th>实付金额</th>
              <th>渠道</th>
              <th>下单时间</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="10" class="empty-state">加载中...</td>
            </tr>
            <tr v-else-if="!filteredOrders.length">
              <td colspan="10" class="empty-state">暂无订单数据</td>
            </tr>
            <tr v-for="o in filteredOrders" v-else :key="o.id">
              <td style="font-family:monospace;font-size:12px;color:var(--color-text-secondary);">{{ o.orderNo }}</td>
              <td>
                <div style="font-weight:500;">{{ o.ticketName }}</div>
                <div style="font-size:11px;color:var(--color-text-muted);">{{ o.scenicName }}</div>
              </td>
              <td>{{ o.userName || o.contactName }}</td>
              <td style="font-family:monospace;font-size:12px;">{{ maskPhone(o.contactPhone) }}</td>
              <td>{{ o.totalQuantity || (o.items?.reduce((s, it) => s + Number(it.quantity || 0), 0)) }}</td>
              <td style="font-weight:600;">¥{{ Number(o.totalAmount || 0).toFixed(2) }}</td>
              <td><span class="tag tag-gray">{{ o.channelName || o.channelCode }}</span></td>
              <td style="font-size:12px;color:var(--color-text-secondary);">{{ o.createdAt }}</td>
              <td>
                <span class="tag" :class="statusClass(o.status)">{{ o.status }}</span>
              </td>
              <td>
                <div style="display:flex;gap:8px;">
                  <span class="action-link" @click="viewOrder(o)">详情</span>
                  <span class="action-link danger" v-if="o.status === '已出票'" @click="refundOrder(o)">退款</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="pagination">
        <span class="pagination-info">共 {{ total }} 条</span>
        <button class="page-btn" :disabled="pageNum <= 1" @click="pageNum--; loadOrders()">«</button>
        <button v-for="p in pages" :key="p" class="page-btn" :class="{ active: p === pageNum }" @click="pageNum = p; loadOrders()">{{ p }}</button>
        <button class="page-btn" :disabled="pageNum >= pages" @click="pageNum++; loadOrders()">»</button>
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
            <span style="font-family:monospace;font-size:13px;color:var(--color-text-secondary);">{{ currentOrder.orderNo }}</span>
            <span class="tag" :class="statusClass(currentOrder.status)">{{ currentOrder.status }}</span>
          </div>
          <div class="divider"></div>
          <div style="font-size:13px;font-weight:600;margin-bottom:10px;color:var(--color-text-secondary);">票务信息</div>
          <div class="info-row"><span class="info-label">票种</span><span class="info-value">{{ currentOrder.ticketName }}</span></div>
          <div class="info-row"><span class="info-label">景区</span><span class="info-value">{{ currentOrder.scenicName }}</span></div>
          <div class="info-row"><span class="info-label">购买数量</span><span class="info-value">{{ currentOrder.totalQuantity }}</span></div>
          <div class="divider"></div>
          <div style="font-size:13px;font-weight:600;margin-bottom:10px;color:var(--color-text-secondary);">购买人信息</div>
          <div class="info-row"><span class="info-label">购买人</span><span class="info-value">{{ currentOrder.userName || currentOrder.contactName }}</span></div>
          <div class="info-row"><span class="info-label">手机号</span><span class="info-value">{{ currentOrder.contactPhone }}</span></div>
          <div class="divider"></div>
          <div style="font-size:13px;font-weight:600;margin-bottom:10px;color:var(--color-text-secondary);">支付信息</div>
          <div class="info-row"><span class="info-label">销售渠道</span><span class="info-value">{{ currentOrder.channelName || currentOrder.channelCode }}</span></div>
          <div class="info-row"><span class="info-label">实付金额</span><span class="info-value" style="font-weight:600;color:var(--color-red);">¥{{ Number(currentOrder.totalAmount || 0).toFixed(2) }}</span></div>
          <div class="info-row"><span class="info-label">下单时间</span><span class="info-value">{{ currentOrder.createdAt }}</span></div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="showDetail = false">关闭</button>
          <button class="btn btn-primary" v-if="currentOrder.status === '已出票'" @click="refundOrder(currentOrder)">申请退款</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from './ui/Message'
import { listOrders, getOrderStats, refundOrder as refundOrderApi } from '../api/order'
import { listScenicOptions } from '../api/scenic'

const orders = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const pages = ref(1)
const loading = ref(false)
const scenics = ref([])
const statusCache = ref({}) // 按 status 缓存数量，避免每次切换都重发

const filterKeyword = ref('')
const filterScenicId = ref('')
const filterChannelCode = ref('')
const filterDateFrom = ref('')
const filterDateTo = ref('')
const filterStatus = ref('')

const showDetail = ref(false)
const currentOrder = ref(null)

const filteredOrders = computed(() => orders.value)

function maskPhone(p) {
  if (!p) return '—'
  const s = String(p)
  if (s.length < 7) return s
  return s.slice(0, 3) + '****' + s.slice(-4)
}

function statusClass(s) {
  return {
    '已支付': 'tag-green', '待支付': 'tag-orange',
    '已核销': 'tag-gray', '已出票': 'tag-blue',
    '已退款': 'tag-red', '已取消': 'tag-gray',
  }[s] || 'tag-gray'
}

function statusCount(s) {
  return statusCache.value[s] || 0
}

async function loadScenics() {
  try { scenics.value = await listScenicOptions() } catch (e) { /* handled */ }
}

async function loadOrders() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (filterKeyword.value) params.keyword = filterKeyword.value
    if (filterScenicId.value) params.scenicId = filterScenicId.value
    if (filterChannelCode.value) params.channelCode = filterChannelCode.value
    if (filterStatus.value) params.status = filterStatus.value
    if (filterDateFrom.value) params.dateFrom = filterDateFrom.value
    if (filterDateTo.value) params.dateTo = filterDateTo.value
    const data = await listOrders(params)
    orders.value = data?.records || []
    total.value = data?.total || 0
    pages.value = data?.pages || 1
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

// 单独拉每个状态的计数（用于顶部 tab 显示）
async function loadStatusCounts() {
  const all = ['已支付', '已出票', '已核销', '已退款']
  await Promise.all(all.map(async (s) => {
    try {
      const data = await listOrders({ status: s, pageNum: 1, pageSize: 1 })
      statusCache.value[s] = data?.total || 0
    } catch (e) { statusCache.value[s] = 0 }
  }))
}

function resetFilters() {
  filterKeyword.value = ''
  filterScenicId.value = ''
  filterChannelCode.value = ''
  filterDateFrom.value = ''
  filterDateTo.value = ''
  filterStatus.value = ''
  pageNum.value = 1
  loadOrders()
}

function viewOrder(o) {
  currentOrder.value = o
  showDetail.value = true
}

async function refundOrder(o) {
  const reason = prompt('请输入退款原因', '客户取消')
  if (!reason) return
  try {
    await refundOrderApi(o.id, { reason })
    ElMessage({ type: 'success', message: '退款成功' })
    showDetail.value = false
    loadOrders()
  } catch (e) { /* handled */ }
}

onMounted(async () => {
  await loadScenics()
  await loadOrders()
  await loadStatusCounts()
})
</script>
