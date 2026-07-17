# API 参考

本文只记录当前源码中实际启用的接口。默认后端地址为 `http://localhost:8080`；前端开发环境通过 Vite 代理使用相对路径 `/api/...`。

## 1. 认证与测试请求头

除注册和登录外，接口需要：

```http
Authorization: Bearer <token>
```

Token 由 `POST /api/auth/login` 或 `POST /api/auth/register` 返回。

| 请求头 | 默认值 | 说明 |
|---|---|---|
| `X-Tenant-Id` | `demo` | 演示租户 |
| `X-User-Id` | `user-1` | 演示用户 |
| `X-Permissions` | 当前账号权限 | 仅测试配置可覆盖；逗号分隔 |
| `X-Features` | 当前账号 Feature | 仅测试配置可覆盖；逗号分隔 |

生产配置忽略这些身份和权限覆盖头。动作 API 不读取 `X-Features`。

### 注册与登录

```http
POST /api/auth/register
POST /api/auth/login
POST /api/auth/logout
```

登录请求：

```json
{ "tenantId": "demo", "username": "admin", "password": "Admin123!" }
```

响应包含 `token` 和 `account`。注册请求包含 `username/password/displayName/email`，注册成功自动绑定 `USER` 角色。

## 2. 获取当前账号

```http
GET /api/accounts/me
```

以下示例中的 `<token>` 均替换为登录响应中的 Token。获取当前账号：

```bash
curl 'http://localhost:8080/api/accounts/me' \
  -H 'Authorization: Bearer <token>'
```

响应包含 `tenantId`、`userId`、`permissions` 和 `features`。

## 3. 获取页面卡片元数据

```http
GET /api/ui/pages/{pageCode}/cards
```

查询参数会原样进入页面上下文，供 Provider 判断和 `dataApi` 占位符替换。当前页面：

| `pageCode` | 典型查询参数 | 默认卡片 |
|---|---|---|
| `home` | 无 | `work_summary`、`recent_activity` |
| `customer_detail` | `customerId` | `basic_info`、`friend_tags`、`behavior_trace` |
| `customer_orders` | `customerId` | `customer_order_list` |

示例：

```bash
curl 'http://localhost:8080/api/ui/pages/customer_detail/cards?customerId=1001' \
  -H 'Authorization: Bearer <token>'
```

精简响应结构：

```json
{
  "version": "1.0",
  "pageCode": "customer_detail",
  "layout": { "type": "grid", "columns": 24, "gap": 16 },
  "cards": [
    {
      "code": "basic_info",
      "title": "基本信息",
      "component": "KeyValueCard",
      "order": 10,
      "span": { "xs": 24, "md": 24, "xl": 12 },
      "dataApi": "/api/customers/{customerId}/basic",
      "loadStrategy": "eager",
      "props": {},
      "actions": []
    }
  ]
}
```

测试环境按权限和 Feature 覆盖调试：

```bash
curl 'http://localhost:8080/api/ui/pages/customer_detail/cards?customerId=1001' \
  -H 'Authorization: Bearer <token>' \
  -H 'X-Permissions: customer:read' \
  -H 'X-Features: customerTags'
```

旧路径 `/api/ui/customer-detail/cards` 和 `/api/ui/home/cards` 当前未启用，请勿用于新代码。

## 4. 卡片数据接口

这些接口属于演示定制模块，不是平台统一协议：

| 方法与路径 | 用途 |
|---|---|
| `GET /api/customers/{id}/basic` | 客户基本信息 |
| `GET /api/customers/{id}/tags` | 客户标签 |
| `GET /api/customers/{id}/traces` | 客户行为轨迹 |
| `GET /api/customers/{id}/orders` | 指定客户的订单列表，包含会员 ID |
| `GET /api/orders?customerId={id}&status={status}` | 按客户与可选状态查询订单 |
| `GET /api/orders/{orderNo}` | 订单详情 |
| `GET /api/members/{memberId}` | 会员详情，包含会员 ID、UnionID、手机号 |
| `GET /api/home/{userId}/work-summary` | 首页工作摘要 |
| `GET /api/home/{userId}/activities` | 首页最近动态 |

客户域接口由 MyBatis-Plus 查询 `V2__create_customer_domain_schema.sql` 创建的业务表。前端只接受以 `/api/` 开头的数据地址，并使用页面上下文替换 `{customerId}`、`{userId}` 等占位符。未替换的占位符会导致卡片加载失败。

客户订单页面元数据：

```bash
curl 'http://localhost:8080/api/ui/pages/customer_orders/cards?customerId=1001' \
  -H 'Authorization: Bearer <token>'
```

订单列表除订单号、客户 ID、会员 ID、金额、状态和下单时间外，还返回关联会员的 UnionID、手机号、等级、状态，以及订单创建和更新时间。

客户基本信息示例：

```json
{
  "customerId": "1001",
  "name": "示例客户 1001",
  "unionId": "o_demo_union_1001",
  "mobile": "13800138000",
  "status": "ACTIVE"
}
```

订单中的 `memberId` 可继续请求 `/api/members/{memberId}` 获取会员身份信息。业务数据不存在时返回 `404`。

## 5. 准备动作

```http
POST /api/ui/actions/{actionCode}/prepare
Content-Type: application/json
```

当前示例 `actionCode`：`customer.basic.update`。

请求：

```json
{
  "pageCode": "customer_detail",
  "cardCode": "basic_info",
  "params": { "customerId": "1001" }
}
```

响应：

```json
{
  "formCode": "customer_basic_edit",
  "initialValues": {
    "customerId": "1001",
    "name": "示例客户 1001",
    "unionId": "o_demo_union_1001",
    "mobile": "13800138000",
    "status": "ACTIVE"
  },
  "version": 1
}
```

`pageCode` 和 `cardCode` 必填。Handler 会校验动作是否允许在该页面和卡片上下文中执行。

## 6. 执行动作

```http
POST /api/ui/actions/{actionCode}/execute
Content-Type: application/json
```

请求：

```json
{
  "pageCode": "customer_detail",
  "cardCode": "basic_info",
  "params": { "customerId": "1001" },
  "values": {
    "name": "更新后的客户",
    "mobile": "13800138000",
    "status": "ACTIVE"
  },
  "version": 1,
  "requestId": "client-generated-unique-id"
}
```

成功响应：

```json
{
  "message": "客户信息已保存",
  "refreshCards": ["basic_info"]
}
```

主要状态码：

| 状态码 | 场景 |
|---|---|
| `400` | 缺少 `pageCode/cardCode/requestId` 或输入不合法 |
| `403` | 权限不足，或 Handler 不支持当前页面/卡片上下文 |
| `404` | 未注册的 `actionCode` |
| `409` | 版本冲突 |

`requestId` 用于幂等；客户数据和乐观锁版本已持久化到 MySQL，演示 Handler 的幂等记录仍只保存在当前进程内存中。
