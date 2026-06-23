import request from '@/utils/request'

// ---- 分类 ----
export function getCategoryPage(params: any) {
  return request({ url: '/admin/category/page', method: 'get', params })
}
export function addCategory(data: any) {
  return request({ url: '/admin/category', method: 'post', data })
}
export function updateCategory(data: any) {
  return request({ url: '/admin/category', method: 'put', data })
}
export function deleteCategory(id: number) {
  return request({ url: '/admin/category', method: 'delete', params: { id } })
}
export function setCategoryStatus(status: number, id: number) {
  return request({ url: `/admin/category/status/${status}`, method: 'post', params: { id } })
}
export function getCategoryList(type?: number) {
  return request({ url: '/admin/category/list', method: 'get', params: type !== undefined ? { type } : {} })
}

// ---- 菜品 ----
export function getDishPage(params: any) {
  return request({ url: '/admin/dish/page', method: 'get', params })
}
export function addDish(data: any) {
  return request({ url: '/admin/dish', method: 'post', data })
}
export function updateDish(data: any) {
  return request({ url: '/admin/dish', method: 'put', data })
}
export function deleteDish(ids: number[]) {
  return request({ url: '/admin/dish', method: 'delete', params: { ids: ids.join(',') } })
}
export function getDishById(id: number) {
  return request({ url: `/admin/dish/${id}`, method: 'get' })
}
export function setDishStatus(status: number, id: number) {
  return request({ url: `/admin/dish/status/${status}`, method: 'post', params: { id } })
}

// ---- 套餐 ----
export function getSetmealPage(params: any) {
  return request({ url: '/admin/setmeal/page', method: 'get', params })
}
export function addSetmeal(data: any) {
  return request({ url: '/admin/setmeal', method: 'post', data })
}
export function updateSetmeal(data: any) {
  return request({ url: '/admin/setmeal', method: 'put', data })
}
export function deleteSetmeal(ids: number[]) {
  return request({ url: '/admin/setmeal', method: 'delete', params: { ids: ids.join(',') } })
}
export function getSetmealById(id: number) {
  return request({ url: `/admin/setmeal/${id}`, method: 'get' })
}
export function setSetmealStatus(status: number, id: number) {
  return request({ url: `/admin/setmeal/status/${status}`, method: 'post', params: { id } })
}

// ---- 通用 ----
export function uploadFile(file: File) {
  const form = new FormData()
  form.append('file', file)
  return request({ url: '/admin/common/upload', method: 'post', data: form, headers: { 'Content-Type': 'multipart/form-data' } })
}

// ---- 店铺 ----
export function getShopStatus() {
  return request({ url: '/admin/shop/status', method: 'get' })
}
export function setShopStatus(status: number) {
  return request({ url: `/admin/shop/${status}`, method: 'put' })
}
