# 卡片级元数据驱动 UI 精简方案

## 1. 目标

先用较低成本实现“客户详情页的卡片可插拔”，验证元数据驱动模式，不在第一阶段建设完整页面 DSL 或低代码平台。

本方案只解决四件事：

1. 后端动态决定页面有哪些卡片；
2. 前端根据卡片类型选择已有组件；
3. 每张卡片独立加载数据和处理异常；
4. 卡片根据模块、租户和权限动态启停。

第一阶段不支持任意组件树、远程组件、复杂表达式和可视化设计器。

---

## 2. 核心模型

```text
客户详情页
  ├─ 基本信息卡片
  ├─ 好友标签卡片
  ├─ 所在群卡片
  └─ 行为轨迹卡片
```

页面仍由前端提供统一容器，但不写死具体业务卡片。后端返回当前用户可见的卡片清单；每张卡片只引用一个前端已注册组件。

---

## 3. 精简元数据协议

```json
{
  "version": "1.0",
  "pageCode": "customer_detail",
  "layout": {
    "type": "grid",
    "columns": 24,
    "gap": 16
  },
  "cards": [
    {
      "code": "basic_info",
      "title": "基本信息",
      "component": "KeyValueCard",
      "order": 10,
      "span": {
        "xs": 24,
        "md": 24,
        "xl": 12
      },
      "dataApi": "/api/customers/{customerId}/basic",
      "loadStrategy": "eager",
      "permission": "customer:read",
      "props": {
        "columns": 2,
        "fields": [
          { "key": "name", "label": "姓名" },
          { "key": "mobile", "label": "手机号", "formatter": "mobile-mask" },
          { "key": "status", "label": "状态", "formatter": "customer-status" }
        ]
      },
      "actions": [
        {
          "code": "edit",
          "label": "编辑",
          "type": "open-form",
          "target": "customer_basic_edit",
          "permission": "customer:update"
        }
      ]
    },
    {
      "code": "behavior_trace",
      "title": "行为轨迹",
      "component": "TimelineCard",
      "order": 40,
      "span": {
        "xs": 24,
        "md": 24,
        "xl": 12
      },
      "dataApi": "/api/customers/{customerId}/traces",
      "loadStrategy": "on-visible",
      "permission": "customer:trace:read",
      "props": {
        "timeField": "timestamp",
        "titleField": "eventName",
        "descriptionField": "description"
      },
      "actions": []
    }
  ]
}
```

### 字段说明

| 字段 | 说明 |
|---|---|
| `code` | 卡片唯一标识 |
| `title` | 卡片标题 |
| `component` | 前端组件注册名 |
| `order` | 显示顺序 |
| `span` | 各断点所占栅格宽度 |
| `dataApi` | 卡片数据接口模板 |
| `loadStrategy` | `eager` 或 `on-visible` |
| `permission` | 查看卡片所需权限 |
| `props` | 组件允许接收的配置 |
| `actions` | 精简的卡片动作列表 |

---

## 4. 后端 SPI

### 4.1 接口

```java
public interface CustomerDetailCardProvider {

    String cardCode();

    boolean supports(CardContext context);

    CardDefinition definition(CardContext context);
}
```

```java
public record CardContext(
    String tenantId,
    String userId,
    String customerId,
    Set<String> permissions,
    Map<String, Object> featureFlags
) {}
```

### 4.2 卡片定义

```java
public record CardDefinition(
    String code,
    String title,
    String component,
    int order,
    ResponsiveSpan span,
    String dataApi,
    String loadStrategy,
    String permission,
    Map<String, Object> props,
    List<CardAction> actions
) {}
```

不要让插件直接返回手写 JSON 字符串。使用强类型对象后，由聚合服务统一序列化和校验。

### 4.3 插件示例

```java
@Component
@ConditionalOnClass(BehaviorTraceService.class)
public class BehaviorTraceCardProvider
        implements CustomerDetailCardProvider {

    @Override
    public String cardCode() {
        return "behavior_trace";
    }

    @Override
    public boolean supports(CardContext context) {
        return context.permissions().contains("customer:trace:read")
            && Boolean.TRUE.equals(
                context.featureFlags().get("customerBehaviorTrace"));
    }

    @Override
    public CardDefinition definition(CardContext context) {
        return new CardDefinition(
            "behavior_trace",
            "行为轨迹",
            "TimelineCard",
            40,
            new ResponsiveSpan(24, 24, 12),
            "/api/customers/{customerId}/traces",
            "on-visible",
            "customer:trace:read",
            Map.of(
                "timeField", "timestamp",
                "titleField", "eventName",
                "descriptionField", "description"
            ),
            List.of()
        );
    }
}
```

### 4.4 聚合接口

```http
GET /api/ui/customer-detail/cards?customerId=1001
```

聚合步骤：

```text
发现所有 Provider
→ supports 过滤
→ 权限检查
→ definition 转换
→ Schema 校验
→ 按 order 排序
→ 返回卡片配置
```

---

## 5. 前端实现

### 5.1 组件注册表

第一阶段只提供少量高复用组件：

```ts
export const cardRegistry = {
  KeyValueCard,
  TagGroupCard,
  GroupListCard,
  TimelineCard,
  TableCard,
  StatisticCard
};
```

组件名称、属性和版本由前后端共同约定。后端不能引用注册表以外的组件。

### 5.2 页面容器

```vue
<template>
  <ResponsiveGrid :columns="24" :gap="16">
    <DynamicCard
      v-for="card in cards"
      :key="card.code"
      :definition="card"
    />
  </ResponsiveGrid>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue';

const cards = ref([]);

onMounted(async () => {
  const result = await api.getCustomerDetailCards(customerId);
  cards.value = result.cards.sort((a, b) => a.order - b.order);
});
</script>
```

### 5.3 动态卡片

```vue
<template>
  <CardShell :title="definition.title">
    <CardError v-if="error" @retry="load" />
    <CardSkeleton v-else-if="loading" />
    <CardEmpty v-else-if="isEmpty(data)" />
    <component
      v-else
      :is="resolvedComponent"
      :data="data"
      v-bind="definition.props"
      @action="handleAction"
    />
  </CardShell>
</template>

<script setup lang="ts">
const resolvedComponent = computed(() =>
  cardRegistry[props.definition.component] ?? UnknownCard
);
```

`DynamicCard` 统一负责：

- 解析 `dataApi` 中的路由参数；
- 立即加载或进入视口后加载；
- loading、empty、error 状态；
- 单卡重试；
- 从白名单注册表解析组件；
- 调用统一动作执行器；
- 记录卡片加载耗时和异常。

---

## 6. 精简动作模型

MVP 只支持以下动作：

- `refresh`：刷新当前卡片；
- `navigate`：跳转到登记过的页面；
- `open-form`：打开登记过的表单；
- `request`：执行允许的后端请求；
- `confirm-request`：确认后执行请求。

```json
{
  "code": "remove_tag",
  "label": "删除",
  "type": "confirm-request",
  "permission": "customer:tag:delete",
  "confirmText": "确认删除该标签？",
  "request": {
    "method": "DELETE",
    "url": "/api/customers/{customerId}/tags/{item.id}"
  },
  "success": {
    "message": "删除成功",
    "refresh": "current-card"
  }
}
```

禁止元数据下发 JavaScript、任意表达式、任意组件 URL 或任意外部请求地址。

---

## 7. 状态与容错

第一阶段不要求后端为每张卡片配置状态文案，由前端提供统一默认值：

| 状态 | 默认行为 |
|---|---|
| loading | 显示统一骨架屏 |
| empty | 显示“暂无数据” |
| error | 显示“加载失败”和重试按钮 |
| forbidden | 后端不返回该卡片 |
| unknown component | 显示“不支持的卡片类型”并上报 |

单张卡片失败不得影响其他卡片。

---

## 8. 安全约束

- `component` 必须存在于前端白名单；
- `formatter` 必须存在于格式化器白名单；
- `action.type` 必须存在于动作白名单；
- `dataApi` 和动作 URL 只允许站内相对地址；
- 元数据禁止包含 HTML、JavaScript 和动态 CSS；
- 后端不返回无权限卡片及敏感字段；
- 所有写操作接口必须再次鉴权，不能信任前端传来的权限；
- 限制卡片数量、元数据大小和字段数量；
- 聚合接口返回前执行协议校验。

---

## 9. 性能策略

MVP 推荐：

- 页面卡片元数据缓存 1～5 分钟；
- 首屏重要卡片使用 `eager`；
- 行为轨迹、群列表等次要卡片使用 `on-visible`；
- 单页卡片较多时再增加批量数据接口；
- 页面卸载时取消未完成请求；
- 使用卡片级请求去重和短期缓存。

首期不要过早建设复杂缓存规则。

---

## 10. MVP 范围

### 必须实现

- 一个固定的客户详情页容器；
- 一个卡片聚合接口；
- 后端 SPI Provider；
- 6 个以内的标准卡片组件；
- 响应式顺序和宽度；
- 独立数据加载；
- `eager`、`on-visible` 两种加载方式；
- loading、empty、error、重试；
- 查看权限和动作权限；
- 精简动作执行器；
- 协议版本、Schema 校验和基础监控。

### 暂不实现

- 卡片内部任意组件树；
- JavaScript 表达式；
- 远程组件或微前端；
- 拖拽式设计器；
- 用户个性化布局；
- 多步骤复杂动作编排；
- 通用低代码表单设计器；
- 动态主题和任意样式配置。

---

## 11. 推荐实施顺序

1. 定义 `CardDefinition` 及 JSON Schema；
2. 实现卡片聚合接口和两个 Provider；
3. 实现 `cardRegistry`、`DynamicCard` 和统一状态；
4. 接入基本信息、好友标签两个卡片；
5. 加入权限过滤和动作执行器；
6. 接入所在群、行为轨迹卡片；
7. 增加懒加载、缓存和监控；
8. 根据实际需求决定是否升级为页面级完整 DSL。

---

## 12. 验收标准

1. 删除某个插件后，对应卡片自动消失且页面正常；
2. 新增使用现有组件类型的 Provider 后，前端不修改客户详情页代码即可展示；
3. 不同权限用户获得不同卡片和动作；
4. 单卡接口失败时其他卡片正常展示；
5. 未知组件不会导致整页白屏；
6. 非首屏卡片进入视口后才请求数据；
7. 所有写操作均在服务端再次鉴权；
8. 可以通过日志定位页面、卡片、客户和定义版本。

---

## 13. 方案边界

“前端零修改”仅适用于新增卡片可以使用现有 `cardRegistry` 组件的情况。

如果新增了关系图谱、复杂旅程、可编辑表格等全新 UI 形态，需要先开发并注册新的前端组件，然后后端才能通过元数据引用。这是有意保留的边界，可以避免精简协议不断膨胀成另一种前端编程语言。

---

## 14. 结论

精简方案以“卡片类型＋数据接口＋少量属性＋有限动作”为核心，能够用较低成本验证 SPI 与元数据驱动 UI 的实际价值。

当标准卡片覆盖率、租户差异化需求和布局配置需求明显增长后，再逐步增加组件树、结构化条件、布局覆盖、版本发布和可视化配置能力，演进为完整的页面级方案。
