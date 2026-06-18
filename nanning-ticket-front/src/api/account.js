import request from './request'

// 账号管理
// GET /api/accounts?keyword=&status=&role=&pageNum=1&pageSize=10
export function listAccounts(params) {
  return request({ url: '/accounts', method: 'get', params })
}

// GET /api/accounts/{id}
export function getAccount(id) {
  return request({ url: `/accounts/${id}`, method: 'get' })
}

// POST /api/accounts
export function createAccount(data) {
  return request({ url: '/accounts', method: 'post', data })
}

// PUT /api/accounts/{id}
export function updateAccount(id, data) {
  return request({ url: `/accounts/${id}`, method: 'put', data })
}

// DELETE /api/accounts/{id}
export function deleteAccount(id) {
  return request({ url: `/accounts/${id}`, method: 'delete' })
}

// PATCH /api/accounts/{id}/status?status=...
export function toggleAccountStatus(id, status) {
  return request({ url: `/accounts/${id}/status`, method: 'patch', params: { status } })
}

// PATCH /api/accounts/{id}/password?newPassword=...
export function resetAccountPassword(id, newPassword) {
  return request({ url: `/accounts/${id}/password`, method: 'patch', params: { newPassword } })
}

// GET /api/accounts/options - 启用的用户下拉
export function listAccountOptions() {
  return request({ url: '/accounts/options', method: 'get' })
}
