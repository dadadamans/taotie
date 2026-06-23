import request from '@/utils/request'

// ---- 订单 ----
export function getOrderPage(params: any) {
  return request({ url: '/admin/order/conditionSearch', method: 'get', params })
}
export function getOrderDetail(id: number) {
  return request({ url: `/admin/order/details/${id}`, method: 'get' })
}
export function getOrderStatistics() {
  return request({ url: '/admin/order/statistics', method: 'get' })
}
export function confirmOrder(data: any) {
  return request({ url: '/admin/order/confirm', method: 'put', data })
}
export function rejectOrder(data: any) {
  return request({ url: '/admin/order/rejection', method: 'put', data })
}
export function cancelOrder(data: any) {
  return request({ url: '/admin/order/cancel', method: 'put', data })
}
export function deliveryOrder(id: number) {
  return request({ url: `/admin/order/delivery/${id}`, method: 'put' })
}
export function completeOrder(id: number) {
  return request({ url: `/admin/order/complete/${id}`, method: 'put' })
}

// ---- 工作台 ----
export function getBusinessData() {
  return request({ url: '/admin/workspace/businessData', method: 'get' })
}
export function getOverviewOrders() {
  return request({ url: '/admin/workspace/overviewOrders', method: 'get' })
}
export function getOverviewDishes() {
  return request({ url: '/admin/workspace/overviewDishes', method: 'get' })
}
export function getOverviewSetmeals() {
  return request({ url: '/admin/workspace/overviewSetmeals', method: 'get' })
}

// ---- 报表 ----
export function getTurnoverStatistics(params: any) {
  return request({ url: '/admin/report/turnoverStatistics', method: 'get', params })
}
export function getUserStatistics(params: any) {
  return request({ url: '/admin/report/userStatistics', method: 'get', params })
}
export function getOrderStatisticsReport(params: any) {
  return request({ url: '/admin/report/ordersStatistics', method: 'get', params })
}
export function getTop10(params: any) {
  return request({ url: '/admin/report/top10', method: 'get', params })
}
export function exportReport() {
  return request({ url: '/admin/report/export', method: 'get', responseType: 'blob' })
}
