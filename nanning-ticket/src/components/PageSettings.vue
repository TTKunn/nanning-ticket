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
          <div style="max-width:480px;" class="form-vertical">
            <div class="form-item">
              <label class="form-label">系统名称</label>
              <input class="form-input" value="AI南宁票务管理系统" />
            </div>
            <div class="form-item">
              <label class="form-label">系统简称</label>
              <input class="form-input" value="AI南宁票务" />
            </div>
            <div class="form-item">
              <label class="form-label">客服电话</label>
              <input class="form-input" value="0771-12345678" />
            </div>
            <div class="form-item">
              <label class="form-label">客服邮箱</label>
              <input class="form-input" value="support@ainanning.com" />
            </div>
            <div class="form-item">
              <label class="form-label">退款政策说明</label>
              <textarea class="form-textarea">游览日期前3天可申请全额退款；游览日期前1天退款扣除10%手续费；游览当天不支持退款。</textarea>
            </div>
            <button class="btn btn-primary">保存设置</button>
          </div>
        </div>
      </div>

      <div class="card">
        <div class="card-header"><span class="card-title">库存预警设置</span></div>
        <div class="card-body">
          <div style="max-width:480px;" class="form-vertical">
            <div class="form-item">
              <label class="form-label">全局预警阈值（张）</label>
              <input class="form-input" type="number" value="50" style="width:120px;" />
              <div style="font-size:12px;color:var(--color-text-muted);margin-top:4px;">当票种剩余库存低于此值时触发预警</div>
            </div>
            <div class="form-item">
              <label class="form-label">紧急预警阈值（张）</label>
              <input class="form-input" type="number" value="20" style="width:120px;" />
              <div style="font-size:12px;color:var(--color-text-muted);margin-top:4px;">低于此值时发送紧急通知</div>
            </div>
            <button class="btn btn-primary">保存</button>
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
                <td><input type="checkbox" :checked="n.inApp" /></td>
                <td><input type="checkbox" :checked="n.email" /></td>
                <td><input type="checkbox" :checked="n.sms" /></td>
                <td style="color:var(--color-text-secondary);font-size:12px;">{{ n.target }}</td>
              </tr>
            </tbody>
          </table>
          <div style="margin-top:16px;">
            <button class="btn btn-primary">保存通知设置</button>
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
              <tr v-for="u in users" :key="u.username">
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
      <div class="card" style="margin-bottom:12px;">
        <div class="card-header"><span class="card-title">API密钥管理</span></div>
        <div class="card-body">
          <div class="alert alert-info" style="margin-bottom:16px;">
            <svg width="14" height="14" viewBox="0 0 16 16" fill="currentColor"><path d="M8 16A8 8 0 108 0a8 8 0 000 16zm.93-9.412l-1 4.705c-.07.34.029.533.304.533.194 0 .487-.07.686-.246l-.088.416c-.287.346-.92.598-1.465.598-.703 0-1.002-.422-.808-1.319l.738-3.468c.064-.293.006-.399-.287-.47l-.451-.081.082-.381 2.29-.287zM8 5.5a1 1 0 110-2 1 1 0 010 2z"/></svg>
            API密钥用于各渠道平台对接，请妥善保管，不要泄露给无关人员
          </div>
          <table>
            <thead>
              <tr>
                <th>渠道名称</th>
                <th>AppID</th>
                <th>API密钥</th>
                <th>回调地址</th>
                <th>状态</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="api in apiConfigs" :key="api.channel">
                <td style="font-weight:500;">{{ api.channel }}</td>
                <td style="font-family:monospace;font-size:12px;">{{ api.appId }}</td>
                <td style="font-family:monospace;font-size:12px;color:var(--color-text-muted);">{{ api.key }}</td>
                <td style="font-size:12px;color:var(--color-text-secondary);">{{ api.callback }}</td>
                <td><span class="tag" :class="api.active ? 'tag-green' : 'tag-gray'">{{ api.active ? '已启用' : '未启用' }}</span></td>
                <td>
                  <div style="display:flex;gap:8px;">
                    <span class="action-link">编辑</span>
                    <span class="action-link">重新生成</span>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>

    <!-- 新增账号弹窗 -->
    <div class="modal-mask" v-if="showUserModal" @click.self="showUserModal = false">
      <div class="modal-box" style="width:440px;">
        <div class="modal-header">
          <span class="modal-title">新增账号</span>
          <button class="modal-close" @click="showUserModal = false">×</button>
        </div>
        <div class="modal-body">
          <div class="form-vertical">
            <div class="form-item">
              <label class="form-label">用户名 <span style="color:var(--color-red)">*</span></label>
              <input class="form-input" placeholder="登录用户名" />
            </div>
            <div class="form-item">
              <label class="form-label">姓名 <span style="color:var(--color-red)">*</span></label>
              <input class="form-input" placeholder="真实姓名" />
            </div>
            <div class="form-item">
              <label class="form-label">角色 <span style="color:var(--color-red)">*</span></label>
              <select class="form-select">
                <option>超级管理员</option>
                <option>运营管理员</option>
                <option>景区管理员</option>
                <option>核销员</option>
                <option>财务</option>
              </select>
            </div>
            <div class="form-item">
              <label class="form-label">所属景区</label>
              <select class="form-select">
                <option value="">不限（全部景区）</option>
                <option>青秀山风景区</option>
                <option>南湖公园</option>
                <option>广西民族博物馆</option>
                <option>邕江景区</option>
              </select>
            </div>
            <div class="form-item">
              <label class="form-label">初始密码 <span style="color:var(--color-red)">*</span></label>
              <input class="form-input" type="password" placeholder="设置初始密码" />
            </div>
          </div>
        </div>
        <div class="modal-footer">
          <button class="btn btn-default" @click="showUserModal = false">取消</button>
          <button class="btn btn-primary" @click="showUserModal = false">创建账号</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'

const activeTab = ref('basic')
const showUserModal = ref(false)

const notifyRules = [
  { event: '新订单提醒', inApp: true, email: false, sms: false, target: '运营管理员' },
  { event: '库存预警', inApp: true, email: true, sms: true, target: '运营管理员、景区管理员' },
  { event: '退款申请', inApp: true, email: true, sms: false, target: '运营管理员、财务' },
  { event: '渠道对账提醒', inApp: true, email: true, sms: false, target: '财务' },
  { event: '系统异常告警', inApp: true, email: true, sms: true, target: '超级管理员' },
]

const users = [
  { username: 'admin', name: '系统管理员', role: '超级管理员', roleClass: 'tag-red', scenic: null, lastLogin: '2026-05-24 09:00', status: '正常' },
  { username: 'ops_zhang', name: '张运营', role: '运营管理员', roleClass: 'tag-blue', scenic: null, lastLogin: '2026-05-24 08:30', status: '正常' },
  { username: 'qxs_li', name: '李景区', role: '景区管理员', roleClass: 'tag-green', scenic: '青秀山风景区', lastLogin: '2026-05-23 17:20', status: '正常' },
  { username: 'verify_wang', name: '王核销', role: '核销员', roleClass: 'tag-gray', scenic: '青秀山风景区', lastLogin: '2026-05-24 09:15', status: '正常' },
  { username: 'finance_chen', name: '陈财务', role: '财务', roleClass: 'tag-orange', scenic: null, lastLogin: '2026-05-22 14:00', status: '正常' },
]

const apiConfigs = [
  { channel: 'AI南宁直销', appId: 'NANNING_001', key: 'sk-****-****-****-abcd', callback: 'https://api.ainanning.com/callback', active: true },
  { channel: '美团旅游', appId: 'MT_NANNING_001', key: 'sk-****-****-****-efgh', callback: 'https://api.ainanning.com/meituan/cb', active: true },
  { channel: '携程旅行', appId: 'CTRIP_NN_001', key: 'sk-****-****-****-ijkl', callback: 'https://api.ainanning.com/ctrip/cb', active: true },
  { channel: '飞猪旅行', appId: 'FZ_NN_001', key: 'sk-****-****-****-mnop', callback: 'https://api.ainanning.com/feizhu/cb', active: false },
]
</script>
