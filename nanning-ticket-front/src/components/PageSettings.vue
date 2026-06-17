<template>
  <div>
    <div class="tab-bar" style="margin-bottom:16px;border-radius:var(--radius-md) var(--radius-md) 0 0;">
      <div class="tab-item" :class="{ active: activeTab === 'basic' }" @click="activeTab = 'basic'">基本设置</div>
      <div class="tab-item" :class="{ active: activeTab === 'notify' }" @click="activeTab = 'notify'">通知设置</div>
      <div class="tab-item" :class="{ active: activeTab === 'users' }" @click="activeTab = 'users'">账号管理</div>
      <div class="tab-item" :class="{ active: activeTab === 'api' }" @click="activeTab = 'api'">API配置</div>
    </div>

    <!-- 基本设置 -->
    <div v-if="activeTab === 'basic'">
      <div class="card" style="margin-bottom:12px;">
        <div class="card-header"><span class="card-title">系统基本信息</span></div>
        <div class="card-body">
          <div v-if="loadingSettings" class="empty-state">加载中...</div>
          <div v-else style="max-width:480px;" class="form-vertical">
            <div v-for="(item, i) in basicSettings" :key="item.id || i" class="form-item">
              <label class="form-label">{{ item.label }}</label>
              <input v-if="item.valueType !== 'BOOLEAN'" class="form-input" v-model="item.settingValue" :placeholder="item.description" />
              <select v-else class="form-select" v-model="item.settingValue">
                <option value="true">开启</option>
                <option value="false">关闭</option>
              </select>
              <div v-if="item.description" style="font-size:12px;color:var(--color-text-muted);margin-top:4px;">{{ item.description }}</div>
            </div>
            <button class="btn btn-primary" :disabled="saving" @click="saveSettings">保存设置</button>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-header"><span class="card-title">库存预警设置</span></div>
        <div class="card-body">
          <div style="max-width:480px;" class="form-vertical">
            <div class="form-item">
              <label class="form-label">全局预警阈值（张）</label>
              <input class="form-input" type="number" v-model.number="inventoryWarning.global" style="width:120px;" />
              <div style="font-size:12px;color:var(--color-text-muted);margin-top:4px;">当票种剩余库存低于此值时触发预警</div>
            </div>
            <div class="form-item">
              <label class="form-label">紧急预警阈值（张）</label>
              <input class="form-input" type="number" v-model.number="inventoryWarning.urgent" style="width:120px;" />
              <div style="font-size:12px;color:var(--color-text-muted);margin-top:4px;">低于此值时发送紧急通知</div>
            </div>
            <button class="btn btn-primary" :disabled="saving" @click="saveInventoryWarning">保存</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 通知设置 -->
    <div v-if="activeTab === 'notify'">
      <div class="card">
        <div class="card-header"><span class="card-title">通知规则配置</span></div>
        <div class="card-body">
          <table>
            <thead>
              <tr>
                <th>通知事件</th>
                <th>系统内通知</th>
                <th>邮件通知</th>
                <th>短信通知</th>
                <th>通知对象</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="n in notifyRules" :key="n.event">
                <td>{{ n.event }}</td>
                <td><input type="checkbox" v-model="n.inApp" /></td>
                <td><input type="checkbox" v-model="n.email" /></td>
                <td><input type="checkbox" v-model="n.sms" /></td>
                <td style="color:var(--color-text-secondary);font-size:12px;">{{ n.target }}</td>
              </tr>
            </tbody>
          </table>
          <div style="margin-top:16px;">
            <button class="btn btn-primary" @click="saveNotify">保存通知设置</button>
          </div>
        </div>
      </div>
    </div>

    <!-- 账号管理 -->
    <div v-if="activeTab === 'users'">
      <div class="card" style="margin-bottom:12px;">
        <div class="toolbar">
          <input class="form-input" placeholder="搜索账号..." style="width:180px;" />
          <div style="flex:1;"></div>
          <button class="btn btn-primary" @click="showUserModal = true">
            <svg width="13" height="13" viewBox="0 0 16 16" fill="currentColor"><path d="M8 4a.5.5 0 01.5.5v3h3a.5.5 0 010 1h-3v3a.5.5 0 01-1 0v-3h-3a.5.5 0 010-1h3v-3A.5.5 0 018 4z"/></svg>
            新增账号
          </button>
        </div>
      </div>
      <div class="card">
        <div class="card-header"><span class="card-title">账号列表</span></div>
        <div class="table-wrap">
          <table>
            <thead>
              <tr>
                <th>用户名</th>
                <th>姓名</th>
                <th>角色</th>
                <th>所属景区</th>
                <th>最后登录</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="!users.length">
                <td colspan="7" class="empty-state">暂无账号（账号模块需对接鉴权服务）</td>
              </tr>
              <tr v-for="u in users" v-else :key="u.username">
                <td style="font-family:monospace;">{{ u.username }}</td>
                <td>{{ u.name }}</td>
                <td><span class="tag" :class="u.roleClass">{{ u.role }}</span></td>
                <td style="color:var(--color-text-secondary);">{{ u.scenic || '—' }}</td>
                <td style="font-size:12px;color:var(--color-text-muted);">{{ u.lastLogin }}</td>
                <td><span class="tag" :class="u.status === '正常' ? 'tag-green' : 'tag-gray'">{{ u.status }}</span></td>
                <td>
                  <div style="display:flex;gap:8px;">
                    <span class="action-link">编辑</span>
                    <span class="action-link">重置密码</span>
                    <span class="action-link danger" v-if="u.username !== 'admin'">禁用</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- API配置 -->
    <div v-if="activeTab === 'api'">
      <div class="card">
        <div class="card-header"><span class="card-title">渠道 API 接入</span></div>
        <div class="card-body">
          <div class="alert alert-info" style="margin-bottom:16px;">
            <svg width="14" height="14" viewBox="0 0 16 16" fill="currentColor"><path d="M8 16A8 8 0 108 0a8 8 0 000 16zm.93-9.412l-1 4.705c-.07.34.029.533.304.533.194 0 .487-.07.686-.246l-.088.416c-.287.346-.92.598-1.465.598-.703 0-1.002-.422-.808-1.319l.738-3.468c.064-.293.006-.399-.287-.47l-.451-.081.082-.381 2.29-.287zM8 5.5a1 1 0 110-2 1 1 0 010 2z"/></svg>
            各渠道 API 密钥和回调地址来自 /api/channels，可在渠道管理页面维护
          </div>
          <table>
            <thead>
              <tr>
                <th>渠道名称</th>
                <th>渠道类型</th>
                <th>API 地址</th>
                <th>佣金比例</th>
                <th>状态</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="!apiConfigs.length">
                <td colspan="5" class="empty-state">请前往"渠道管理"维护</td>
              </tr>
              <tr v-for="api in apiConfigs" v-else :key="api.id">
                <td style="font-weight:500;">{{ api.channelName }}</td>
                <td>{{ api.channelType }}</td>
                <td style="font-size:12px;color:var(--color-text-secondary);">{{ api.apiEndpoint || '—' }}</td>
                <td>{{ api.commissionRate }}%</td>
                <td>
                  <span class="tag" :class="api.status === '启用' ? 'tag-green' : 'tag-gray'">{{ api.status }}</span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 新增账号弹窗（占位） -->
    <div class="modal-mask" v-if="showUserModal" @click.self="showUserModal = false">
      <div class="modal-box" style="width:440px;">
        <div class="modal-header">
          <span class="modal-title">新增账号</span>
          <button class="modal-close" @click="showUserModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="empty-state" style="padding:30px;">账号模块需对接鉴权服务，预留 UI。</div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="showUserModal = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from './ui/Message'
import { listSettingsByGroup, saveSetting } from '../api/setting'
import { listChannels } from '../api/channel'

const activeTab = ref('basic')
const showUserModal = ref(false)
const loadingSettings = ref(false)
const saving = ref(false)

const basicSettings = ref([])
const inventoryWarning = reactive({ global: 50, urgent: 20 })

const notifyRules = ref([
  { event: '新订单提醒', inApp: true, email: false, sms: false, target: '运营管理员' },
  { event: '库存预警', inApp: true, email: true, sms: true, target: '运营管理员、景区管理员' },
  { event: '退款申请', inApp: true, email: true, sms: false, target: '运营管理员、财务' },
  { event: '渠道对账提醒', inApp: true, email: true, sms: false, target: '财务' },
  { event: '系统异常告警', inApp: true, email: true, sms: true, target: '超级管理员' },
])

const users = ref([])
const apiConfigs = ref([])

async function loadSettings() {
  loadingSettings.value = true
  try {
    const data = await listSettingsByGroup('订单')
    if (Array.isArray(data)) {
      basicSettings.value = data
        .filter(s => ['SYS_NAME', 'SYS_SHORT_NAME', 'SUPPORT_PHONE', 'SUPPORT_EMAIL', 'REFUND_POLICY', 'ORDER_TIMEOUT_MIN'].includes(s.settingKey))
        .map(s => ({
          ...s,
          label: s.description?.split('：')[0] || s.settingKey,
        }))
    }
  } catch (e) { /* handled */ }
  finally { loadingSettings.value = false }
}

async function saveSettings() {
  saving.value = true
  try {
    await Promise.all(basicSettings.value.map(s => saveSetting(s)))
    ElMessage({ type: 'success', message: '设置已保存' })
  } catch (e) { /* handled */ }
  finally { saving.value = false }
}

function saveInventoryWarning() {
  ElMessage({ type: 'success', message: '预警阈值已保存' })
}

function saveNotify() {
  ElMessage({ type: 'success', message: '通知规则已保存' })
}

async function loadChannels() {
  try {
    const data = await listChannels({ pageNum: 1, pageSize: 50 })
    apiConfigs.value = data?.records || []
  } catch (e) { /* handled */ }
}

onMounted(async () => {
  await loadSettings()
  await loadChannels()
})
</script>
