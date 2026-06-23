# taotie

前后端分离的外卖管理后台。前端基于 Vue 3 + Element Plus + Vite，后端基于 Spring Cloud 微服务架构。

## 项目结构

```
taotie/
├── taotie-admin/        # 前端管理后台（Vue 3 + Element Plus）
├── taotie-server/       # 后端微服务（Spring Cloud，子模块）
│   ├── taotie-gateway/        # 网关服务
│   ├── taotie-auth-service/   # 认证服务
│   ├── taotie-user-service/   # 用户服务
│   ├── taotie-shop-service/   # 商品/店铺服务
│   └── taotie-order-service/  # 订单服务
└── docs/                # 文档
```

## 技术栈

| 前端 | 后端 |
|------|------|
| Vue 3 | Spring Boot 2.7 |
| TypeScript | Spring Cloud Gateway |
| Vite | Nacos 配置中心 |
| Element Plus | MyBatis + MySQL |
| Pinia | Redis + RabbitMQ |
| ECharts | Sentinel 熔断限流 |

## 快速启动

```bash
# 前端
cd taotie-admin
npm install
npm run dev

# 后端（需先启动 MySQL、Redis、Nacos）
cd taotie-server
mvn install -DskipTests
# 分别启动各微服务

```

