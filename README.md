# 卡片级元数据驱动 UI

这是一个可运行的元数据驱动 UI MVP。后端根据页面、租户、权限和 Feature Flag 动态装配卡片元数据；前端只渲染白名单内的 Vue 组件，并让每张卡片独立加载数据、处理失败和执行受控动作。

项目适合用来验证 CRM/ERP 详情页、工作台和租户定制模块的卡片化扩展方式。它不是低代码平台，也不允许后端下发 JavaScript、Vue 模板、任意 URL 或远程组件。

## 技术栈

| 层次 | 技术 |
|---|---|
| 后端 | Java 11、Spring Boot 2.7.18、Spring Security、MyBatis-Plus 3.5.17、Flyway |
| 前端 | Vue 3、TypeScript、Vue Router、Vite、Vitest |
| 数据库 | MySQL 8；测试使用 H2 MySQL 兼容模式 |
| 元数据协议 | 强类型 Java 模型 + 后端白名单校验 + 前端组件/路由/表单注册表 |

## 5 分钟启动

准备 Java 11+、Maven 3.6+、Node.js、npm 和 MySQL 8。仓库提供 Docker Compose：

```bash
docker compose up -d mysql
```

默认数据库、用户名和密码都是 `metadata_ui`。Flyway 会自动建表，应用首次启动会创建管理员 `admin / Admin123!`，请在非本地环境通过环境变量修改密码和 JWT 密钥。

仓库包含 `package-lock.json`，首次安装优先使用 `npm ci`。

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
- 客户列表：<http://localhost:5173/customers>
- 客户详情：<http://localhost:5173/customer-detail?customerId=1001>
- 订单列表：<http://localhost:5173/orders>
- 指定客户订单：<http://localhost:5173/orders?customerId=1001>

Vite 将 `/api` 代理到 `http://localhost:8080`。账号、角色、权限、菜单，以及客户、标签、轨迹、订单和会员演示数据均保存在 MySQL。

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

客户详情的“基本信息”卡片还演示了完整修改链路：同一个业务实现类同时实现 `CardProvider` 和 `UiActionHandler`，从元数据动作经过前端表单与 Action API，最终执行修改并定向刷新卡片。

客户列表和订单列表分别由 `customer_list`、`order_list` 元数据页面装配。点击客户行进入客户详情；点击订单行进入订单所属客户的详情；客户详情中的“查看订单”进入带 `customerId` 的订单列表。`DataTableCard` 的行点击同样由动作元数据驱动。

账号与权限使用 RBAC 模型：注册用户自动绑定 `USER` 角色，JWT 请求会从数据库重新加载最新角色和权限，菜单按角色与权限动态返回；管理员可在“权限管理”中配置用户、角色、权限和菜单。

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
- [账号与权限开发指南](docs/账号与权限开发指南.md)：MySQL、JWT、RBAC、菜单和管理接口

第一次接手项目，建议依次阅读 README → 开发指南 → 架构说明，再根据任务查看卡片扩展或动作设计文档。
