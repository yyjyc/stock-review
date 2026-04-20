import request from '@/utils/request'

export function saveActiveMarketValue(data) {
  return request.post('/api/active-market-value/save', data)
}

export function getActiveMarketValuePage(params) {
  return request.get('/api/active-market-value/page', { params })
}

export function getLatestActiveMarketValue() {
  return request.get('/api/active-market-value/latest')
}

export function getRecentActiveMarketValue(days) {
  return request.get('/api/active-market-value/recent', { params: { days } })
}

export function deleteActiveMarketValue(id) {
  return request.delete(`/api/active-market-value/${id}`)
}
