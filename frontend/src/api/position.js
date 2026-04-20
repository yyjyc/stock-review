import request from '@/utils/request'

export function getPositionPage(params) {
  return request.get('/api/position/page', { params })
}

export function getPositionSummary() {
  return request.get('/api/position/summary')
}

export function refreshPositionPrices() {
  return request.post('/api/position/refresh-prices')
}

export function getPositionById(id) {
  return request.get(`/api/position/${id}`)
}

export function initPosition(data) {
  return request.post('/api/position/init', data)
}

export function addPosition(data) {
  return request.post('/api/position/add', data)
}

export function reducePosition(data) {
  return request.post('/api/position/reduce', data)
}

export function clearPosition(data) {
  return request.post('/api/position/clear', data)
}

export function getAdjustReasonList() {
  return request.get('/api/adjust-reason/list')
}

export function saveAdjustReason(data) {
  return request.post('/api/adjust-reason/save', data)
}

export function deleteAdjustReason(id) {
  return request.delete(`/api/adjust-reason/${id}`)
}

export function savePositionPlan(data) {
  return request.post('/api/position/plan', data)
}

export function updatePosition(id, data) {
  return request.put(`/api/position/${id}`, data)
}

export function deletePosition(id) {
  return request.delete(`/api/position/${id}`)
}

export function closeAlert(id) {
  return request.put(`/api/position/${id}/alert`)
}
