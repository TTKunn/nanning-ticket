import request from './request'

// 窗口售票（创建销售）
export function listSales(params) {
  return request({ url: '/sales', method: 'get', params })
}

export function getSale(id) {
  return request({ url: `/sales/${id}`, method: 'get' })
}

export function createSale(data) {
  return request({ url: '/sales', method: 'post', data })
}

export function refundSale(id, data) {
  return request({ url: `/sales/${id}/refund`, method: 'post', data })
}

export function cancelSale(id, reason) {
  return request({ url: `/sales/${id}/cancel`, method: 'post', params: { reason } })
}
