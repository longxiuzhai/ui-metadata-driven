# 客户详情页：卡片级元数据驱动 MVP

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

访问 `http://localhost:5173/?customerId=1001`。Vite 会把 `/api` 代理到 `localhost:8080`。

## 验证过滤

默认请求头会开放全部演示能力。可直接请求并改变请求上下文：

```bash
curl 'http://localhost:8080/api/ui/customer-detail/cards?customerId=1001' \
  -H 'X-Permissions: customer:read' \
  -H 'X-Features: customerTags'
```

此时只返回基本信息卡，且因为没有 `customer:update`，编辑动作也会被移除。

关闭行为轨迹 Provider：

```bash
mvn spring-boot:run -Dspring-boot.run.arguments=--modules.behavior-trace.enabled=false
```

## 后端模块边界

```text
card-platform                  通用卡片协议、Condition、校验器，不含客户业务
customer-detail-contract       客户详情 SPI 接口与上下文，供插件编译依赖
customer-detail-feature        客户详情聚合服务、元数据接口、基础信息卡
plugins/customer-tags-plugin   标签 Provider + 标签数据接口（独立 JAR）
plugins/behavior-trace-plugin  轨迹 Provider + 轨迹数据接口（独立 JAR）
application                    Spring Boot 启动与插件依赖装配
```

插件只依赖 `customer-detail-contract`，不会依赖主应用或聚合实现。每个插件通过 `META-INF/spring.factories` 暴露自己的 AutoConfiguration，因此即使第三方插件使用不同的 Java 根包，也不依赖主应用的包扫描。要验证物理移除插件，可删除 `application/pom.xml` 中对应插件依赖后重新启动；其 Provider 和数据接口会同时退出应用上下文。

## 新增卡片

1. 实现 `CustomerDetailCardProvider` 并注册为 Spring Bean；
2. 按需添加 `@ConditionalOnProperty` 等启动期条件和 `@CardConditional` 请求期条件；
3. 复用前端注册表已有组件时，无需修改客户详情页；
4. 新 UI 形态必须先加入前端 `cardRegistry` 与后端组件白名单。

后端不会信任元数据做真实鉴权。示例请求头只用于演示上下文，生产环境应由 Spring Security 的认证信息构造 `CardContext`，数据接口仍需逐个鉴权。

更完整的 Provider 模板、三层 Condition 职责和生产接入检查项见 [`docs/扩展卡片开发指南.md`](docs/扩展卡片开发指南.md)。客户详情协议 Schema 位于 `backend/customer-detail-contract/src/main/resources/schema/customer-detail-card-v1.schema.json`。
