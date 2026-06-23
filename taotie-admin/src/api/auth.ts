import request from '@/utils/request'

export function login(data: { username: string; password: string }) {
  return request({
    url: '/admin/employee/login',
    method: 'post',
    data,
  })
}

export function logout() {
  return request({
    url: '/admin/employee/logout',
    method: 'post',
  })
}
