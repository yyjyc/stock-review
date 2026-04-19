import request from '@/utils/request'

export function searchStock(keyword) {
  return request.get('/stock/search', { params: { keyword } })
}

export function searchStockLocal(keyword) {
  return request.get('/stock/search-local', { params: { keyword } })
}

export function getStockPrice(stockCode) {
  return request.get(`/stock/price/${stockCode}`)
}

export function batchGetStockPrice(stockCodes) {
  return request.post('/stock/batch-price', stockCodes)
}

export function preloadStockInfo() {
  return request.post('/stock/preload')
}

export function getPreloadStatus() {
  return request.get('/stock/preload/status')
}
