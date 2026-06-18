import request from './request'

// 库存分页：GET /api/inventories?ticketId=&dateFrom=&dateTo=&pageNum=&pageSize=
export function listInventories(params) {
  return request({ url: '/inventories', method: 'get', params })
}

export function createInventory(data) {
  return request({ url: '/inventories', method: 'post', data })
}

export function batchCreateInventory(data) {
  return request({ url: '/inventories/batch', method: 'post', data })
}

// 查询指定票种已存在库存的日期：GET /api/inventories/dates?ticketId=
// 返回 ['2026-06-17', '2026-06-18', ...]
export function listInventoryDates(ticketId) {
  return request({ url: '/inventories/dates', method: 'get', params: { ticketId } })
}

// 批量更新：PUT /api/inventories/batch
// operation: SET_TOTAL | INCREMENT | DECREMENT | SET_STATUS | SET_REMARK
export function batchUpdateInventory(data) {
  return request({ url: '/inventories/batch', method: 'put', data })
}

// 批量删除：DELETE /api/inventories/batch
// 默认仅删 sold=0 的记录，onlyUnsold=false 强制删除
export function batchDeleteInventory(data) {
  return request({ url: '/inventories/batch', method: 'delete', data })
}

export function toggleInventoryStatus(id, status) {
  return request({ url: `/inventories/${id}/status`, method: 'patch', params: { status } })
}
