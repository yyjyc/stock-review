import request from '@/utils/request'

export function getTodayPlans(planStatus) {
  return request.get('/api/plan/today', { params: { planStatus } })
}

export function getPlansByDateRange(startDate, endDate, planStatus) {
  return request.get('/api/plan/range', { params: { startDate, endDate, planStatus } })
}

export function executePlan(id, shares, price) {
  return request.post('/api/plan/execute', null, {
    params: { id, shares, price }
  })
}

export function markAsNotExecuted(id) {
  return request.post('/api/plan/not-executed', null, {
    params: { id }
  })
}

export function getPlanPage(pageNum, pageSize, startDate, endDate, planStatus, stockName) {
  return request.get('/api/plan/page', {
    params: { pageNum, pageSize, startDate, endDate, planStatus, stockName }
  })
}
