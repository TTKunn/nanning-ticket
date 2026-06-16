<template>
  <div>
    <div class="card" style="margin-bottom:12px;">
      <div class="toolbar">
        <div class="form-item">
          <input class="form-input" placeholder="园区名称..." style="width:180px;" />
        </div>
        <div class="form-item">
          <select class="form-select">
            <option>全部状态</option>
            <option>运营中</option>
            <option>暂停运营</option>
          </select>
        </div>
        <button class="btn btn-default">查询</button>
        <div style="flex:1;"></div>
        <button class="btn btn-primary" @click="showModal = true">新增园区</button>
      </div>
    </div>

    <div class="card" style="margin-bottom:12px;">
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>园区名称</th>
              <th>园区地址</th>
              <th>收费项目</th>
              <th>项目规则</th>
              <th>票种数量</th>
              <th>本月销售</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="scenic in scenics" :key="scenic.id">
              <td>
                <div style="display:flex;align-items:center;gap:10px;">
                  <div :style="{ width:'36px', height:'36px', background:scenic.iconBg, borderRadius:'var(--radius)', display:'flex', alignItems:'center', justifyContent:'center', fontSize:'16px', flexShrink:0 }">
                    {{ scenic.icon }}
                  </div>
                  <div>
                    <div style="font-weight:600;">{{ scenic.name }}</div>
                    <div style="font-size:11px;color:var(--color-text-muted);">{{ scenic.level }}</div>
                  </div>
                </div>
              </td>
              <td style="color:var(--color-text-secondary);font-size:12px;">{{ scenic.address }}</td>
              <td>{{ scenic.projectCount }} 个</td>
              <td>{{ scenic.ruleCount }} 条</td>
              <td>{{ scenic.ticketCount }} 种</td>
              <td style="font-weight:600;">¥{{ scenic.monthSales }}</td>
              <td><span class="tag" :class="scenic.status === '运营中' ? 'tag-green' : 'tag-gray'">{{ scenic.status }}</span></td>
              <td>
                <div style="display:flex;gap:8px;">
                  <span class="action-link" @click="editScenic(scenic)">编辑</span>
                  <span class="action-link">收费项目</span>
                  <span class="action-link">项目规则</span>
                  <span class="action-link danger">{{ scenic.status === '运营中' ? '暂停' : '恢复' }}</span>
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
            </tbody>
          </table>
        </div>
      </div>

      <div class="card">
        <div class="card-header">
          <span class="card-title">设计补充说明</span>
        </div>
        <div class="card-body" style="display:grid;gap:10px;font-size:13px;color:var(--color-text-secondary);">
          <div>园区页面不再只维护地址和简介，还要成为“收费项目”和“规则配置”的入口。</div>
          <div>一个园区下可以有多个收费项目，例如“入园门票”“游船”“观光车”“演艺项目”。</div>
          <div>项目规则配置建议单独放到“项目规则配置”一级导航中，园区页只保留入口和汇总信息。</div>
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
              <input class="form-input" :value="editingScenic?.name || ''" placeholder="请输入园区名称" />
            </div>
            <div class="form-item">
              <label class="form-label">园区地址</label>
              <input class="form-input" placeholder="详细地址" />
            </div>
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">开放时间</label>
                <input class="form-input" placeholder="如：08:00-18:00" />
              </div>
              <div class="form-item">
                <label class="form-label">当前收费项目数</label>
                <input class="form-input" placeholder="如：4" />
              </div>
            </div>
            <div class="form-item">
              <label class="form-label">园区说明</label>
              <textarea class="form-textarea" placeholder="描述园区的入园、游玩项目和票务运营特点..."></textarea>
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
const editingScenic = ref(null)

const scenics = [
  { id: 1, name: '青秀山风景区', icon: '山', iconBg: '#f0fdf4', level: '国家5A级景区', address: '南宁市青秀区青秀山路', projectCount: 4, ruleCount: 3, ticketCount: 5, monthSales: '24,720', status: '运营中' },
  { id: 2, name: '南湖公园', icon: '湖', iconBg: '#eff6ff', level: '城市公园', address: '南宁市青秀区民族大道', projectCount: 2, ruleCount: 2, ticketCount: 2, monthSales: '14,400', status: '运营中' },
  { id: 3, name: '邕江景区', icon: '江', iconBg: '#fff7ed', level: '夜游景区', address: '南宁市邕江沿岸', projectCount: 3, ruleCount: 2, ticketCount: 3, monthSales: '13,640', status: '运营中' },
]

const projects = [
  { scenic: '青秀山风景区', name: '园区入园', type: '门票', rule: '青秀山园区入园规则' },
  { scenic: '青秀山风景区', name: '观光车', type: '游玩票', rule: '青秀山观光车规则' },
  { scenic: '邕江景区', name: '夜游游船', type: '全包票', rule: '夜游全包票规则' },
  { scenic: '南湖公园', name: '节庆联票', type: '套票', rule: '联票活动规则' },
]

function editScenic(scenic) {
  editingScenic.value = scenic
  showModal.value = true
}
</script>
