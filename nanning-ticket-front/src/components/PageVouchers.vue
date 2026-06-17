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
          <input class="form-input" v-model="filterKeyword" placeholder="票据号/订单号..." style="width:220px;" />
        </div>
        <div class="form-item">
          <select class="form-select" v-model="filterScenicId">
            <option value="">全部园区</option>
            <option v-for="s in scenics" :key="s.id" :value="s.id">{{ s.name }}</option>
          </select>
        </div>
        <div class="form-item">
          <select class="form-select" v-model="filterStatus">
            <option value="">全部状态</option>
            <option value="待使用">待使用</option>
            <option value="已使用">已使用</option>
            <option value="已退款">已退款</option>
            <option value="已作废">已作废</option>
          </select>
        </div>
        <button class="btn btn-default" @click="loadVouchers">查询</button>
        <div style="flex:1;"></div>
        <button class="btn btn-default" @click="showRevokeModal = true">批量作废</button>
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
              <th>园区</th>
              <th>游客</th>
              <th>有效期</th>
              <th>出票时间</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="9" class="empty-state">加载中...</td>
            </tr>
            <tr v-else-if="!vouchers.length">
              <td colspan="9" class="empty-state">暂无票据数据</td>
            </tr>
            <tr v-for="voucher in vouchers" v-else :key="voucher.id">
              <td style="font-family:monospace;font-size:12px;color:var(--color-blue);">{{ voucher.voucherCode }}</td>
              <td style="font-family:monospace;font-size:12px;color:var(--color-text-secondary);">{{ voucher.saleNo }}</td>
              <td>
                <div style="font-weight:600;">{{ voucher.ticketName }}</div>
                <div style="font-size:11px;color:var(--color-text-muted);">¥{{ voucher.unitPrice }}</div>
              </td>
              <td>{{ voucher.scenicName }}</td>
              <td style="font-size:12px;">{{ voucher.visitorName || '—' }}</td>
              <td style="font-size:12px;color:var(--color-text-secondary);">
                {{ voucher.validFrom || '—' }} ~ {{ voucher.validTo || '—' }}
              </td>
              <td style="font-size:12px;">{{ voucher.createdAt }}</td>
              <td>
                <span class="tag" :class="statusClass(voucher.status)">{{ voucher.status }}</span>
              </td>
              <td>
                <div style="display:flex;gap:8px;">
                  <span class="action-link" @click="openDetail(voucher)">详情</span>
                  <span class="action-link danger" v-if="voucher.status === '待使用'" @click="revokeOne(voucher)">作废</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="pagination">
        <span class="pagination-info">共 {{ total }} 条</span>
        <button class="page-btn" :disabled="pageNum <= 1" @click="pageNum--; loadVouchers()">«</button>
        <button v-for="p in pages" :key="p" class="page-btn" :class="{ active: p === pageNum }" @click="pageNum = p; loadVouchers()">{{ p }}</button>
        <button class="page-btn" :disabled="pageNum >= pages" @click="pageNum++; loadVouchers()">»</button>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <div class="modal-mask" v-if="showDetail && currentVoucher" @click.self="showDetail = false">
      <div class="modal-box" style="width:560px;">
        <div class="modal-header">
          <span class="modal-title">票据详情</span>
          <button class="modal-close" @click="showDetail = false">×</button>
        </div>
        <div class="modal-body">
          <div class="info-row"><span class="info-label">票据号</span><span class="info-value" style="font-family:monospace;">{{ currentVoucher.voucherCode }}</span></div>
          <div class="info-row"><span class="info-label">关联订单</span><span class="info-value">{{ currentVoucher.saleNo }}</span></div>
          <div class="info-row"><span class="info-label">票种</span><span class="info-value">{{ currentVoucher.ticketName }}</span></div>
          <div class="info-row"><span class="info-label">园区</span><span class="info-value">{{ currentVoucher.scenicName }}</span></div>
          <div class="info-row"><span class="info-label">单价</span><span class="info-value">¥{{ currentVoucher.unitPrice }}</span></div>
          <div class="info-row"><span class="info-label">有效期</span><span class="info-value">{{ currentVoucher.validFrom }} ~ {{ currentVoucher.validTo }}</span></div>
          <div class="info-row"><span class="info-label">状态</span><span class="info-value">{{ currentVoucher.status }}</span></div>
          <div class="info-row"><span class="info-label">出票时间</span><span class="info-value">{{ currentVoucher.createdAt }}</span></div>
          <div class="info-row"><span class="info-label">备注</span><span class="info-value">{{ currentVoucher.remark || '—' }}</span></div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="showDetail = false">关闭</button>
        </div>
      </div>
    </div>

    <!-- 批量作废弹窗 -->
    <div class="modal-mask" v-if="showRevokeModal" @click.self="showRevokeModal = false">
      <div class="modal-box" style="width:480px;">
        <div class="modal-header">
          <span class="modal-title">批量作废票据</span>
          <button class="modal-close" @click="showRevokeModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-vertical">
            <div class="form-item">
              <label class="form-label">票据号（多行 / 逗号分隔）</label>
              <textarea class="form-textarea" v-model="revokeForm.codes" placeholder="V202606140001, V202606140002" />
            </div>
            <div class="form-item">
              <label class="form-label">作废原因</label>
              <input class="form-input" v-model="revokeForm.reason" placeholder="如：二维码污损" />
            </div>
            <div class="form-item">
              <label class="form-label">操作员</label>
              <input class="form-input" v-model="revokeForm.staffName" placeholder="姓名" />
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="showRevokeModal = false">取消</button>
          <button class="btn btn-primary" :disabled="saving" @click="submitRevoke">{{ saving ? '提交中...' : '确认作废' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from './ui/Message'
import { listVouchers, getVoucherStats, revokeVouchers, getVoucherByCode } from '../api/voucher'
import { listScenicOptions } from '../api/scenic'

const loading = ref(false)
const saving = ref(false)
const vouchers = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(15)
const pages = ref(1)
const scenics = ref([])

const filterKeyword = ref('')
const filterScenicId = ref('')
const filterStatus = ref('')

const summaryStats = ref([
  { label: '已出票票据', value: '0', sub: '含系统直销与分销渠道' },
  { label: '待使用', value: '0', sub: '尚未入园或体验' },
  { label: '已使用', value: '0', sub: '已核销' },
  { label: '已退款/作废', value: '0', sub: '无效票据' },
])

const showDetail = ref(false)
const currentVoucher = ref(null)

const showRevokeModal = ref(false)
const revokeForm = reactive({ codes: '', reason: '', staffName: '' })

function statusClass(status) {
  return {
    '待使用': 'tag-blue',
    '已使用': 'tag-gray',
    '已退款': 'tag-red',
    '已作废': 'tag-red',
  }[status] || 'tag-gray'
}

async function loadScenics() {
  try { scenics.value = await listScenicOptions() } catch (e) { /* handled */ }
}

async function loadVouchers() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (filterKeyword.value) params.keyword = filterKeyword.value
    if (filterScenicId.value) params.scenicId = filterScenicId.value
    if (filterStatus.value) params.status = filterStatus.value
    const data = await listVouchers(params)
    vouchers.value = data?.records || []
    total.value = data?.total || 0
    pages.value = data?.pages || 1
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

async function loadStats() {
  try {
    const data = await getVoucherStats(filterScenicId.value || undefined)
    if (data) {
      summaryStats.value[0].value = String(data.totalCount || 0)
      summaryStats.value[1].value = String(data.unusedCount || 0)
      summaryStats.value[2].value = String(data.usedCount || 0)
      summaryStats.value[3].value = String((data.refundCount || 0) + (data.revokedCount || 0))
      summaryStats.value[3].sub = `核销率 ${data.usageRate || 0}%`
    }
  } catch (e) { /* handled */ }
}

async function openDetail(voucher) {
  // 优先拉详情接口；如未提供，回退到列表数据
  try {
    const detail = await getVoucherByCode(voucher.voucherCode)
    currentVoucher.value = detail || voucher
  } catch (e) {
    currentVoucher.value = voucher
  }
  showDetail.value = true
}

async function revokeOne(voucher) {
  if (!confirm(`确定作废票据 ${voucher.voucherCode} ?`)) return
  try {
    await revokeVouchers({ ids: [voucher.id], reason: '管理端手动作废', staffName: '管理员' })
    ElMessage({ type: 'success', message: '已作废' })
    loadVouchers()
    loadStats()
  } catch (e) { /* handled */ }
}

async function submitRevoke() {
  if (!revokeForm.codes) { ElMessage({ type: 'warning', message: '请填写票据号' }); return }
  const ids = revokeForm.codes.split(/[,\n\s]+/).filter(Boolean).map(Number).filter(Boolean)
  if (!ids.length) { ElMessage({ type: 'warning', message: '票据号格式不正确（应为 ID 数字）' }); return }
  saving.value = true
  try {
    await revokeVouchers({ ids, reason: revokeForm.reason, staffName: revokeForm.staffName || '管理员' })
    ElMessage({ type: 'success', message: `已作废 ${ids.length} 张` })
    showRevokeModal.value = false
    revokeForm.codes = ''
    loadVouchers()
    loadStats()
  } catch (e) { /* handled */ }
  finally { saving.value = false }
}

onMounted(async () => {
  await loadScenics()
  await loadVouchers()
  await loadStats()
})
</script>
