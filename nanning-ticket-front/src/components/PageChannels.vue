<template>
  <div>
    <div class="card" style="margin-bottom:12px;">
      <div class="toolbar">
        <div class="form-item">
          <input class="form-input" v-model="filterKeyword" placeholder="渠道名称..." style="width:180px;" />
        </div>
        <div class="form-item">
          <select class="form-select" v-model="filterStatus">
            <option value="">全部状态</option>
            <option value="启用">启用</option>
            <option value="禁用">禁用</option>
          </select>
        </div>
        <button class="btn btn-default" @click="loadChannels">查询</button>
        <div style="flex:1;"></div>
        <button class="btn btn-primary" @click="openCreate">
          <svg width="13" height="13" viewBox="0 0 16 16" fill="currentColor"><path d="M8 4a.5.5 0 01.5.5v3h3a.5.5 0 010 1h-3v3a.5.5 0 01-1 0v-3h-3a.5.5 0 010-1h3v-3A.5.5 0 018 4z"/></svg>
          新增渠道
        </button>
      </div>
    </div>

    <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:12px;margin-bottom:12px;">
      <div v-if="loading" class="card">
        <div class="card-body" style="text-align:center;color:var(--color-text-muted);">加载中...</div>
      </div>
      <div v-else-if="!channels.length" class="card" style="grid-column:1 / -1;">
        <div class="card-body empty-state" style="text-align:center;color:var(--color-text-muted);">暂无渠道数据</div>
      </div>
      <div class="card" v-for="ch in channels" v-else :key="ch.id" style="cursor:pointer;" @click="viewChannel(ch)">
        <div class="card-body">
          <div style="display:flex;align-items:flex-start;justify-content:space-between;margin-bottom:12px;">
            <div style="display:flex;align-items:center;gap:10px;">
              <div :style="{ width:'40px', height:'40px', background:ch.iconBg || 'var(--color-blue-light)', borderRadius:'var(--radius)', display:'flex', alignItems:'center', justifyContent:'center', fontSize:'18px' }">
                {{ ch.icon || ch.channelName?.slice(0,1) || '渠' }}
              </div>
              <div>
                <div style="font-weight:600;font-size:14px;">{{ ch.channelName }}</div>
                <div style="font-size:11px;color:var(--color-text-muted);">{{ ch.channelType }}</div>
              </div>
            </div>
            <span class="tag" :class="ch.status === '启用' ? 'tag-green' : 'tag-gray'">
              {{ ch.status }}
            </span>
          </div>
          <div class="divider"></div>
          <div style="display:grid;grid-template-columns:1fr 1fr;gap:8px;margin-top:10px;">
            <div>
              <div style="font-size:11px;color:var(--color-text-muted);">本月销售额</div>
              <div style="font-weight:600;font-size:15px;margin-top:2px;">¥{{ Number(ch.monthSales || 0).toLocaleString() }}</div>
            </div>
            <div>
              <div style="font-size:11px;color:var(--color-text-muted);">本月出票</div>
              <div style="font-weight:600;font-size:15px;margin-top:2px;">{{ ch.monthTickets || 0 }} 张</div>
            </div>
            <div>
              <div style="font-size:11px;color:var(--color-text-muted);">佣金比例</div>
              <div style="font-size:13px;margin-top:2px;color:var(--color-orange);">{{ ch.commissionRate }}%</div>
            </div>
            <div>
              <div style="font-size:11px;color:var(--color-text-muted);">订单数</div>
              <div style="font-size:13px;margin-top:2px;">{{ ch.orderCount || 0 }}</div>
            </div>
          </div>
          <div class="divider"></div>
          <div style="display:flex;gap:8px;margin-top:8px;">
            <span class="action-link" @click.stop="openEdit(ch)">编辑</span>
            <span class="action-link" @click.stop="adjustCommissionModal(ch)">调整佣金</span>
            <span class="action-link danger" @click.stop="toggleStatus(ch)" v-if="ch.status === '启用'">暂停</span>
            <span class="action-link" @click.stop="toggleStatus(ch)" v-else>启用</span>
          </div>
        </div>
      </div>
    </div>

    <div class="card">
      <div class="card-header">
        <span class="card-title">渠道对账汇总（本月）</span>
        <button class="btn btn-primary" @click="showSettleModal = true">生成结算单</button>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>渠道名称</th>
              <th>出票数量</th>
              <th>销售总额</th>
              <th>佣金比例</th>
              <th>应付佣金</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="settleLoading">
              <td colspan="7" class="empty-state">加载中...</td>
            </tr>
            <tr v-else-if="!reconciliation.length">
              <td colspan="7" class="empty-state">暂无对账数据</td>
            </tr>
            <tr v-for="r in reconciliation" v-else :key="r.channelId">
              <td style="font-weight:500;">{{ r.channelName }}</td>
              <td>{{ r.orderCount || 0 }}</td>
              <td>¥{{ Number(r.totalGmv || 0).toFixed(2) }}</td>
              <td>{{ r.commissionRate }}%</td>
              <td style="font-weight:600;">¥{{ Number(r.totalGmv || 0) * Number(r.commissionRate || 0) / 100 }}</td>
              <td>
                <span class="tag tag-gray">已统计</span>
              </td>
              <td>
                <span class="action-link" @click="openSettle(r)">发起结算</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 新增/编辑渠道 -->
    <div class="modal-mask" v-if="showModal" @click.self="showModal = false">
      <div class="modal-box" style="width:560px;">
        <div class="modal-header">
          <span class="modal-title">{{ editingChannel ? '编辑渠道' : '新增渠道' }}</span>
          <button class="modal-close" @click="showModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-vertical">
            <div class="form-item">
              <label class="form-label">渠道名称 <span style="color:var(--color-red)">*</span></label>
              <input class="form-input" v-model="form.channelName" placeholder="如：美团旅游" />
            </div>
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">渠道编码</label>
                <input class="form-input" v-model="form.channelCode" placeholder="如：MEITUAN" />
              </div>
              <div class="form-item">
                <label class="form-label">渠道类型</label>
                <select class="form-select" v-model="form.channelType">
                  <option value="OTA">OTA平台</option>
                  <option value="DIRECT">自有渠道</option>
                  <option value="AGENT">代理商</option>
                  <option value="GROUP">企业团购</option>
                </select>
              </div>
            </div>
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">佣金比例(%)</label>
                <input class="form-input" type="number" v-model.number="form.commissionRate" />
              </div>
              <div class="form-item">
                <label class="form-label">状态</label>
                <select class="form-select" v-model="form.status">
                  <option value="启用">启用</option>
                  <option value="禁用">禁用</option>
                </select>
              </div>
            </div>
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">联系人</label>
                <input class="form-input" v-model="form.contactName" />
              </div>
              <div class="form-item">
                <label class="form-label">联系电话</label>
                <input class="form-input" v-model="form.contactPhone" />
              </div>
            </div>
            <div class="form-item">
              <label class="form-label">API 接入地址</label>
              <input class="form-input" v-model="form.apiEndpoint" />
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="showModal = false">取消</button>
          <button class="btn btn-primary" :disabled="saving" @click="submitForm">{{ saving ? '保存中...' : '保存' }}</button>
        </div>
      </div>
    </div>

    <!-- 调整佣金 -->
    <div class="modal-mask" v-if="showCommissionModal" @click.self="showCommissionModal = false">
      <div class="modal-box" style="width:420px;">
        <div class="modal-header">
          <span class="modal-title">调整佣金比例</span>
          <button class="modal-close" @click="showCommissionModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-vertical">
            <div class="form-item">
              <label class="form-label">渠道</label>
              <input class="form-input" :value="commissionForm.channelName" disabled />
            </div>
            <div class="form-item">
              <label class="form-label">新佣金比例(%)</label>
              <input class="form-input" type="number" v-model.number="commissionForm.rate" />
            </div>
            <div class="form-item">
              <label class="form-label">调整原因</label>
              <input class="form-input" v-model="commissionForm.reason" />
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="showCommissionModal = false">取消</button>
          <button class="btn btn-primary" :disabled="saving" @click="submitCommission">保存</button>
        </div>
      </div>
    </div>

    <!-- 生成结算单 -->
    <div class="modal-mask" v-if="showSettleModal" @click.self="showSettleModal = false">
      <div class="modal-box" style="width:480px;">
        <div class="modal-header">
          <span class="modal-title">生成结算单</span>
          <button class="modal-close" @click="showSettleModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-vertical">
            <div class="form-item">
              <label class="form-label">渠道</label>
              <select class="form-select" v-model="settleForm.channelId">
                <option v-for="ch in channels" :key="ch.id" :value="ch.id">{{ ch.channelName }}</option>
              </select>
            </div>
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">开始日期</label>
                <input class="form-input" type="date" v-model="settleForm.periodStart" />
              </div>
              <div class="form-item">
                <label class="form-label">结束日期</label>
                <input class="form-input" type="date" v-model="settleForm.periodEnd" />
              </div>
            </div>
            <div class="form-item">
              <label class="form-label">备注</label>
              <input class="form-input" v-model="settleForm.remark" />
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="showSettleModal = false">取消</button>
          <button class="btn btn-primary" :disabled="saving" @click="submitSettle">生成</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from './ui/Message'
import {
  listChannels, createChannel, updateChannel, toggleChannelStatus, adjustCommission,
  getChannelStats, createSettlement,
} from '../api/channel'

const channels = ref([])
const reconciliation = ref([])
const loading = ref(false)
const settleLoading = ref(false)
const saving = ref(false)

const filterKeyword = ref('')
const filterStatus = ref('')

const showModal = ref(false)
const showCommissionModal = ref(false)
const showSettleModal = ref(false)
const editingChannel = ref(null)

const form = reactive({
  channelCode: '',
  channelName: '',
  channelType: 'OTA',
  icon: '',
  iconBg: '#eff6ff',
  commissionRate: 8,
  contactName: '',
  contactPhone: '',
  apiEndpoint: '',
  status: '启用',
})

const commissionForm = reactive({ id: null, channelName: '', rate: 0, reason: '' })
const settleForm = reactive({
  channelId: '',
  periodStart: '',
  periodEnd: '',
  remark: '',
})

function resetForm() {
  Object.assign(form, {
    channelCode: '', channelName: '', channelType: 'OTA', icon: '', iconBg: '#eff6ff',
    commissionRate: 8, contactName: '', contactPhone: '', apiEndpoint: '', status: '启用',
  })
}

function thisMonthRange() {
  const d = new Date()
  const start = new Date(d.getFullYear(), d.getMonth(), 1)
  const end = new Date(d.getFullYear(), d.getMonth() + 1, 0)
  const fmt = (dt) => dt.toISOString().slice(0, 10)
  return { periodStart: fmt(start), periodEnd: fmt(end) }
}

async function loadChannels() {
  loading.value = true
  try {
    const params = { pageNum: 1, pageSize: 20 }
    if (filterKeyword.value) params.keyword = filterKeyword.value
    if (filterStatus.value) params.status = filterStatus.value
    const data = await listChannels(params)
    channels.value = (data?.records || []).map(c => ({
      ...c,
      monthSales: c.monthSales ?? c.totalGmv ?? 0,
      monthTickets: c.monthTickets ?? c.orderCount ?? 0,
    }))
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

async function loadStats() {
  settleLoading.value = true
  try {
    const data = await getChannelStats()
    if (data?.channelList) {
      reconciliation.value = data.channelList
    }
  } catch (e) { /* handled */ }
  finally { settleLoading.value = false }
}

function openCreate() {
  editingChannel.value = null
  resetForm()
  showModal.value = true
}

function openEdit(ch) {
  editingChannel.value = ch
  Object.assign(form, {
    channelCode: ch.channelCode,
    channelName: ch.channelName,
    channelType: ch.channelType,
    icon: ch.icon,
    iconBg: ch.iconBg,
    commissionRate: ch.commissionRate,
    contactName: ch.contactName,
    contactPhone: ch.contactPhone,
    apiEndpoint: ch.apiEndpoint,
    status: ch.status,
  })
  showModal.value = true
}

async function submitForm() {
  if (!form.channelName) { ElMessage({ type: 'warning', message: '请输入渠道名称' }); return }
  saving.value = true
  try {
    if (editingChannel.value) {
      await updateChannel(editingChannel.value.id, form)
      ElMessage({ type: 'success', message: '编辑成功' })
    } else {
      await createChannel(form)
      ElMessage({ type: 'success', message: '新增成功' })
    }
    showModal.value = false
    loadChannels()
    loadStats()
  } catch (e) { /* handled */ }
  finally { saving.value = false }
}

async function toggleStatus(ch) {
  const next = ch.status === '启用' ? '禁用' : '启用'
  try {
    await toggleChannelStatus(ch.id, next)
    ElMessage({ type: 'success', message: '状态已更新' })
    loadChannels()
    loadStats()
  } catch (e) { /* handled */ }
}

function adjustCommissionModal(ch) {
  commissionForm.id = ch.id
  commissionForm.channelName = ch.channelName
  commissionForm.rate = ch.commissionRate
  commissionForm.reason = ''
  showCommissionModal.value = true
}

async function submitCommission() {
  saving.value = true
  try {
    await adjustCommission(commissionForm.id, { commissionRate: commissionForm.rate, reason: commissionForm.reason })
    ElMessage({ type: 'success', message: '佣金已更新' })
    showCommissionModal.value = false
    loadChannels()
  } catch (e) { /* handled */ }
  finally { saving.value = false }
}

function openSettle(r) {
  Object.assign(settleForm, thisMonthRange(), {
    channelId: r.channelId,
    remark: `${r.channelName} ${settleForm.periodStart} 对账`,
  })
  showSettleModal.value = true
}

async function submitSettle() {
  if (!settleForm.channelId) { ElMessage({ type: 'warning', message: '请选择渠道' }); return }
  saving.value = true
  try {
    await createSettlement(settleForm)
    ElMessage({ type: 'success', message: '结算单已生成' })
    showSettleModal.value = false
  } catch (e) { /* handled */ }
  finally { saving.value = false }
}

function viewChannel(ch) {
  // 预留：跳转渠道详情
}

onMounted(async () => {
  await loadChannels()
  await loadStats()
})
</script>
