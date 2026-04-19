import request from '@/utils/request'

export function getPositionPage(params) {
  return request.get('/position/page', { params })
}

export function getPositionSummary() {
  return request.get('/position/summary')
}

export function refreshPositionPrices() {
  return request.post('/position/refresh-prices')
}

export function getPositionById(id) {
  return request.get(`/position/${id}`)
}

export function initPosition(data) {
  return request.post('/position/init', data)
}

export function addPosition(data) {
  return request.post('/position/add', data)
}

export function reducePosition(data) {
  return request.post('/position/reduce', data)
}

export function clearPosition(data) {
  return request.post('/position/clear', data)
}

export function getAdjustReasonList() {
  return request.get('/adjust-reason/list')
}

export function saveAdjustReason(data) {
  return request.post('/adjust-reason/save', data)
}

export function deleteAdjustReason(id) {
  return request.delete(`/adjust-reason/${id}`)
}

export function savePositionPlan(data) {
  return request.post('/position/plan', data)
}

export function updatePosition(id, data) {
  return request.put(`/position/${id}`, data)
}

export function deletePosition(id) {
  return request.delete(`/position/${id}`)
}

export function closeAlert(id) {
  return request.put(`/position/${id}/alert`)
}
