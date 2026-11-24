# 订单管理前端

基于赛博威中台前端脚手架（`cyberway-mp-frontend-template` release 1.23.0）与中台组件库
`cyberway-middle-ui-components` 1.23.0 开发的订单管理页面，对接订单后端服务
（`backend-demo`）的订单 CRUD 接口。

整体架构、启动流程、路由与登录机制、请求流转链路和关键代码说明见
[docs/architecture-and-runtime.md](docs/architecture-and-runtime.md)。

## 功能

- 订单分页查询：支持订单号模糊查询、客户 ID、订单状态筛选。
- 订单列表：订单号、客户 ID、订单金额、订单状态、创建时间，金额千分位展示，状态标签区分颜色。
- 订单详情：弹框展示订单完整信息（含最后修改时间）。
- 新增 / 编辑订单：弹框表单，包含必填、长度、金额精度等前端校验。
- 删除订单：二次确认后物理删除，删除失败给出明确提示。
- 服务调用链路：订单列表和详情经订单调用服务 B（`backend-demo-consumer-server`），
  由 B 通过 Nacos 服务发现 + Feign 调用订单服务 A 获取数据；新增、编辑、删除直连服务 A。
- 国际化：中文、英文两套语言包，文案统一通过 `t()` 输出。
- 交互提示：成功、失败、删除确认统一使用脚手架的 `useMessage`。

## 技术栈

| 能力 | 说明 |
| --- | --- |
| 页面框架 | Vue 3.2 + TypeScript |
| 本地开发 | Vite 5 |
| 基础组件 | Ant Design Vue 3.2 |
| 中台组件库 | cyberway-middle-ui-components 1.23.0（表格、查询面板、弹框表单等） |
| 中台脚手架 | mp-cli，运行时通过模块联邦加载，提供登录、布局、路由、网络与状态能力 |
| 接口请求 | `mp-cli/network` 提供的 `axiosInstance` |

## 目录结构

```text
src
├── components                订单金额、订单状态标签等本模块复用组件
├── locales/lang              中英文语言包，按 global、order-management 拆分
├── router                    订单管理路由
├── service                   订单接口、请求异常处理
├── views/order-management    列表页、表单弹框、详情弹框、字段配置
├── main.ts                   应用入口（setupApp）
└── index.ts                  模块联邦入口
db/order-test-data.sql        订单联调测试数据
```

## 本地启动

环境要求：Node 18 及以上、Yarn 1.22；订单后端服务已按 `backend-demo` 的 README 启动
（Nacos、MySQL、服务 A 和服务 B），服务 A 默认地址 `http://127.0.0.1:8081`，服务 B 默认
地址 `http://127.0.0.1:8082`；本机可访问中台开发环境。

1. 安装依赖：

   ```bash
   yarn
   ```

2. 启动订单后端服务：先启动 Nacos，再启动服务 A（端口 8081）和服务 B（端口 8082）。

3. 启动前端：

   ```bash
   yarn dev
   ```

4. 浏览器访问 `http://localhost:3000`，登录中台后从菜单进入「订单管理 → 订单列表」，
   也可以直接访问完整页面地址 `http://localhost:3000/#/order-management/list`。

> 请使用 `localhost` 访问，不要使用 `127.0.0.1`（Vite 默认监听 IPv6 回环地址）。
> 修改 `vite.config.ts` 或 `.env.*` 后需要重启 `yarn dev` 才会生效；端口 3000 被占用时
> 启动会直接报错（已开启 `strictPort`），不会自动切换到 3001。

> `http://192.168.90.25:51671` 是中台开发环境自带的演示应用（消息管理），不是本项目的
> 页面入口；本项目的页面由本地 Vite 开发服务器提供。

### 页面访问与菜单显示

- 本项目使用 hash 路由，页面地址以 `#/` 开头，例如订单列表：
  `http://localhost:3000/#/order-management/list`。
- 登录后左侧菜单没有「订单管理」时，需要把前端路由上报为中台资源：
  1. 登录后在浏览器开发者工具 Console 中执行 `window.generateResourceTree()`；
  2. 在“生成成功，是否更新资源?”确认框中点击「确认」；
  3. 在平台资源管理中把生成的资源授权给当前角色，刷新页面后菜单即可显示。

  `window.generateResourceTree()` 只在非 `prd` 环境提供，用于开发阶段上报路由资源。
- 项目在 `src/main.ts` 中设置了 `usePermissionCheck: false`，未配置菜单资源时也可以直接
  通过上面的地址访问页面，便于开发阶段自测。
- 如果平台为项目分配了独立的应用实例，请同步修改 `src/main.ts` 中的 `appKey` 以及
  `.env.*` 中的 `VITE_APP_ID`。
- 未登录时访问业务页面会先跳转登录页；访问平台演示应用的其他路径出现 404 属于正常
  现象，本项目页面不在该演示应用内。

### 接口代理

本地开发时由 Vite 代理解决跨域问题，配置位于 `vite.config.ts` 与 `.env.development`：

- `/api/consumer`：代理到 `VITE_PROXY_CONSUMER_API`（默认 `http://127.0.0.1:8082`），并去掉
  `/api` 前缀，对应服务 B 的真实路径 `/consumer/orders`，用于列表和详情查询。
- `/api/orders`：代理到 `VITE_PROXY_ORDER_API`（默认 `http://127.0.0.1:8081`），并去掉
  `/api` 前缀，对应服务 A 的真实路径 `/orders`，用于新增、修改、删除。
- 其余 `/api` 请求：代理到 `VITE_PROXY_PLATFORM_API`（默认中台开发环境
  `http://192.168.90.25:46807`），用于登录与平台公共接口。

接口地址变化时，修改 `.env.development` 中的 `VITE_PROXY_ORDER_API`、
`VITE_PROXY_CONSUMER_API`、`VITE_PROXY_PLATFORM_API` 即可，无需改动页面代码。

## 测试数据

`db/order-test-data.sql` 提供 30 条订单测试数据，覆盖三种订单状态、不同客户、不同金额和
不同创建时间，便于验证列表、分页与筛选：

```bash
mysql -u root -p java_simulation_project < db/order-test-data.sql
```

脚本可重复执行：先按固定 ID 区间清理旧数据，再批量写入，执行末尾会输出写入条数。

## 接口约定

页面按接口用途选择调用服务：

| 前端请求（开发环境） | 转发到 | 后端真实路径 | 说明 |
| --- | --- | --- | --- |
| `GET` `/api/consumer/orders?current=1&pageSize=20&orderNoLike=&customerId=&status=` | 服务 B（8082） | `/consumer/orders` | B 通过 Feign 调 A 分页查询 |
| `GET` `/api/consumer/orders/{id}` | 服务 B（8082） | `/consumer/orders/{id}` | B 通过 Feign 调 A 查询详情 |
| `POST` `/api/orders` | 服务 A（8081） | `/orders` | 新增订单，返回订单 ID |
| `PUT` `/api/orders/{id}` | 服务 A（8081） | `/orders/{id}` | 完整修改订单 |
| `DELETE` `/api/orders/{id}` | 服务 A（8081） | `/orders/{id}` | 删除订单（物理删除） |

统一响应结构为 `{ success, errorCode, msg, data }`；分页结果 `data` 为
`{ current, totalPage, total, pageSize, datas }`，`current` 从 1 开始，`pageSize` 最大 100；
订单状态取值为 `CREATED`、`PAID`、`CANCELLED`。后端错误码 `90000101` 表示订单号已存在，
`90000102` 表示订单不存在，页面已针对这两种情况给出提示。

完整接口说明见后端项目 `docs/order-api.md`。

> 注意：服务 B 返回的订单 ID、客户 ID 为 JSON 数字（Long），可能超过 JavaScript 安全整数
> 范围。前端在 `src/service/order/index.ts` 中通过 `src/service/json.ts` 做了一次大整数
> 安全解析，保证订单 ID 精度不丢失，否则编辑、删除会带着错误的 ID 请求后端并提示订单不存在。
> 删除确认框关闭了自带的成功提示，只有接口真正成功后才提示“订单删除成功”。

## 开发规范

- 目录按页面、路由、语言包、service 分层；组件名使用大驼峰，文件名使用小写中杠。
- 页面文案统一走 `t()`，新增文案时同步维护 `zh-CN`、`en-US` 两个语言包。
- 列表、查询面板、弹框表单的字段配置集中在 `views/order-management/use-config.ts`，
  配置随语言切换同步更新。
- 操作成功、失败提示使用 `useMessage`，不在页面内自行实现弹窗。
- 代码检查与格式化：

  ```bash
  yarn lint        # ESLint
  yarn lint:style  # Stylelint
  yarn format      # Prettier
  ```

- 生产构建：

  ```bash
  yarn build:prd
  ```
