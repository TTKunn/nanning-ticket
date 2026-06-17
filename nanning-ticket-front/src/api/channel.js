import request from './request'

// 渠道分页
export function listChannels(params) {
  return request({ url: '/channels', method: 'get', params })
}

// 渠道下拉（仅启用项）
export function listChannelOptions() {
  return request({ url: '/channels/options', method: 'get' })
}

export function createChannel(data) {
  return request({ url: '/channels', method: 'post', data })
}

export function updateChannel(id, data) {
  return request({ url: `/channels/${id}`, method: 'put', data })
}

export function toggleChannelStatus(id, status) {
  return request({ url: `/channels/${id}/status`, method: 'patch', params: { status } })
}

// 渠道维度统计
export function getChannelStats() {
  return request({ url: '/channels/stats', method: 'get' })
}

// 调整佣金比例
export function adjustCommission(id, data) {
  return request({ url: `/channels/${id}/commission`, method: 'patch', data })
}

// 结算单
export function listSettlements(params) {
  return request({ url: '/channel-settlements', method: 'get', params })
}

export function createSettlement(data) {
  return request({ url: '/channel-settlements', method: 'post', data })
}

export function confirmSettlement(id, data) {
  return request({ url: `/channel-settlements/${id}/confirm`, method: 'post', data })
}

export function paySettlement(id, data) {
  return request({ url: `/channel-settlements/${id}/pay`, method: 'post', data })
}
