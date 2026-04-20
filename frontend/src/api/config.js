import request from '@/utils/request'

export function getOutflowThreshold() {
  return request.get('/api/config/outflow-threshold')
}

export function getInflowThreshold() {
  return request.get('/api/config/inflow-threshold')
}

export function setOutflowThreshold(threshold) {
  return request.post('/api/config/outflow-threshold', null, { params: { threshold } })
}

export function setInflowThreshold(threshold) {
  return request.post('/api/config/inflow-threshold', null, { params: { threshold } })
}
