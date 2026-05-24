<template>
  <div>
    <!-- 工具栏 -->
    <div class="card" style="margin-bottom:12px;">
      <div class="toolbar">
        <div class="form-item">
          <input class="form-input" placeholder="核销码/订单号..." style="width:200px;" />
        </div>
        <div class="form-item">
          <select class="form-select">
            <option>全部景区</option>
            <option v-for="s in scenics" :key="s">{{ s }}</option>
          </select>
        </div>
        <div class="form-item">
          <input class="form-input" type="date" style="width:140px;" />
        </div>
        <button class="btn btn-default">查询</button>
        <div style="flex:1;"></div>
        <button class="btn btn-primary" @click="showScanModal = true">
          <svg width="13" height="13" viewBox="0 0 16 16" fill="currentColor"><path d="M0 .5A.5.5 0 01.5 0h3a.5.5 0 010 1H1v2.5a.5.5 0 01-1 0V.5zm12 0a.5.5 0 01.5-.5h3a.5.5 0 01.5.5v3a.5.5 0 01-1 0V1h-2.5a.5.5 0 01-.5-.5zM.5 12a.5.5 0 01.5.5V15h2.5a.5.5 0 010 1H.5a.5.5 0 01-.5-.5v-3a.5.5 0 01.5-.5zm15 0a.5.5 0 01.5.5v3a.5.5 0 01-.5.5h-3a.5.5 0 010-1H15v-2.5a.5.5 0 01.5-.5zM3 4.5a.5.5 0 011 0v7a.5.5 0 01-1 0v-7zm2 0a.5.5 0 011 0v7a.5.5 0 01-1 0v-7zm2 0a.5.5 0 011 0v7a.5.5 0 01-1 0v-7zm2 0a.5.5 0 011 0v7a.5.5 0 01-1 0v-7zm2 0a.5.5 0 011 0v7a.5.5 0 01-1 0v-7z"/></svg>
          扫码核销
        </button>
        <button class="btn btn-default">手动核销</button>
      </div>
    </div>

    <!-- 今日核销统计 -->
    <div class="stat-grid" style="grid-template-columns:repeat(4,1fr);margin-bottom:12px;">
      <div class="stat-card" v-for="s in verifyStats" :key="s.label">
        <div class="stat-card-header">
          <span class="stat-card-label">{{ s.label }}</span>
        </div>
        <div class="stat-card-value" :style="{ color: s.color }">{{ s.value }}</div>
        <div style="font-size:12px;color:var(--color-text-muted);margin-top:4px;">{{ s.sub }}</div>
      </div>
    </div>

    <!-- 核销记录表格 -->
    <div class="card">
      <div class="card-header">
        <span class="card-title">核销记录</span>
        <button class="btn btn-default btn-sm">
          <svg width="12" height="12" viewBox="0 0 16 16" fill="currentColor"><path d="M.5 9.9a.5.5 0 01.5.5v2.5a1 1 0 001 1h12a1 1 0 001-1v-2.5a.5.5 0 011 0v2.5a2 2 0 01-2 2H2a2 2 0 01-2-2v-2.5a.5.5 0 01.5-.5z"/><path d="M7.646 11.854a.5.5 0 00.708 0l3-3a.5.5 0 00-.708-.708L8.5 10.293V1.5a.5.5 0 00-1 0v8.793L5.354 8.146a.5.5 0 10-.708.708l3 3z"/></svg>
          导出记录
        </button>
      </div>
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>核销码</th>
              <th>订单号</th>
              <th>票种名称</th>
              <th>景区</th>
              <th>游客姓名</th>
              <th>核销数量</th>
              <th>核销时间</th>
              <th>核销人员</th>
              <th>核销方式</th>
              <th>状态</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in verifyRecords" :key="r.code">
              <td style="font-family:monospace;font-size:12px;color:var(--color-blue);">{{ r.code }}</td>
              <td style="font-family:monospace;font-size:11px;color:var(--color-text-muted);">{{ r.orderId }}</td>
              <td>{{ r.ticket }}</td>
              <td style="color:var(--color-text-secondary);">{{ r.scenic }}</td>
              <td>{{ r.visitor }}</td>
              <td>{{ r.qty }}</td>
              <td style="font-size:12px;color:var(--color-text-secondary);">{{ r.time }}</td>
              <td>{{ r.operator }}</td>
              <td>
                <span class="tag" :class="r.method === '扫码' ? 'tag-blue' : 'tag-gray'">{{ r.method }}</span>
              </td>
              <td>
                <span class="tag tag-green">核销成功</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <div class="pagination">
        <span class="pagination-info">共 {{ verifyRecords.length }} 条</span>
        <button class="page-btn" disabled>«</button>
        <button class="page-btn active">1</button>
        <button class="page-btn">»</button>
      </div>
    </div>

    <!-- 扫码核销弹窗 -->
    <div class="modal-mask" v-if="showScanModal" @click.self="showScanModal = false">
      <div class="modal-box" style="width:400px;">
        <div class="modal-header">
          <span class="modal-title">扫码核销</span>
          <button class="modal-close" @click="showScanModal = false">×</button>
        </div>
        <div class="modal-body" style="text-align:center;">
          <!-- 二维码扫描区域 -->
          <div style="width:200px;height:200px;border:2px dashed var(--color-border-dark);border-radius:var(--radius-md);margin:0 auto 16px;display:flex;flex-direction:column;align-items:center;justify-content:center;background:var(--color-gray-50);">
            <svg width="48" height="48" viewBox="0 0 16 16" fill="var(--color-gray-300)">
              <path d="M0 .5A.5.5 0 01.5 0h3a.5.5 0 010 1H1v2.5a.5.5 0 01-1 0V.5zm12 0a.5.5 0 01.5-.5h3a.5.5 0 01.5.5v3a.5.5 0 01-1 0V1h-2.5a.5.5 0 01-.5-.5zM.5 12a.5.5 0 01.5.5V15h2.5a.5.5 0 010 1H.5a.5.5 0 01-.5-.5v-3a.5.5 0 01.5-.5zm15 0a.5.5 0 01.5.5v3a.5.5 0 01-.5.5h-3a.5.5 0 010-1H15v-2.5a.5.5 0 01.5-.5z"/>
            </svg>
            <div style="font-size:12px;color:var(--color-text-muted);margin-top:8px;">摄像头区域</div>
          </div>
          <div style="font-size:13px;color:var(--color-text-secondary);margin-bottom:16px;">或手动输入核销码</div>
          <input class="form-input" placeholder="请输入核销码..." style="width:100%;text-align:center;font-family:monospace;" />
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="showScanModal = false">取消</button>
          <button class="btn btn-primary" @click="showScanModal = false">确认核销</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const showScanModal = ref(false)
const scenics = ['青秀山风景区', '南湖公园', '广西民族博物馆', '邕江景区', '良凤江国家森林公园']

const verifyStats = [
  { label: '今日核销总量', value: '284', sub: '较昨日 +32', color: 'var(--color-text-primary)' },
  { label: '待核销订单', value: '12', sub: '需及时处理', color: 'var(--color-orange)' },
  { label: '核销成功率', value: '98.6%', sub: '本月平均', color: 'var(--color-green)' },
  { label: '异常核销', value: '2', sub: '本月累计', color: 'var(--color-red)' },
]

const verifyRecords = [
  { code: 'VF20260524001', orderId: 'NN2026052400121', ticket: '邕江游船夜游', scenic: '邕江景区', visitor: '王**', qty: 3, time: '2026-05-24 09:30', operator: '李检票员', method: '扫码' },
  { code: 'VF20260524002', orderId: 'NN2026052400117', ticket: '青秀山儿童票', scenic: '青秀山风景区', visitor: '孙**', qty: 2, time: '2026-05-24 09:15', operator: '张检票员', method: '扫码' },
  { code: 'VF20260524003', orderId: 'NN2026052400115', ticket: '广西民族博物馆票', scenic: '广西民族博物馆', visitor: '吴**', qty: 1, time: '2026-05-24 09:05', operator: '系统', method: '手动' },
  { code: 'VF20260524004', orderId: 'NN2026052400113', ticket: '青秀山成人票', scenic: '青秀山风景区', visitor: '郑**', qty: 2, time: '2026-05-24 08:50', operator: '张检票员', method: '扫码' },
  { code: 'VF20260524005', orderId: 'NN2026052400110', ticket: '南湖公园联票', scenic: '南湖公园', visitor: '冯**', qty: 1, time: '2026-05-24 08:35', operator: '王检票员', method: '扫码' },
]
</script>
