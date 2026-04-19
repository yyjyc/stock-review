import request from '@/utils/request'

export function saveStockOperation(data) {
  return request.post('/stock-operation/save', data)
}

export function getStockOperationPage(params) {
  return request.get('/stock-operation/page', { params })
}

export function getOperationStatistics() {
  return request.get('/stock-operation/statistics')
}

export function deleteStockOperation(id) {
  return request.delete(`/stock-operation/${id}`)
}
