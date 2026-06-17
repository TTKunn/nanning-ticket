import request from './request'

// 园区分页查询
// GET /api/scenics?keyword=&status=&pageNum=1&pageSize=10
// 后端 Result.data = { records, total, pageNum, pageSize, pages }
export function listScenics(params) {
  return request({ url: '/scenics', method: 'get', params })
}

// 园区下拉选项（仅"运营中"）
export function listScenicOptions() {
  return request({ url: '/scenics/options', method: 'get' })
}

// 园区详情
export function getScenic(id) {
  return request({ url: `/scenics/${id}`, method: 'get' })
}

// 新建园区
export function createScenic(data) {
  return request({ url: '/scenics', method: 'post', data })
}

// 编辑园区
export function updateScenic(id, data) {
  return request({ url: `/scenics/${id}`, method: 'put', data })
}

// 切换状态（PATCH /api/scenics/{id}/status?status=...）
export function toggleScenicStatus(id, status) {
  return request({ url: `/scenics/${id}/status`, method: 'patch', params: { status } })
}
