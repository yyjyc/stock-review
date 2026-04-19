import request from '@/utils/request'

export function saveStockSelection(data) {
  return request.post('/stock-selection/save', data)
}

export function getStockSelectionPage(params) {
  return request.get('/stock-selection/page', { params })
}

export function deleteStockSelection(id) {
  return request.delete(`/stock-selection/${id}`)
}

export function markAsOperated(id) {
  return request.post(`/stock-selection/mark-operated/${id}`)
}

export function refreshSelectionPrices() {
  return request.post('/stock-selection/refresh-prices')
}

export function saveStockExperience(data) {
  return request.post('/stock-experience/save', data)
}

export function getStockExperiencePage(params) {
  return request.get('/stock-experience/page', { params })
}

export function getStockExperienceList() {
  return request.get('/stock-experience/list')
}

export function deleteStockExperience(id) {
  return request.delete(`/stock-experience/${id}`)
}

export function getSelectionReasonList() {
  return request.get('/selection-reason/list')
}

export function saveSelectionReason(data) {
  return request.post('/selection-reason/save', data)
}

export function deleteSelectionReason(id) {
  return request.delete(`/selection-reason/${id}`)
}
