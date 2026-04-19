import request from '@/utils/request'

export function getOutflowThreshold() {
  return request.get('/config/outflow-threshold')
}

export function getInflowThreshold() {
  return request.get('/config/inflow-threshold')
}

export function setOutflowThreshold(threshold) {
  return request.post('/config/outflow-threshold', null, { params: { threshold } })
}

export function setInflowThreshold(threshold) {
  return request.post('/config/inflow-threshold', null, { params: { threshold } })
}
