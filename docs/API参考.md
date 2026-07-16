# API 参考

本文只记录当前源码中实际启用的接口。默认后端地址为 `http://localhost:8080`；前端开发环境通过 Vite 代理使用相对路径 `/api/...`。

## 1. 演示上下文请求头

| 请求头 | 默认值 | 说明 |
|---|---|---|
| `X-Tenant-Id` | `demo` | 演示租户 |
| `X-User-Id` | `user-1` | 演示用户 |
| `X-Permissions` | 当前演示账号权限 | 逗号分隔；提供空字符串表示空权限 |
| `X-Features` | 当前演示账号 Feature | 逗号分隔；提供空字符串表示空 Feature |

动作 API 使用前三个请求头，不读取 `X-Features`。这些头是演示覆盖机制，不是生产认证协议。

## 2. 获取当前账号

```http
GET /api/accounts/me
```

示例：

```bash
curl 'http://localhost:8080/api/accounts/me'
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

示例：

```bash
curl 'http://localhost:8080/api/ui/pages/customer_detail/cards?customerId=1001'
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

按权限和 Feature 调试：

```bash
curl 'http://localhost:8080/api/ui/pages/customer_detail/cards?customerId=1001' \
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
| `GET /api/home/{userId}/work-summary` | 首页工作摘要 |
| `GET /api/home/{userId}/activities` | 首页最近动态 |

前端只接受以 `/api/` 开头的数据地址，并使用页面上下文替换 `{customerId}`、`{userId}` 等占位符。未替换的占位符会导致卡片加载失败。

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
    "name": "示例客户",
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

`requestId` 用于幂等；演示 Handler 的幂等记录只保存在当前进程内存中。
