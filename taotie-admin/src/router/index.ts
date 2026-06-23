import { createRouter, createWebHistory } from 'vue-router'
import type { RouteRecordRaw } from 'vue-router'

const routes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', hidden: true, noAuth: true },
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '工作台', icon: 'Odometer' },
      },
      {
        path: 'order',
        name: 'Order',
        component: () => import('@/views/order/index.vue'),
        meta: { title: '订单管理', icon: 'List' },
      },
      {
        path: 'order/detail/:id',
        name: 'OrderDetail',
        component: () => import('@/views/order/detail.vue'),
        meta: { title: '订单详情', hidden: true },
      },
      {
        path: 'dish',
        name: 'Dish',
        component: () => import('@/views/dish/index.vue'),
        meta: { title: '菜品管理', icon: 'ColdDrink' },
      },
      {
        path: 'dish/form',
        name: 'DishForm',
        component: () => import('@/views/dish/form.vue'),
        meta: { title: '新增/编辑菜品', hidden: true },
      },
      {
        path: 'setmeal',
        name: 'Setmeal',
        component: () => import('@/views/setmeal/index.vue'),
        meta: { title: '套餐管理', icon: 'Present' },
      },
      {
        path: 'setmeal/form',
        name: 'SetmealForm',
        component: () => import('@/views/setmeal/form.vue'),
        meta: { title: '新增/编辑套餐', hidden: true },
      },
      {
        path: 'category',
        name: 'Category',
        component: () => import('@/views/category/index.vue'),
        meta: { title: '分类管理', icon: 'CollectionTag' },
      },
      {
        path: 'employee',
        name: 'Employee',
        component: () => import('@/views/employee/index.vue'),
        meta: { title: '员工管理', icon: 'User' },
      },
      {
        path: 'employee/form',
        name: 'EmployeeForm',
        component: () => import('@/views/employee/form.vue'),
        meta: { title: '新增/编辑员工', hidden: true },
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/statistics/index.vue'),
        meta: { title: '数据统计', icon: 'DataAnalysis' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard',
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
})

router.beforeEach((to, _from, next) => {
  const token = localStorage.getItem('token')
  if (to.meta.noAuth) {
    next()
  } else if (!token) {
    next('/login')
  } else {
    next()
  }
})

export default router
