# Frontend

Vue 3 + TypeScript 前端。页面主体由后端下发的卡片元数据动态装配。

## 目录结构

```text
src/
├── actions/             # 声明式卡片动作：参数解析、路由和表单注册
├── components/          # 页面级通用组件
│   └── cards/           # 卡片展示组件及白名单 registry.ts
├── forms/               # 动态表单的具体实现
├── views/               # 路由对应的业务视图及页面级状态
├── styles/              # 应用外壳的全局样式
├── App.vue              # 认证初始化、应用外壳和路由视图分发
├── api.ts               # HTTP 请求、页面元数据和卡片数据接口
├── auth.ts              # 登录状态、账号和权限菜单
├── formatters.ts        # 元数据可引用的字段格式化函数
├── router.ts            # Vue Router 配置
└── types.ts             # 前后端元数据协议及共享类型
```

## 动态卡片链路

1. `App.vue` 根据当前路由选择业务视图。
2. `views/MetadataCardPage.vue` 请求 `CardPageDefinition`，并为每项元数据创建一个 `DynamicCard`。
3. `DynamicCard.vue` 根据 `loadStrategy` 立即加载或在接近视口时加载数据。
4. `components/cards/registry.ts` 将元数据中的 `component` 名称映射到受信任的 Vue 组件。
5. 展示组件接收 `data` 和元数据中的 `props`，只负责渲染。
6. 用户点击动作后，`ActionRuntime.ts` 解析参数并执行刷新、跳转或打开表单。

刷新采用 `refreshTokens[cardCode]`：动作运行时通知 `App.vue` 递增指定卡片的 token，
对应 `DynamicCard` 监听变化并重新请求数据，因此不会无差别刷新整页。

## 新增卡片类型

在 `src/components/cards` 新增展示组件，并在 `registry.ts` 显式注册。卡片组件应保持纯展示：
不要自行读取路由、请求页面元数据或执行卡片动作，这些职责由 `DynamicCard` 和动作运行时统一处理。

## 常用命令

```bash
npm run dev
npm test
npm run build
```

TypeScript 配置启用了 `noEmit`，类型检查不会再向源码目录生成 `.js` 副本；生产文件只由 Vite 输出到 `dist`。
