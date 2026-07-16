# 卡片级元数据驱动 MVP

这是“卡片级元数据驱动 UI 精简方案”的可运行验证工程，后端为 Spring Boot 2.7 + Java 11，前端为 Vue 3 + TypeScript。

## 架构取舍

```text
Spring Bean 自动发现 Provider
  → 启动期 Condition（模块/JAR 是否存在、配置开关）
  → 请求期 @CardConditional（权限、Feature Flag、租户）
  → Provider.supports（复杂业务条件）
  → 强类型 Definition + 白名单校验
  → 排序并返回元数据
  → Vue Registry 解析组件，卡片独立加载/失败/懒加载
```

这里的 SPI 是 Spring 容器扩展点，不使用 JDK `ServiceLoader`：业务模块只需引入依赖并声明 Provider Bean，即可同时获得依赖注入、AOP 与 Spring Boot Condition 能力。若未来要求运行时装卸外部 JAR，再单独引入插件 ClassLoader，MVP 阶段不承担这部分复杂度。

## 启动

需要 Java 11+、Maven、Node.js。

```bash
cd backend
mvn install
cd application
mvn spring-boot:run
```

```bash
cd frontend
npm install
npm run dev
```

访问首页 `http://localhost:5173/`，或客户详情页 `http://localhost:5173/customer-detail?customerId=1001`。Vite 会把 `/api` 代理到 `localhost:8080`。

## 验证过滤

默认请求头会开放全部演示能力。可直接请求并改变请求上下文：

```bash
curl 'http://localhost:8080/api/ui/pages/customer_detail/cards?customerId=1001' \
  -H 'X-Permissions: customer:read' \
  -H 'X-Features: customerTags'
```

此时只返回基本信息卡，且因为没有 `customer:update`，编辑动作也会被移除。

关闭行为轨迹 Provider：

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--metadata.cards.behavior-trace.enabled=false
```

## 后端模块边界

```text
card-platform                  通用卡片协议、Condition、校验器，不含客户业务
tenant-customization           客户详情、首页等租户定制 Provider 与数据接口
application                    统一接口、账号身份、权限上下文和 Spring Boot 启动
```

定制模块只依赖 `card-platform`，不会依赖核心应用。它通过 `META-INF/spring.factories` 暴露 AutoConfiguration，因此不依赖主应用包扫描。移除 `application/pom.xml` 中的 `tenant-customization` 依赖后，定制 Provider 和数据接口会同时退出应用上下文。

所有页面共用 `GET /api/ui/pages/{pageCode}/cards`；原客户详情和首页地址作为兼容入口保留。演示账号可通过 `GET /api/accounts/me` 查看。

## 新增卡片

1. 在 `tenant-customization` 实现通用 `CardProvider`，声明 `pageCode()` 和 `cardCode()`；
2. 按需添加 `@ConditionalOnProperty` 等启动期条件和 `@CardConditional` 请求期条件；
3. 复用前端注册表已有组件时，无需修改客户详情页；
4. 新 UI 形态必须先加入前端 `cardRegistry` 与后端组件白名单。

后端不会信任元数据做真实鉴权。示例请求头只用于演示上下文，生产环境应由 Spring Security 的认证信息构造 `CardRequestContext`，数据接口仍需逐个鉴权。

更完整的 Provider 模板、三层 Condition 职责和生产接入检查项见 [`docs/扩展卡片开发指南.md`](docs/扩展卡片开发指南.md)。
