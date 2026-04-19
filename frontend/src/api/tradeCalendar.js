import request from '@/utils/request'

export function getTradeDateInfo() {
  return request.get('/trade-calendar/trade-date-info')
}

export function getCurrentReviewDate() {
  return request.get('/trade-calendar/current-review-date')
}

export function getNextTradeDay() {
  return request.get('/trade-calendar/next-trade-day')
}
