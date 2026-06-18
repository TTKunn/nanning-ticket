<template>
  <div>
    <div class="alert alert-info">
      <svg width="14" height="14" viewBox="0 0 16 16" fill="currentColor"><path d="M8 16A8 8 0 108 0a8 8 0 000 16zm.93-9.412l-1 4.705c-.07.34.029.533.304.533.194 0 .487-.07.686-.246l-.088.416c-.287.346-.92.598-1.465.598-.703 0-1.002-.422-.808-1.319l.738-3.468c.064-.293.006-.399-.287-.47l-.451-.081.082-.381 2.29-.287zM8 5.5a1 1 0 110-2 1 1 0 010 2z"/></svg>
      当前票种原型已按"票种分组 + 库存方式 + 购票限制 + 退票规则"重构，不包含转增、激活、播音设备等非核心业务。
    </div>

    <div class="card" style="margin-bottom:12px;">
      <div class="toolbar">
        <div class="form-item">
          <input class="form-input" placeholder="搜索票种名称..." v-model="filterKeyword" style="width:200px;" />
        </div>
        <div class="form-item">
          <select class="form-select" v-model="filterScenic">
            <option value="">全部园区</option>
            <option v-for="s in scenics" :key="s.id" :value="s.id">{{ s.name }}</option>
          </select>
        </div>
        <div class="form-item">
          <select class="form-select" v-model="filterGroup">
            <option value="">全部票种分组</option>
            <option value="单票">单票</option>
            <option value="套票">套票</option>
            <option value="联票">联票</option>
          </select>
        </div>
        <div class="form-item">
          <select class="form-select" v-model="filterStatus">
            <option value="">全部状态</option>
            <option value="在售">在售</option>
            <option value="停售">停售</option>
          </select>
        </div>
        <button class="btn btn-default" @click="loadTickets">查询</button>
        <div style="flex:1;"></div>
        <button class="btn btn-primary" @click="openCreate">新增票种</button>
      </div>
    </div>

    <div class="card">
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>票种名称</th>
              <th>所属园区</th>
              <th>票种分组</th>
              <th>销售价</th>
              <th>成本价</th>
              <th>有效期（天）</th>
              <th>标签</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="9" class="empty-state">加载中...</td>
            </tr>
            <tr v-else-if="!filteredTickets.length">
              <td colspan="9" class="empty-state">暂无票种数据</td>
            </tr>
            <tr v-for="ticket in filteredTickets" v-else :key="ticket.id">
              <td>
                <div style="font-weight:600;">{{ ticket.name }}</div>
                <div style="font-size:11px;color:var(--color-text-muted);">编码: {{ ticket.code }}</div>
              </td>
              <td>{{ ticket.scenicName }}</td>
              <td><span class="tag" :class="groupClass(ticket.category)">{{ ticket.category }}</span></td>
              <td style="font-weight:600;color:var(--color-red);">¥{{ ticket.price }}</td>
              <td style="color:var(--color-text-secondary);">¥{{ ticket.costPrice }}</td>
              <td>{{ ticket.validDays }} 天</td>
              <td style="font-size:12px;color:var(--color-text-secondary);">
                <span v-for="t in ticket.tags" :key="t" class="tag tag-gray" style="margin-right:4px;">{{ t }}</span>
              </td>
              <td><span class="tag" :class="ticket.status === '在售' ? 'tag-green' : 'tag-gray'">{{ ticket.status }}</span></td>
              <td>
                <div style="display:flex;gap:8px;align-items:center;">
                  <span class="action-link" @click="openEdit(ticket)">编辑</span>
                  <span class="action-link danger" @click="toggleStatus(ticket)">
                    {{ ticket.status === '在售' ? '下架' : '上架' }}
                  </span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="pagination">
        <span class="pagination-info">共 {{ total }} 条</span>
        <button class="page-btn" :disabled="pageNum <= 1" @click="pageNum--; loadTickets()">«</button>
        <button v-for="p in pages" :key="p" class="page-btn" :class="{ active: p === pageNum }" @click="pageNum = p; loadTickets()">{{ p }}</button>
        <button class="page-btn" :disabled="pageNum >= pages" @click="pageNum++; loadTickets()">»</button>
      </div>
    </div>

    <div class="modal-mask" v-if="showModal" @click.self="showModal = false">
      <div class="modal-box" style="width:760px;">
        <div class="modal-header">
          <span class="modal-title">{{ editingTicket ? '编辑票种' : '新增票种' }}</span>
          <button class="modal-close" @click="showModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-vertical">
            <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">所属园区</label>
                <select class="form-select" v-model="form.scenicId">
                  <option value="">请选择</option>
                  <option v-for="s in scenics" :key="s.id" :value="s.id">{{ s.name }}</option>
                </select>
              </div>
              <div class="form-item">
                <label class="form-label">票种名称</label>
                <input class="form-input" v-model="form.name" placeholder="请输入票种名称" />
              </div>
              <div class="form-item">
                <label class="form-label">票种编码</label>
                <input class="form-input" v-model="form.code" placeholder="唯一编码" />
              </div>
            </div>

            <div style="display:grid;grid-template-columns:1fr 1fr 1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">票种分组</label>
                <select class="form-select" v-model="form.category">
                  <option value="单票">单票</option>
                  <option value="套票">套票</option>
                  <option value="联票">联票</option>
                </select>
              </div>
              <div class="form-item">
                <label class="form-label">销售价</label>
                <input class="form-input" type="number" v-model.number="form.price" placeholder="0.00" />
              </div>
              <div class="form-item">
                <label class="form-label">成本价</label>
                <input class="form-input" type="number" v-model.number="form.costPrice" placeholder="0.00" />
              </div>
              <div class="form-item">
                <label class="form-label">有效期（天）</label>
                <input class="form-input" type="number" v-model.number="form.validDays" placeholder="如：1" />
              </div>
            </div>

            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">是否可退</label>
                <select class="form-select" v-model="form.refundable">
                  <option :value="true">允许退票</option>
                  <option :value="false">不可退</option>
                </select>
              </div>
              <div class="form-item">
                <label class="form-label">状态</label>
                <select class="form-select" v-model="form.status">
                  <option value="在售">在售</option>
                  <option value="停售">停售</option>
                </select>
              </div>
            </div>

            <div class="form-item">
              <label class="form-label">票种标签</label>
              <div style="display:flex;gap:8px;align-items:center;">
                <input
                  class="form-input"
                  v-model="tagInput"
                  placeholder="输入标签后回车添加，如：热销"
                  style="flex:1;"
                  @keydown.enter.prevent="addTag"
                />
                <button type="button" class="btn btn-default" @click="addTag">添加</button>
              </div>
              <div v-if="form.tags.length" style="display:flex;flex-wrap:wrap;gap:6px;margin-top:8px;">
                <span v-for="(t, idx) in form.tags" :key="t + idx" class="tag tag-blue" style="display:inline-flex;align-items:center;gap:4px;padding:4px 8px;">
                  {{ t }}
                  <button type="button" @click="removeTag(idx)" style="background:transparent;border:0;color:inherit;cursor:pointer;padding:0;line-height:1;font-size:12px;" title="删除">×</button>
                </span>
              </div>
              <div v-else style="font-size:12px;color:var(--color-text-muted);margin-top:6px;">尚未添加标签</div>
              <div style="margin-top:8px;display:flex;flex-wrap:wrap;gap:6px;align-items:center;">
                <span style="font-size:12px;color:var(--color-text-muted);">建议标签：</span>
                <span
                  v-for="s in suggestedTags"
                  :key="s"
                  class="tag tag-gray"
                  style="cursor:pointer;"
                  @click="addSuggestedTag(s)"
                >+ {{ s }}</span>
              </div>
            </div>

            <div class="form-item">
              <label class="form-label">票种说明</label>
              <textarea class="form-textarea" v-model="form.description" placeholder="说明票种内容、适用人群、特殊规则..."></textarea>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="showModal = false">取消</button>
          <button class="btn btn-primary" :disabled="saving" @click="submitForm">{{ saving ? '保存中...' : '保存' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage } from './ui/Message'
import { listTickets, createTicket, updateTicket, toggleTicketStatus } from '../api/ticket'
import { listScenicOptions } from '../api/scenic'

const tickets = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const pages = ref(1)
const loading = ref(false)
const saving = ref(false)

const scenics = ref([])

const filterKeyword = ref('')
const filterScenic = ref('')
const filterGroup = ref('')
const filterStatus = ref('')

const showModal = ref(false)
const editingTicket = ref(null)
const tagInput = ref('')
const suggestedTags = ['热销', '推荐', '限时', '新品', '优惠', '特惠', '节假日', '周末']
const form = reactive({
  scenicId: '',
  name: '',
  code: '',
  category: '单票',
  price: 0,
  costPrice: 0,
  validDays: 1,
  refundable: true,
  description: '',
  status: '在售',
  tags: [],
})

const filteredTickets = computed(() => tickets.value)

function groupClass(group) {
  return {
    单票: 'tag-blue',
    游玩票: 'tag-orange',
    全包票: 'tag-green',
    套票: 'tag-gray',
    联票: 'tag-orange',
  }[group] || 'tag-gray'
}

async function loadScenics() {
  try { scenics.value = await listScenicOptions() } catch (e) { /* handled */ }
}

async function loadTickets() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (filterKeyword.value) params.keyword = filterKeyword.value
    if (filterScenic.value) params.scenicId = filterScenic.value
    if (filterGroup.value) params.category = filterGroup.value
    if (filterStatus.value) params.status = filterStatus.value
    const data = await listTickets(params)
    tickets.value = data?.records || []
    total.value = data?.total || 0
    pages.value = data?.pages || 1
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

function resetForm() {
  Object.assign(form, {
    scenicId: '', name: '', code: '', category: '单票',
    price: 0, costPrice: 0, validDays: 1, refundable: true,
    description: '', status: '在售', tags: [],
  })
  tagInput.value = ''
}

function addTag() {
  const v = (tagInput.value || '').trim()
  if (!v) return
  if (form.tags.includes(v)) {
    ElMessage({ type: 'warning', message: '该标签已存在' })
    return
  }
  if (form.tags.length >= 10) {
    ElMessage({ type: 'warning', message: '最多添加 10 个标签' })
    return
  }
  form.tags.push(v)
  tagInput.value = ''
}

function addSuggestedTag(s) {
  if (form.tags.includes(s)) return
  if (form.tags.length >= 10) {
    ElMessage({ type: 'warning', message: '最多添加 10 个标签' })
    return
  }
  form.tags.push(s)
}

function removeTag(idx) {
  form.tags.splice(idx, 1)
}

function openCreate() {
  editingTicket.value = null
  resetForm()
  showModal.value = true
}

function openEdit(ticket) {
  editingTicket.value = ticket
  Object.assign(form, {
    scenicId: ticket.scenicId,
    name: ticket.name,
    code: ticket.code,
    category: ticket.category,
    price: ticket.price,
    costPrice: ticket.costPrice,
    validDays: ticket.validDays,
    refundable: ticket.refundable,
    description: ticket.description,
    status: ticket.status,
    tags: ticket.tags || [],
  })
  showModal.value = true
}

async function submitForm() {
  if (!form.scenicId) { ElMessage({ type: 'warning', message: '请选择所属园区' }); return }
  if (!form.name) { ElMessage({ type: 'warning', message: '请输入票种名称' }); return }
  saving.value = true
  try {
    if (editingTicket.value) {
      await updateTicket(editingTicket.value.id, form)
      ElMessage({ type: 'success', message: '保存成功' })
    } else {
      await createTicket(form)
      ElMessage({ type: 'success', message: '新增成功' })
    }
    showModal.value = false
    loadTickets()
  } catch (e) { /* handled */ }
  finally { saving.value = false }
}

async function toggleStatus(ticket) {
  const next = ticket.status === '在售' ? '停售' : '在售'
  try {
    await toggleTicketStatus(ticket.id, next)
    ElMessage({ type: 'success', message: '状态已更新' })
    loadTickets()
  } catch (e) { /* handled */ }
}

onMounted(async () => {
  await loadScenics()
  await loadTickets()
})
</script>
