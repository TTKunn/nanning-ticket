<template>
  <div>
    <div class="alert alert-warning" style="margin-bottom:12px;">
      <svg width="14" height="14" viewBox="0 0 16 16" fill="currentColor"><path d="M8.982 1.566a1.13 1.13 0 00-1.96 0L.165 13.233c-.457.778.091 1.767.98 1.767h13.713c.889 0 1.438-.99.98-1.767L8.982 1.566zM8 5c.535 0 .954.462.9.995l-.35 3.507a.552.552 0 01-1.1 0L7.1 5.995A.905.905 0 018 5zm.002 6a1 1 0 110 2 1 1 0 010-2z"/></svg>
      当前有 <strong>{{ warningCount }}</strong> 个票种今日库存低于预警线，请优先调整日历库存而不是仅补总库存。
    </div>

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
          <input class="form-input" v-model="filterKeyword" placeholder="搜索票种..." style="width:180px;" />
        </div>
        <div class="form-item">
          <select class="form-select" v-model="filterScenic">
            <option value="">全部园区</option>
            <option v-for="s in scenics" :key="s.id" :value="s.id">{{ s.name }}</option>
          </select>
        </div>
        <div class="form-item">
          <input class="form-input" type="date" v-model="filterDate" style="width:150px;" />
        </div>
        <div class="form-item">
          <select class="form-select" v-model="filterStatus">
            <option value="">全部状态</option>
            <option value="开放">开放</option>
            <option value="关闭">关闭</option>
          </select>
        </div>
        <button class="btn btn-default" @click="loadInventories">查询</button>
        <div style="flex:1;"></div>
        <button class="btn btn-primary" @click="openBatchAdjust">批量调整</button>
      </div>
    </div>

    <div class="card">
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>票种</th>
              <th>所属园区</th>
              <th>日期</th>
              <th>总库存</th>
              <th>已售</th>
              <th>剩余</th>
              <th>状态</th>
              <th>备注</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="9" class="empty-state">加载中...</td>
            </tr>
            <tr v-else-if="!inventoryList.length">
              <td colspan="9" class="empty-state">暂无库存数据</td>
            </tr>
            <tr v-for="item in inventoryList" v-else :key="item.id">
              <td>
                <div style="font-weight:600;">{{ item.ticketName }}</div>
                <div style="font-size:11px;color:var(--color-text-muted);">{{ item.ticketCode }}</div>
              </td>
              <td>{{ item.scenicName }}</td>
              <td>{{ item.inventoryDate }}</td>
              <td>{{ item.total }}</td>
              <td>{{ item.sold || 0 }}</td>
              <td>
                <span :style="{ fontWeight:600, color: getStockColor(item.remaining, item.warning) }">
                  {{ item.remaining }}
                </span>
              </td>
              <td>
                <span class="tag" :class="item.status === '开放' ? 'tag-green' : 'tag-gray'">{{ item.status }}</span>
              </td>
              <td style="font-size:12px;color:var(--color-text-secondary);">{{ item.remark || '—' }}</td>
              <td>
                <div style="display:flex;gap:8px;">
                  <span class="action-link" @click="openAdjust(item)">调整</span>
                  <span class="action-link danger" @click="closeInventory(item)" v-if="item.status === '开放'">关闭</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="pagination">
        <span class="pagination-info">共 {{ total }} 条</span>
        <button class="page-btn" :disabled="pageNum <= 1" @click="pageNum--; loadInventories()">«</button>
        <button v-for="p in pages" :key="p" class="page-btn" :class="{ active: p === pageNum }" @click="pageNum = p; loadInventories()">{{ p }}</button>
        <button class="page-btn" :disabled="pageNum >= pages" @click="pageNum++; loadInventories()">»</button>
      </div>
    </div>

    <!-- 调整日库存弹窗 -->
    <div class="modal-mask" v-if="showAdjustModal" @click.self="showAdjustModal = false">
      <div class="modal-box" style="width:460px;">
        <div class="modal-header">
          <span class="modal-title">调整库存{{ selectedItem ? ' - ' + selectedItem.ticketName : '' }}</span>
          <button class="modal-close" @click="showAdjustModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-vertical">
            <div class="form-item">
              <label class="form-label">日期</label>
              <input class="form-input" type="date" v-model="form.inventoryDate" />
            </div>
            <div class="form-item">
              <label class="form-label">调整后总库存</label>
              <input class="form-input" type="number" v-model.number="form.total" placeholder="请输入调整数量" />
            </div>
            <div class="form-item">
              <label class="form-label">备注</label>
              <input class="form-input" v-model="form.remark" placeholder="调整原因 / 说明" />
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="showAdjustModal = false">取消</button>
          <button class="btn btn-primary" :disabled="saving" @click="submitAdjust">{{ saving ? '保存中...' : '保存调整' }}</button>
        </div>
      </div>
    </div>

    <!-- 批量调整弹窗 -->
    <div class="modal-mask" v-if="showBatchModal" @click.self="showBatchModal = false">
      <div class="modal-box" style="width:520px;">
        <div class="modal-header">
          <span class="modal-title">批量创建库存</span>
          <button class="modal-close" @click="showBatchModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-vertical">
            <div class="form-item">
              <label class="form-label">票种</label>
              <select class="form-select" v-model="batchForm.ticketId">
                <option value="">请选择</option>
                <option v-for="t in ticketOptions" :key="t.id" :value="t.id">{{ t.name }}</option>
              </select>
            </div>
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">开始日期</label>
                <input class="form-input" type="date" v-model="batchForm.startDate" />
              </div>
              <div class="form-item">
                <label class="form-label">结束日期</label>
                <input class="form-input" type="date" v-model="batchForm.endDate" />
              </div>
            </div>
            <div class="form-item">
              <label class="form-label">每日总库存</label>
              <input class="form-input" type="number" v-model.number="batchForm.total" placeholder="如：500" />
            </div>
            <div class="form-item">
              <label class="form-label">备注</label>
              <input class="form-input" v-model="batchForm.remark" placeholder="可选" />
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="showBatchModal = false">取消</button>
          <button class="btn btn-primary" :disabled="saving" @click="submitBatch">{{ saving ? '提交中...' : '批量创建' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from './ui/Message'
import { listInventories, createInventory, batchCreateInventory, toggleInventoryStatus } from '../api/inventory'
import { listScenicOptions } from '../api/scenic'
import { listTicketOptions } from '../api/ticket'

const inventoryList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(20)
const pages = ref(1)
const loading = ref(false)
const saving = ref(false)

const scenics = ref([])
const ticketOptions = ref([])

const filterKeyword = ref('')
const filterScenic = ref('')
const filterDate = ref('')
const filterStatus = ref('')

const summaryStats = ref([
  { label: '启用票种', value: '0', sub: '含单票、套票、联票' },
  { label: '总库存余额', value: '0', sub: '跨票种汇总剩余总量' },
  { label: '今日库存余额', value: '0', sub: '按当日统计' },
  { label: '预警票种', value: '0', sub: '需补充日历库存' },
])

const showAdjustModal = ref(false)
const showBatchModal = ref(false)
const selectedItem = ref(null)
const form = reactive({ inventoryDate: '', total: 0, remark: '' })
const batchForm = reactive({
  ticketId: '',
  startDate: '',
  endDate: '',
  total: 0,
  remark: '',
})

const warningCount = computed(() => inventoryList.value.filter(it => Number(it.remaining || 0) <= 20).length)

function getStockColor(remaining) {
  if (!remaining || remaining === 0) return 'var(--color-gray-400)'
  if (remaining < 20) return 'var(--color-red)'
  if (remaining < 50) return 'var(--color-orange)'
  return 'var(--color-text-primary)'
}

async function loadScenics() {
  try { scenics.value = await listScenicOptions() } catch (e) { /* handled */ }
}
async function loadTicketOptions() {
  try { ticketOptions.value = await listTicketOptions() } catch (e) { /* handled */ }
}

async function loadInventories() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (filterKeyword.value) params.keyword = filterKeyword.value
    if (filterScenic.value) params.scenicId = filterScenic.value
    if (filterDate.value) {
      params.dateFrom = filterDate.value
      params.dateTo = filterDate.value
    }
    if (filterStatus.value) params.status = filterStatus.value
    const data = await listInventories(params)
    inventoryList.value = data?.records || []
    total.value = data?.total || 0
    pages.value = data?.pages || 1

    // 计算 summary
    let totalStock = 0, todayRemain = 0
    inventoryList.value.forEach(it => {
      totalStock += Number(it.total || 0)
      todayRemain += Number(it.remaining || 0)
    })
    summaryStats.value[0].value = String(inventoryList.value.length)
    summaryStats.value[1].value = totalStock.toLocaleString()
    summaryStats.value[2].value = todayRemain.toLocaleString()
    summaryStats.value[3].value = String(warningCount.value)
    summaryStats.value[3].sub = filterDate.value ? `按 ${filterDate.value} 统计` : '按全部日期统计'
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

function openAdjust(item) {
  selectedItem.value = item
  Object.assign(form, {
    inventoryDate: item.inventoryDate,
    total: item.total,
    remark: item.remark || '',
  })
  showAdjustModal.value = true
}

function openBatchAdjust() {
  Object.assign(batchForm, {
    ticketId: '',
    startDate: filterDate.value || new Date().toISOString().slice(0, 10),
    endDate: filterDate.value || new Date().toISOString().slice(0, 10),
    total: 500, remark: '',
  })
  showBatchModal.value = true
}

async function submitAdjust() {
  if (!selectedItem.value) return
  saving.value = true
  try {
    await createInventory({
      ticketId: selectedItem.value.ticketId,
      inventoryDate: form.inventoryDate,
      total: form.total,
      status: '开放',
      remark: form.remark,
    })
    ElMessage({ type: 'success', message: '库存已更新' })
    showAdjustModal.value = false
    loadInventories()
  } catch (e) { /* handled */ }
  finally { saving.value = false }
}

async function submitBatch() {
  if (!batchForm.ticketId) { ElMessage({ type: 'warning', message: '请选择票种' }); return }
  if (!batchForm.startDate || !batchForm.endDate) { ElMessage({ type: 'warning', message: '请选择日期范围' }); return }
  saving.value = true
  try {
    await batchCreateInventory(batchForm)
    ElMessage({ type: 'success', message: '批量创建成功' })
    showBatchModal.value = false
    loadInventories()
  } catch (e) { /* handled */ }
  finally { saving.value = false }
}

async function closeInventory(item) {
  try {
    await toggleInventoryStatus(item.id, '关闭')
    ElMessage({ type: 'success', message: '已关闭' })
    loadInventories()
  } catch (e) { /* handled */ }
}

onMounted(async () => {
  await loadScenics()
  await loadTicketOptions()
  await loadInventories()
})
</script>
