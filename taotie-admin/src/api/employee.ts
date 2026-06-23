import request from '@/utils/request'

export function getEmployeePage(params: any) {
  return request({ url: '/admin/employee/page', method: 'get', params })
}

export function getEmployeeById(id: number) {
  return request({ url: `/admin/employee/${id}`, method: 'get' })
}

export function addEmployee(data: any) {
  return request({ url: '/admin/employee', method: 'post', data })
}

export function updateEmployee(data: any) {
  return request({ url: '/admin/employee', method: 'put', data })
}

export function setEmployeeStatus(id: number, status: number) {
  return request({ url: `/admin/employee/status/${status}`, method: 'post', params: { id } })
}

export function editPassword(data: { empId: number; oldPassword: string; newPassword: string }) {
  return request({ url: '/admin/employee/editPassword', method: 'put', data })
}
