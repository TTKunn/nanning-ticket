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

    <div class="grid-2" style="align-items:start;">
      <div class="card">
        <div class="card-header">
          <span class="card-title">窗口售票台</span>
          <span class="tag tag-blue">业务人员操作端</span>
        </div>
        <div class="card-body">
          <div class="form-vertical">
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">所属园区</label>
                <select class="form-select" v-model="selectedScenicId">
                  <option value="">请选择</option>
                  <option v-for="scenic in scenics" :key="scenic.id" :value="scenic.id">{{ scenic.name }}</option>
                </select>
              </div>
              <div class="form-item">
                <label class="form-label">票种分组</label>
                <select class="form-select" v-model="selectedCategory">
                  <option value="">全部分组</option>
                  <option value="单票">单票</option>
                  <option value="套票">套票</option>
                  <option value="联票">联票</option>
                </select>
              </div>
            </div>

            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">票种</label>
                <select class="form-select" v-model="selectedTicketId">
                  <option value="">请选择</option>
                  <option v-for="ticket in filteredTickets" :key="ticket.id" :value="ticket.id">{{ ticket.name }}</option>
                </select>
              </div>
              <div class="form-item">
                <label class="form-label">游玩日期</label>
                <input class="form-input" type="date" v-model="visitDate" />
              </div>
            </div>

            <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">单价</label>
                <input class="form-input" :value="`¥${selectedTicket?.price || 0}`" disabled />
              </div>
              <div class="form-item">
                <label class="form-label">数量</label>
                <input class="form-input" type="number" min="1" v-model.number="quantity" />
              </div>
              <div class="form-item">
                <label class="form-label">剩余日库存</label>
                <input class="form-input" :value="todayRemaining" disabled />
              </div>
            </div>

            <div class="divider"></div>
            <div style="font-size:13px;font-weight:600;color:var(--color-text-secondary);margin-bottom:8px;">购票人信息</div>

            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">购票人姓名</label>
                <input class="form-input" v-model="form.visitorName" placeholder="请输入姓名" />
              </div>
              <div class="form-item">
                <label class="form-label">手机号</label>
                <input class="form-input" v-model="form.visitorPhone" placeholder="请输入手机号" />
              </div>
            </div>

            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">支付方式</label>
                <select class="form-select" v-model="form.paymentMethod">
                  <option value="现金">现金</option>
                  <option value="微信">微信支付</option>
                  <option value="支付宝">支付宝</option>
                  <option value="刷卡">POS刷卡</option>
                </select>
              </div>
              <div class="form-item">
                <label class="form-label">售票员</label>
                <input class="form-input" v-model="form.salespersonName" placeholder="如：李华" />
              </div>
            </div>

            <div class="alert alert-warning" style="margin-top:10px;">
              <svg width="14" height="14" viewBox="0 0 16 16" fill="currentColor"><path d="M8.982 1.566a1.13 1.13 0 00-1.96 0L.165 13.233c-.457.778.091 1.767.98 1.767h13.713c.889 0 1.438-.99.98-1.767L8.982 1.566zM8 5c.535 0 .954.462.9.995l-.35 3.507a.552.552 0 01-1.1 0L7.1 5.995A.905.905 0 018 5zm.002 6a1 1 0 110 2 1 1 0 010-2z"/></svg>
              剩余库存 <strong>{{ todayRemaining }}</strong>；价格 <strong>¥{{ selectedTicket?.price || 0 }}</strong>；合计 <strong>¥{{ amount }}</strong>
            </div>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <span class="card-title">出票摘要</span>
          <button class="btn btn-primary" :disabled="submitting || !canSubmit" @click="submitSale">
            {{ submitting ? '出票中...' : '确认出票' }}
          </button>
        </div>
        <div class="card-body">
          <div class="info-row"><span class="info-label">园区</span><span class="info-value">{{ selectedScenicName }}</span></div>
          <div class="info-row"><span class="info-label">票种</span><span class="info-value">{{ selectedTicket?.name || '—' }}</span></div>
          <div class="info-row"><span class="info-label">游玩日期</span><span class="info-value">{{ visitDate }}</span></div>
          <div class="info-row"><span class="info-label">购票人</span><span class="info-value">{{ form.visitorName || '—' }}</span></div>
          <div class="divider"></div>
          <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;margin-bottom:12px;">
            <div class="card" style="box-shadow:none;">
              <div class="card-body" style="padding:12px;">
                <div style="font-size:12px;color:var(--color-text-muted);">票面金额</div>
                <div style="font-size:22px;font-weight:700;">¥{{ amount }}</div>
              </div>
            </div>
            <div class="card" style="box-shadow:none;">
              <div class="card-body" style="padding:12px;">
                <div style="font-size:12px;color:var(--color-text-muted);">出票渠道</div>
                <div style="font-size:16px;font-weight:700;">本地系统</div>
              </div>
            </div>
          </div>

          <div style="font-size:13px;font-weight:600;margin-bottom:8px;color:var(--color-text-secondary);">今日窗口售票记录</div>
          <table>
            <thead>
              <tr>
                <th>时间</th>
                <th>票种</th>
                <th>数量</th>
                <th>金额</th>
                <th>状态</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="loadingRecent">
                <td colspan="5" class="empty-state">加载中...</td>
              </tr>
              <tr v-else-if="!recentRecords.length">
                <td colspan="5" class="empty-state">暂无售票记录</td>
              </tr>
              <tr v-for="record in recentRecords" v-else :key="record.id">
                <td style="font-size:12px;color:var(--color-text-secondary);">{{ record.time }}</td>
                <td>{{ record.ticket }}</td>
                <td>{{ record.qty }}</td>
                <td style="font-weight:600;">¥{{ record.amount }}</td>
                <td><span class="tag tag-green">已出票</span></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage } from './ui/Message'
import { listScenicOptions } from '../api/scenic'
import { listTicketOptions } from '../api/ticket'
import { listSales, createSale } from '../api/sale'
import { listInventories } from '../api/inventory'

const scenics = ref([])
const tickets = ref([])
const inventories = ref([])

const selectedScenicId = ref('')
const selectedCategory = ref('')
const selectedTicketId = ref('')
const visitDate = ref(new Date().toISOString().slice(0, 10))
const quantity = ref(1)
const submitting = ref(false)
const loadingRecent = ref(false)

const recentRecords = ref([])

const summaryStats = ref([
  { label: '今日窗口售票', value: '0', sub: '业务员现场售票订单数' },
  { label: '今日现场收入', value: '¥0', sub: '现金+电子支付合计' },
  { label: '待出票异常', value: '0', sub: '库存冲突或信息不完整' },
  { label: '现场退票', value: '0', sub: '按简化规则自动判定' },
])

const form = reactive({
  visitorName: '',
  visitorPhone: '',
  paymentMethod: '微信',
  salespersonName: '',
})

const selectedScenicName = computed(() => scenics.value.find(s => s.id === selectedScenicId.value)?.name || '—')

const filteredTickets = computed(() => {
  if (!selectedScenicId.value) return []
  return tickets.value
    .filter(t => t.scenicId === selectedScenicId.value)
    .filter(t => !selectedCategory.value || t.category === selectedCategory.value)
})

const selectedTicket = computed(() => tickets.value.find(t => t.id === selectedTicketId.value))

const todayRemaining = computed(() => {
  const inv = inventories.value.find(i => i.ticketId === selectedTicketId.value && i.inventoryDate === visitDate.value)
  return inv ? Number(inv.remaining || 0) : '—'
})

const amount = computed(() => Number(selectedTicket.value?.price || 0) * Number(quantity.value || 0))

const canSubmit = computed(() => {
  return selectedScenicId.value && selectedTicketId.value && visitDate.value && quantity.value > 0 && form.visitorName
})

watch(selectedScenicId, () => {
  // 切换园区时重置票种
  selectedTicketId.value = filteredTickets.value[0]?.id || ''
})

async function loadBaseData() {
  try {
    const [s, t] = await Promise.all([listScenicOptions(), listTicketOptions()])
    scenics.value = s || []
    tickets.value = t || []
    if (scenics.value.length) selectedScenicId.value = scenics.value[0].id
    if (tickets.value.length) selectedTicketId.value = tickets.value[0].id
  } catch (e) { /* handled */ }
}

async function loadRecent() {
  loadingRecent.value = true
  try {
    const data = await listSales({ pageNum: 1, pageSize: 10, dateFrom: visitDate.value, dateTo: visitDate.value })
    const records = data?.records || []
    recentRecords.value = records.map(r => ({
      id: r.id,
      time: r.createdAt ? r.createdAt.slice(11, 16) : '',
      ticket: r.items?.[0]?.ticketName || '—',
      qty: r.items?.reduce((s, it) => s + Number(it.quantity || 0), 0) || 0,
      amount: Number(r.totalAmount || 0).toFixed(2),
    }))
    summaryStats.value[0].value = `${data?.total || 0} 单`
    summaryStats.value[1].value = `¥${records.reduce((s, r) => s + Number(r.totalAmount || 0), 0).toLocaleString()}`
  } catch (e) { /* handled */ }
  finally { loadingRecent.value = false }
}

async function loadInventory() {
  if (!selectedTicketId.value || !visitDate.value) return
  try {
    const data = await listInventories({
      ticketId: selectedTicketId.value,
      dateFrom: visitDate.value,
      dateTo: visitDate.value,
      pageSize: 1,
    })
    inventories.value = data?.records || []
  } catch (e) { /* handled */ }
}

async function submitSale() {
  if (!canSubmit.value) {
    ElMessage({ type: 'warning', message: '请完善购票信息' })
    return
  }
  submitting.value = true
  try {
    await createSale({
      scenicId: selectedScenicId.value,
      windowName: '1号窗口',
      salespersonName: form.salespersonName || '业务员',
      visitorName: form.visitorName,
      visitorPhone: form.visitorPhone,
      paymentMethod: form.paymentMethod,
      items: [{
        ticketId: Number(selectedTicketId.value),
        inventoryDate: visitDate.value,
        quantity: Number(quantity.value),
      }],
    })
    ElMessage({ type: 'success', message: '出票成功' })
    form.visitorName = ''
    form.visitorPhone = ''
    loadRecent()
    loadInventory()
  } catch (e) { /* handled */ }
  finally { submitting.value = false }
}

onMounted(async () => {
  await loadBaseData()
  await loadRecent()
  await loadInventory()
})
</script>
