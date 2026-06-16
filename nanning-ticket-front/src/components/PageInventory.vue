<template>
  <div>
    <div class="alert alert-warning" style="margin-bottom:12px;">
      <svg width="14" height="14" viewBox="0 0 16 16" fill="currentColor"><path d="M8.982 1.566a1.13 1.13 0 00-1.96 0L.165 13.233c-.457.778.091 1.767.98 1.767h13.713c.889 0 1.438-.99.98-1.767L8.982 1.566zM8 5c.535 0 .954.462.9.995l-.35 3.507a.552.552 0 01-1.1 0L7.1 5.995A.905.905 0 018 5zm.002 6a1 1 0 110 2 1 1 0 010-2z"/></svg>
      当前有 <strong>3</strong> 个票种今日库存低于预警线，请优先调整日历库存而不是仅补总库存。
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
          <input class="form-input" placeholder="搜索票种..." style="width:180px;" />
        </div>
        <div class="form-item">
          <select class="form-select">
            <option>全部园区</option>
            <option v-for="s in scenics" :key="s">{{ s }}</option>
          </select>
        </div>
        <div class="form-item">
          <input class="form-input" type="date" value="2026-06-07" style="width:150px;" />
        </div>
        <div class="form-item">
          <select class="form-select">
            <option>全部状态</option>
            <option>库存充足</option>
            <option>日库存偏低</option>
            <option>总库存偏低</option>
            <option>已售罄</option>
          </select>
        </div>
        <button class="btn btn-default">查询</button>
        <div style="flex:1;"></div>
        <button class="btn btn-primary" @click="showAdjustModal = true">调整日库存</button>
        <button class="btn btn-default">调整总库存</button>
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
              <th>总库存</th>
              <th>今日库存</th>
              <th>今日已售</th>
              <th>今日剩余</th>
              <th>预警线</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="item in inventoryList" :key="item.id">
              <td>
                <div style="font-weight:600;">{{ item.name }}</div>
                <div style="font-size:11px;color:var(--color-text-muted);">{{ item.stockMode }}</div>
              </td>
              <td>{{ item.scenic }}</td>
              <td><span class="tag" :class="groupClass(item.group)">{{ item.group }}</span></td>
              <td>{{ item.totalStock }}</td>
              <td>{{ item.dailyStock }}</td>
              <td>{{ item.dailySold }}</td>
              <td>
                <span :style="{ fontWeight:600, color:getStockColor(item.dailyRemaining, item.warning) }">{{ item.dailyRemaining }}</span>
              </td>
              <td>{{ item.warning }}</td>
              <td><span class="tag" :class="statusClass(item)">{{ statusText(item) }}</span></td>
              <td>
                <div style="display:flex;gap:8px;">
                  <span class="action-link" @click="openAdjust(item)">调日库存</span>
                  <span class="action-link">库存日历</span>
                  <span class="action-link">明细</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="modal-mask" v-if="showAdjustModal" @click.self="showAdjustModal = false">
      <div class="modal-box" style="width:460px;">
        <div class="modal-header">
          <span class="modal-title">调整日库存{{ selectedItem ? ' - ' + selectedItem.name : '' }}</span>
          <button class="modal-close" @click="showAdjustModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-vertical">
            <div class="form-item">
              <label class="form-label">适用日期</label>
              <input class="form-input" type="date" value="2026-06-07" />
            </div>
            <div class="form-item">
              <label class="form-label">当前日库存</label>
              <input class="form-input" :value="selectedItem?.dailyStock || 0" disabled />
            </div>
            <div class="form-item">
              <label class="form-label">调整后日库存</label>
              <input class="form-input" type="number" placeholder="请输入调整数量" />
            </div>
            <div class="form-item">
              <label class="form-label">调整原因</label>
              <select class="form-select">
                <option>节假日备货</option>
                <option>活动备货</option>
                <option>项目临时限流</option>
                <option>人工修正</option>
              </select>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="showAdjustModal = false">取消</button>
          <button class="btn btn-primary" @click="showAdjustModal = false">保存调整</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const showAdjustModal = ref(false)
const selectedItem = ref(null)

const scenics = ['青秀山风景区', '南湖公园', '广西民族博物馆', '邕江景区']

const summaryStats = [
  { label: '启用票种', value: '8', sub: '含门票、游玩票、全包票' },
  { label: '总库存余额', value: '6,920', sub: '跨票种汇总剩余总量' },
  { label: '今日库存余额', value: '1,142', sub: '按 2026-06-07 统计' },
  { label: '预警票种', value: '3', sub: '需补充日历库存' },
]

const inventoryList = [
  { id: 1, name: '青秀山成人门票', scenic: '青秀山风景区', group: '门票', stockMode: '总库存 + 日库存', totalStock: 5000, dailyStock: 500, dailySold: 412, dailyRemaining: 88, warning: 80 },
  { id: 2, name: '青秀山观光车票', scenic: '青秀山风景区', group: '游玩票', stockMode: '仅日库存', totalStock: '—', dailyStock: 180, dailySold: 150, dailyRemaining: 30, warning: 40 },
  { id: 3, name: '邕江夜游全包票', scenic: '邕江景区', group: '全包票', stockMode: '总库存 + 日库存', totalStock: 800, dailyStock: 80, dailySold: 68, dailyRemaining: 12, warning: 20 },
  { id: 4, name: '南湖节庆套票', scenic: '南湖公园', group: '套票', stockMode: '仅日库存', totalStock: '—', dailyStock: 120, dailySold: 40, dailyRemaining: 80, warning: 30 },
]

function groupClass(group) {
  return {
    门票: 'tag-blue',
    游玩票: 'tag-orange',
    全包票: 'tag-green',
    套票: 'tag-gray',
  }[group] || 'tag-gray'
}

function getStockColor(remaining, warning) {
  if (remaining === 0) return 'var(--color-gray-400)'
  if (remaining < warning * 0.5) return 'var(--color-red)'
  if (remaining < warning) return 'var(--color-orange)'
  return 'var(--color-text-primary)'
}

function statusClass(item) {
  if (item.dailyRemaining === 0) return 'tag-gray'
  if (item.dailyRemaining < item.warning * 0.5) return 'tag-red'
  if (item.dailyRemaining < item.warning) return 'tag-orange'
  return 'tag-green'
}

function statusText(item) {
  if (item.dailyRemaining === 0) return '已售罄'
  if (item.dailyRemaining < item.warning * 0.5) return '日库存告急'
  if (item.dailyRemaining < item.warning) return '日库存偏低'
  return '库存充足'
}

function openAdjust(item) {
  selectedItem.value = item
  showAdjustModal.value = true
}
</script>
