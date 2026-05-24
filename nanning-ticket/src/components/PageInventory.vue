<template>
  <div>
    <!-- 库存汇总 -->
    <div class="stat-grid" style="grid-template-columns:repeat(3,1fr);margin-bottom:12px;">
      <div class="stat-card" v-for="s in summaryStats" :key="s.label">
        <div class="stat-card-header">
          <span class="stat-card-label">{{ s.label }}</span>
        </div>
        <div class="stat-card-value" :style="{ color: s.color || 'var(--color-text-primary)' }">{{ s.value }}</div>
        <div style="font-size:12px;color:var(--color-text-muted);margin-top:4px;">{{ s.sub }}</div>
      </div>
    </div>

    <!-- 预警提示 -->
    <div class="alert alert-warning" style="margin-bottom:12px;">
      <svg width="14" height="14" viewBox="0 0 16 16" fill="currentColor"><path d="M8.982 1.566a1.13 1.13 0 00-1.96 0L.165 13.233c-.457.778.091 1.767.98 1.767h13.713c.889 0 1.438-.99.98-1.767L8.982 1.566zM8 5c.535 0 .954.462.9.995l-.35 3.507a.552.552 0 01-1.1 0L7.1 5.995A.905.905 0 018 5zm.002 6a1 1 0 110 2 1 1 0 010-2z"/></svg>
      当前有 <strong>3</strong> 个票种库存低于预警线（50张），请及时补充库存
    </div>

    <!-- 工具栏 -->
    <div class="card" style="margin-bottom:12px;">
      <div class="toolbar">
        <div class="form-item">
          <input class="form-input" placeholder="搜索票种..." style="width:180px;" />
        </div>
        <div class="form-item">
          <select class="form-select">
            <option>全部景区</option>
            <option v-for="s in scenics" :key="s">{{ s }}</option>
          </select>
        </div>
        <div class="form-item">
          <select class="form-select">
            <option>全部状态</option>
            <option>库存充足</option>
            <option>库存偏低</option>
            <option>紧急补货</option>
            <option>已售罄</option>
          </select>
        </div>
        <button class="btn btn-default">查询</button>
        <div style="flex:1;"></div>
        <button class="btn btn-primary" @click="showAddModal = true">
          <svg width="13" height="13" viewBox="0 0 16 16" fill="currentColor"><path d="M8 4a.5.5 0 01.5.5v3h3a.5.5 0 010 1h-3v3a.5.5 0 01-1 0v-3h-3a.5.5 0 010-1h3v-3A.5.5 0 018 4z"/></svg>
          补充库存
        </button>
        <button class="btn btn-default">批量调整</button>
      </div>
    </div>

    <!-- 库存表格 -->
    <div class="card">
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>票种名称</th>
              <th>所属景区</th>
              <th>总库存</th>
              <th>已售出</th>
              <th>已核销</th>
              <th>剩余可售</th>
              <th>库存占用率</th>
              <th>预警线</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in inventoryList" :key="item.id">
              <td>
                <div style="font-weight:500;">{{ item.name }}</div>
              </td>
              <td style="color:var(--color-text-secondary);">{{ item.scenic }}</td>
              <td>{{ item.total }}</td>
              <td>{{ item.sold }}</td>
              <td>{{ item.verified }}</td>
              <td>
                <span :style="{ fontWeight: 600, color: getStockColor(item.remaining, item.warning) }">
                  {{ item.remaining }}
                </span>
              </td>
              <td style="min-width:120px;">
                <div style="display:flex;align-items:center;gap:8px;">
                  <div class="progress-bar" style="flex:1;">
                    <div class="progress-fill"
                      :style="{ width: (item.sold/item.total*100) + '%', background: getProgressColor(item.sold/item.total) }">
                    </div>
                  </div>
                  <span style="font-size:12px;color:var(--color-text-muted);width:36px;text-align:right;">
                    {{ Math.round(item.sold/item.total*100) }}%
                  </span>
                </div>
              </td>
              <td style="color:var(--color-text-muted);">{{ item.warning }}</td>
              <td>
                <span class="tag" :class="getStatusClass(item.remaining, item.warning)">
                  {{ getStatusText(item.remaining, item.warning) }}
                </span>
              </td>
              <td>
                <div style="display:flex;gap:8px;">
                  <span class="action-link" @click="openAddStock(item)">补货</span>
                  <span class="action-link">调整预警</span>
                  <span class="action-link">明细</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="pagination">
        <span class="pagination-info">共 {{ inventoryList.length }} 条</span>
        <button class="page-btn" disabled>«</button>
        <button class="page-btn active">1</button>
        <button class="page-btn">»</button>
      </div>
    </div>

    <!-- 补货弹窗 -->
    <div class="modal-mask" v-if="showAddModal" @click.self="showAddModal = false">
      <div class="modal-box" style="width:420px;">
        <div class="modal-header">
          <span class="modal-title">补充库存{{ selectedItem ? ' — ' + selectedItem.name : '' }}</span>
          <button class="modal-close" @click="showAddModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-vertical">
            <div class="form-item" v-if="!selectedItem">
              <label class="form-label">选择票种</label>
              <select class="form-select">
                <option v-for="item in inventoryList" :key="item.id">{{ item.name }}</option>
              </select>
            </div>
            <div class="form-item">
              <label class="form-label">补充数量</label>
              <input class="form-input" type="number" placeholder="请输入补充数量" />
            </div>
            <div class="form-item">
              <label class="form-label">补货原因</label>
              <select class="form-select">
                <option>常规补货</option>
                <option>节假日备货</option>
                <option>活动备货</option>
                <option>其他</option>
              </select>
            </div>
            <div class="form-item">
              <label class="form-label">备注</label>
              <textarea class="form-textarea" placeholder="可选填备注信息..."></textarea>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="showAddModal = false">取消</button>
          <button class="btn btn-primary" @click="showAddModal = false">确认补货</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const showAddModal = ref(false)
const selectedItem = ref(null)

const scenics = ['青秀山风景区', '南湖公园', '广西民族博物馆', '邕江景区', '良凤江国家森林公园']

const summaryStats = [
  { label: '总票种数', value: '6', sub: '上架中 5 个，已下架 1 个' },
  { label: '总剩余库存', value: '1,248', sub: '较昨日减少 284 张', color: 'var(--color-text-primary)' },
  { label: '库存预警票种', value: '3', sub: '需及时补充库存', color: 'var(--color-red)' },
]

const inventoryList = [
  { id: 1, name: '青秀山成人票', scenic: '青秀山风景区', total: 500, sold: 412, verified: 380, remaining: 88, warning: 50 },
  { id: 2, name: '青秀山儿童票', scenic: '青秀山风景区', total: 200, sold: 98, verified: 90, remaining: 102, warning: 30 },
  { id: 3, name: '南湖公园联票', scenic: '南湖公园', total: 300, sold: 285, verified: 260, remaining: 15, warning: 50 },
  { id: 4, name: '广西民族博物馆票', scenic: '广西民族博物馆', total: 1000, sold: 968, verified: 900, remaining: 32, warning: 50 },
  { id: 5, name: '邕江游船夜游', scenic: '邕江景区', total: 200, sold: 155, verified: 140, remaining: 45, warning: 50 },
  { id: 6, name: '良凤江森林公园票', scenic: '良凤江国家森林公园', total: 400, sold: 120, verified: 100, remaining: 280, warning: 50 },
]

function getStockColor(remaining, warning) {
  if (remaining === 0) return 'var(--color-gray-400)'
  if (remaining < warning * 0.5) return 'var(--color-red)'
  if (remaining < warning) return 'var(--color-orange)'
  return 'var(--color-text-primary)'
}

function getProgressColor(ratio) {
  if (ratio > 0.95) return 'var(--color-red)'
  if (ratio > 0.8) return 'var(--color-orange)'
  return 'var(--color-blue)'
}

function getStatusClass(remaining, warning) {
  if (remaining === 0) return 'tag-gray'
  if (remaining < warning * 0.5) return 'tag-red'
  if (remaining < warning) return 'tag-orange'
  return 'tag-green'
}

function getStatusText(remaining, warning) {
  if (remaining === 0) return '已售罄'
  if (remaining < warning * 0.5) return '紧急补货'
  if (remaining < warning) return '库存偏低'
  return '库存充足'
}

function openAddStock(item) {
  selectedItem.value = item
  showAddModal.value = true
}
</script>
