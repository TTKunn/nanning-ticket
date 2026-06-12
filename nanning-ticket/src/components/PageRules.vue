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
      当前原型按“园区 -> 项目规则 -> 票种”组织，支持把门票、游玩票、全包票分开配置。
    </div>

    <div class="card" style="margin-bottom:12px;">
      <div class="toolbar">
        <div class="form-item">
          <input class="form-input" placeholder="规则名称/收费项目..." style="width:220px;" />
        </div>
        <div class="form-item">
          <select class="form-select">
            <option>全部园区</option>
            <option v-for="scenic in scenics" :key="scenic">{{ scenic }}</option>
          </select>
        </div>
        <div class="form-item">
          <select class="form-select">
            <option>全部票组</option>
            <option>门票</option>
            <option>游玩票</option>
            <option>全包票</option>
            <option>套票</option>
          </select>
        </div>
        <button class="btn btn-default">查询</button>
        <div style="flex:1;"></div>
        <button class="btn btn-primary" @click="showModal = true">新增项目规则</button>
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
              <th>规则名称</th>
              <th>票种分组</th>
              <th>收费对象</th>
              <th>核销方式</th>
              <th>退票规则</th>
              <th>关联票种</th>
              <th>售卖渠道</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="rule in ruleList" :key="rule.name">
              <td>{{ rule.scenic }}</td>
              <td>
                <div style="font-weight:600;">{{ rule.name }}</div>
                <div style="font-size:11px;color:var(--color-text-muted);">{{ rule.desc }}</div>
              </td>
              <td><span class="tag" :class="groupClass(rule.group)">{{ rule.group }}</span></td>
              <td>{{ rule.target }}</td>
              <td>{{ rule.verifyMode }}</td>
              <td>{{ rule.refundRule }}</td>
              <td>{{ rule.ticketCount }} 种</td>
              <td style="font-size:12px;color:var(--color-text-secondary);">{{ rule.channels }}</td>
              <td><span class="tag" :class="rule.enabled ? 'tag-green' : 'tag-gray'">{{ rule.enabled ? '启用中' : '停用' }}</span></td>
              <td>
                <div style="display:flex;gap:8px;">
                  <span class="action-link" @click="openEdit(rule)">编辑</span>
                  <span class="action-link">关联票种</span>
                  <span class="action-link danger">{{ rule.enabled ? '停用' : '启用' }}</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <div class="grid-2">
      <div class="card">
        <div class="card-header">
          <span class="card-title">设计建议</span>
        </div>
        <div class="card-body" style="font-size:13px;color:var(--color-text-secondary);display:grid;gap:10px;">
          <div><strong style="color:var(--color-text-primary);">门票</strong>：用于园区入园收费，通常一票一次入园，可与游玩票拆开核销。</div>
          <div><strong style="color:var(--color-text-primary);">游玩票</strong>：用于园区内单独收费项目，如游船、索道、演艺、设备体验。</div>
          <div><strong style="color:var(--color-text-primary);">全包票</strong>：将入园和多个项目打包，需定义包含项目及核销次数。</div>
          <div><strong style="color:var(--color-text-primary);">套票</strong>：用于节庆或联名场景，建议复用票种分组但单独管理日历库存。</div>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <span class="card-title">规则字段建议</span>
        </div>
        <div class="card-body">
          <table>
            <thead>
              <tr>
                <th>字段</th>
                <th>建议说明</th>
              </tr>
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
      <div class="modal-box" style="width:760px;">
        <div class="modal-header">
          <span class="modal-title">{{ editingRule ? '编辑项目规则' : '新增项目规则' }}</span>
          <button class="modal-close" @click="showModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-vertical">
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">所属园区</label>
                <select class="form-select">
                  <option v-for="scenic in scenics" :key="scenic">{{ scenic }}</option>
                </select>
              </div>
              <div class="form-item">
                <label class="form-label">规则名称</label>
                <input class="form-input" :value="editingRule?.name || ''" placeholder="如：园区入园规则" />
              </div>
            </div>

            <div style="display:grid;grid-template-columns:1fr 1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">票种分组</label>
                <select class="form-select">
                  <option>门票</option>
                  <option>游玩票</option>
                  <option>全包票</option>
                  <option>套票</option>
                </select>
              </div>
              <div class="form-item">
                <label class="form-label">收费对象</label>
                <select class="form-select">
                  <option>园区入园</option>
                  <option>园区内单项目</option>
                  <option>多个项目打包</option>
                </select>
              </div>
              <div class="form-item">
                <label class="form-label">核销方式</label>
                <select class="form-select">
                  <option>单次核销</option>
                  <option>分项目核销</option>
                  <option>入园+项目双核销</option>
                </select>
              </div>
            </div>

            <div class="form-item">
              <label class="form-label">包含项目</label>
              <div style="display:grid;grid-template-columns:repeat(4,1fr);gap:10px;font-size:13px;color:var(--color-text-secondary);">
                <label><input type="checkbox" checked /> 园区入园</label>
                <label><input type="checkbox" /> 玻璃栈道</label>
                <label><input type="checkbox" /> 游船</label>
                <label><input type="checkbox" /> 小火车</label>
              </div>
            </div>

            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">退票规则</label>
                <select class="form-select">
                  <option>未使用可退</option>
                  <option>未使用可退 + 过期自动退</option>
                  <option>仅过期自动退</option>
                </select>
              </div>
              <div class="form-item">
                <label class="form-label">适用渠道</label>
                <div style="display:grid;grid-template-columns:repeat(3,1fr);gap:8px;font-size:13px;color:var(--color-text-secondary);margin-top:8px;">
                  <label><input type="checkbox" checked /> 本地系统</label>
                  <label><input type="checkbox" checked /> 网售</label>
                  <label><input type="checkbox" checked /> 分销平台</label>
                </div>
              </div>
            </div>

            <div class="form-item">
              <label class="form-label">规则说明</label>
              <textarea class="form-textarea" placeholder="说明该规则对应的收费边界、可售票种和检退票要求..."></textarea>
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="showModal = false">取消</button>
          <button class="btn btn-primary" @click="showModal = false">保存规则</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const showModal = ref(false)
const editingRule = ref(null)

const scenics = ['青秀山风景区', '南湖公园', '广西民族博物馆', '邕江景区']

const summaryStats = [
  { label: '园区项目规则', value: '9', sub: '覆盖 4 个园区，启用中 8 个' },
  { label: '门票规则', value: '4', sub: '入园收费规则单独维护' },
  { label: '游玩票规则', value: '3', sub: '支持项目独立检票' },
  { label: '全包/套票规则', value: '2', sub: '用于联票和组合套餐' },
]

const ruleList = [
  { scenic: '青秀山风景区', name: '青秀山园区入园规则', desc: '园区入园收费一次', group: '门票', target: '园区入园', verifyMode: '单次核销', refundRule: '未使用可退', ticketCount: 3, channels: '本地系统、网售、分销', enabled: true },
  { scenic: '青秀山风景区', name: '青秀山观光车规则', desc: '园区内游玩项目单独收费', group: '游玩票', target: '园区内单项目', verifyMode: '单项目核销', refundRule: '未使用可退', ticketCount: 2, channels: '本地系统、网售', enabled: true },
  { scenic: '邕江景区', name: '夜游全包票规则', desc: '入园+游船一体化套餐', group: '全包票', target: '多个项目打包', verifyMode: '双核销', refundRule: '未使用可退 + 过期自动退', ticketCount: 1, channels: '网售、分销', enabled: true },
  { scenic: '南湖公园', name: '联票活动规则', desc: '节假日套票组合', group: '套票', target: '多个项目打包', verifyMode: '分项目核销', refundRule: '过期自动退', ticketCount: 2, channels: '本地系统、网售', enabled: false },
]

const ruleFields = [
  { name: '票种分组', desc: '区分门票、游玩票、全包票，决定后续配置入口和库存方式。' },
  { name: '收费对象', desc: '明确是园区入园收费还是园区内某个游玩项目收费。' },
  { name: '核销方式', desc: '决定是一次性核销还是入园、项目分开核销。' },
  { name: '退票规则', desc: '当前按简化策略配置为未使用可退、过期自动退。' },
  { name: '关联票种', desc: '规则下可挂多个票种，不同票种共享同一业务规则。' },
]

function groupClass(group) {
  return {
    门票: 'tag-blue',
    游玩票: 'tag-orange',
    全包票: 'tag-green',
    套票: 'tag-gray',
  }[group] || 'tag-gray'
}

function openEdit(rule) {
  editingRule.value = rule
  showModal.value = true
}
</script>
