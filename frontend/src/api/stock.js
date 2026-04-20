import request from '@/utils/request'

export function searchStock(keyword) {
  return request.get('/api/stock/search', { params: { keyword } })
}

export function searchStockLocal(keyword) {
  return request.get('/api/stock/search-local', { params: { keyword } })
}

export function getStockPrice(stockCode) {
  return request.get(`/api/stock/price/${stockCode}`)
}

export function batchGetStockPrice(stockCodes) {
  return request.post('/api/stock/batch-price', stockCodes)
}

export function preloadStockInfo() {
  return request.post('/api/stock/preload')
}

export function getPreloadStatus() {
  return request.get('/api/stock/preload/status')
}
