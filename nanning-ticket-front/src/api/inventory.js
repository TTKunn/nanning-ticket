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

export function toggleInventoryStatus(id, status) {
  return request({ url: `/inventories/${id}/status`, method: 'patch', params: { status } })
}
