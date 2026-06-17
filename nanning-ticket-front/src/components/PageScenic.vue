<template>
  <div>
    <div class="card" style="margin-bottom:12px;">
      <div class="toolbar">
        <div class="form-item">
          <input class="form-input" v-model="filterKeyword" placeholder="园区名称..." style="width:180px;" />
        </div>
        <div class="form-item">
          <select class="form-select" v-model="filterStatus">
            <option value="">全部状态</option>
            <option value="运营中">运营中</option>
            <option value="暂停运营">暂停运营</option>
          </select>
        </div>
        <button class="btn btn-default" @click="loadScenics">查询</button>
        <div style="flex:1;"></div>
        <button class="btn btn-primary" @click="openCreate">新增园区</button>
      </div>
    </div>

    <div class="card" style="margin-bottom:12px;">
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>园区名称</th>
              <th>园区地址</th>
              <th>等级</th>
              <th>开放时间</th>
              <th>本月销售</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="loading">
              <td colspan="7" class="empty-state">加载中...</td>
            </tr>
            <tr v-else-if="!scenics.length">
              <td colspan="7" class="empty-state">暂无园区数据</td>
            </tr>
            <tr v-for="scenic in scenics" v-else :key="scenic.id">
              <td>
                <div style="display:flex;align-items:center;gap:10px;">
                  <div :style="{ width:'36px', height:'36px', background:scenic.iconBg || 'var(--color-blue-light)', borderRadius:'var(--radius)', display:'flex', alignItems:'center', justifyContent:'center', fontSize:'16px', flexShrink:0 }">
                    {{ scenic.icon || scenic.name?.slice(0,1) || '园' }}
                  </div>
                  <div>
                    <div style="font-weight:600;">{{ scenic.name }}</div>
                    <div style="font-size:11px;color:var(--color-text-muted);">ID: {{ scenic.id }}</div>
                  </div>
                </div>
              </td>
              <td style="color:var(--color-text-secondary);font-size:12px;">{{ scenic.address }}</td>
              <td>{{ scenic.level }}</td>
              <td>{{ scenic.openTime }}</td>
              <td style="font-weight:600;">¥{{ scenic.monthSales }}</td>
              <td><span class="tag" :class="scenic.status === '运营中' ? 'tag-green' : 'tag-gray'">{{ scenic.status }}</span></td>
              <td>
                <div style="display:flex;gap:8px;">
                  <span class="action-link" @click="openEdit(scenic)">编辑</span>
                  <span class="action-link">收费项目</span>
                  <span class="action-link">项目规则</span>
                  <span class="action-link danger" @click="toggleStatus(scenic)">{{ scenic.status === '运营中' ? '暂停' : '恢复' }}</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="pagination">
        <span class="pagination-info">共 {{ total }} 条</span>
        <button class="page-btn" :disabled="pageNum <= 1" @click="pageNum--; loadScenics()">«</button>
        <button v-for="p in pages" :key="p" class="page-btn" :class="{ active: p === pageNum }" @click="pageNum = p; loadScenics()">{{ p }}</button>
        <button class="page-btn" :disabled="pageNum >= pages" @click="pageNum++; loadScenics()">»</button>
      </div>
    </div>

    <div class="grid-2">
      <div class="card">
        <div class="card-header">
          <span class="card-title">园区收费项目示例</span>
        </div>
        <div class="card-body">
          <table>
            <thead>
              <tr>
                <th>园区</th>
                <th>收费项目</th>
                <th>收费类型</th>
                <th>关联规则</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="project in projects" :key="project.name">
                <td>{{ project.scenic }}</td>
                <td>{{ project.name }}</td>
                <td>{{ project.type }}</td>
                <td>{{ project.rule }}</td>
              </tr>
              <tr v-if="!projects.length">
                <td colspan="4" class="empty-state">暂无项目</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <span class="card-title">设计补充说明</span>
        </div>
        <div class="card-body" style="display:grid;gap:10px;font-size:13px;color:var(--color-text-secondary);">
          <div>园区页面不再只维护地址和简介，还要成为"收费项目"和"规则配置"的入口。</div>
          <div>一个园区下可以有多个收费项目，例如"入园门票""游船""观光车""演艺项目"。</div>
          <div>项目规则配置建议单独放到"项目规则配置"一级导航中，园区页只保留入口和汇总信息。</div>
        </div>
      </div>
    </div>

    <div class="modal-mask" v-if="showModal" @click.self="showModal = false">
      <div class="modal-box" style="width:620px;">
        <div class="modal-header">
          <span class="modal-title">{{ editingScenic ? '编辑园区' : '新增园区' }}</span>
          <button class="modal-close" @click="showModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-vertical">
            <div class="form-item">
              <label class="form-label">园区名称</label>
              <input class="form-input" v-model="form.name" placeholder="请输入园区名称" />
            </div>
            <div class="form-item">
              <label class="form-label">园区地址</label>
              <input class="form-input" v-model="form.address" placeholder="详细地址" />
            </div>
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">开放时间</label>
                <input class="form-input" v-model="form.openTime" placeholder="如：08:00-18:00" />
              </div>
              <div class="form-item">
                <label class="form-label">景区等级</label>
                <input class="form-input" v-model="form.level" placeholder="如：国家5A级景区" />
              </div>
            </div>
            <div class="form-item">
              <label class="form-label">园区说明</label>
              <textarea class="form-textarea" v-model="form.description" placeholder="描述园区的入园、游玩项目和票务运营特点..."></textarea>
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
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from './ui/Message'
import { listScenics, createScenic, updateScenic, toggleScenicStatus } from '../api/scenic'

const scenics = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)
const pages = ref(1)
const loading = ref(false)
const saving = ref(false)

const filterKeyword = ref('')
const filterStatus = ref('')

const showModal = ref(false)
const editingScenic = ref(null)
const form = reactive({
  name: '', address: '', openTime: '', level: '', description: '',
})

const projects = [
  { scenic: '青秀山风景区', name: '园区入园', type: '门票', rule: '青秀山园区入园规则' },
  { scenic: '青秀山风景区', name: '观光车', type: '游玩票', rule: '青秀山观光车规则' },
  { scenic: '邕江景区', name: '夜游游船', type: '全包票', rule: '夜游全包票规则' },
  { scenic: '南湖公园', name: '节庆联票', type: '套票', rule: '联票活动规则' },
]

async function loadScenics() {
  loading.value = true
  try {
    const params = {
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    }
    if (filterKeyword.value) params.keyword = filterKeyword.value
    if (filterStatus.value) params.status = filterStatus.value
    const data = await listScenics(params)
    // 后端分页 Result.data = { records, total, pages, ... }
    scenics.value = (data?.records || []).map((s) => ({
      ...s,
      monthSales: s.monthSales || '0',
    }))
    total.value = data?.total || 0
    pages.value = data?.pages || 1
  } catch (e) {
    // 错误提示已由拦截器处理
  } finally {
    loading.value = false
  }
}

function resetForm() {
  Object.assign(form, { name: '', address: '', openTime: '', level: '', description: '' })
}

function openCreate() {
  editingScenic.value = null
  resetForm()
  showModal.value = true
}

function openEdit(scenic) {
  editingScenic.value = scenic
  Object.assign(form, {
    name: scenic.name || '',
    address: scenic.address || '',
    openTime: scenic.openTime || '',
    level: scenic.level || '',
    description: scenic.description || '',
  })
  showModal.value = true
}

async function submitForm() {
  if (!form.name) {
    ElMessage({ type: 'warning', message: '请填写园区名称' })
    return
  }
  saving.value = true
  try {
    if (editingScenic.value) {
      await updateScenic(editingScenic.value.id, form)
      ElMessage({ type: 'success', message: '编辑成功' })
    } else {
      await createScenic({ ...form, status: '运营中' })
      ElMessage({ type: 'success', message: '新增成功' })
    }
    showModal.value = false
    loadScenics()
  } catch (e) {
    // 错误提示已由拦截器处理
  } finally {
    saving.value = false
  }
}

async function toggleStatus(scenic) {
  const next = scenic.status === '运营中' ? '暂停运营' : '运营中'
  try {
    await toggleScenicStatus(scenic.id, next)
    ElMessage({ type: 'success', message: '状态已更新' })
    loadScenics()
  } catch (e) {
    /* handled */
  }
}

onMounted(loadScenics)
</script>
