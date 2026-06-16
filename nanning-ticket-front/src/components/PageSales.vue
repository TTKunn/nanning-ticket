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

    <div class="grid-2" style="align-items:start;">
      <div class="card">
        <div class="card-header">
          <span class="card-title">窗口售票台</span>
          <span class="tag tag-blue">业务人员操作端</span>
        </div>
        <div class="card-body">
          <div class="form-vertical">
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">所属园区</label>
                <select class="form-select" v-model="selectedScenic">
                  <option v-for="scenic in scenics" :key="scenic">{{ scenic }}</option>
                </select>
              </div>
              <div class="form-item">
                <label class="form-label">票种分组</label>
                <select class="form-select" v-model="selectedGroup">
                  <option>门票</option>
                  <option>游玩票</option>
                  <option>全包票</option>
                </select>
              </div>
            </div>

            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">票种</label>
                <select class="form-select" v-model="selectedTicketName">
                  <option v-for="ticket in filteredTickets" :key="ticket.name">{{ ticket.name }}</option>
                </select>
              </div>
              <div class="form-item">
                <label class="form-label">游玩日期</label>
                <input class="form-input" type="date" value="2026-06-07" />
              </div>
            </div>

            <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">单价</label>
                <input class="form-input" :value="`¥${selectedTicket?.price || 0}`" disabled />
              </div>
              <div class="form-item">
                <label class="form-label">数量</label>
                <input class="form-input" type="number" min="1" v-model="quantity" />
              </div>
              <div class="form-item">
                <label class="form-label">剩余日库存</label>
                <input class="form-input" :value="selectedTicket?.dailyRemaining || 0" disabled />
              </div>
            </div>

            <div class="divider"></div>
            <div style="font-size:13px;font-weight:600;color:var(--color-text-secondary);margin-bottom:8px;">购票人信息</div>

            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">购票人姓名</label>
                <input class="form-input" placeholder="请输入姓名" />
              </div>
              <div class="form-item">
                <label class="form-label">手机号</label>
                <input class="form-input" placeholder="请输入手机号" />
              </div>
            </div>

            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">证件号码</label>
                <input class="form-input" placeholder="实名票时填写" />
              </div>
              <div class="form-item">
                <label class="form-label">支付方式</label>
                <select class="form-select">
                  <option>现金</option>
                  <option>微信支付</option>
                  <option>支付宝</option>
                  <option>POS刷卡</option>
                </select>
              </div>
            </div>

            <div class="alert alert-warning" style="margin-top:10px;">
              <svg width="14" height="14" viewBox="0 0 16 16" fill="currentColor"><path d="M8.982 1.566a1.13 1.13 0 00-1.96 0L.165 13.233c-.457.778.091 1.767.98 1.767h13.713c.889 0 1.438-.99.98-1.767L8.982 1.566zM8 5c.535 0 .954.462.9.995l-.35 3.507a.552.552 0 01-1.1 0L7.1 5.995A.905.905 0 018 5zm.002 6a1 1 0 110 2 1 1 0 010-2z"/></svg>
              当前票种限购 {{ selectedTicket?.limit || 0 }} 张/人；仅展示核心售票流程，不含播音设备、指定售票员等非核心能力。
            </div>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <span class="card-title">出票摘要</span>
          <button class="btn btn-primary">确认出票</button>
        </div>
        <div class="card-body">
          <div class="info-row"><span class="info-label">园区</span><span class="info-value">{{ selectedScenic }}</span></div>
          <div class="info-row"><span class="info-label">票种分组</span><span class="info-value">{{ selectedGroup }}</span></div>
          <div class="info-row"><span class="info-label">票种</span><span class="info-value">{{ selectedTicket?.name }}</span></div>
          <div class="info-row"><span class="info-label">检票方式</span><span class="info-value">{{ selectedTicket?.verifyMode }}</span></div>
          <div class="info-row"><span class="info-label">退票规则</span><span class="info-value">{{ selectedTicket?.refundRule }}</span></div>
          <div class="divider"></div>
          <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;margin-bottom:12px;">
            <div class="card" style="box-shadow:none;">
              <div class="card-body" style="padding:12px;">
                <div style="font-size:12px;color:var(--color-text-muted);">票面金额</div>
                <div style="font-size:22px;font-weight:700;">¥{{ amount }}</div>
              </div>
            </div>
            <div class="card" style="box-shadow:none;">
              <div class="card-body" style="padding:12px;">
                <div style="font-size:12px;color:var(--color-text-muted);">出票渠道</div>
                <div style="font-size:16px;font-weight:700;">本地系统</div>
              </div>
            </div>
          </div>

          <div style="font-size:13px;font-weight:600;margin-bottom:8px;color:var(--color-text-secondary);">今日窗口售票记录</div>
          <table>
            <thead>
              <tr>
                <th>时间</th>
                <th>票种</th>
                <th>数量</th>
                <th>金额</th>
                <th>状态</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="record in recentRecords" :key="record.id">
                <td style="font-size:12px;color:var(--color-text-secondary);">{{ record.time }}</td>
                <td>{{ record.ticket }}</td>
                <td>{{ record.qty }}</td>
                <td style="font-weight:600;">¥{{ record.amount }}</td>
                <td><span class="tag tag-green">已出票</span></td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'

const scenics = ['青秀山风景区', '南湖公园', '邕江景区']
const selectedScenic = ref('青秀山风景区')
const selectedGroup = ref('门票')
const selectedTicketName = ref('青秀山成人门票')
const quantity = ref(2)

const tickets = [
  { scenic: '青秀山风景区', group: '门票', name: '青秀山成人门票', price: 60, dailyRemaining: 320, limit: 5, verifyMode: '单次入园核销', refundRule: '未使用可退' },
  { scenic: '青秀山风景区', group: '游玩票', name: '青秀山观光车票', price: 20, dailyRemaining: 180, limit: 2, verifyMode: '单项目核销', refundRule: '未使用可退' },
  { scenic: '邕江景区', group: '全包票', name: '邕江夜游全包票', price: 168, dailyRemaining: 60, limit: 4, verifyMode: '入园+项目双核销', refundRule: '未使用可退 + 过期自动退' },
  { scenic: '南湖公园', group: '门票', name: '南湖公园活动门票', price: 35, dailyRemaining: 120, limit: 4, verifyMode: '单次入园核销', refundRule: '过期自动退' },
]

const summaryStats = [
  { label: '今日窗口售票', value: '82 单', sub: '业务员现场售票订单数' },
  { label: '今日现场收入', value: '¥6,820', sub: '现金+电子支付合计' },
  { label: '待出票异常', value: '2', sub: '库存冲突或信息不完整' },
  { label: '现场退票', value: '3', sub: '按简化规则自动判定' },
]

const recentRecords = [
  { id: 'S001', time: '10:26', ticket: '青秀山成人门票', qty: 2, amount: 120 },
  { id: 'S002', time: '10:18', ticket: '青秀山观光车票', qty: 1, amount: 20 },
  { id: 'S003', time: '10:02', ticket: '邕江夜游全包票', qty: 3, amount: 504 },
]

const filteredTickets = computed(() => {
  const list = tickets.filter(ticket => ticket.scenic === selectedScenic.value && ticket.group === selectedGroup.value)
  if (list.length && !list.some(ticket => ticket.name === selectedTicketName.value)) {
    selectedTicketName.value = list[0].name
  }
  return list
})

const selectedTicket = computed(() => filteredTickets.value.find(ticket => ticket.name === selectedTicketName.value) || filteredTickets.value[0])
const amount = computed(() => Number(selectedTicket.value?.price || 0) * Number(quantity.value || 0))
</script>
