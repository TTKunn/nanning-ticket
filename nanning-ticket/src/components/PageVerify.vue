<template>
  <div>
    <div class="card" style="margin-bottom:12px;">
      <div class="toolbar">
        <div class="form-item">
          <input class="form-input" placeholder="核销码/票据号/订单号..." style="width:220px;" />
        </div>
        <div class="form-item">
          <select class="form-select">
            <option>全部园区</option>
            <option v-for="s in scenics" :key="s">{{ s }}</option>
          </select>
        </div>
        <div class="form-item">
          <select class="form-select">
            <option>全部检票类型</option>
            <option>入园检票</option>
            <option>项目检票</option>
            <option>套餐二次检票</option>
          </select>
        </div>
        <button class="btn btn-default">查询</button>
        <div style="flex:1;"></div>
        <button class="btn btn-primary" @click="showScanModal = true">扫码检票</button>
        <button class="btn btn-default">手工检票</button>
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
        <button class="btn btn-default btn-sm">导出记录</button>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>检票码</th>
              <th>票据号</th>
              <th>票种名称</th>
              <th>票种分组</th>
              <th>园区</th>
              <th>游客姓名</th>
              <th>检票类型</th>
              <th>检票时间</th>
              <th>检票人员</th>
              <th>状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="record in verifyRecords" :key="record.code">
              <td style="font-family:monospace;font-size:12px;color:var(--color-blue);">{{ record.code }}</td>
              <td style="font-family:monospace;font-size:12px;color:var(--color-text-muted);">{{ record.voucherCode }}</td>
              <td>{{ record.ticket }}</td>
              <td><span class="tag" :class="groupClass(record.group)">{{ record.group }}</span></td>
              <td>{{ record.scenic }}</td>
              <td>{{ record.visitor }}</td>
              <td>{{ record.verifyType }}</td>
              <td style="font-size:12px;color:var(--color-text-secondary);">{{ record.time }}</td>
              <td>{{ record.operator }}</td>
              <td><span class="tag tag-green">检票成功</span></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="modal-mask" v-if="showScanModal" @click.self="showScanModal = false">
      <div class="modal-box" style="width:420px;">
        <div class="modal-header">
          <span class="modal-title">扫码检票</span>
          <button class="modal-close" @click="showScanModal = false">×</button>
        </div>
        <div class="modal-body" style="text-align:center;">
          <div style="width:220px;height:220px;border:2px dashed var(--color-border-dark);border-radius:var(--radius-md);margin:0 auto 16px;display:flex;flex-direction:column;align-items:center;justify-content:center;background:var(--color-gray-50);">
            <svg width="48" height="48" viewBox="0 0 16 16" fill="var(--color-gray-300)">
              <path d="M0 .5A.5.5 0 01.5 0h3a.5.5 0 010 1H1v2.5a.5.5 0 01-1 0V.5zm12 0a.5.5 0 01.5-.5h3a.5.5 0 01.5.5v3a.5.5 0 01-1 0V1h-2.5a.5.5 0 01-.5-.5zM.5 12a.5.5 0 01.5.5V15h2.5a.5.5 0 010 1H.5a.5.5 0 01-.5-.5v-3a.5.5 0 01.5-.5zm15 0a.5.5 0 01.5.5v3a.5.5 0 01-.5.5h-3a.5.5 0 010-1H15v-2.5a.5.5 0 01.5-.5z"/>
            </svg>
            <div style="font-size:12px;color:var(--color-text-muted);margin-top:8px;">摄像头检票区域</div>
          </div>
          <div style="font-size:13px;color:var(--color-text-secondary);margin-bottom:12px;">支持门票入园检票、游玩票项目检票、全包票二次检票</div>
          <input class="form-input" placeholder="请输入检票码或票据号..." style="width:100%;text-align:center;font-family:monospace;" />
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="showScanModal = false">取消</button>
          <button class="btn btn-primary" @click="showScanModal = false">确认检票</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const showScanModal = ref(false)
const scenics = ['青秀山风景区', '南湖公园', '邕江景区']

const verifyStats = [
  { label: '今日检票总量', value: '284', sub: '入园检票 + 项目检票', color: 'var(--color-text-primary)' },
  { label: '入园检票', value: '198', sub: '门票和全包票首检', color: 'var(--color-blue)' },
  { label: '项目检票', value: '84', sub: '游玩票和套餐二次检票', color: 'var(--color-green)' },
  { label: '异常检票', value: '2', sub: '重复检票或票据无效', color: 'var(--color-red)' },
]

const verifyRecords = [
  { code: 'VF20260607001', voucherCode: 'VC202606070002', ticket: '邕江夜游全包票', group: '全包票', scenic: '邕江景区', visitor: '李**', verifyType: '入园检票', time: '2026-06-07 09:30', operator: '李检票员' },
  { code: 'VF20260607002', voucherCode: 'VC202606070003', ticket: '青秀山观光车票', group: '游玩票', scenic: '青秀山风景区', visitor: '王**', verifyType: '项目检票', time: '2026-06-07 09:15', operator: '张检票员' },
  { code: 'VF20260607003', voucherCode: 'VC202606070001', ticket: '青秀山成人门票', group: '门票', scenic: '青秀山风景区', visitor: '张**', verifyType: '入园检票', time: '2026-06-07 09:05', operator: '王检票员' },
]

function groupClass(group) {
  return {
    门票: 'tag-blue',
    游玩票: 'tag-orange',
    全包票: 'tag-green',
  }[group] || 'tag-gray'
}
</script>
