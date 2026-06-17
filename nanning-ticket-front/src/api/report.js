import request from './request'

// 报表核心指标
export function getReportOverview(params) {
  return request({ url: '/reports/overview', method: 'get', params })
}

// 时间趋势（折线/柱状）interval = DAY | WEEK | MONTH
export function getReportTrend(params) {
  return request({ url: '/reports/trend', method: 'get', params })
}

// 多维排名 groupBy = CHANNEL | SCENIC | TICKET | PAY_METHOD | WINDOW
export function getReportRanking(params) {
  return request({ url: '/reports/ranking', method: 'get', params })
}

// 检票转化漏斗
export function getReportVisitFunnel(params) {
  return request({ url: '/reports/visit-funnel', method: 'get', params })
}

// 库存日报
export function getReportInventory(params) {
  return request({ url: '/reports/inventory', method: 'get', params })
}

// 支付方式分布
export function getReportPayment(params) {
  return request({ url: '/reports/payment', method: 'get', params })
}
