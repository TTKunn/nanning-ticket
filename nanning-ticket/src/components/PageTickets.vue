<template>
  <div>
    <!-- 工具栏 -->
    <div class="card" style="margin-bottom:12px;">
      <div class="toolbar">
        <div class="form-item">
          <input class="form-input" placeholder="搜索票种名称..." v-model="search" style="width:200px;" />
        </div>
        <div class="form-item">
          <select class="form-select" v-model="filterScenic">
            <option value="">全部景区</option>
            <option v-for="s in scenics" :key="s" :value="s">{{ s }}</option>
          </select>
        </div>
        <div class="form-item">
          <select class="form-select" v-model="filterStatus">
            <option value="">全部状态</option>
            <option value="on">上架中</option>
            <option value="off">已下架</option>
          </select>
        </div>
        <button class="btn btn-default">
          <svg width="13" height="13" viewBox="0 0 16 16" fill="currentColor"><path d="M11.742 10.344a6.5 6.5 0 10-1.397 1.398h-.001c.03.04.062.078.098.115l3.85 3.85a1 1 0 001.415-1.414l-3.85-3.85a1.007 1.007 0 00-.115-.099zM12 6.5a5.5 5.5 0 11-11 0 5.5 5.5 0 0111 0z"/></svg>
          查询
        </button>
        <div style="flex:1;"></div>
        <button class="btn btn-primary" @click="showModal = true">
          <svg width="13" height="13" viewBox="0 0 16 16" fill="currentColor"><path d="M8 4a.5.5 0 01.5.5v3h3a.5.5 0 010 1h-3v3a.5.5 0 01-1 0v-3h-3a.5.5 0 010-1h3v-3A.5.5 0 018 4z"/></svg>
          新增票种
        </button>
      </div>
    </div>

    <!-- 表格 -->
    <div class="card">
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th style="width:40px;"><input type="checkbox" /></th>
              <th>票种名称</th>
              <th>所属景区</th>
              <th>票种类型</th>
              <th>原价</th>
              <th>销售价</th>
              <th>有效期</th>
              <th>总库存</th>
              <th>已售</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="t in filteredTickets" :key="t.id">
              <td><input type="checkbox" /></td>
              <td>
                <div style="font-weight:500;">{{ t.name }}</div>
                <div style="font-size:11px;color:var(--color-text-muted);">ID: {{ t.id }}</div>
              </td>
              <td>{{ t.scenic }}</td>
              <td><span class="tag tag-gray">{{ t.type }}</span></td>
              <td style="color:var(--color-text-muted);text-decoration:line-through;">¥{{ t.originalPrice }}</td>
              <td style="font-weight:600;color:var(--color-red);">¥{{ t.price }}</td>
              <td style="font-size:12px;color:var(--color-text-secondary);">{{ t.validity }}</td>
              <td>{{ t.total }}</td>
              <td>
                <span :style="{ color: t.sold / t.total > 0.8 ? 'var(--color-red)' : 'var(--color-text-primary)' }">
                  {{ t.sold }}
                </span>
              </td>
              <td>
                <span class="tag" :class="t.status === '上架中' ? 'tag-green' : 'tag-gray'">{{ t.status }}</span>
              </td>
              <td>
                <div style="display:flex;gap:8px;align-items:center;">
                  <span class="action-link" @click="editTicket(t)">编辑</span>
                  <span class="action-link" @click="manageChannels(t)">渠道分配</span>
                  <span class="action-link danger">{{ t.status === '上架中' ? '下架' : '上架' }}</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="pagination">
        <span class="pagination-info">共 {{ filteredTickets.length }} 条</span>
        <button class="page-btn" disabled>«</button>
        <button class="page-btn active">1</button>
        <button class="page-btn">2</button>
        <button class="page-btn">»</button>
      </div>
    </div>

    <!-- 新增/编辑弹窗 -->
    <div class="modal-mask" v-if="showModal" @click.self="showModal = false">
      <div class="modal-box">
        <div class="modal-header">
          <span class="modal-title">{{ editingTicket ? '编辑票种' : '新增票种' }}</span>
          <button class="modal-close" @click="showModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-vertical">
            <div class="form-item">
              <label class="form-label">票种名称 <span style="color:var(--color-red)">*</span></label>
              <input class="form-input" placeholder="请输入票种名称" :value="editingTicket?.name || ''" />
            </div>
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">所属景区 <span style="color:var(--color-red)">*</span></label>
                <select class="form-select">
                  <option value="">请选择景区</option>
                  <option v-for="s in scenics" :key="s" :value="s">{{ s }}</option>
                </select>
              </div>
              <div class="form-item">
                <label class="form-label">票种类型</label>
                <select class="form-select">
                  <option>成人票</option>
                  <option>儿童票</option>
                  <option>老人票</option>
                  <option>学生票</option>
                  <option>联票</option>
                </select>
              </div>
            </div>
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">原价（元）</label>
                <input class="form-input" type="number" placeholder="0.00" />
              </div>
              <div class="form-item">
                <label class="form-label">销售价（元）<span style="color:var(--color-red)">*</span></label>
                <input class="form-input" type="number" placeholder="0.00" />
              </div>
            </div>
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">总库存</label>
                <input class="form-input" type="number" placeholder="0" />
              </div>
              <div class="form-item">
                <label class="form-label">有效期</label>
                <input class="form-input" placeholder="如：购买后30天内有效" />
              </div>
            </div>
            <div class="form-item">
              <label class="form-label">使用须知</label>
              <textarea class="form-textarea" placeholder="请输入使用须知、注意事项等..."></textarea>
            </div>
            <div class="form-item">
              <label class="form-label">状态</label>
              <select class="form-select">
                <option>上架中</option>
                <option>已下架</option>
              </select>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="showModal = false">取消</button>
          <button class="btn btn-primary" @click="showModal = false">保存</button>
        </div>
      </div>
    </div>

    <!-- 渠道分配弹窗 -->
    <div class="modal-mask" v-if="showChannelModal" @click.self="showChannelModal = false">
      <div class="modal-box">
        <div class="modal-header">
          <span class="modal-title">渠道分配 — {{ selectedTicket?.name }}</span>
          <button class="modal-close" @click="showChannelModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="alert alert-info">
            <svg width="14" height="14" viewBox="0 0 16 16" fill="currentColor"><path d="M8 16A8 8 0 108 0a8 8 0 000 16zm.93-9.412l-1 4.705c-.07.34.029.533.304.533.194 0 .487-.07.686-.246l-.088.416c-.287.346-.92.598-1.465.598-.703 0-1.002-.422-.808-1.319l.738-3.468c.064-.293.006-.399-.287-.47l-.451-.081.082-.381 2.29-.287zM8 5.5a1 1 0 110-2 1 1 0 010 2z"/></svg>
            为该票种设置各渠道的分配库存和渠道价格
          </div>
          <table>
            <thead>
              <tr>
                <th>渠道名称</th>
                <th>分配库存</th>
                <th>渠道价格</th>
                <th>佣金比例</th>
                <th>状态</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="ch in channelList" :key="ch.name">
                <td>{{ ch.name }}</td>
                <td><input class="form-input" type="number" :value="ch.quota" style="width:80px;height:28px;" /></td>
                <td><input class="form-input" type="number" :value="ch.price" style="width:80px;height:28px;" /></td>
                <td><input class="form-input" :value="ch.commission" style="width:70px;height:28px;" /></td>
                <td>
                  <label style="display:flex;align-items:center;gap:4px;cursor:pointer;font-size:12px;">
                    <input type="checkbox" :checked="ch.enabled" />
                    启用
                  </label>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="showChannelModal = false">取消</button>
          <button class="btn btn-primary" @click="showChannelModal = false">保存分配</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

const search = ref('')
const filterScenic = ref('')
const filterStatus = ref('')
const showModal = ref(false)
const showChannelModal = ref(false)
const editingTicket = ref(null)
const selectedTicket = ref(null)

const scenics = ['青秀山风景区', '南湖公园', '广西民族博物馆', '邕江景区', '良凤江国家森林公园']

const tickets = [
  { id: 'TK001', name: '青秀山成人票', scenic: '青秀山风景区', type: '成人票', originalPrice: '80', price: '60', validity: '购买后90天', total: 500, sold: 412, status: '上架中' },
  { id: 'TK002', name: '青秀山儿童票', scenic: '青秀山风景区', type: '儿童票', originalPrice: '40', price: '30', validity: '购买后90天', total: 200, sold: 98, status: '上架中' },
  { id: 'TK003', name: '南湖公园联票', scenic: '南湖公园', type: '联票', originalPrice: '150', price: '120', validity: '购买后60天', total: 300, sold: 285, status: '上架中' },
  { id: 'TK004', name: '广西民族博物馆票', scenic: '广西民族博物馆', type: '成人票', originalPrice: '30', price: '30', validity: '购买后30天', total: 1000, sold: 968, status: '上架中' },
  { id: 'TK005', name: '邕江游船夜游', scenic: '邕江景区', type: '成人票', originalPrice: '100', price: '88', validity: '购买后30天', total: 200, sold: 155, status: '上架中' },
  { id: 'TK006', name: '良凤江森林公园票', scenic: '良凤江国家森林公园', type: '成人票', originalPrice: '50', price: '45', validity: '购买后60天', total: 400, sold: 120, status: '已下架' },
]

const filteredTickets = computed(() => tickets.filter(t => {
  if (search.value && !t.name.includes(search.value)) return false
  if (filterScenic.value && t.scenic !== filterScenic.value) return false
  if (filterStatus.value === 'on' && t.status !== '上架中') return false
  if (filterStatus.value === 'off' && t.status !== '已下架') return false
  return true
}))

const channelList = [
  { name: 'AI南宁直销', quota: 200, price: 60, commission: '0%', enabled: true },
  { name: '美团旅游', quota: 100, price: 58, commission: '8%', enabled: true },
  { name: '携程旅行', quota: 100, price: 59, commission: '10%', enabled: true },
  { name: '飞猪旅行', quota: 50, price: 57, commission: '9%', enabled: false },
  { name: '同程旅行', quota: 50, price: 58, commission: '8%', enabled: false },
]

function editTicket(t) {
  editingTicket.value = t
  showModal.value = true
}

function manageChannels(t) {
  selectedTicket.value = t
  showChannelModal.value = true
}
</script>
