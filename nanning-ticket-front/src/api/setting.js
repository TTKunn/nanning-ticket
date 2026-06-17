import request from './request'

// 系统参数分页查询
export function listSettings(params) {
  return request({ url: '/settings', method: 'get', params })
}

// 按分组批量取参数
export function listSettingsByGroup(group) {
  return request({ url: '/settings/by-group', method: 'get', params: { group } })
}

// 按 key 查参数
export function getSettingByKey(key) {
  return request({ url: '/settings/by-key', method: 'get', params: { key } })
}

export function saveSetting(data) {
  return request({ url: '/settings', method: 'post', data })
}

export function toggleSettingStatus(id, status) {
  return request({ url: `/settings/${id}/status`, method: 'patch', params: { status } })
}

// 操作日志
export function listOpLogs(params) {
  return request({ url: '/op-logs', method: 'get', params })
}
