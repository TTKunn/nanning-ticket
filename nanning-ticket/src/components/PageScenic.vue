<template>
  <div>
    <!-- 工具栏 -->
    <div class="card" style="margin-bottom:12px;">
      <div class="toolbar">
        <div class="form-item">
          <input class="form-input" placeholder="景区名称..." style="width:180px;" />
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
        <button class="btn btn-primary" @click="showModal = true">
          <svg width="13" height="13" viewBox="0 0 16 16" fill="currentColor"><path d="M8 4a.5.5 0 01.5.5v3h3a.5.5 0 010 1h-3v3a.5.5 0 01-1 0v-3h-3a.5.5 0 010-1h3v-3A.5.5 0 018 4z"/></svg>
          新增景区
        </button>
      </div>
    </div>

    <!-- 景区列表 -->
    <div class="card">
      <div class="table-wrap">
        <table>
          <thead>
            <tr>
              <th>景区名称</th>
              <th>景区地址</th>
              <th>景区类型</th>
              <th>开放时间</th>
              <th>票种数量</th>
              <th>本月销售</th>
              <th>状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="s in scenics" :key="s.id">
              <td>
                <div style="display:flex;align-items:center;gap:10px;">
                  <div :style="{ width:'36px', height:'36px', background:s.iconBg, borderRadius:'var(--radius)', display:'flex', alignItems:'center', justifyContent:'center', fontSize:'16px', flexShrink:0 }">
                    {{ s.icon }}
                  </div>
                  <div>
                    <div style="font-weight:500;">{{ s.name }}</div>
                    <div style="font-size:11px;color:var(--color-text-muted);">{{ s.level }}</div>
                  </div>
                </div>
              </td>
              <td style="color:var(--color-text-secondary);font-size:12px;">{{ s.address }}</td>
              <td><span class="tag tag-gray">{{ s.type }}</span></td>
              <td style="font-size:12px;">{{ s.openTime }}</td>
              <td>
                <span style="font-weight:600;">{{ s.ticketCount }}</span>
                <span style="color:var(--color-text-muted);font-size:12px;"> 种</span>
              </td>
              <td style="font-weight:600;">¥{{ s.monthSales }}</td>
              <td>
                <span class="tag" :class="s.status === '运营中' ? 'tag-green' : 'tag-gray'">{{ s.status }}</span>
              </td>
              <td>
                <div style="display:flex;gap:8px;">
                  <span class="action-link" @click="editScenic(s)">编辑</span>
                  <span class="action-link">票种管理</span>
                  <span class="action-link danger">{{ s.status === '运营中' ? '暂停' : '恢复' }}</span>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

    <!-- 新增/编辑景区弹窗 -->
    <div class="modal-mask" v-if="showModal" @click.self="showModal = false">
      <div class="modal-box" style="width:560px;">
        <div class="modal-header">
          <span class="modal-title">{{ editingScenic ? '编辑景区' : '新增景区' }}</span>
          <button class="modal-close" @click="showModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-vertical">
            <div class="form-item">
              <label class="form-label">景区名称 <span style="color:var(--color-red)">*</span></label>
              <input class="form-input" placeholder="请输入景区名称" :value="editingScenic?.name || ''" />
            </div>
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">景区类型</label>
                <select class="form-select">
                  <option>自然风景区</option>
                  <option>文化遗址</option>
                  <option>主题公园</option>
                  <option>博物馆</option>
                  <option>城市公园</option>
                  <option>水上乐园</option>
                </select>
              </div>
              <div class="form-item">
                <label class="form-label">景区等级</label>
                <select class="form-select">
                  <option>5A级</option>
                  <option>4A级</option>
                  <option>3A级</option>
                  <option>无评级</option>
                </select>
              </div>
            </div>
            <div class="form-item">
              <label class="form-label">景区地址</label>
              <input class="form-input" placeholder="详细地址" />
            </div>
            <div style="display:grid;grid-template-columns:1fr 1fr;gap:12px;">
              <div class="form-item">
                <label class="form-label">开放时间</label>
                <input class="form-input" placeholder="如：08:00-18:00" />
              </div>
              <div class="form-item">
                <label class="form-label">联系电话</label>
                <input class="form-input" placeholder="景区联系电话" />
              </div>
            </div>
            <div class="form-item">
              <label class="form-label">景区简介</label>
              <textarea class="form-textarea" placeholder="景区简介，将展示在AI南宁前端..."></textarea>
            </div>
            <div class="form-item">
              <label class="form-label">注意事项</label>
              <textarea class="form-textarea" placeholder="游览注意事项..."></textarea>
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
  { id: 1, name: '青秀山风景区', icon: '🌿', iconBg: '#f0fdf4', level: '国家5A级景区', address: '南宁市青秀区青秀山路', type: '自然风景区', openTime: '06:00-22:00', ticketCount: 3, monthSales: '24,720', status: '运营中' },
  { id: 2, name: '南湖公园', icon: '🏞', iconBg: '#eff6ff', level: '国家4A级景区', address: '南宁市青秀区民族大道', type: '城市公园', openTime: '06:00-21:00', ticketCount: 2, monthSales: '14,400', status: '运营中' },
  { id: 3, name: '广西民族博物馆', icon: '🏛', iconBg: '#fefce8', level: '国家一级博物馆', address: '南宁市青秀区青秀山路', type: '博物馆', openTime: '09:00-17:00（周一闭馆）', ticketCount: 1, monthSales: '29,040', status: '运营中' },
  { id: 4, name: '邕江景区', icon: '🚢', iconBg: '#fff7ed', level: '国家4A级景区', address: '南宁市邕江沿岸', type: '水上游览', openTime: '夜游：19:00-22:00', ticketCount: 2, monthSales: '13,640', status: '运营中' },
  { id: 5, name: '良凤江国家森林公园', icon: '🌲', iconBg: '#f0fdf4', level: '国家森林公园', address: '南宁市江南区良凤江', type: '自然风景区', openTime: '08:00-18:00', ticketCount: 1, monthSales: '5,400', status: '运营中' },
]

function editScenic(s) {
  editingScenic.value = s
  showModal.value = true
}
</script>
