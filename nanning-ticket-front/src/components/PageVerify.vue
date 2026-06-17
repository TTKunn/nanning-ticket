<template>
  <div>
    <div class="card" style="margin-bottom:12px;">
      <div class="toolbar">
        <div class="form-item">
          <input class="form-input" v-model="filterKeyword" placeholder="核销码/票据号/订单号..." style="width:220px;" />
        </div>
        <div class="form-item">
          <select class="form-select" v-model="filterScenicId">
            <option value="">全部园区</option>
            <option v-for="s in scenics" :key="s.id" :value="s.id">{{ s.name }}</option>
          </select>
        </div>
        <button class="btn btn-default" @click="loadRecords">查询</button>
        <div style="flex:1;"></div>
        <button class="btn btn-primary" @click="showScanModal = true">扫码检票</button>
      </div>
    </div>

    <div class="stat-grid" style="grid-template-columns:repeat(4,1fr);margin-bottom:12px;">
      <div class="stat-card" v-for="s in verifyStats" :key="s.label">
        <div class="stat-card-header">
          <span class="stat-card-label">{{ s.label }}</span>
        </div>
        <div class="stat-card-value" :style="{ color: s.color }">{{ s.value }}</div>
        <div style="font-size:12px;color:var(--color-text-muted);margin-top:4px;">{{ s.sub }}</div>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <span class="card-title">检票记录</span>
        <span class="action-link" @click="loadRecords">刷新</span>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>检票码</th>
              <th>票据号</th>
              <th>票种名称</th>
              <th>园区</th>
              <th>游客姓名</th>
              <th>检票方式</th>
              <th>检票时间</th>
              <th>检票人员</th>
              <th>状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="9" class="empty-state">加载中...</td>
            </tr>
            <tr v-else-if="!verifyRecords.length">
              <td colspan="9" class="empty-state">暂无检票记录</td>
            </tr>
            <tr v-for="record in verifyRecords" v-else :key="record.id">
              <td style="font-family:monospace;font-size:12px;color:var(--color-blue);">{{ record.verifyNo }}</td>
              <td style="font-family:monospace;font-size:12px;color:var(--color-text-muted);">{{ record.voucherCode }}</td>
              <td>{{ record.ticketName }}</td>
              <td>{{ record.scenicName }}</td>
              <td>{{ record.visitorName }}</td>
              <td>{{ record.verifyMethod }}</td>
              <td style="font-size:12px;color:var(--color-text-secondary);">{{ record.verifyTime }}</td>
              <td>{{ record.verifyStaffName }}</td>
              <td>
                <span class="tag" :class="record.result === '成功' ? 'tag-green' : 'tag-red'">
                  {{ record.result }}
                </span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="pagination">
        <span class="pagination-info">共 {{ total }} 条</span>
        <button class="page-btn" :disabled="pageNum <= 1" @click="pageNum--; loadRecords()">«</button>
        <button v-for="p in pages" :key="p" class="page-btn" :class="{ active: p === pageNum }" @click="pageNum = p; loadRecords()">{{ p }}</button>
        <button class="page-btn" :disabled="pageNum >= pages" @click="pageNum++; loadRecords()">»</button>
      </div>
    </div>

    <!-- 扫码检票弹窗 -->
    <div class="modal-mask" v-if="showScanModal" @click.self="showScanModal = false">
      <div class="modal-box" style="width:460px;">
        <div class="modal-header">
          <span class="modal-title">扫码/手工检票</span>
          <button class="modal-close" @click="showScanModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-vertical">
            <div class="form-item">
              <label class="form-label">所属园区</label>
              <select class="form-select" v-model="form.scenicId">
                <option value="">请选择</option>
                <option v-for="s in scenics" :key="s.id" :value="s.id">{{ s.name }}</option>
              </select>
            </div>
            <div class="form-item">
              <label class="form-label">检票方式</label>
              <select class="form-select" v-model="form.verifyMethod">
                <option value="扫码">扫码</option>
                <option value="手工">手工</option>
              </select>
            </div>
            <div class="form-item">
              <label class="form-label">票据码</label>
              <input class="form-input" v-model="form.voucherCode" placeholder="请输入或扫描票据码" style="font-family:monospace;" />
            </div>
            <div class="form-item">
              <label class="form-label">检票员</label>
              <input class="form-input" v-model="form.verifyStaffName" placeholder="姓名" />
            </div>
            <div class="form-item">
              <label class="form-label">终端名称</label>
              <input class="form-input" v-model="form.deviceName" placeholder="如：1号闸机" />
            </div>
            <div v-if="lastResult" class="alert" :class="lastResult.result === '成功' ? 'alert-info' : 'alert-error'">
              <svg width="14" height="14" viewBox="0 0 16 16" fill="currentColor"><path d="M16 8A8 8 0 11.001 8 8 8 0 0116 8z"/></svg>
              <div>
                <div style="font-weight:600;">{{ lastResult.result === '成功' ? '检票成功' : '检票失败：' + (lastResult.failReason || '未知原因') }}</div>
                <div v-if="lastResult.result === '成功'" style="font-size:12px;margin-top:4px;">
                  {{ lastResult.ticketName }} / {{ lastResult.scenicName }} / 票价 ¥{{ lastResult.unitPrice }}
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="showScanModal = false">关闭</button>
          <button class="btn btn-primary" :disabled="submitting || !form.voucherCode" @click="doVerify">{{ submitting ? '检票中...' : '确认检票' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from './ui/Message'
import { listVerifies, verifyVoucher, getTodayStats } from '../api/verify'
import { listScenicOptions } from '../api/scenic'

const showScanModal = ref(false)
const submitting = ref(false)
const loading = ref(false)

const scenics = ref([])
const verifyRecords = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(15)
const pages = ref(1)

const filterKeyword = ref('')
const filterScenicId = ref('')

const form = reactive({
  scenicId: '',
  voucherCode: '',
  verifyMethod: '扫码',
  verifyStaffName: '',
  deviceName: '1号闸机',
})

const lastResult = ref(null)

const verifyStats = ref([
  { label: '今日检票总量', value: '0', sub: '入园检票 + 项目检票', color: 'var(--color-text-primary)' },
  { label: '成功核销', value: '0', sub: '已正常核销', color: 'var(--color-blue)' },
  { label: '入园检票', value: '0', sub: '门票和全包票首检', color: 'var(--color-blue)' },
  { label: '异常检票', value: '0', sub: '重复检票或票据无效', color: 'var(--color-red)' },
])

async function loadScenics() {
  try { scenics.value = await listScenicOptions() } catch (e) { /* handled */ }
}

async function loadRecords() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (filterKeyword.value) params.keyword = filterKeyword.value
    if (filterScenicId.value) params.scenicId = filterScenicId.value
    const data = await listVerifies(params)
    verifyRecords.value = data?.records || []
    total.value = data?.total || 0
    pages.value = data?.pages || 1
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

async function loadStats() {
  try {
    const data = await getTodayStats(filterScenicId.value || undefined)
    if (data) {
      verifyStats.value[0].value = String(data.totalCount || 0)
      verifyStats.value[1].value = String(data.successCount || 0)
      verifyStats.value[2].value = String(data.entryCount || 0)
      verifyStats.value[3].value = String(data.failCount || 0)
    }
  } catch (e) { /* handled */ }
}

async function doVerify() {
  submitting.value = true
  lastResult.value = null
  try {
    const result = await verifyVoucher({ ...form })
    lastResult.value = result
    if (result?.result === '成功') {
      ElMessage({ type: 'success', message: '检票成功' })
      form.voucherCode = ''
      loadRecords()
      loadStats()
    } else {
      ElMessage({ type: 'error', message: '检票失败：' + (result?.failReason || '原因未知') })
    }
  } catch (e) { /* handled */ }
  finally { submitting.value = false }
}

onMounted(async () => {
  await loadScenics()
  await loadRecords()
  await loadStats()
})
</script>
