<template>
  <div>
    <div class="alert alert-info">
      <svg width="14" height="14" viewBox="0 0 16 16" fill="currentColor"><path d="M8 16A8 8 0 108 0a8 8 0 000 16zm.93-9.412l-1 4.705c-.07.34.029.533.304.533.194 0 .487-.07.686-.246l-.088.416c-.287.346-.92.598-1.465.598-.703 0-1.002-.422-.808-1.319l.738-3.468c.064-.293.006-.399-.287-.47l-.451-.081.082-.381 2.29-.287zM8 5.5a1 1 0 110-2 1 1 0 010 2z"/></svg>
      当前票种原型已按“票种分组 + 库存方式 + 购票限制 + 退票规则”重构，不包含转增、激活、播音设备等非核心业务。
    </div>

    <div class="card" style="margin-bottom:12px;">
      <div class="toolbar">
        <div class="form-item">
          <input class="form-input" placeholder="搜索票种名称..." v-model="search" style="width:200px;" />
        </div>
        <div class="form-item">
          <select class="form-select" v-model="filterScenic">
            <option value="">全部园区</option>
            <option v-for="s in scenics" :key="s" :value="s">{{ s }}</option>
          </select>
        </div>
        <div class="form-item">
          <select class="form-select" v-model="filterGroup">
            <option value="">全部票种分组</option>
            <option value="门票">门票</option>
            <option value="游玩票">游玩票</option>
            <option value="全包票">全包票</option>
            <option value="套票">套票</option>
          </select>
        </div>
        <div class="form-item">
          <select class="form-select" v-model="filterStatus">
            <option value="">全部状态</option>
            <option value="on">上架中</option>
            <option value="off">已下架</option>
          </select>
        </div>
        <button class="btn btn-default">查询</button>
        <div style="flex:1;"></div>
        <button class="btn btn-primary" @click="showModal = true">新增票种</button>
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
              <th>关联规则</th>
              <th>销售价</th>
              <th>库存方式</th>
              <th>购票限制</th>
              <th>退票规则</th>
              <th>渠道</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="ticket in filteredTickets" :key="ticket.id">
              <td>
                <div style="font-weight:600;">{{ ticket.name }}</div>
                <div style="font-size:11px;color:var(--color-text-muted);">ID: {{ ticket.id }}</div>
              </td>
              <td>{{ ticket.scenic }}</td>
              <td><span class="tag" :class="groupClass(ticket.group)">{{ ticket.group }}</span></td>
              <td style="font-size:12px;color:var(--color-text-secondary);">{{ ticket.rule }}</td>
              <td style="font-weight:600;color:var(--color-red);">¥{{ ticket.price }}</td>
              <td>
                <div>{{ ticket.stockMode }}</div>
                <div style="font-size:11px;color:var(--color-text-muted);">总库存 {{ ticket.totalStock }} / 日库存 {{ ticket.dailyStock }}</div>
              </td>
              <td style="font-size:12px;color:var(--color-text-secondary);">{{ ticket.limitSummary }}</td>
              <td>{{ ticket.refundRule }}</td>
              <td style="font-size:12px;color:var(--color-text-secondary);">{{ ticket.channels }}</td>
              <td><span class="tag" :class="ticket.status === '上架中' ? 'tag-green' : 'tag-gray'">{{ ticket.status }}</span></td>
              <td>
                <div style="display:flex;gap:8px;align-items:center;">
                  <span class="action-link" @click="editTicket(ticket)">编辑</span>
                  <span class="action-link">日历库存</span>
                  <span class="action-link">渠道分配</span>
                  <span class="action-link danger">{{ ticket.status === '上架中' ? '下架' : '上架' }}</span>
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

    <div class="modal-mask" v-if="showModal" @click.self="showModal = false">
      <div class="modal-box" style="width:860px;">
        <div class="modal-header">
          <span class="modal-title">{{ editingTicket ? '编辑票种' : '新增票种' }}</span>
          <button class="modal-close" @click="showModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="tab-bar" style="margin:-20px -20px 16px;border-radius:0;">
            <div class="tab-item active">基础信息</div>
            <div class="tab-item active">库存与价格</div>
            <div class="tab-item active">购票限制</div>
            <div class="tab-item active">检退票规则</div>
          </div>

          <div class="form-vertical">
            <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">所属园区</label>
                <select class="form-select">
                  <option v-for="s in scenics" :key="s">{{ s }}</option>
                </select>
              </div>
              <div class="form-item">
                <label class="form-label">票种名称</label>
                <input class="form-input" :value="editingTicket?.name || ''" placeholder="请输入票种名称" />
              </div>
              <div class="form-item">
                <label class="form-label">票种分组</label>
                <select class="form-select">
                  <option>门票</option>
                  <option>游玩票</option>
                  <option>全包票</option>
                  <option>套票</option>
                </select>
              </div>
            </div>

            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">关联项目规则</label>
                <select class="form-select">
                  <option>青秀山园区入园规则</option>
                  <option>青秀山观光车规则</option>
                  <option>邕江夜游全包票规则</option>
                </select>
              </div>
              <div class="form-item">
                <label class="form-label">售卖渠道</label>
                <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:8px;font-size:13px;color:var(--color-text-secondary);margin-top:8px;">
                  <label><input type="checkbox" checked /> 本地系统</label>
                  <label><input type="checkbox" checked /> 网售</label>
                  <label><input type="checkbox" checked /> 分销平台</label>
                </div>
              </div>
            </div>

            <div class="divider"></div>
            <div style="font-size:13px;font-weight:600;color:var(--color-text-secondary);margin-bottom:8px;">库存与价格</div>
            <div style="display:grid;grid-template-columns:1fr 1fr 1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">销售价</label>
                <input class="form-input" type="number" placeholder="0.00" />
              </div>
              <div class="form-item">
                <label class="form-label">库存方式</label>
                <select class="form-select">
                  <option>总库存 + 日库存</option>
                  <option>仅总库存</option>
                  <option>仅日库存</option>
                </select>
              </div>
              <div class="form-item">
                <label class="form-label">总库存</label>
                <input class="form-input" type="number" placeholder="0" />
              </div>
              <div class="form-item">
                <label class="form-label">默认日库存</label>
                <input class="form-input" type="number" placeholder="0" />
              </div>
            </div>

            <div class="divider"></div>
            <div style="font-size:13px;font-weight:600;color:var(--color-text-secondary);margin-bottom:8px;">购票限制</div>
            <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">单人限购上限</label>
                <input class="form-input" type="number" placeholder="如：5" />
              </div>
              <div class="form-item">
                <label class="form-label">最早可购时间</label>
                <input class="form-input" placeholder="如：提前 15 天" />
              </div>
              <div class="form-item">
                <label class="form-label">最晚可购时间</label>
                <input class="form-input" placeholder="如：当天 18:00 截止" />
              </div>
            </div>
            <div class="form-item">
              <label class="form-label">提交字段配置</label>
              <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:8px;font-size:13px;color:var(--color-text-secondary);">
                <label><input type="checkbox" checked /> 姓名</label>
                <label><input type="checkbox" checked /> 手机号</label>
                <label><input type="checkbox" /> 身份证</label>
                <label><input type="checkbox" /> 自定义字段</label>
              </div>
            </div>

            <div class="divider"></div>
            <div style="font-size:13px;font-weight:600;color:var(--color-text-secondary);margin-bottom:8px;">检票与退票</div>
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">检票方式</label>
                <select class="form-select">
                  <option>单次核销</option>
                  <option>分项目核销</option>
                  <option>双核销</option>
                </select>
              </div>
              <div class="form-item">
                <label class="form-label">退票规则</label>
                <select class="form-select">
                  <option>未使用可退</option>
                  <option>未使用可退 + 过期自动退</option>
                  <option>过期自动退</option>
                </select>
              </div>
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
import { computed, ref } from 'vue'

const search = ref('')
const filterScenic = ref('')
const filterGroup = ref('')
const filterStatus = ref('')
const showModal = ref(false)
const editingTicket = ref(null)

const scenics = ['青秀山风景区', '南湖公园', '广西民族博物馆', '邕江景区']

const tickets = [
  { id: 'TK001', name: '青秀山成人门票', scenic: '青秀山风景区', group: '门票', rule: '青秀山园区入园规则', price: 60, stockMode: '总库存 + 日库存', totalStock: 5000, dailyStock: 500, limitSummary: '限购 5 张/人，姓名+手机号', refundRule: '未使用可退', channels: '本地系统、网售、分销', status: '上架中' },
  { id: 'TK002', name: '青秀山观光车票', scenic: '青秀山风景区', group: '游玩票', rule: '青秀山观光车规则', price: 20, stockMode: '仅日库存', totalStock: '—', dailyStock: 180, limitSummary: '限购 2 张/人，手机号', refundRule: '未使用可退', channels: '本地系统、网售', status: '上架中' },
  { id: 'TK003', name: '邕江夜游全包票', scenic: '邕江景区', group: '全包票', rule: '夜游全包票规则', price: 168, stockMode: '总库存 + 日库存', totalStock: 800, dailyStock: 80, limitSummary: '限购 4 张/人，姓名+手机号', refundRule: '未使用可退 + 过期自动退', channels: '网售、分销', status: '上架中' },
  { id: 'TK004', name: '南湖节庆套票', scenic: '南湖公园', group: '套票', rule: '联票活动规则', price: 99, stockMode: '仅日库存', totalStock: '—', dailyStock: 120, limitSummary: '限购 4 张/人，手机号', refundRule: '过期自动退', channels: '本地系统、网售', status: '已下架' },
]

const filteredTickets = computed(() => tickets.filter(ticket => {
  if (search.value && !ticket.name.includes(search.value)) return false
  if (filterScenic.value && ticket.scenic !== filterScenic.value) return false
  if (filterGroup.value && ticket.group !== filterGroup.value) return false
  if (filterStatus.value === 'on' && ticket.status !== '上架中') return false
  if (filterStatus.value === 'off' && ticket.status !== '已下架') return false
  return true
}))

function groupClass(group) {
  return {
    门票: 'tag-blue',
    游玩票: 'tag-orange',
    全包票: 'tag-green',
    套票: 'tag-gray',
  }[group] || 'tag-gray'
}

function editTicket(ticket) {
  editingTicket.value = ticket
  showModal.value = true
}
</script>
