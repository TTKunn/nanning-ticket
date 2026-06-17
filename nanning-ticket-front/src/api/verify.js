import request from './request'

// 检票记录分页查询
export function listVerifies(params) {
  return request({ url: '/verifies', method: 'get', params })
}

// 检票（闸机/终端核心接口）
export function verifyVoucher(data) {
  return request({ url: '/verifies', method: 'post', data })
}

// 按票据码查询检票历史
export function getVerifyByCode(voucherCode) {
  return request({ url: '/verifies/by-code', method: 'get', params: { voucherCode } })
}

// 园区当日核销统计
export function getTodayStats(scenicId) {
  return request({ url: '/verifies/today-stats', method: 'get', params: { scenicId } })
}
