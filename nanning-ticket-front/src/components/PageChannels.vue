<template>
  <div>
    <!-- 工具栏 -->
    <div class="card" style="margin-bottom:12px;">
      <div class="toolbar">
        <div class="form-item">
          <input class="form-input" placeholder="渠道名称..." style="width:180px;" />
        </div>
        <div class="form-item">
          <select class="form-select">
            <option>全部状态</option>
            <option>合作中</option>
            <option>已暂停</option>
            <option>待审核</option>
          </select>
        </div>
        <button class="btn btn-default">查询</button>
        <div style="flex:1;"></div>
        <button class="btn btn-primary" @click="showModal = true">
          <svg width="13" height="13" viewBox="0 0 16 16" fill="currentColor"><path d="M8 4a.5.5 0 01.5.5v3h3a.5.5 0 010 1h-3v3a.5.5 0 01-1 0v-3h-3a.5.5 0 010-1h3v-3A.5.5 0 018 4z"/></svg>
          新增渠道
        </button>
      </div>
    </div>

    <!-- 渠道卡片列表 -->
    <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:12px;margin-bottom:12px;">
      <div class="card" v-for="ch in channels" :key="ch.id" style="cursor:pointer;" @click="viewChannel(ch)">
        <div class="card-body">
          <div style="display:flex;align-items:flex-start;justify-content:space-between;margin-bottom:12px;">
            <div style="display:flex;align-items:center;gap:10px;">
              <div :style="{ width:'40px', height:'40px', background:ch.iconBg, borderRadius:'var(--radius)', display:'flex', alignItems:'center', justifyContent:'center', fontSize:'18px' }">
                {{ ch.icon }}
              </div>
              <div>
                <div style="font-weight:600;font-size:14px;">{{ ch.name }}</div>
                <div style="font-size:11px;color:var(--color-text-muted);">{{ ch.type }}</div>
              </div>
            </div>
            <span class="tag" :class="ch.status === '合作中' ? 'tag-green' : ch.status === '待审核' ? 'tag-yellow' : 'tag-gray'">
              {{ ch.status }}
            </span>
          </div>
          <div class="divider"></div>
          <div style="display:grid;grid-template-columns:1fr 1fr;gap:8px;margin-top:10px;">
            <div>
              <div style="font-size:11px;color:var(--color-text-muted);">本月销售额</div>
              <div style="font-weight:600;font-size:15px;margin-top:2px;">¥{{ ch.monthSales }}</div>
            </div>
            <div>
              <div style="font-size:11px;color:var(--color-text-muted);">本月出票</div>
              <div style="font-weight:600;font-size:15px;margin-top:2px;">{{ ch.monthTickets }} 张</div>
            </div>
            <div>
              <div style="font-size:11px;color:var(--color-text-muted);">佣金比例</div>
              <div style="font-size:13px;margin-top:2px;color:var(--color-orange);">{{ ch.commission }}</div>
            </div>
            <div>
              <div style="font-size:11px;color:var(--color-text-muted);">接入票种</div>
              <div style="font-size:13px;margin-top:2px;">{{ ch.ticketCount }} 种</div>
            </div>
          </div>
          <div class="divider"></div>
          <div style="display:flex;gap:8px;margin-top:8px;">
            <span class="action-link" @click.stop="editChannel(ch)">编辑</span>
            <span class="action-link" @click.stop>票种配置</span>
            <span class="action-link" @click.stop>对账记录</span>
            <span class="action-link danger" @click.stop v-if="ch.status === '合作中'">暂停</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 渠道对账汇总 -->
    <div class="card">
      <div class="card-header">
        <span class="card-title">渠道对账汇总（本月）</span>
        <div style="display:flex;gap:8px;">
          <select class="form-select" style="height:28px;font-size:12px;">
            <option>2026年05月</option>
            <option>2026年04月</option>
            <option>2026年03月</option>
          </select>
          <button class="btn btn-default btn-sm">导出对账单</button>
        </div>
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
              <th>已结算</th>
              <th>待结算</th>
              <th>结算状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="r in reconciliation" :key="r.channel">
              <td style="font-weight:500;">{{ r.channel }}</td>
              <td>{{ r.tickets }}</td>
              <td>¥{{ r.sales }}</td>
              <td>{{ r.commission }}</td>
              <td style="font-weight:600;">¥{{ r.commissionAmt }}</td>
              <td style="color:var(--color-green);">¥{{ r.settled }}</td>
              <td style="color:var(--color-orange);font-weight:600;">¥{{ r.pending }}</td>
              <td>
                <span class="tag" :class="r.settleStatus === '已结清' ? 'tag-green' : r.settleStatus === '部分结算' ? 'tag-orange' : 'tag-gray'">
                  {{ r.settleStatus }}
                </span>
              </td>
              <td>
                <div style="display:flex;gap:8px;">
                  <span class="action-link">查看明细</span>
                  <span class="action-link" v-if="r.settleStatus !== '已结清'">发起结算</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 新增/编辑渠道弹窗 -->
    <div class="modal-mask" v-if="showModal" @click.self="showModal = false">
      <div class="modal-box">
        <div class="modal-header">
          <span class="modal-title">{{ editingChannel ? '编辑渠道' : '新增渠道' }}</span>
          <button class="modal-close" @click="showModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-vertical">
            <div class="form-item">
              <label class="form-label">渠道名称 <span style="color:var(--color-red)">*</span></label>
              <input class="form-input" placeholder="如：美团旅游" :value="editingChannel?.name || ''" />
            </div>
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">渠道类型</label>
                <select class="form-select">
                  <option>OTA平台</option>
                  <option>自有渠道</option>
                  <option>代理商</option>
                  <option>企业团购</option>
                </select>
              </div>
              <div class="form-item">
                <label class="form-label">默认佣金比例</label>
                <input class="form-input" placeholder="如：8%" />
              </div>
            </div>
            <div class="form-item">
              <label class="form-label">联系人</label>
              <input class="form-input" placeholder="渠道对接人姓名" />
            </div>
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">联系电话</label>
                <input class="form-input" placeholder="联系电话" />
              </div>
              <div class="form-item">
                <label class="form-label">联系邮箱</label>
                <input class="form-input" placeholder="联系邮箱" />
              </div>
            </div>
            <div class="form-item">
              <label class="form-label">API接入地址</label>
              <input class="form-input" placeholder="渠道API回调地址（可选）" />
            </div>
            <div class="form-item">
              <label class="form-label">备注</label>
              <textarea class="form-textarea" placeholder="合作备注..."></textarea>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="showModal = false">取消</button>
          <button class="btn btn-primary" @click="showModal = false">保存</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const showModal = ref(false)
const editingChannel = ref(null)

const channels = [
  { id: 1, name: 'AI南宁直销', type: '自有渠道', icon: '宁', iconBg: '#eff6ff', status: '合作中', monthSales: '11,384', monthTickets: 284, commission: '0%', ticketCount: 6 },
  { id: 2, name: '美团旅游', type: 'OTA平台', icon: '美', iconBg: '#fff7ed', status: '合作中', monthSales: '7,120', monthTickets: 178, commission: '8%', ticketCount: 5 },
  { id: 3, name: '携程旅行', type: 'OTA平台', icon: '携', iconBg: '#f0fdf4', status: '合作中', monthSales: '5,400', monthTickets: 135, commission: '10%', ticketCount: 4 },
  { id: 4, name: '飞猪旅行', type: 'OTA平台', icon: '飞', iconBg: '#fef2f2', status: '合作中', monthSales: '2,880', monthTickets: 72, commission: '9%', ticketCount: 3 },
  { id: 5, name: '同程旅行', type: 'OTA平台', icon: '同', iconBg: '#f9fafb', status: '已暂停', monthSales: '0', monthTickets: 0, commission: '8%', ticketCount: 2 },
  { id: 6, name: '南宁旅行社', type: '代理商', icon: '旅', iconBg: '#fefce8', status: '待审核', monthSales: '0', monthTickets: 0, commission: '12%', ticketCount: 0 },
]

const reconciliation = [
  { channel: 'AI南宁直销', tickets: 284, sales: '11,384.00', commission: '0%', commissionAmt: '0.00', settled: '11,384.00', pending: '0.00', settleStatus: '已结清' },
  { channel: '美团旅游', tickets: 178, sales: '7,120.00', commission: '8%', commissionAmt: '569.60', settled: '569.60', pending: '0.00', settleStatus: '已结清' },
  { channel: '携程旅行', tickets: 135, sales: '5,400.00', commission: '10%', commissionAmt: '540.00', settled: '270.00', pending: '270.00', settleStatus: '部分结算' },
  { channel: '飞猪旅行', tickets: 72, sales: '2,880.00', commission: '9%', commissionAmt: '259.20', settled: '0.00', pending: '259.20', settleStatus: '未结算' },
]

function editChannel(ch) {
  editingChannel.value = ch
  showModal.value = true
}

function viewChannel(ch) {
  // 可扩展为详情页
}
</script>
