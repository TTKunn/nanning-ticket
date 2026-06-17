import request from './request'

// 在线订单分页查询
export function listOrders(params) {
  return request({ url: '/orders', method: 'get', params })
}

// 创建订单
export function createOrder(data) {
  return request({ url: '/orders', method: 'post', data })
}

// 取消订单
export function cancelOrder(id, data) {
  return request({ url: `/orders/${id}/cancel`, method: 'post', data })
}

// 全单退款
export function refundOrder(id, data) {
  return request({ url: `/orders/${id}/refund`, method: 'post', data })
}

// 订单状态统计
export function getOrderStats(scenicId) {
  return request({ url: '/orders/stats', method: 'get', params: { scenicId } })
}
