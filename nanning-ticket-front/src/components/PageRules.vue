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

    <div class="alert alert-info">
      <svg width="14" height="14" viewBox="0 0 16 16" fill="currentColor"><path d="M8 16A8 8 0 108 0a8 8 0 000 16zm.93-9.412l-1 4.705c-.07.34.029.533.304.533.194 0 .487-.07.686-.246l-.088.416c-.287.346-.92.598-1.465.598-.703 0-1.002-.422-.808-1.319l.738-3.468c.064-.293.006-.399-.287-.47l-.451-.081.082-.381 2.29-.287zM8 5.5a1 1 0 110-2 1 1 0 010 2z"/></svg>
      当前原型按"园区 -> 项目规则 -> 票种"组织，支持把门票、游玩票、全包票分开配置。
    </div>

    <div class="card" style="margin-bottom:12px;">
      <div class="toolbar">
        <div class="form-item">
          <input class="form-input" v-model="filterKeyword" placeholder="规则名称/收费项目..." style="width:220px;" />
        </div>
        <div class="form-item">
          <select class="form-select" v-model="filterScenicId">
            <option value="">全部园区</option>
            <option v-for="s in scenics" :key="s.id" :value="s.id">{{ s.name }}</option>
          </select>
        </div>
        <div class="form-item">
          <select class="form-select" v-model="filterType">
            <option value="">全部类型</option>
            <option value="折扣">折扣</option>
            <option value="减免">减免</option>
            <option value="套餐">套餐</option>
            <option value="限制">限制</option>
          </select>
        </div>
        <button class="btn btn-default" @click="loadRules">查询</button>
        <div style="flex:1;"></div>
        <button class="btn btn-primary" @click="openCreate">新增项目规则</button>
      </div>
    </div>

    <div class="card" style="margin-bottom:12px;">
      <div class="card-header">
        <span class="card-title">园区项目规则</span>
        <span style="font-size:12px;color:var(--color-text-muted);">用于定义园区入园、游玩项目和全包套餐的收费边界</span>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>园区</th>
              <th>规则编码</th>
              <th>规则名称</th>
              <th>类型</th>
              <th>优先级</th>
              <th>有效期</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="8" class="empty-state">加载中...</td>
            </tr>
            <tr v-else-if="!ruleList.length">
              <td colspan="8" class="empty-state">暂无规则数据</td>
            </tr>
            <tr v-for="rule in ruleList" v-else :key="rule.id">
              <td>{{ rule.scenicName }}</td>
              <td style="font-family:monospace;color:var(--color-text-secondary);">{{ rule.code || '—' }}</td>
              <td>
                <div style="font-weight:600;">{{ rule.name }}</div>
                <div style="font-size:11px;color:var(--color-text-muted);">{{ rule.description }}</div>
              </td>
              <td><span class="tag" :class="typeClass(rule.type)">{{ rule.type }}</span></td>
              <td>{{ rule.priority }}</td>
              <td style="font-size:12px;color:var(--color-text-secondary);">
                {{ rule.effectiveFrom || '—' }} ~ {{ rule.effectiveTo || '永久' }}
              </td>
              <td><span class="tag" :class="rule.status === '启用' ? 'tag-green' : 'tag-gray'">{{ rule.status }}</span></td>
              <td>
                <div style="display:flex;gap:8px;">
                  <span class="action-link" @click="openEdit(rule)">编辑</span>
                  <span class="action-link danger" @click="toggleStatus(rule)">
                    {{ rule.status === '启用' ? '停用' : '启用' }}
                  </span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="pagination">
        <span class="pagination-info">共 {{ total }} 条</span>
        <button class="page-btn" :disabled="pageNum <= 1" @click="pageNum--; loadRules()">«</button>
        <button v-for="p in pages" :key="p" class="page-btn" :class="{ active: p === pageNum }" @click="pageNum = p; loadRules()">{{ p }}</button>
        <button class="page-btn" :disabled="pageNum >= pages" @click="pageNum++; loadRules()">»</button>
      </div>
    </div>

    <div class="grid-2">
      <div class="card">
        <div class="card-header"><span class="card-title">设计建议</span></div>
        <div class="card-body" style="font-size:13px;color:var(--color-text-secondary);display:grid;gap:10px;">
          <div><strong style="color:var(--color-text-primary);">门票</strong>：用于园区入园收费，通常一票一次入园，可与游玩票拆开核销。</div>
          <div><strong style="color:var(--color-text-primary);">游玩票</strong>：用于园区内单独收费项目，如游船、索道、演艺、设备体验。</div>
          <div><strong style="color:var(--color-text-primary);">全包票</strong>：将入园和多个项目打包，需定义包含项目及核销次数。</div>
          <div><strong style="color:var(--color-text-primary);">套票</strong>：用于节庆或联名场景，建议复用票种分组但单独管理日历库存。</div>
        </div>
      </div>

      <div class="card">
        <div class="card-header"><span class="card-title">规则字段建议</span></div>
        <div class="card-body">
          <table>
            <thead>
              <tr><th>字段</th><th>建议说明</th></tr>
            </thead>
            <tbody>
              <tr v-for="field in ruleFields" :key="field.name">
                <td style="font-weight:600;">{{ field.name }}</td>
                <td style="color:var(--color-text-secondary);">{{ field.desc }}</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <div class="modal-mask" v-if="showModal" @click.self="showModal = false">
      <div class="modal-box" style="width:680px;">
        <div class="modal-header">
          <span class="modal-title">{{ editingRule ? '编辑项目规则' : '新增项目规则' }}</span>
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
                <label class="form-label">规则编码 <span style="color:var(--color-danger);">*</span></label>
                <input class="form-input" v-model="form.code" placeholder="如：ENTRY_BASE_RULE" style="font-family:monospace;" />
              </div>
              <div class="form-item">
                <label class="form-label">规则名称 <span style="color:var(--color-danger);">*</span></label>
                <input class="form-input" v-model="form.name" placeholder="如：园区入园规则" />
              </div>
            </div>

            <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">规则类型</label>
                <select class="form-select" v-model="form.type">
                  <option value="折扣">折扣</option>
                  <option value="减免">减免</option>
                  <option value="套餐">套餐</option>
                  <option value="限制">限制</option>
                </select>
              </div>
              <div class="form-item">
                <label class="form-label">优先级</label>
                <input class="form-input" type="number" v-model="form.priority" placeholder="数字越大越优先" />
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
                <label class="form-label">生效起</label>
                <input class="form-input" type="date" v-model="form.effectiveFrom" />
              </div>
              <div class="form-item">
                <label class="form-label" style="display:flex;align-items:center;justify-content:space-between;">
                  <span>生效止</span>
                  <label style="display:flex;align-items:center;gap:4px;font-size:12px;color:var(--color-text-secondary);font-weight:normal;cursor:pointer;">
                    <input type="checkbox" v-model="form.permanent" />
                    永久
                  </label>
                </label>
                <input class="form-input" type="date" v-model="form.effectiveTo" :disabled="form.permanent" :placeholder="form.permanent ? '永久生效' : ''" />
              </div>
            </div>

            <div class="form-item">
              <label class="form-label">规则说明</label>
              <textarea class="form-textarea" v-model="form.description" placeholder="说明该规则对应的收费边界、可售票种和检退票要求..."></textarea>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="showModal = false">取消</button>
          <button class="btn btn-primary" :disabled="saving" @click="submitForm">{{ saving ? '保存中...' : '保存规则' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from './ui/Message'
import { listRules, createRule, updateRule, toggleRuleStatus } from '../api/rule'
import { listScenicOptions } from '../api/scenic'

const ruleList = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const pages = ref(1)
const loading = ref(false)
const saving = ref(false)

const scenics = ref([])

const filterKeyword = ref('')
const filterScenicId = ref('')
const filterType = ref('')

const summaryStats = ref([
  { label: '项目规则总数', value: '0', sub: '覆盖 0 个园区，启用中 0 个' },
  { label: '折扣规则', value: '0', sub: '按优先级匹配' },
  { label: '减免规则', value: '0', sub: '面向特殊人群' },
  { label: '套餐/限制规则', value: '0', sub: '组合销售场景' },
])

const showModal = ref(false)
const editingRule = ref(null)
const form = reactive({
  scenicId: '',
  code: '',
  name: '',
  type: '折扣',
  priority: 100,
  status: '启用',
  effectiveFrom: '',
  effectiveTo: '',
  permanent: false,
  description: '',
})

const ruleFields = [
  { name: '规则编码', desc: '同一园区下唯一，建议使用大写英文加下划线（如 ENTRY_BASE_RULE）。' },
  { name: '规则类型', desc: '区分折扣、减免、套餐、限制，决定规则作用于价格还是流程。' },
  { name: '优先级', desc: '数字越大越优先；多规则叠加时按优先级排序生效。' },
  { name: '适用范围', desc: '通过园区 + 票种 + 渠道三个维度控制规则的命中范围。' },
  { name: '生效区间', desc: '留空代表永久生效；指定起止日期后按区间启用。' },
  { name: '状态', desc: '启用表示参与价格计算，禁用表示停用但保留配置。' },
]

function typeClass(type) {
  return {
    折扣: 'tag-blue',
    减免: 'tag-orange',
    套餐: 'tag-green',
    限制: 'tag-gray',
  }[type] || 'tag-gray'
}

async function loadScenics() {
  try {
    scenics.value = await listScenicOptions()
  } catch (e) { /* handled */ }
}

async function loadRules() {
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize: pageSize.value }
    if (filterKeyword.value) params.keyword = filterKeyword.value
    if (filterScenicId.value) params.scenicId = filterScenicId.value
    if (filterType.value) params.type = filterType.value
    const data = await listRules(params)
    ruleList.value = data?.records || []
    total.value = data?.total || 0
    pages.value = data?.pages || 1
    // 统计卡片
    summaryStats.value[0].value = String(total.value)
    summaryStats.value[0].sub = `覆盖 ${scenics.value.length || 0} 个园区，启用中 ${ruleList.value.filter(r => r.status === '启用').length} 个`
    summaryStats.value[1].value = String(ruleList.value.filter(r => r.type === '折扣').length)
    summaryStats.value[2].value = String(ruleList.value.filter(r => r.type === '减免').length)
    summaryStats.value[3].value = String(ruleList.value.filter(r => r.type === '套餐' || r.type === '限制').length)
  } catch (e) { /* handled */ }
  finally { loading.value = false }
}

function resetForm() {
  Object.assign(form, {
    scenicId: '', code: '', name: '', type: '折扣', priority: 100,
    status: '启用', effectiveFrom: '', effectiveTo: '', permanent: false, description: '',
  })
}

function openCreate() {
  editingRule.value = null
  resetForm()
  showModal.value = true
}

function openEdit(rule) {
  editingRule.value = rule
  Object.assign(form, {
    scenicId: rule.scenicId,
    code: rule.code || '',
    name: rule.name,
    type: rule.type,
    priority: rule.priority || 100,
    status: rule.status,
    effectiveFrom: rule.effectiveFrom || '',
    effectiveTo: rule.effectiveTo || '',
    permanent: !rule.effectiveTo,
    description: rule.description || '',
  })
  showModal.value = true
}

async function submitForm() {
  if (!form.scenicId) { ElMessage({ type: 'warning', message: '请选择所属园区' }); return }
  if (!form.code) { ElMessage({ type: 'warning', message: '请填写规则编码' }); return }
  if (!form.name) { ElMessage({ type: 'warning', message: '请填写规则名称' }); return }
  // 构造提交载荷：勾选"永久"时清空 effectiveTo，并剔除前端专用字段
  const payload = {
    scenicId: form.scenicId,
    code: form.code,
    name: form.name,
    type: form.type,
    priority: form.priority,
    status: form.status,
    effectiveFrom: form.effectiveFrom || null,
    effectiveTo: form.permanent ? null : (form.effectiveTo || null),
    description: form.description,
  }
  saving.value = true
  try {
    if (editingRule.value) {
      await updateRule(editingRule.value.id, payload)
      ElMessage({ type: 'success', message: '保存成功' })
    } else {
      await createRule(payload)
      ElMessage({ type: 'success', message: '新增成功' })
    }
    showModal.value = false
    loadRules()
  } catch (e) { /* handled */ }
  finally { saving.value = false }
}

async function toggleStatus(rule) {
  const next = rule.status === '启用' ? '禁用' : '启用'
  try {
    await toggleRuleStatus(rule.id, next)
    ElMessage({ type: 'success', message: '状态已更新' })
    loadRules()
  } catch (e) { /* handled */ }
}

onMounted(async () => {
  await loadScenics()
  await loadRules()
})
</script>
