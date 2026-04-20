import request from '@/utils/request'

export function getTradeDateInfo() {
  return request.get('/api/trade-calendar/trade-date-info')
}

export function getCurrentReviewDate() {
  return request.get('/api/trade-calendar/current-review-date')
}

export function getNextTradeDay() {
  return request.get('/api/trade-calendar/next-trade-day')
}
