import request from '@/utils/request'

export function saveStockSelection(data) {
  return request.post('/api/stock-selection/save', data)
}

export function getStockSelectionPage(params) {
  return request.get('/api/stock-selection/page', { params })
}

export function deleteStockSelection(id) {
  return request.delete(`/api/stock-selection/${id}`)
}

export function markAsOperated(id) {
  return request.post(`/api/stock-selection/mark-operated/${id}`)
}

export function refreshSelectionPrices() {
  return request.post('/api/stock-selection/refresh-prices')
}

export function saveStockExperience(data) {
  return request.post('/api/stock-experience/save', data)
}

export function getStockExperiencePage(params) {
  return request.get('/api/stock-experience/page', { params })
}

export function getStockExperienceList() {
  return request.get('/api/stock-experience/list')
}

export function deleteStockExperience(id) {
  return request.delete(`/api/stock-experience/${id}`)
}

export function getSelectionReasonList() {
  return request.get('/api/selection-reason/list')
}

export function saveSelectionReason(data) {
  return request.post('/api/selection-reason/save', data)
}

export function deleteSelectionReason(id) {
  return request.delete(`/api/selection-reason/${id}`)
}
