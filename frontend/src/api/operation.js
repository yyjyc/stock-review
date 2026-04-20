import request from '@/utils/request'

export function saveStockOperation(data) {
  return request.post('/api/stock-operation/save', data)
}

export function getStockOperationPage(params) {
  return request.get('/api/stock-operation/page', { params })
}

export function getOperationStatistics() {
  return request.get('/api/stock-operation/statistics')
}

export function deleteStockOperation(id) {
  return request.delete(`/api/stock-operation/${id}`)
}
