import request from './request'

// POST /api/auth/login
// 公开接口；返回 { token, tokenExpireAt, user, scenicIdList, roleList }
export function login(data) {
  return request({ url: '/auth/login', method: 'post', data, silence: true })
}

// POST /api/auth/logout
export function logout() {
  return request({ url: '/auth/logout', method: 'post' })
}

// GET /api/auth/me
export function fetchMe() {
  return request({ url: '/auth/me', method: 'get' })
}

// PUT /api/auth/password
export function changePassword(data) {
  return request({ url: '/auth/password', method: 'put', data })
}
