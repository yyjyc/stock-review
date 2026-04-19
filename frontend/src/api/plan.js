import request from '@/utils/request'

export function getTodayPlans(planStatus) {
  return request.get('/plan/today', { params: { planStatus } })
}

export function getPlansByDateRange(startDate, endDate, planStatus) {
  return request.get('/plan/range', { params: { startDate, endDate, planStatus } })
}

export function executePlan(id, shares, price) {
  return request.post('/plan/execute', null, {
    params: { id, shares, price }
  })
}

export function markAsNotExecuted(id) {
  return request.post('/plan/not-executed', null, {
    params: { id }
  })
}

export function getPlanPage(pageNum, pageSize, startDate, endDate, planStatus, stockName) {
  return request.get('/plan/page', {
    params: { pageNum, pageSize, startDate, endDate, planStatus, stockName }
  })
}
