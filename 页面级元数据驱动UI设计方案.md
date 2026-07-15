# 页面级元数据驱动 UI 设计方案

## 1. 文档目的

本文设计一套适用于 CRM、ERP、运营后台等复杂业务系统的页面级元数据驱动 UI 方案。

系统通过元数据描述页面结构、布局、数据源、组件和交互动作，由前端渲染引擎解释执行；后端通过 SPI 发现并聚合业务模块，从而支持页面模块按需启用、租户差异化配置、权限控制及持续扩展。

该方案的目标不是让后端编写前端页面，而是由前后端共同维护一套稳定、受控、可演进的 UI 协议：

- 后端描述业务意图和当前可用能力；
- 前端负责组件实现、设计规范和交互体验；
- 元数据协议连接业务能力与 UI 能力；
- 复杂场景允许使用自定义业务组件作为扩展出口。

---

## 2. 适用场景

适合：

- 客户详情、订单详情、商品详情等聚合型详情页；
- 首页工作台、数据看板、运营驾驶舱；
- 不同租户、角色或产品版本具有不同模块的 SaaS 系统；
- 业务模块需要独立开发、部署、启停的插件化系统；
- 页面结构相似，但字段、模块和操作持续变化的后台系统。

不适合完全依赖元数据实现的场景：

- 强动画、强交互、像素级定制页面；
- 实时协作、画布、流程设计器等高度状态化应用；
- 组件体系中从未出现过的全新交互形态；
- 面向消费者的高品牌定制页面。

这些场景可以继续使用普通前端页面或自定义业务组件，并接入统一页面容器。

---

## 3. 核心设计原则

### 3.1 页面是容器，卡片是业务边界

页面负责标题、页面上下文、区域划分、响应式布局和模块编排。每张卡片作为相对独立的业务单元，拥有自己的数据源、视图、动作、权限和加载状态。

### 3.2 元数据描述意图，不下发执行代码

后端可以表达“使用时间轴”“打开编辑表单”“调用删除接口”，但不能下发 Vue 模板、JavaScript 函数或任意 CSS。前端只能解释白名单中的组件、动作和格式化器。

### 3.3 标准组件与自定义组件并存

常规页面由标准组件组合；无法低成本表达的复杂场景使用已注册的业务组件。避免为了少量特殊需求无限扩张 DSL。

### 3.4 独立失败，渐进加载

单卡片加载失败、组件缺失或数据异常时，不应影响整个页面。首屏关键数据优先加载，次要卡片可以懒加载。

### 3.5 协议必须可校验、可版本化、可观测

所有页面元数据必须经过 Schema 校验；协议、组件及数据契约需要版本；运行时记录页面和卡片级指标。

---

## 4. 分层模型

```text
页面 Page
  └── 区域 Region
        └── 卡片 Card
              └── 组件树 Component Tree
                    ├── 属性 Props
                    ├── 数据绑定 Binding
                    ├── 条件 Condition
                    └── 动作 Action
```

| 层级 | 职责 | 示例 |
|---|---|---|
| Page | 页面上下文、标题、刷新和整体策略 | 客户详情页 |
| Region | 页面区域、栅格、标签页等容器 | 顶部概要区、主内容区 |
| Card | 独立业务和数据加载边界 | 基本信息、好友标签 |
| Component | 卡片内部展示结构 | 字段列表、表格、时间轴 |
| Binding | 将数据绑定到组件属性 | `$data.name` |
| Action | 声明用户交互 | 编辑、跳转、删除、刷新 |

---

## 5. 总体架构

```text
业务插件
  └─ PageCardProvider
       ├─ 卡片定义
       ├─ 启用条件
       └─ 数据服务
            ↓
页面元数据聚合服务
  ├─ SPI 发现
  ├─ 租户/权限/灰度过滤
  ├─ 布局配置合并
  ├─ 协议校验
  └─ 缓存与版本处理
            ↓
页面配置 API
            ↓
前端动态页面引擎
  ├─ 页面容器
  ├─ 布局引擎
  ├─ 组件注册表
  ├─ 数据加载器
  ├─ 动作执行器
  └─ 错误边界与监控
```

---

## 6. 前后端职责边界

### 6.1 后端职责

- 发现当前部署中可用的页面卡片；
- 根据租户、用户、角色、权限、实体和灰度条件筛选卡片；
- 提供卡片业务字段、数据接口和允许执行的动作；
- 聚合插件默认布局、租户布局和用户个性化布局；
- 对元数据进行校验、版本转换、缓存和审计；
- 对所有数据读取和写操作进行真实鉴权。

### 6.2 前端职责

- 实现并注册标准组件和业务组件；
- 保证设计系统、主题、响应式和无障碍一致性；
- 校验和解析元数据，处理未知版本与未知组件；
- 加载卡片数据，呈现 loading、empty、error 等状态；
- 通过白名单动作执行器处理交互；
- 提供卡片级错误隔离、日志和性能指标。

---

## 7. 页面元数据协议

建议定义专用 UI DSL，不要直接使用 JSON Schema 作为 UI 描述。JSON Schema 可用于校验这套 DSL。

顶层结构：

```json
{
  "protocolVersion": "1.0",
  "page": {},
  "layout": {},
  "cards": [],
  "permissions": {},
  "extensions": {}
}
```

### 7.1 完整示例

```json
{
  "protocolVersion": "1.0",
  "page": {
    "code": "customer_detail",
    "title": "客户详情",
    "entity": {
      "type": "customer",
      "id": "1001"
    },
    "refreshPolicy": "manual"
  },
  "layout": {
    "type": "responsive-grid",
    "columns": {
      "xs": 1,
      "md": 2,
      "xl": 3
    },
    "gap": 16
  },
  "cards": [
    {
      "id": "basic_info",
      "type": "standard",
      "definitionVersion": "1.2.0",
      "meta": {
        "title": "基本信息",
        "icon": "user",
        "order": 10,
        "collapsible": false
      },
      "layout": {
        "span": {
          "xs": 1,
          "md": 2,
          "xl": 2
        },
        "minHeight": 220
      },
      "dataSource": {
        "type": "http",
        "request": {
          "method": "GET",
          "url": "/api/customers/{customerId}/basic"
        },
        "params": {
          "customerId": {
            "from": "route",
            "path": "customerId"
          }
        },
        "responsePath": "data",
        "loadStrategy": "eager",
        "cache": {
          "strategy": "stale-while-revalidate",
          "ttl": 60
        }
      },
      "view": {
        "component": "Descriptions",
        "props": {
          "columns": 2
        },
        "children": [
          {
            "component": "Field",
            "props": {
              "label": "客户姓名",
              "value": {
                "binding": "$data.name"
              }
            }
          },
          {
            "component": "Field",
            "props": {
              "label": "手机号",
              "value": {
                "binding": "$data.mobile"
              },
              "formatter": "mobile-mask"
            }
          }
        ]
      },
      "actions": [
        {
          "id": "edit_customer",
          "label": "编辑",
          "placement": "card-header",
          "permission": "customer:update",
          "trigger": "click",
          "handler": {
            "type": "open-form",
            "formCode": "customer_basic_edit",
            "initialValues": {
              "binding": "$data"
            }
          }
        }
      ],
      "states": {
        "loading": {
          "type": "skeleton",
          "rows": 4
        },
        "empty": {
          "title": "暂无客户信息"
        },
        "error": {
          "title": "加载失败",
          "retryable": true
        }
      }
    }
  ]
}
```

---

## 8. 组件体系

### 8.1 基础组件

- Text、Image、Icon、Link；
- Button、Badge、Divider；
- Empty、Alert、Skeleton。

### 8.2 版式及数据组件

- Stack、Grid、Tabs；
- Descriptions、List、DataTable；
- TagGroup、Timeline、Statistic、Chart；
- Form、Modal、Drawer。

### 8.3 业务组件

- CustomerRelationshipGraph；
- FriendTagManager；
- GroupMembershipPanel；
- BehaviorJourney；
- RiskAssessmentPanel。

前端通过注册表暴露组件：

```ts
export const componentRegistry = {
  Text,
  Button,
  Grid,
  Descriptions,
  DataTable,
  TagGroup,
  Timeline,
  CustomerRelationshipGraph,
  FriendTagManager
};
```

元数据只能引用注册表中的组件。组件不存在时，应在当前卡片显示兼容性占位信息并上报日志，不能导致整页崩溃。

---

## 9. 三种渲染模式

### 9.1 标准 DSL

适合常规字段、列表、表格、标签和时间轴：

```json
{
  "type": "standard",
  "view": {
    "component": "TagGroup"
  }
}
```

### 9.2 本地自定义组件

适合复杂交互，组件仍随前端应用发布：

```json
{
  "type": "custom",
  "component": "CustomerRelationshipGraph",
  "componentVersion": "^2.0"
}
```

### 9.3 远程组件

只有业务模块确实需要独立发布时才考虑微前端或远程模块：

```json
{
  "type": "remote",
  "remoteModule": "customer-risk-plugin",
  "component": "RiskCard"
}
```

远程组件会带来依赖冲突、安全、隔离、加载性能和版本兼容成本，不建议在第一阶段采用。

---

## 10. 数据绑定与条件表达式

数据绑定只支持受限路径：

```json
{
  "binding": "$data.customer.name",
  "defaultValue": "-"
}
```

允许的数据域可包括：

- `$data`：当前卡片数据；
- `$page`：页面上下文；
- `$route`：路由参数；
- `$user`：经过脱敏的用户上下文；
- `$item`：列表当前项。

条件表达式应采用结构化 DSL，不执行 JavaScript：

```json
{
  "visibleWhen": {
    "all": [
      { "path": "$data.status", "operator": "eq", "value": "ACTIVE" },
      { "path": "$user.permissions", "operator": "contains", "value": "customer:update" }
    ]
  }
}
```

---

## 11. 动作模型

动作采用声明式白名单。建议第一阶段支持：

- `refresh`；
- `navigate`；
- `open-modal`、`open-drawer`、`open-form`；
- `http-request`；
- `confirm`；
- `download`、`copy`；
- `toast`；
- `sequence`。

删除标签示例：

```json
{
  "id": "remove_tag",
  "label": "删除",
  "permission": "customer:tag:delete",
  "handler": {
    "type": "sequence",
    "steps": [
      {
        "type": "confirm",
        "title": "确认删除该标签？"
      },
      {
        "type": "http-request",
        "request": {
          "method": "DELETE",
          "url": "/api/customers/{customerId}/tags/{item.id}"
        }
      },
      {
        "type": "refresh",
        "target": "friend_tags"
      },
      {
        "type": "toast",
        "level": "success",
        "message": "删除成功"
      }
    ]
  }
}
```

动作执行器统一处理二次确认、防重复提交、成功提示、异常提示、定向刷新、审计及必要的失败回滚。

前端权限只用于控制显示，所有动作接口仍必须在服务端再次鉴权。

---

## 12. 后端 SPI 设计

### 12.1 核心接口

```java
public interface PageCardProvider {

    String pageCode();

    String cardCode();

    boolean supports(CardContext context);

    CardDefinition definition(CardContext context);
}
```

上下文对象：

```java
public record CardContext(
    String tenantId,
    String userId,
    Set<String> roles,
    Set<String> permissions,
    String entityId,
    String locale,
    String device,
    Map<String, Object> featureFlags
) {}
```

卡片定义应使用强类型对象，不建议插件直接拼接 JSON 字符串：

```java
public record CardDefinition(
    String id,
    String type,
    String definitionVersion,
    CardMeta meta,
    CardLayout layout,
    DataSourceDefinition dataSource,
    ComponentNode view,
    List<ActionDefinition> actions,
    CardStateDefinition states
) {}
```

### 12.2 条件分层

| 条件 | 处理位置 | 示例 |
|---|---|---|
| 部署条件 | Spring Condition | 是否安装好友模块 |
| 租户条件 | `supports`/订阅服务 | 租户是否购买该模块 |
| 用户条件 | 权限服务 | 是否能查看行为轨迹 |
| 实体条件 | 运行时业务规则 | 是否为企业客户 |
| 灰度条件 | Feature Flag | 是否启用新版卡片 |

### 12.3 页面聚合流程

```text
发现 PageCardProvider
→ 按 pageCode 筛选
→ 判断部署条件
→ 判断租户和 Feature Flag
→ 判断用户权限
→ 判断当前实体条件
→ 合并租户及用户布局
→ 校验并转换协议版本
→ 排序、缓存并返回
```

---

## 13. 数据加载策略

### 13.1 独立加载

卡片各自请求数据，适合数据较重或耗时差异明显的模块。

```json
{ "loadStrategy": "independent" }
```

### 13.2 批量加载

将首屏关键卡片合并请求，避免请求风暴：

```http
POST /api/ui/data/batch
```

```json
{
  "requests": [
    { "cardId": "basic_info" },
    { "cardId": "friend_tags" },
    { "cardId": "groups" }
  ]
}
```

### 13.3 懒加载

卡片进入视口、Tab 激活或折叠区展开时加载：

```json
{ "loadStrategy": "on-visible" }
```

推荐组合：页面元数据一次加载，首屏关键数据批量加载，非首屏卡片懒加载，慢卡片独立加载。

---

## 14. 状态与错误隔离

每张卡片至少定义：

- loading；
- empty；
- error；
- forbidden；
- normal。

前端渲染器需要保证：

- 元数据错误只影响当前卡片；
- 数据接口错误允许单卡重试；
- 未知组件显示兼容性提示；
- 不支持的协议版本拒绝渲染并上报；
- 数据字段缺失按默认值策略处理；
- 页面其他卡片继续正常工作。

---

## 15. 布局配置与合并

布局可以来自三层：

```text
插件默认布局
  ↓
租户管理员布局
  ↓
用户个性化布局
```

推荐优先级：

```text
用户配置 > 租户配置 > 插件默认配置
```

允许覆盖：排序、宽度、折叠、默认显示、Tab 分组。

禁止覆盖：数据接口、权限、安全约束、组件类型约束和协议版本。

```json
{
  "customizable": {
    "order": true,
    "span": true,
    "visible": true,
    "removable": false
  }
}
```

---

## 16. 协议和版本治理

建议维护四类版本：

- `protocolVersion`：UI DSL 版本；
- `componentVersion`：组件契约版本；
- `definitionVersion`：卡片定义版本；
- `dataContractVersion`：数据结构版本。

兼容策略：

- 新增可选字段保持向后兼容；
- 删除、重命名或改变语义时升级主版本；
- 前端声明支持的协议范围；
- 后端可以针对客户端能力转换协议；
- 未知属性默认忽略；
- 未知组件明确降级；
- 元数据发布前必须进行静态校验和预览。

---

## 17. 安全设计

必须落实以下限制：

- 组件、动作和格式化器全部使用白名单；
- 禁止元数据携带 JavaScript 或可执行模板；
- API 只允许相对路径或登记过的服务标识；
- 富文本内容必须清洗；
- 服务端过滤无权限字段并对所有动作再次鉴权；
- 条件表达式仅支持受限操作符；
- 限制元数据大小、节点数量、嵌套深度和解析时间；
- 写请求执行 CSRF、幂等、防重放等平台安全策略；
- 元数据的发布、修改和回滚需要审计记录。

---

## 18. 缓存与性能

元数据缓存键建议包含：

```text
pageCode
+ tenantId
+ roleSet
+ featureFlagVersion
+ locale
+ protocolVersion
```

业务数据缓存键建议包含：

```text
cardCode
+ entityId
+ userPermissionScope
+ queryParameters
```

元数据接口可配合 `ETag` 和短期私有缓存：

```http
ETag: "customer-detail-meta-v18"
Cache-Control: private, max-age=60
```

前端可缓存已校验和解析的 Schema、组件解析结果及稳定的页面布局。

---

## 19. 可观测性

每张卡片附带追踪信息：

```json
{
  "telemetry": {
    "pageCode": "customer_detail",
    "cardCode": "behavior_trace",
    "definitionVersion": "2.1.0"
  }
}
```

建议采集：

- 页面元数据加载耗时；
- 卡片数据加载耗时及错误率；
- 首屏可用时间；
- Schema 校验和解析失败率；
- 未知组件次数；
- 卡片曝光和使用频率；
- 动作执行成功率；
- 插件版本、租户与故障的关联关系。

---

## 20. 推荐接口

### 20.1 获取页面定义

```http
GET /api/ui/pages/customer_detail?entityId=1001
```

返回当前用户可以使用的完整页面元数据。

### 20.2 批量加载卡片数据

```http
POST /api/ui/pages/customer_detail/data:batch
```

### 20.3 获取单卡数据

```http
GET /api/customers/1001/behavior-traces
```

### 20.4 保存布局

```http
PUT /api/ui/pages/customer_detail/layout
```

只接收允许用户覆盖的布局字段，不接收完整卡片定义。

---

## 21. 落地路线

### 第一阶段：卡片级动态化

- Page、Card 两层结构；
- 固定响应式栅格；
- 5～8 个通用卡片组件；
- 每卡片独立数据源；
- 基础动作和权限；
- SPI 发现及条件过滤；
- 卡片级 loading、empty、error；
- Schema 校验和错误隔离。

### 第二阶段：卡片内部组合

- Component Tree；
- 数据绑定和格式化器；
- 结构化条件表达式；
- 首屏数据批量加载；
- 管理员布局配置；
- 元数据预览和调试工具。

### 第三阶段：平台化

- 可视化页面设计器；
- 元数据版本、发布、灰度和回滚；
- 用户个性化布局；
- 组件市场及使用分析；
- 元数据差异对比；
- 自动化协议兼容性测试。

---

## 22. 验收标准

第一阶段至少满足：

1. 新增一个使用现有标准组件的卡片时，前端无须修改业务页面代码；
2. 卡片可根据模块、租户、权限和 Feature Flag 动态出现或隐藏；
3. 单卡失败不影响页面其他内容；
4. 所有组件和动作均来自白名单；
5. 元数据不包含可执行脚本；
6. 页面及卡片协议具备明确版本；
7. 支持首屏加载、独立加载和懒加载中的至少两种；
8. 能从日志定位页面、卡片、租户及定义版本；
9. 布局配置只能修改允许覆盖的字段；
10. 新增全新 UI 形态时，可以通过注册自定义组件扩展。

---

## 23. 结论

页面级元数据驱动方案的价值，是将页面中频繁变化的业务结构从固定页面代码中抽离，形成可组合、可配置、可治理的页面能力。

成功落地的关键不是让元数据描述所有前端细节，而是建立清晰边界：

- 页面容器管理组合；
- 卡片承载业务边界；
- 标准组件覆盖常见形态；
- 自定义组件解决复杂场景；
- 后端 SPI 决定当前可用模块；
- 白名单 DSL 保证安全和一致性；
- 版本、校验、灰度、错误隔离和监控保证长期演进。

在此基础上，客户详情页可以逐步演进为通用的企业业务页面装配平台。
