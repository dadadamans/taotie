# 饕餮外卖管理后台 — 实施方案

> 🟢 **当前进度：第一 ~ 九步已完成，下一步 → 第十步：打磨与收尾**

## 项目结构总览

```
taotie-admin/
├── public/
│   └── favicon.svg                    # 饕餮纹 Logo
├── src/
│   ├── api/                          # 后端 API 调用层
│   │   ├── auth.ts                   #   登录/退出
│   │   ├── employee.ts               #   员工管理
│   │   ├── shop.ts                   #   菜品/套餐/分类/上传
│   │   └── order.ts                  #   订单/工作台/报表
│   ├── assets/
│   │   ├── logo/                     # Logo 图片资源
│   │   └── images/                   # 其他页面图片
│   ├── layouts/
│   │   └── MainLayout.vue            # 主布局（侧边栏 + TopBar + 内容区）
│   ├── router/
│   │   └── index.ts                  # 路由配置
│   ├── stores/
│   │   ├── app.ts                    # 应用级状态（侧边栏折叠等）
│   │   └── user.ts                   # 用户认证状态
│   ├── styles/
│   │   ├── _variables.scss            # 全局 SCSS 变量（品牌色、间距、圆角）
│   │   ├── reset.scss                # 样式重置
│   │   └── theme.scss                # Element Plus 主题覆盖
│   ├── utils/
│   │   ├── request.ts                # axios 封装（统一拦截器、Token 注入、错误处理）
│   │   └── auth.ts                   # Token 存取工具
│   ├── views/
│   │   ├── login/index.vue           # 登录页
│   │   ├── dashboard/index.vue       # 工作台
│   │   ├── order/
│   │   │   ├── index.vue             # 订单列表
│   │   │   └── detail.vue            # 订单详情
│   │   ├── dish/
│   │   │   ├── index.vue             # 菜品列表
│   │   │   └── form.vue              # 新增/编辑菜品
│   │   ├── setmeal/
│   │   │   ├── index.vue             # 套餐列表
│   │   │   └── form.vue              # 新增/编辑套餐
│   │   ├── category/index.vue        # 分类管理
│   │   ├── employee/
│   │   │   ├── index.vue             # 员工列表
│   │   │   └── form.vue              # 新增/编辑员工
│   │   └── statistics/index.vue      # 数据统计
│   ├── App.vue
│   └── main.ts
├── index.html
├── vite.config.ts
├── tsconfig.json / tsconfig.app.json
└── package.json
```

---

## 第一步：基础设施 & 主题系统

> **目标**：项目能正常启动，全局紫色主题生效，布局骨架可见
> **验证**：`npm run dev` 打开浏览器，看到紫色主题的登录页

### 1.1 项目配置
- [x] Vite + Vue 3 + TypeScript 脚手架创建完毕
- [x] 依赖安装：Element Plus、Pinia、Vue Router 4、axios、ECharts、Sass
- [x] `vite.config.ts`：配置 `@` 路径别名、Element Plus 自动导入、代理转发 `/admin` → `localhost:8080`
- [x] `index.html`：标题改为"饕餮外卖管理后台"，`lang="zh-CN"`

### 1.2 主题系统
- [x] `src/styles/_variables.scss`：定义饕餮紫 `#6D28D9`、琥珀金 `#F59E0B`、玄黑 `#0C0A1D` 等品牌色
- [x] `src/styles/reset.scss`：全局样式重置，引入字体栈
- [x] `src/main.ts`：集成 Element Plus + Pinia + Router，全局注册 Element Plus 图标

### 1.3 Logo 与 Favicon
- [x] `public/favicon.svg`：饕餮纹简化 SVG Logo（紫底金纹）
- [ ] 后续可替换为设计团队产出的正式 Logo 图片

### 1.4 路由骨架
- [x] `src/router/index.ts`：登录页 + 7 个业务模块路由，含 beforeEach 登录守卫
- [ ] 后续可根据需要添加权限判断逻辑

---

## 第二步：布局与登录

> **目标**：登录页能认证跳转，布局侧边栏 + TopBar 完整呈现
> **验证**：输入账号密码 → 跳转到工作台 → 看到深色侧边栏 + 紫色 TopBar

### 2.1 登录页
- [x] 深色渐变背景 + 毛玻璃登录卡片
- [x] 居中 Logo 图片 + "饕餮外卖" + "管理后台" 标题
- [x] 账号/密码输入框 + 登录按钮（带 loading）
- [x] 调通 `POST /admin/employee/login` 接口 → 存储 Token

### 2.2 主流布局
- [x] `layouts/MainLayout.vue`
  - [x] 左侧深色侧边栏（Logo + 导航菜单）
  - [x] 顶部白色 TopBar（折叠按钮 + 面包屑 + 用户头像下拉）
  - [x] 中间内容区（router-view）
- [x] 侧边栏折叠/展开功能（通过 `useAppStore`）
- [x] 退出登录功能

### 2.3 请求封装
- [x] `utils/request.ts`：axios 拦截器
  - [x] 请求拦截：自动注入 Token 到 header
  - [x] 响应拦截：401 自动跳转登录页、统一错误提示

### 2.4 API 层
- [x] `api/auth.ts`：login / logout
- [x] `api/employee.ts`：员工 CRUD
- [x] `api/shop.ts`：分类 / 菜品 / 套餐 / 文件上传
- [x] `api/order.ts`：订单 / 工作台 / 报表

---

## 第三步：员工管理（第一个完整 CRUD）

> **目标**：跑通完整的"列表 → 新增 → 编辑 → 启用/禁用 → 删除"流程
> **验证**：员工页面可以增删改查，状态开关可用

### 3.1 员工列表
- [x] `views/employee/index.vue`
  - [x] 搜索栏：用户名搜索 + 查询按钮 + 新增按钮
  - [x] Element Plus 表格：账号、姓名、手机号、状态、操作列（无删除按钮，后端无删除接口）
  - [x] 分页器
  - [x] 状态列：Switch 开关（启用/禁用），调 `POST /admin/employee/status/{status}`
  - [x] 操作列：编辑按钮
  - [x] 调 `GET /admin/employee/page` 获取列表数据

### 3.2 新增/编辑员工
- [x] `views/employee/form.vue`
  - [x] 表单：用户名（编辑时禁用）、姓名、手机号、性别、身份证号
  - [x] 新增调 `POST /admin/employee`，编辑调 `PUT /admin/employee`
  - [x] 编辑时通过路由参数 `id` 调 `GET /admin/employee/{id}` 回显

---

## 第四步：分类管理

> **目标**：菜品分类 + 套餐分类的管理页面
> **验证**：可以分别查看菜品分类和套餐分类，并执行增删改

### 4.1 分类列表
- [x] `views/category/index.vue`
  - [x] Tab 切换：菜品分类 / 套餐分类（`type` 参数）
  - [x] 表格：分类名称、类型、排序、状态、操作
  - [x] 状态 Switch 开关
  - [x] 新增/编辑弹窗（Dialog）
  - [x] 删除确认

### 4.2 接口对接
- [x] `GET /admin/category/page` 分页查询
- [x] `POST /admin/category` 新增
- [x] `PUT /admin/category` 修改
- [x] `DELETE /admin/category` 删除（传 `id`）
- [x] `POST /admin/category/status/{status}` 启用/禁用

---

## 第五步：菜品管理

> **目标**：菜品列表 + 增删改 + 图片上传 + 停售起售
> **验证**：能上传菜品图片，新增菜品后出现在列表，可切换状态

### 5.1 菜品列表
- [x] `views/dish/index.vue`
  - [x] 筛选栏：分类下拉（调 `category/list?type=1`）、菜品名称搜索、状态筛选
  - [x] 表格：图片缩略图、菜品名称、分类（DishVO.categoryName）、价格、状态（Switch）、操作
  - [x] 批量删除（多选 + 确认弹窗）
  - [x] 起售/停售（Switch + 操作列文字按钮）

### 5.2 新增/编辑菜品
- [x] `views/dish/form.vue`
  - [x] 表单：菜品名称、分类（el-select）、价格（el-input-number）、图片上传（el-upload + axios）、描述
  - [x] 口味做法：动态增删行，每行包含口味名 + 标签输入（Enter 添加，✕ 删除）
  - [x] 编辑时通过 `GET /admin/dish/{id}` 回显（含 flavors 解析）
  - [x] 图片上传调 `POST /admin/common/upload`，回显缩略图

### 5.3 接口对接
- [ ] `GET /admin/dish/page` 分页
- [ ] `POST /admin/dish` 新增
- [ ] `PUT /admin/dish` 修改
- [ ] `DELETE /admin/dish` 批量删除（传 `ids`）
- [ ] `POST /admin/dish/status/{status}` 起售/停售
- [ ] `POST /admin/common/upload` 图片上传

---

## 第六步：套餐管理

> **目标**：套餐列表 + 增删改 + 包含菜品选择
> **验证**：能创建包含多个菜品的套餐

### 6.1 套餐列表
- [x] `views/setmeal/index.vue`
  - [x] 表格：套餐名称、分类、价格、状态、操作
  - [x] 状态 Switch
  - [x] 起售/停售（Switch + 操作列文字按钮）
  - [x] 分类下拉筛选（category/list?type=2）、名称搜索、状态筛选
  - [x] 多选批量删除

### 6.2 新增/编辑套餐
- [x] `views/setmeal/form.vue`
  - [x] 表单：套餐名称、分类（el-select）、价格（el-input-number）、图片上传（el-upload）、描述
  - [x] 关联菜品选择器 Dialog：加载全部菜品（getDishPage），多选，去重添加
  - [x] 套餐菜品表格：名称、原价、份数（el-input-number）、小计、删除
  - [x] 编辑时通过 `GET /admin/setmeal/{id}` 回显（含 setmealDishes）

### 6.3 接口对接
- [x] `GET /admin/setmeal/page` 分页
- [x] `POST /admin/setmeal` 新增
- [x] `PUT /admin/setmeal` 修改
- [ ] `DELETE /admin/setmeal` 批量删除
- [ ] `POST /admin/setmeal/status/{status}` 起售/停售

---

## 第七步：订单管理

> **目标**：订单列表 + 详情 + 接单/拒单/派送/完成 全流程操作
> **验证**：查看订单列表，点击详情查看完整信息，执行各状态操作

### 7.1 订单列表
- [x] `views/order/index.vue`
  - [x] Tab 状态切换（全部/待付款/待接单/已接单/派送中/已完成/已取消），带 badge 统计数
  - [x] 搜索栏：订单号、手机号、日期范围
  - [x] 表格：订单号、订单菜品、金额、状态（彩色 Tag）、收货人、手机号、下单时间、操作（详情）
  - [x] 状态 Tag：待付款(琥珀)、待接单(紫)、已接单(蓝)、派送中(浅蓝)、已完成(翠绿)、已取消(朱红)

### 7.2 订单详情
- [x] `views/order/detail.vue`
  - [x] 订单信息卡片（el-descriptions）
  - [x] 收货信息卡片
  - [x] 商品明细表格（含图片、名称、口味、单价、数量、小计）
  - [x] 操作按钮区：根据状态动态显示（待接单→接单/拒单/取消，已接单→派送/取消，派送中→完成，待付款→取消）
  - [x] 拒单/取消：弹出 Dialog 填写原因（必填，200字限制）

### 7.3 接口对接
- [x] `GET /admin/order/conditionSearch` 条件搜索
- [x] `GET /admin/order/details/{id}` 详情
- [ ] `GET /admin/order/statistics` 各状态数量
- [ ] `PUT /admin/order/confirm` 接单
- [ ] `PUT /admin/order/rejection` 拒单
- [ ] `PUT /admin/order/cancel` 取消
- [ ] `PUT /admin/order/delivery/{id}` 派送
- [ ] `PUT /admin/order/complete/{id}` 完成

---

## 第八步：工作台 Dashboard

> **目标**：登录后首页展示营业数据概览
> **验证**：进入工作台看到 4 个数据卡片 + 订单概览

### 8.1 工作台布局
- [x] `views/dashboard/index.vue`
  - [x] 顶部问候语（时间感知）+ 用户名
  - [x] 4 个统计卡片：营业额(琥珀)、有效订单(紫)、客单价(蓝)、新增用户(翠绿)
  - [x] 订单管理卡片：待接单/待派送/已完成/已取消/全部
  - [x] 菜品总览 + 套餐总览卡片（起售数/停售数）
  - [x] 订单完成率 el-progress 进度条
  - [x] 4 API 并行请求（Promise.all）

### 8.2 数据可视化（可选）
- [ ] 近期营业额趋势折线图（ECharts）
- [ ] 订单状态占比饼图

---

## 第九步：数据统计

> **目标**：多维度报表页面，支持日期范围选择
> **验证**：选择日期范围，查看营业额/用户/订单/Top10 图表

### 9.1 统计页面
- [x] `views/statistics/index.vue`
  - [x] 日期范围选择器（默认近 7 天）
  - [x] ECharts 面积折线图：营业额趋势（紫色渐变填充）
  - [x] ECharts 混合图：用户统计（折线=总用户 + 柱状=新增用户）
  - [x] 订单统计摘要卡片（总数/有效数/完成率）
  - [x] ECharts 横向柱状图：销量排名 TOP10（琥珀色渐变）
  - [x] 导出报表：调 `GET /admin/report/export` 下载 Excel
  - [x] 窗口 resize 自适应 + 组件销毁清理

### 9.2 接口对接
- [ ] `GET /admin/report/turnoverStatistics?begin=&end=`
- [ ] `GET /admin/report/userStatistics?begin=&end=`
- [ ] `GET /admin/report/ordersStatistics?begin=&end=`
- [ ] `GET /admin/report/top10?begin=&end=`

---

## 第十步：打磨与收尾

> **目标**：提升用户体验，修复细节

### 10.1 交互优化
- [ ] 全局加载状态（全屏 loading 或骨架屏）
- [ ] 操作成功/失败的消息提示
- [ ] 删除确认弹窗
- [ ] 表单校验完善
- [ ] 空数据展示（饕餮纹插画占位图）

### 10.2 主题细节
- [ ] 滚动条样式美化
- [ ] 表格表头紫色主题
- [ ] 过渡动画（页面切换、弹窗、菜单展开）
- [ ] 响应式适配：小屏幕下侧边栏自动折叠

### 10.3 构建与部署
- [ ] 环境变量配置（开发/生产）
- [ ] 构建优化（代码分割、按需加载）
- [ ] Nginx 部署配置示例

---

## 附录：后端微服务 API 一览

```
GateWay (localhost:8080)
├── /admin/employee/login     POST   → auth-service   登录
├── /admin/employee/logout    POST   → auth-service   退出
├── /admin/employee/**        GET/POST/PUT → user-service   员工 CRUD
├── /admin/category/**        GET/POST/PUT/DELETE → shop-service   分类
├── /admin/dish/**            GET/POST/PUT/DELETE → shop-service   菜品
├── /admin/setmeal/**         GET/POST/PUT/DELETE → shop-service   套餐
├── /admin/common/upload      POST   → shop-service   文件上传
├── /admin/order/**           GET/PUT → order-service   订单
├── /admin/workspace/**       GET    → order-service   工作台
├── /admin/report/**          GET    → order-service   报表
└── /user/**                  → user-service / auth-service   C端接口（暂不涉及）
```

## 附录：开发命令

```bash
# 启动开发服务器
npm run dev

# 构建生产包
npm run build

# 预览构建产物
npm run preview
```
