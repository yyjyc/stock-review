import request from '@/utils/request'

export function saveActiveMarketValue(data) {
  return request.post('/active-market-value/save', data)
}

export function getActiveMarketValuePage(params) {
  return request.get('/active-market-value/page', { params })
}

export function getLatestActiveMarketValue() {
  return request.get('/active-market-value/latest')
}

export function getRecentActiveMarketValue(days) {
  return request.get('/active-market-value/recent', { params: { days } })
}

export function deleteActiveMarketValue(id) {
  return request.delete(`/active-market-value/${id}`)
}
