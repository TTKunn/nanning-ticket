import request from './request'

// 规则分页查询：GET /api/rules?scenicId=&type=&status=&pageNum=&pageSize=
export function listRules(params) {
  return request({ url: '/rules', method: 'get', params })
}

export function listRuleOptions(params) {
  return request({ url: '/rules/options', method: 'get', params })
}

export function createRule(data) {
  return request({ url: '/rules', method: 'post', data })
}

export function updateRule(id, data) {
  return request({ url: `/rules/${id}`, method: 'put', data })
}

export function toggleRuleStatus(id, status) {
  return request({ url: `/rules/${id}/status`, method: 'patch', params: { status } })
}
