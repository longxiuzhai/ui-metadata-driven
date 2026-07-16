# 卡片级元数据驱动 UI

这是一个可运行的元数据驱动 UI MVP。后端根据页面、租户、权限和 Feature Flag 动态装配卡片元数据；前端只渲染白名单内的 Vue 组件，并让每张卡片独立加载数据、处理失败和执行受控动作。

项目适合用来验证 CRM/ERP 详情页、工作台和租户定制模块的卡片化扩展方式。它不是低代码平台，也不允许后端下发 JavaScript、Vue 模板、任意 URL 或远程组件。

## 技术栈

| 层次 | 技术 |
|---|---|
| 后端 | Java 11、Spring Boot 2.7.18、Maven 多模块 |
| 前端 | Vue 3、TypeScript、Vue Router、Vite、Vitest |
| 元数据协议 | 强类型 Java 模型 + 后端白名单校验 + 前端组件/路由/表单注册表 |

## 5 分钟启动

准备 Java 11+、Maven 3.6+、Node.js 和 npm。仓库包含 `package-lock.json`，首次安装优先使用 `npm ci`。

终端一：

```bash
cd backend
mvn install
cd application
mvn spring-boot:run
```

终端二：

```bash
cd frontend
npm ci
npm run dev
```

打开：

- 首页：<http://localhost:5173/>
- 客户详情：<http://localhost:5173/customer-detail?customerId=1001>

Vite 将 `/api` 代理到 `http://localhost:8080`。后端使用内存演示数据，不需要数据库或其他中间件。

## 项目结构

```text
.
├── backend/
│   ├── card-platform/          # 通用元数据/动作协议、SPI、条件、校验和装配
│   ├── tenant-customization/   # 演示租户的卡片 Provider、数据接口和动作 Handler
│   └── application/            # Spring Boot 入口、统一 API、账号上下文和集成测试
├── frontend/
│   └── src/                    # 页面容器、卡片组件、运行时和白名单注册表
├── docs/                       # 当前实现对应的开发与架构文档
├── 卡片级元数据驱动UI精简方案.md  # MVP 的早期方案
└── 页面级元数据驱动UI设计方案.md  # 更完整方案的设计草案
```

后端依赖方向是：

```text
card-platform ← tenant-customization
      ↑                 ↑
      └──── application ┘
```

`application` 负责组装最终应用；平台模块不依赖业务定制模块。

## 核心链路

```text
GET /api/ui/pages/{pageCode}/cards
  → 创建当前账号/租户/权限/Feature 上下文
  → 查找该页面的 CardProvider
  → 启动开关、通用条件和 supports 依次过滤
  → 校验并排序 CardDefinition
  → 前端 cardRegistry 解析组件
  → 每张卡片独立请求 dataApi
```

客户详情的“基本信息”卡片还演示了完整修改链路：元数据动作 → 前端表单注册表 → Action prepare/execute API → 后端 `UiActionHandler` → 定向刷新卡片。

## 常用验证命令

```bash
# 后端全部测试（从仓库根目录执行）
mvn test

# 前端测试与生产构建
cd frontend
npm test
npm run build
```

当前有效的统一接口是 `/api/ui/pages/{pageCode}/cards`；旧的页面专用地址不再保留。后端测试、前端测试和构建命令应作为提交前基线全部通过。

## 文档导航

- [文档索引](docs/README.md)：各类文档的用途和阅读顺序
- [开发指南](docs/开发指南.md)：环境、启动、调试、测试、开发流程和提交检查
- [架构说明](docs/架构说明.md)：模块边界、请求链路、扩展点和安全边界
- [API 参考](docs/API参考.md)：当前有效接口、请求头、协议和示例
- [扩展卡片开发指南](docs/扩展卡片开发指南.md)：新增 Provider、条件和组件的纵向步骤
- [页面动作与编辑能力设计方案](docs/页面动作与编辑能力设计方案.md)：动作协议和编辑链路

第一次接手项目，建议依次阅读 README → 开发指南 → 架构说明，再根据任务查看卡片扩展或动作设计文档。
