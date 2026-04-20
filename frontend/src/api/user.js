import request from '@/utils/request'

export function getUserList(params) {
  return request({ url: '/api/users', method: 'get', params })
}

export function createUser(data) {
  return request({ url: '/api/users', method: 'post', data })
}

export function updateUserStatus(id, status) {
  return request({ url: `/api/users/${id}/status`, method: 'put', params: { status } })
}

export function resetPassword(id, newPassword) {
  return request({ url: `/api/users/${id}/password`, method: 'put', params: { newPassword } })
}
