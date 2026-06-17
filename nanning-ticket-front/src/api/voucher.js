import request from './request'

// 票据分页查询
export function listVouchers(params) {
  return request({ url: '/vouchers', method: 'get', params })
}

// 按票据码查询
export function getVoucherByCode(voucherCode) {
  return request({ url: '/vouchers/by-code', method: 'get', params: { voucherCode } })
}

// 按销售单查全部票据
export function getVouchersBySale(saleId) {
  return request({ url: '/vouchers/by-sale', method: 'get', params: { saleId } })
}

// 批量作废
export function revokeVouchers(data) {
  return request({ url: '/vouchers/revoke', method: 'post', data })
}

// 批量补发
export function reissueVouchers(data) {
  return request({ url: '/vouchers/reissue', method: 'post', data })
}

// 标记打印
export function markPrinted(data) {
  return request({ url: '/vouchers/mark-printed', method: 'post', data })
}

// 状态统计
export function getVoucherStats(scenicId) {
  return request({ url: '/vouchers/stats', method: 'get', params: { scenicId } })
}
