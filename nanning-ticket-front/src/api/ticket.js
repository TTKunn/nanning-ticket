import request from './request'

// 票种分页：GET /api/tickets?scenicId=&category=&status=&pageNum=&pageSize=
export function listTickets(params) {
  return request({ url: '/tickets', method: 'get', params })
}

export function listTicketOptions(params) {
  return request({ url: '/tickets/options', method: 'get', params })
}

export function createTicket(data) {
  return request({ url: '/tickets', method: 'post', data })
}

export function updateTicket(id, data) {
  return request({ url: `/tickets/${id}`, method: 'put', data })
}

export function toggleTicketStatus(id, status) {
  return request({ url: `/tickets/${id}/status`, method: 'patch', params: { status } })
}
