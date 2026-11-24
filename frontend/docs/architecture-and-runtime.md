# 订单管理前端架构设计与运行逻辑

本文档说明订单管理前端项目的整体架构、启动流程、路由与登录机制、请求流转链路、页面设计以及关键代码位置，用于开发交接和答辩讲解。

---

## 1. 项目定位与整体架构

本项目是订单后端服务（`backend-demo`）的前端页面，基于赛博威中台前端模板（release 1.23.0）开发，运行在中台脚手架 `mp-cli` 之上。

```text
┌─────────────────────────────────────────────────────────────────────┐
│                         浏览器（localhost:3000）                      │
│                                                                     │
│  业务页面（本项目）                    mp-cli 中台脚手架（远程加载）      │
│  ├─ 订单列表 / 详情 / 新增 / 编辑       ├─ 登录页、布局、菜单、页签       │
│  ├─ 订单 service 请求层                ├─ 路由守卫、权限判断             │
│  └─ 中台组件库 CTable / CModalForm     └─ Axios 网络层（Token 注入等）   │
│                    │                              │                 │
└────────────────────┼──────────────────────────────┼─────────────────┘
                     │ Vite dev proxy                │ /api 其他请求
                     │ /api/consumer、/api/orders    │
                     ▼                              ▼
      订单调用服务 B（8082）                中台开发环境（46807）
      backend-demo-consumer-server         登录 / 用户 / 菜单 / 配置
              │  Feign + Nacos 服务发现
              ▼
      订单服务 A（8081）
      backend-demo-server
              │
              ▼
      MySQL（java_simulation_project）
```

接口调用关系（本项目按作业要求区分两类链路）：

| 前端接口 | 调用链路 | 说明 |
| --- | --- | --- |
| 列表查询、详情查询 | 浏览器 → 服务 B（8082）→ Feign → 服务 A（8081）→ MySQL | 必须经过 B 转发，用于体现 A/B 服务发现与 Feign 调用 |
| 新增、修改、删除 | 浏览器 → 服务 A（8081）→ MySQL | B 只暴露查询接口，写操作直连 A |


分层职责：

| 层 | 职责 | 位置 |
| --- | --- | --- |
| 页面层 | 列表、详情、表单弹框与交互 | `src/views/order-management` |
| 组件层 | 订单状态标签、金额展示等本模块复用组件 | `src/components` |
| 请求层 | 订单接口封装、错误语义识别 | `src/service` |
| 路由层 | 订单模块路由定义与懒加载 | `src/router` |
| 国际化层 | 中英文语言包 | `src/locales/lang` |
| 应用入口 | `setupApp` 初始化、中台配置 | `src/main.ts`、`src/index.ts` |

中台脚手架（`mp-cli`）不打包在本项目内，页面运行时通过 Module Federation 从环境地址远程加载，负责登录、布局、菜单、路由守卫、网络拦截器和权限判断。本项目只负责订单业务页面本身，这是与普通 Vue 项目最大的区别。

---

## 2. 技术栈与目录结构

### 2.1 技术栈

| 能力 | 选型 |
| --- | --- |
| 页面框架 | Vue 3.2 + TypeScript |
| 本地开发/构建 | Vite 5 |
| 基础组件 | Ant Design Vue 3.2 |
| 中台组件库 | cyberway-middle-ui-components 1.23.0 |
| 中台脚手架 | mp-cli 1.23.0（运行时模块联邦加载） |
| 网络请求 | 脚手架提供、基于 Axios 的 `axiosInstance` |
| 国际化 | vue-i18n（脚手架统一初始化） |

### 2.2 目录结构

```text
src
├── components
│   ├── order-amount.vue           订单金额展示组件（列表、详情复用）
│   └── order-status-tag.vue       订单状态标签组件（列表、详情复用）
├── locales/lang
│   ├── index.ts                   语言包汇总（zh_CN / en_US）
│   ├── zh-CN
│   │   ├── index.ts               导出各业务模块语言包
│   │   ├── global.ts              通用文案（操作、关闭等）
│   │   └── order-management.ts    订单模块文案
│   └── en-US                      英文语言包，结构同上
├── router
│   ├── index.ts                   路由汇总，combineRoutes 合并模块路由
│   └── order-management.ts        订单模块路由（懒加载）
├── service
│   ├── order
│   │   ├── index.ts               订单接口封装
│   │   └── types.ts               接口类型定义
│   └── request-error.ts           接口错误码与错误提示判断
├── views/order-management
│   ├── list.vue                   订单列表页
│   ├── use-config.ts              列表列、查询项、表单项配置
│   ├── order-form-modal.vue       新增/编辑弹框
│   └── order-detail-modal.vue     详情弹框
├── index.ts                       模块联邦入口：读取 env.json 后加载 main.ts
└── main.ts                        应用入口：setupApp 初始化
db/order-test-data.sql             订单测试数据 SQL
vite.config.ts                     构建配置与本地开发代理
```

目录划分遵循 D4 前端开发规范：页面放 `views`，模块内跨页面复用组件放 `src/components`，接口放 `src/service`，语言包放 `src/locales/lang`，路由放 `src/router`。

---

## 3. 启动流程与运行逻辑

### 3.1 启动时序

```text
index.html
  └─ 加载 /src/index.ts
       ├─ fetch /env.json                  读取当前环境所需的中台入口地址
       ├─ 写入 window.mpEnv / window.env   供 Module Federation 使用
       └─ import('./main')
            └─ main.ts
                 └─ setupApp(appConfig)    脚手架初始化（登录、布局、路由、网络、i18n）
                      ├─ loadSetting        加载模板默认配置
                      ├─ createApp          创建 Vue 实例
                      ├─ setupStore         初始化 Pinia stores
                      ├─ setupPermission    注入权限判断函数
                      ├─ setupI18n          初始化多语言，合并本项目语言包
                      ├─ setupNetwork        创建 axios 实例与拦截器
                      ├─ setupRouter         创建路由与守卫
                      ├─ beforeAppMounted    获取应用实例信息（appInstanceId）
                      └─ app.mount('#app')
```

### 3.2 环境配置

页面入口并不直接把中台地址写死，而是先读取 `public/env.json` 中当前环境对应的 `mp_cli` 脚本地址（本地开发对应内网开发环境），再由 Vite 的 Module Federation 在运行时加载。这就是本项目能使用中台登录、菜单、布局的原因。

本地开发相关变量在 `.env.development`：

```ini
VITE_BASE_API = '/api'
VITE_PROXY_ORDER_API = 'http://127.0.0.1:8081'
VITE_PROXY_CONSUMER_API = 'http://127.0.0.1:8082'
VITE_PROXY_PLATFORM_API = 'http://192.168.90.25:46807'
VITE_APP_ID = '126124990557847552'
VITE_ENV = 'local'
```

- `VITE_BASE_API`：业务请求统一前缀，使用相对路径，保证浏览器请求同源、不产生跨域。
- `VITE_PROXY_ORDER_API`：订单服务 A 地址，新增、修改、删除接口由 Vite 代理转发到这里。
- `VITE_PROXY_CONSUMER_API`：订单调用服务 B 地址，列表、详情接口由 Vite 代理转发到这里，再由 B 通过 Feign 调用 A。
- `VITE_PROXY_PLATFORM_API`：中台开发环境地址，用于登录、用户信息、菜单等平台接口。
- `VITE_APP_ID`：中台应用实例 ID，脚手架用它拉取应用配置与菜单。
- `VITE_ENV`：当前环境标识，与 `env.json` 中的环境节点对应。

### 3.3 应用入口代码

`src/main.ts` 是应用初始化入口，关键配置如下：

```ts
const app: App = await setupApp({
  appKey: 'myAppKey',
  deviceId,
  router: {
    routes,
    usePermissionCheck: false,
  },
  template: {
    template: 'AntdTpm',
  },
  afterLoginRedirect: '/order-management/list',
  env: process.env.VITE_ENV,
  langPack,
  network: {
    baseUrl: process.env.VITE_BASE_API,
    requestTimeout: 0,
  },
  appInstanceId: process.env.VITE_APP_ID,
});
```

说明：

| 配置 | 作用 |
| --- | --- |
| `router.routes` | 本项目路由，登录后由脚手架挂载到主布局下 |
| `usePermissionCheck: false` | 开发阶段关闭资源权限校验，未配置菜单资源时也能直接通过 URL 打开页面 |
| `template.template` | 中台布局模板，`AntdTpm` 为 TPM 风格 |
| `afterLoginRedirect` | 登录成功后默认进入订单列表 |
| `network.baseUrl` | 所有业务请求的公共前缀，取自环境变量 |
| `appInstanceId` | 中台应用实例，决定加载哪套应用配置与菜单 |

此外入口统一注册了未捕获异常处理：

```ts
app.config.errorHandler = (err, instance, info) => {
  console.error('[order-management] 未捕获异常', info, err, instance);
};
```

这符合 D4 规范中“在应用入口配置 `errorHandler` 集中处理未捕获错误”的要求。

---

## 4. 路由与登录机制

### 4.1 路由定义

订单模块路由在 `src/router/order-management.ts`：

```ts
const routes: RouteRecordRaw[] = [
  {
    path: basePath,
    name: routeName,
    redirect: `${basePath}/list`,
    component: RouterTransition,
    meta: {
      title: '订单管理',
      icon: '',
    },
    children: [
      {
        path: 'list',
        name: `${routeName}-list`,
        meta: {
          title: '订单列表',
          icon: '',
        },
        component: () =>
          import(
            /* webpackChunkName: "order-management-list" */ '../views/order-management/list.vue'
          ),
      },
    ],
  },
];
```

要点：

1. `routeName` 和 `basePath` 抽成常量，与 D4 示例一致；
2. 父路由使用 `RouterTransition` 作为容器，子页面按路由动态导入，实现页面级代码拆分；
3. `meta.title` 是菜单与浏览器标题的兜底文案，生产环境会被平台资源名称覆盖；
4. 路由 `name` 需要与平台资源编码对应，生产环境未注册的资源会进入 404。

`src/router/index.ts` 通过 `combineRoutes` 把本模块路由包装成“业务模块”交给脚手架：

```ts
import orderManagement from './order-management';
import myPackage from '../../package.json';
import { combineRoutes } from 'mp-cli/router/utils';

export default [...combineRoutes(orderManagement, myPackage)];
```

包装后的结构与 `package.json` 的 `name`、`description` 关联，平台生成资源时以 `name` 作为模块编码。

### 4.2 登录与路由守卫

中台脚手架的守卫逻辑（来源：mp-cli 运行时，不在本项目内）：

1. 检查本地是否存在 Token；
2. 没有 Token：非白名单页面跳转登录页，并携带 `redirect` 参数；
3. 已有 Token：首次进入时调用 `afterLogin` 获取用户信息、系统配置和应用菜单；
4. `afterLogin` 内部完成两件事：
   - 把 `appConfig.router.routes`（本项目路由）动态挂载到 `Layout` 布局路由下；
   - 根据平台返回的菜单生成对应资源路由。
5. 登录成功或刷新页面后，订单页面即可被匹配到并渲染。

因此会出现一个容易误解的现象：**刚启动时控制台打印 `No match found for location ...` 是正常的**，此时路由还没被动态注册，登录后注册完成就会正常跳转。

开发阶段如果菜单中没有“订单管理”，可在登录后的浏览器控制台执行：

```js
window.generateResourceTree();
```

确认后脚手架会把前端路由上报为中台资源（开发环境才提供该函数），再在平台资源管理中授权给当前角色即可在菜单中显示。详见 README 的“页面访问与菜单显示”。

### 4.3 为什么用 hash 路由

模板默认 `historyMode: false`，即 hash 路由，页面地址形如：

```text
http://localhost:3000/#/order-management/list
```

优点是静态资源服务器不需要额外配置 history fallback，本地开发和内网部署都更省事；如果部署环境支持 rewrite，也可以在 `router.historyMode` 打开 history 模式。

---

## 5. 请求流转链路（重点）

### 5.1 一次订单查询的完整链路（经过服务 B）

```text
订单列表 page (list.vue)
  └─ useTable 的 request -> fetchTableData()
       └─ queryOrders(query)                       src/service/order/index.ts
            └─ axiosInstance.get('/consumer/orders')
                 ├─ 请求拦截器注入公共请求头
                 │    Token / App-Instance-Id / App-Lang /
                 │    App-FrontRoute / App-FrontView / App-FrontName
                 ├─ 拼接 baseURL：/api + /consumer/orders = /api/consumer/orders
                 └─ 浏览器发出同源请求
                      └─ Vite dev proxy（仅开发环境）
                           ├─ 匹配 /api/consumer -> 127.0.0.1:8082
                           │    去掉 /api 前缀，请求服务 B 的 /consumer/orders
                           └─ 其他 /api/*       -> 中台开发环境 46807
                      └─ 服务 B（consumer）收到请求
                           └─ OrderConsumerController
                                └─ OrderApi（Feign 实现 OrderFeignService）
                                     └─ OrderClient（@FeignClient）
                                          └─ Nacos 服务发现 backend-demo-server
                                               └─ 服务 A 的 OrderController
                                                    └─ MySQL 查询并返回 Page<OrderListVo>
                           └─ 服务 B 把 A 的 ResponseResult 原样返回
            └─ 响应拦截器（脚手架默认）
                 ├─ resultCode === '00000000' 或 success === true -> resolve
                 └─ 否则 reject，页面 catch 处理
       └─ 列表数据 dataState.tableData / pagination.total 更新
```

新增、修改、删除不经过 B：`axiosInstance.post/put/delete('/orders')` → `/api/orders` → 代理到服务 A，直接调用 A 的写接口。

### 5.2 请求层代码

`src/service/order/index.ts` 统一封装订单接口，页面不直接拼请求。查询类接口按作业要求经过服务 B：

```ts
export async function queryOrders(query: OrderQuery): Promise<PageResult<OrderListItem>> {
  const { data } = await axiosInstance.get<ApiResult<PageResult<OrderListItem>>>(
    '/consumer/orders',
    {
      params: query,
    },
  );
  return data.data;
}

export async function getOrder(id: string): Promise<OrderDetail> {
  const { data } = await axiosInstance.get<ApiResult<OrderDetail>>(`/consumer/orders/${id}`);
  return data.data;
}
```

写接口则直接调用服务 A：

```ts
export async function createOrder(formData: OrderFormData): Promise<string> {
  const { data } = await axiosInstance.post<ApiResult<string>>('/orders', formData);
  return data.data;
}
```

这里有三层结构：

1. `axiosInstance` 由脚手架创建，封装了 Token、语言、应用实例等公共请求头；
2. 后端统一返回 `{ success, errorCode, msg, data }`，拦截器校验成功与否；
3. service 层把 `data.data` 拆出来，页面只拿到真正的业务数据，接口类型在 `types.ts` 中定义。

页面无需感知请求走 A 还是走 B：service 层已经按接口用途决定路径，列表、详情走 `/consumer/**`，写操作走 `/orders`。

**服务 B 返回值的精度处理（踩坑记录）**

服务 B 的响应中，订单 ID、客户 ID 是 Long 类型，被序列化成 JSON **数字**，例如 `"id":736323163322843136`；而 JavaScript 的 Number 最大安全整数是 `9007199254740991`，直接 `JSON.parse` 会得到 `736323163322843100` 这类丢失精度的值。这样前端再拿这个 ID 去调用 A 的修改/删除接口，就会带错 ID，后端返回“订单不存在”。

因此查询 B 的两个接口都加了一层自定义 `transformResponse`，在 `JSON.parse` 之前把超出安全范围的大整数加引号（`src/service/json.ts` 的 `parseJsonKeepLongAsString`），并在 service 层统一把 `id`、`customerId`、`amount` 归一化为字符串：

```ts
const { data } = await axiosInstance.get<ApiResult<PageResult<OrderListItem>>>(
  '/consumer/orders',
  {
    params: query,
    transformResponse: [parseConsumerResponse],
  },
);
```

对比效果：`JSON.parse` 得到 `736323163322843100`，安全解析后得到 `736323163322843136`，编辑、删除使用的 ID 与数据库一致。

### 5.3 本地开发代理配置

开发环境需要同时访问三类服务，通过 `vite.config.ts` 的代理按前缀分流：

```ts
server: {
  port: envConfig.VITE_PORT,
  // 本地开发代理：订单查询经服务 B 调用服务 A，写接口直连服务 A，其余接口转发到中台开发环境
  proxy: {
    '/api/orders': {
      target: envConfig.VITE_PROXY_ORDER_API || 'http://127.0.0.1:8081',
      changeOrigin: true,
      rewrite: (path: string) => path.replace(/^\/api/, ''),
    },
    '/api/consumer': {
      target: envConfig.VITE_PROXY_CONSUMER_API || 'http://127.0.0.1:8082',
      changeOrigin: true,
      rewrite: (path: string) => path.replace(/^\/api/, ''),
    },
    '/api': {
      target: envConfig.VITE_PROXY_PLATFORM_API || 'http://192.168.90.25:46807',
      changeOrigin: true,
    },
  },
},
```

注意三个细节：

- `/api/orders`、`/api/consumer` 两条规则必须写在 `/api` 前面，否则会被通用规则先匹配，请求会错误地转发到中台环境；
- 服务 A 与 B 的接口本身没有 `/api` 前缀，所以这两条规则用 `rewrite` 去掉 `/api`；中台接口本身带 `/api`，所以通用规则不做 rewrite；
- `/api/consumer` 与服务 B 的控制器路径 `/consumer/orders` 对应，浏览器地址中 `#` 前面的 `/api` 只是前端统一前缀。

生产环境不使用 Vite 代理，`VITE_BASE_API` 由构建环境配置为网关地址，`/api` 前缀由网关按同样的规则转发。

### 5.4 服务 B 转发设计（A/B 调用链路）

作业要求部分接口必须经过服务 B 转发到服务 A，本项目按“查询走 B、写入走 A”拆分：

| 接口 | 前端请求 | 经过服务 | 服务端最终处理 |
| --- | --- | --- | --- |
| 分页查询 | `GET /api/consumer/orders` | B（8082） | B 的 `OrderConsumerController.queryOrders` → `OrderFeignService` → `OrderClient` → A 的 `OrderController.queryOrders` |
| 详情查询 | `GET /api/consumer/orders/{id}` | B（8082） | B 的 `OrderConsumerController.getOrder` → Feign → A 的 `OrderController.getOrder` |
| 新增 | `POST /api/orders` | A（8081） | A 的 `OrderController.createOrder` |
| 修改 | `PUT /api/orders/{id}` | A（8081） | A 的 `OrderController.modifyOrder` |
| 删除 | `DELETE /api/orders/{id}` | A（8081） | A 的 `OrderController.deleteOrder` |

服务 B 的转发由后端代码完成，前端只负责请求路径：

1. `OrderConsumerController` 注入 `OrderApi`，在 consumer 服务中该接口的实现是 `OrderFeignService`；
2. `OrderFeignService` 持有 `OrderClient`，通过 `@FeignClient` + Nacos 服务发现调用 `backend-demo-server`；
3. B 的异常处理器把 A 的业务异常映射为 HTTP 语义：`90000101` → 409、`90000102` → 404、其他远程失败 → 502；
4. 前端因此可以按与直连 A 相同的方式处理错误码，无需区分请求经过 A 还是 B。

这样既完成了 B → A 的 Feign 调用链路，也避免了为写操作在 B 上重复定义接口。

### 5.5 错误处理策略

脚手架默认拦截器的行为（来源：mp-cli 网络层）：

| HTTP 状态 | 脚手架默认行为 |
| --- | --- |
| 401 | 提示会话超时，清除登录态并跳转登录页 |
| 500 | 弹出后端错误信息 |
| 400 | 弹出后端参数错误信息 |
| 503 | 进入系统维护页 |
| 其他（404/409 等） | 直接 reject，不弹提示 |

本项目在此基础上做了两层处理：

1. 业务错误码转换：`90000101`（订单号已存在）、`90000102`（订单不存在）在页面中通过 `useMessage().errorTip` 提示对应文案；
2. 避免重复提示：框架已经提示过的 400/500/401 不再重复弹窗。

对应代码在 `src/service/request-error.ts`：

```ts
export function getServerErrorCode(error: unknown): string {
  const axiosError = error as AxiosError<ServerErrorBody>;
  return axiosError?.response?.data?.errorCode ?? '';
}

export function isErrorPromptedByFramework(error: unknown): boolean {
  const status = (error as AxiosError)?.response?.status;
  return typeof status === 'number' && PROMPTED_STATUS_LIST.includes(status);
}
```

页面中的使用方式（`order-form-modal.vue` 的保存逻辑）：

```ts
catch (error) {
  const errorCode = getServerErrorCode(error);
  if (errorCode === '90000101') {
    errorTip(t('order-management.message.orderNoExists'));
  } else if (errorCode === '90000102') {
    errorTip(t('order-management.message.orderNotFound'));
  } else if (!isErrorPromptedByFramework(error)) {
    errorTip(t('order-management.message.submitFailed'));
  }
}
```

这样既覆盖了业务语义，又不会出现一个错误弹两次提示的问题。

服务 B 转发链路上的错误同样适用：B 的 `ConsumerExceptionHandler` 会把 A 的业务异常还原为相同语义的 HTTP 状态（`90000101` → 409、`90000102` → 404），其余远程调用失败返回 502（框架按 500 类错误提示），因此页面不需要区分请求是直连 A 还是经过 B。

### 5.6 请求相关常见问题

| 现象 | 原因 | 处理 |
| --- | --- | --- |
| `/api/orders` 返回 500 | 订单服务 A 的 8081 未启动，Vite 代理转发失败 | 启动服务 A，等待约 25 秒就绪后刷新 |
| `/api/consumer` 返回 500/502 | 订单服务 B 的 8082 未启动，或 B 调用 A 失败（A 未注册、Nacos 异常） | 按 Nacos → A → B 顺序启动并确认都已注册 |
| 列表/详情报错但写操作正常 | 服务 B 异常，A 正常 | 单独检查服务 B 与 Nacos 服务列表 |
| 登录、菜单接口 500/异常 | dev server 长时间运行或切换网络后代理连接异常 | 重启 `yarn dev` |
| 401 | 未登录或 Token 过期 | 重新登录 |
| 409 + `90000101` | 订单号重复 | 页面提示“订单号已存在” |
| 404 + `90000102` | 订单不存在或已删除 | 页面提示“订单不存在” |
| 列表正常但编辑/删除提示订单不存在 | 服务 B 返回的 Long 主键被 JSON 数字截断，前端拿到错误 ID | 已通过 `src/service/json.ts` 大整数安全解析修复，重启 dev server 后生效 |
| 删除时同时提示“操作成功”和失败 | `deleteConfirm` 默认会在点确认时先弹成功提示 | 调用时第二个参数传 `false`，接口成功后再提示 |

---

## 6. 页面设计与关键代码

### 6.1 订单列表页

页面入口 `src/views/order-management/list.vue`，结构为「查询面板 + 表格面板 + 两个弹框」，与公司最佳实践中列表页写法一致：

```vue
<c-search-panel
  :key="locale"
  :title="t('order-management.list.searchTitle')"
  :fields="searchFields"
  :label-width="90"
  :is-del-empty="true"
  @query="handleQuery"
  @reset="handleReset"
/>
<c-table
  :title="t('order-management.list.tableTitle')"
  :columns="columns"
  :data-source="dataState.tableData"
  :loading="loading"
  :pagination="pagination"
  :actions="tableActions"
  :cell-actions="cellActions"
  row-key="id"
  :scroll="{ x: 1040 }"
  @change="didTableChange"
  @did-click-cell="handleCellAction"
>
```

设计要点：

1. **表格分页交给 `useTable`**：`src/views/order-management/list.vue` 中 `useTable({ request: fetchTableData })` 统一管理 `loading`、`pagination`、`dataState`，页面只关心查询参数组装：
   ```ts
   const { loading, pagination, dataState, didTableChange, getTableData } = useTable({
     request: fetchTableData,
     config: { showTotal: true, defaultPageSize: 20 },
   });
   ```
2. **分页页码换算**：`useTable` 的 `current` 从 0 开始，而后端从 1 开始，所以在 `fetchTableData` 中做了 `current + 1`：
   ```ts
   const { datas, total } = await queryOrders({
     current: current + 1,
     pageSize,
     ...
   });
   ```
3. **总条数类型转换**：后端 Long 以字符串返回，页面用 `Number(total)` 转成数字再交给分页组件。
4. **查询条件管理**：`searchData` 保存当前查询条件，查询时先清空再合并，避免已清空的输入框仍然带着旧条件：
   ```ts
   function handleQuery(formModel: Record<string, any>) {
     Object.keys(searchData).forEach((key) => delete searchData[key]);
     Object.assign(searchData, formModel);
     getTableData(true);
   }
   ```
5. **表格列自定义渲染**：订单号列用插槽渲染为可点击链接，金额、状态交给复用组件：
   ```vue
   <template #orderNo="{ record }">
     <a-button type="link" size="small" @click="openDetail(record.id)">
       {{ record.orderNo }}
     </a-button>
   </template>
   <template #amount="{ record }">
     <order-amount :value="record.amount" />
   </template>
   <template #status="{ record }">
     <order-status-tag :status="record.status" />
   </template>
   ```
6. **行内操作**：操作列的 `did-click-cell` 事件统一处理“详情、编辑、删除”：
   ```ts
   function handleCellAction({ record, action }: { record: OrderListItem; action: CTableCellAction }) {
     if (action.key === 'details') {
       openDetail(record.id);
       return;
     }
     if (action.key === 'edit') {
       formModalRef.value?.open(record);
       return;
     }
     if (action.key === 'delete') {
       void removeOrder(record);
     }
   }
   ```
7. **删除二次确认**：使用脚手架的 `useMessage().deleteConfirm`。注意它的第二个参数 `showTip` 默认是 `true`，在点击确认时就会先弹一个“操作成功”，如果随后接口失败（如订单不存在）就会出现“成功/失败同时提示”的误导。这里传 `false` 关闭默认提示，接口成功后再提示：
   ```ts
   const confirmed = await deleteConfirm(content, false);
   if (!confirmed) return;
   await deleteOrder(record.id);
   successTip(t('order-management.message.deleteSuccess'));
   ```
8. **保存后刷新策略**：新增后回到第一页（新订单按创建时间倒序在最前），编辑后保持当前页：
   ```ts
   function handleSaved({ isCreate }: { isCreate: boolean }) {
     getTableData(isCreate);
   }
   ```

### 6.2 字段配置集中管理

列表列、查询项、表单项统一在 `src/views/order-management/use-config.ts` 中生成，页面不散落字段定义：

```ts
export function useConfig() {
  const { t } = useI18n();

  const statusOptions = computed(() =>
    ORDER_STATUS_LIST.map((status) => ({
      value: status,
      label: t(`order-management.status.${status}`),
    })),
  );

  const columns = computed<CTableColumn[]>(() => [/* 订单号、客户 ID、金额、状态、创建时间、操作 */]);
  const searchFields = computed<FormItemSchema[]>(() => [/* 订单号、客户 ID、状态 */]);
  const formFields = computed<FormItemSchema[]>(() => [/* 订单号、客户 ID、金额、状态 */]);

  return { columns, cellActions, searchFields, formFields };
}
```

这样设计有三个好处：

1. 字段只维护一处，列表展示、查询条件、新增/编辑弹框的字段含义保持一致；
2. 用 `computed` 依赖 `vue-i18n` 的 `t`，切换语言时列头、查询项、表单项会同步更新；
3. 后续增加字段只需改 `use-config.ts`、语言包和接口类型，页面模板基本不用动。

### 6.3 新增/编辑弹框

`src/views/order-management/order-form-modal.vue` 使用中台组件库的 `c-modal-form`，通过配置化表单完成新增与编辑：

```vue
<c-modal-form
  ref="modalRef"
  :key="locale"
  v-model:show="visible"
  :title="modalTitle"
  :fields="formFields"
  :form-config="formConfig"
  :width="720"
  :destroy-on-close="false"
  force-render
  :async-fn="handleSave"
  @cancel="handleCancel"
/>
```

设计要点：

1. **同一个弹框复用新增/编辑**：`editingId` 是否有值决定调用新增还是修改接口，标题也随之切换；
2. **打开时统一回显与清空**：新增时传入空值并把状态默认设为 `CREATED`，编辑时把列表行数据回填表单；金额、客户 ID 从字符串转为数字，保证输入框正常显示：
   ```ts
   modalRef.value?.setFieldsValue({
     orderNo: record?.orderNo ?? '',
     customerId: record ? Number(record.customerId) : undefined,
     amount: record ? Number(record.amount) : undefined,
     status: record?.status ?? 'CREATED',
   });
   ```
3. **提交前做数据归一化**：订单号 `trim`，客户 ID、金额转 Number，避免提交字符串导致后端参数绑定失败；
4. **提交结果处理**：成功提示并关闭弹框、通知列表刷新；失败按业务错误码提示，弹框保持打开方便修改；
5. **`force-render` + `destroy-on-close=false`**：保证弹框组件提前渲染、表单实例可用，`open` 时能可靠回显；配合 `:key="locale"` 在切换语言时重建表单文案。

### 6.4 详情弹框

`src/views/order-management/order-detail-modal.vue` 使用 `c-modal` + Ant Design Vue 的 `a-descriptions` 展示详情，打开时按 ID 查询最新数据：

```ts
async function open(id: string) {
  visible.value = true;
  loading.value = true;
  detail.value = {};
  try {
    detail.value = await getOrder(id);
  } catch (error) {
    // 订单不存在等异常提示
    visible.value = false;
  } finally {
    loading.value = false;
  }
}
```

详情为什么不用单独路由页：订单详情字段少、与列表强相关，弹框交互成本低、返回状态简单；如果后续需要分享详情链接或更复杂的详情页，可以改为独立路由页。

### 6.5 复用组件

| 组件 | 使用位置 | 说明 |
| --- | --- | --- |
| `components/order-status-tag.vue` | 列表状态列、详情弹框 | 状态编码到颜色、文案的统一映射 |
| `components/order-amount.vue` | 列表金额列、详情弹框 | 金额千分位、两位小数、带货币符号展示 |

提取依据遵循 D4 规范：被两处及以上使用、且职责可以用一句话说明。状态标签内部用 `computed` 跟随语言切换：

```ts
const statusLabel = computed(() =>
  props.status ? t(`order-management.status.${props.status}`) : '--',
);
```

金额组件负责把后端返回的字符串金额安全转成展示文本，接口返回空值或非法值时显示 `--`：

```ts
const text = amount.toLocaleString('zh-CN', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});
return `¥ ${text}`;
```

---

## 7. 国际化设计

### 7.1 语言包结构

`src/locales/lang/index.ts` 汇总中英文语言包并交给 `setupApp`：

```ts
import * as zhCN from './zh-CN';
import * as enUS from './en-US';
export default { zh_CN: zhCN, en_US: enUS, zh_HK: {} };
```

每个语言目录下按模块拆分：

```text
zh-CN
├── index.ts               导出 global、order-management 等模块
├── global.ts              通用文案
└── order-management.ts    订单模块文案
```

### 7.2 key 的命名规则（容易踩坑）

脚手架初始化 i18n 时，会把语言包顶层模块名从驼峰转换为中杠形式，例如代码中导出的 `orderManagement` 在运行时对应的 key 前缀是 `order-management`。因此页面里的写法是：

```ts
t('order-management.list.orderNo')
t('order-management.message.createSuccess')
```

而不是 `t('orderManagement.list.orderNo')`。状态这种运行时拼接的 key 也一样：

```ts
t(`order-management.status.${props.status}`)
```

### 7.3 页面中的使用方式

| 场景 | 方式 |
| --- | --- |
| 模板/页面提示 | `import { t } from 'mp-cli/i18n'`，用全局中台语言实例 |
| 配置类代码（`use-config.ts`） | `useI18n()` 取 `t`，配合 `computed` 在语言切换时更新 |
| 状态标签、金额组件 | 组件内部 `useI18n()` + `computed` |
| 表格/查询/表单 | 配置用 `computed` 生成；`c-search-panel` 的字段只在初始化时读取，因此用 `:key="locale"` 在语言切换时重建 |

---

## 8. 数据与接口设计

### 8.1 订单模型（与后端一致）

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | Long → 前端 string | 主键，后端序列化为字符串避免精度丢失 |
| `orderNo` | string | 订单号，唯一，最长 32 |
| `customerId` | Long → 前端 string | 客户 ID |
| `amount` | BigDecimal → 前端 string | 金额，`DECIMAL(12,2)`，最多两位小数 |
| `status` | enum | `CREATED` / `PAID` / `CANCELLED` |
| `createdAt` | datetime | 创建时间 |
| `updatedAt` | datetime | 最后修改时间（仅详情返回） |

前端类型定义在 `src/service/order/types.ts`，接口返回类型与后端 VO 保持一致。

### 8.2 接口清单

| 前端请求（开发环境） | 前端调用 | 调用链路 | 后端真实路径 |
| --- | --- | --- | --- |
| `GET` `/api/consumer/orders?current=&pageSize=&orderNoLike=&customerId=&status=` | `queryOrders` | B → A | 服务 B `/consumer/orders` |
| `GET` `/api/consumer/orders/{id}` | `getOrder` | B → A | 服务 B `/consumer/orders/{id}` |
| `POST` `/api/orders` | `createOrder` | A | 服务 A `/orders` |
| `PUT` `/api/orders/{id}` | `modifyOrder` | A | 服务 A `/orders/{id}` |
| `DELETE` `/api/orders/{id}` | `deleteOrder` | A | 服务 A `/orders/{id}` |

后端统一返回 `{ success, errorCode, msg, data }`；分页 `data` 为 `{ current, totalPage, total, pageSize, datas }`。分页结果按 `created_time DESC, id DESC` 稳定排序，由后端保证。服务 B 的分页接口复用 A 的 `OrderPageDto`，因此筛选参数与直连 A 时一致。

### 8.3 测试数据

`db/order-test-data.sql` 提供 30 条测试订单，覆盖三种状态、不同客户、不同金额和不同创建时间，用于验证列表、分页和筛选。脚本先按固定 ID 区间清理再插入，可重复执行。

---

## 9. 运行方式

### 9.1 本地开发

1. 启动 Nacos、MySQL；
2. 启动订单后端服务 A（8081）与服务 B（8082），确认两者都已注册到 Nacos；
3. 前端项目执行 `yarn` 安装依赖，再执行 `yarn dev`；
4. 浏览器访问 `http://localhost:3000`，登录中台；
5. 从菜单进入订单管理，或直接访问 `http://localhost:3000/#/order-management/list`。

注意：列表和详情依赖服务 B，新增、修改、删除依赖服务 A，测试完整增删改查前需要两个服务都处于运行状态。

### 9.2 构建

| 命令 | 说明 |
| --- | --- |
| `yarn build:prd` | 生产构建 |
| `yarn build:test` | 测试环境构建 |
| `yarn build:dev` | 开发环境构建 |
| `yarn lint` | ESLint 检查 |
| `yarn lint:style` | Stylelint 检查 |
| `yarn format` | Prettier 格式化 |

构建后中台脚手架仍以模块联邦方式在运行时加载，本项目产物作为业务模块资源使用。

---

## 10. 关键代码索引

| 文件 | 关键内容 | 讲解要点 |
| --- | --- | --- |
| `src/index.ts` | 读取 `env.json`、挂载 `window.mpEnv`、加载 `main.ts` | 中台入口地址是运行时决定的 |
| `src/main.ts` | `setupApp` 配置、未捕获异常处理 | 登录、布局、路由、网络都由脚手架初始化 |
| `src/router/order-management.ts` | 订单路由、`RouterTransition`、懒加载 | 路由 name 对应平台资源编码 |
| `src/router/index.ts` | `combineRoutes` 合并模块路由 | 模块编码来自 `package.json` |
| `src/service/order/index.ts` | 订单接口封装：查询走服务 B，写操作走服务 A | 区分 A/B 调用链路的入口 |
| `src/service/order/types.ts` | 接口与类型定义 | Long/BigDecimal 用字符串接收 |
| `src/service/json.ts` | 大整数安全的 JSON 解析 | 解决 B 返回 Long 数字导致的订单 ID 精度丢失 |
| `src/service/request-error.ts` | 错误码、默认提示判断 | 避免业务提示与框架提示重复 |
| `src/views/order-management/list.vue` | 查询、分页、行内操作、删除 | `useTable` 数据流与页码换算 |
| `src/views/order-management/use-config.ts` | 表格列、查询项、表单项配置 | 配置复用与语言切换 |
| `src/views/order-management/order-form-modal.vue` | 新增/编辑复用、表单校验、错误码提示 | 回显、归一化、保存后刷新 |
| `src/views/order-management/order-detail-modal.vue` | 详情加载与展示 | 加载态、异常关闭 |
| `src/components/order-status-tag.vue` | 状态颜色与文案 | 复用依据 |
| `src/components/order-amount.vue` | 金额格式化 | 字符串金额转展示文本 |
| `src/locales/lang/zh-CN/order-management.ts` | 订单模块文案 | key 使用 `order-management` 前缀 |
| `vite.config.ts` | 开发代理、构建配置 | `/api/orders`、`/api/consumer` 与 `/api` 分流 |
| `db/order-test-data.sql` | 测试数据 | 可重复执行 |

---

## 11. 导师可能问到的问题（准备好直接回答）

### 11.1 架构与请求链路

**Q1：前端请求是怎么流转到后端的？**
查询类请求：浏览器 → 页面调用 service 层 → 脚手架 `axiosInstance`（自动注入 Token、语言、应用实例等请求头）→ baseURL `/api` 拼接出 `/api/consumer/orders` → 本地开发时由 Vite 代理转发到 `127.0.0.1:8082` 的服务 B `/consumer/orders` → 服务 B 通过 Feign + Nacos 调用服务 A → A 查询数据库返回统一响应 → B 原样返回 → 拦截器判断成功/失败 → service 返回 `data.data` → 页面更新表格。
写操作请求：`/api/orders` 由 Vite 代理转发到 `127.0.0.1:8081` 的服务 A，直接执行新增、修改、删除。
生产环境不走 Vite 代理，`/api` 直接指向网关。

**Q2：为什么不直接请求 `http://127.0.0.1:8081`？**
会跨域。使用相对路径 `/api` + Vite 代理，浏览器请求同源，开发环境不需要后端开 CORS；同时通过环境变量切换后端地址，不改代码。

**Q3：为什么 `/api/orders`、`/api/consumer` 要单独代理，其他 `/api` 去中台？**
本项目同时需要调用三类服务：订单服务 A（本地 8081）、订单调用服务 B（本地 8082）和中台公共接口（登录、用户、菜单）。代理按前缀分流：`/api/consumer` 去掉前缀命中 B 的 `/consumer/orders`，`/api/orders` 去掉前缀命中 A 的 `/orders`，其余请求转发到中台开发环境。

**Q3.1：为什么列表和详情一定要经过服务 B？**
这是本次作业的硬性要求之一，用于验证 B → A 的服务发现和 Feign 调用链路。所以前端查询类接口统一走 B 的 `/consumer/orders`、`/consumer/orders/{id}`；B 只暴露查询接口，新增、编辑、删除没有在 B 上重复定义，前端直接调用 A 的写接口，职责更清晰。

**Q3.2：经过 B 之后，错误码处理需要改吗？**
不需要。B 的异常处理器会把 A 的业务异常还原成相同的 HTTP 语义（`90000101` → 409、`90000102` → 404），前端仍按同一个错误码判断并提示；B 自身调用 A 失败时返回 502，属于系统异常，由框架统一提示。

**Q3.3：为什么列表页能显示数据，但点编辑/删除会提示“订单不存在”？**
这是大整数精度问题导致的：B 把 Long 类型的订单 ID 序列化成 JSON 数字，浏览器 `JSON.parse` 时超过 `2^53-1` 的部分被截断，前端拿着被截断的 ID 去调 A 的修改/删除接口就查不到订单。处理方式是在 service 层对 B 的响应做安全解析（`src/service/json.ts`），把大整数保留为字符串；同时页面展示的 ID 与数据库主键完全一致，问题就消失了。

**Q3.4：为什么删除时先弹出“操作成功”，随后又提示失败？**
`useMessage().deleteConfirm(content, showTip)` 的第二个参数默认是 `true`，在用户点“确认”时就会先弹一次成功提示，与接口实际结果无关。页面调用时传 `false`，改为接口成功后再统一 `successTip`，避免误导。

**Q4：网络拦截器做了什么？**
请求侧注入 Token、`App-Instance-Id`、`App-Lang`、当前路由信息等 Header；响应侧根据 `resultCode`/`success` 判断成功，失败时按 HTTP 状态统一处理 401/400/500/503。业务页面只处理自身业务错误码，不重复实现全局错误逻辑。

**Q5：为什么 service 里要 `return data.data`？**
后端统一包装了一层 `ResponseResult`，拦截器已经校验成功与否，service 层把真正业务数据拆出来给页面，页面不需要关心响应包装结构。

### 11.2 页面与组件设计

**Q6：分页是怎么实现的？**
表格使用中台组件库的 `useTable`，它管理 `loading`、`pagination`、`dataState`，`request` 回调负责组装参数调接口。注意它的 `current` 从 0 开始，而后端从 1 开始，所以 `fetchTableData` 里做了 `current + 1`；`total` 后端返回字符串，需要 `Number()` 转换。

**Q7：为什么用弹框做新增/编辑，不用单独页面？**
列表与表单强相关、字段不多，弹框交互成本低，保存后能直接刷新列表并保持上下文；如果后续表单字段变多或需要独立链接，可以拆成路由页。

**Q8：新增和编辑是怎么复用的？**
同一个 `order-form-modal`，用 `editingId` 区分：为空的打开是新增，有值为编辑。打开时统一重置表单并回填数据，保存时根据 `editingId` 调 `createOrder` 或 `modifyOrder`。

**Q9：组件为什么要拆出 `OrderStatusTag`、`OrderAmount`？**
这两个组件分别在列表和详情两处使用，职责单一（状态展示、金额格式化），符合 D4 中“被两处及以上使用、职责可以用一句话说明”的提取条件；拆出后状态颜色映射和金额格式化只维护一份。

**Q10：字段配置为什么集中到 `use-config.ts`？**
列表列、查询项、表单字段都依赖同一份字段语义，集中在配置里便于维护；同时用 `computed` 依赖 i18n，切换语言时相关文案能同步更新。

### 11.3 校验、错误与交互

**Q11：前端校验和后端校验是什么关系？**
前端校验用于提升体验、减少无效请求：订单号必填且最长 32、客户 ID 大于 0、金额大于 0 且最多两位小数、状态必填，规则与后端 Bean Validation 保持一致。后端仍有完整校验和数据库唯一约束，前端校验不替代后端校验；像订单号重复这种并发问题最终由后端唯一约束兜底，前端根据错误码 `90000101` 提示。

**Q12：为什么提示有的弹、有的不弹？**
脚手架默认拦截器已经对 400/500/401 弹提示，页面再弹会重复。所以页面只针对框架不处理的业务状态码（如 409 的 `90000101`、404 的 `90000102`）调用 `useMessage().errorTip`，并用 `isErrorPromptedByFramework` 避免重复提示。

**Q13：删除为什么用物理删除？**
这是后端接口的既定语义（README 和接口文档注明物理删除，重复删除返回订单不存在）。前端只负责二次确认和错误提示，删除语义由后端决定。

### 11.4 路由、权限与国际化

**Q14：页面路由是怎么挂到中台菜单上的？**
本项目通过 `combineRoutes` 把订单模块路由交给 `setupApp`；登录后脚手架在 `afterLogin` 中把业务路由动态挂到主布局，并按平台返回的菜单资源生成路由与权限。开发环境可以在控制台执行 `window.generateResourceTree()` 把路由上报为中台资源，再授权给角色。

**Q15：`usePermissionCheck: false` 是什么意思？会有安全问题吗？**
它关闭的是“页面路由 name 必须在用户权限资源里”的前端校验，开发阶段方便没有菜单资源时直接通过 URL 打开页面。真正接口安全由后端控制，前端权限只影响页面/按钮展示。生产环境如果需要按资源控制页面访问，应把它打开并配置好资源。
表格行内操作、按钮也可以配置 `permission` 权限码（中台规范是 `xxx-viewable` / `xxx-actionable`），项目目前没有配置，后续接入权限时直接补在 `cellActions`、`actions` 上即可。

**Q16：国际化是怎么做的？**
语言包按语言、按模块拆分在 `src/locales/lang`，`setupApp` 初始化时合并；页面用 `t('order-management.xxx')`。注意脚手架会把语言包顶层模块名的驼峰转成中杠，所以订单模块 key 前缀是 `order-management`。配置类字段用 `computed` 包住，切换语言时列头、查询项会更新；`c-search-panel` 的 fields 只在初始化时读取，所以用 `:key="locale"` 触发重建。

**Q17：路由为什么用懒加载？**
页面通过 `import()` 动态引入，Vite 按路由拆分 chunk，首屏不需要加载列表页代码；这也是 D4 规范中“页面组件按路由动态导入”的要求。

### 11.5 工程与质量

**Q18：代码检查怎么做的？**
`yarn lint` 跑 ESLint（Vue/TypeScript 规则）、`yarn lint:style` 跑 Stylelint、`yarn format` 跑 Prettier；提交前已保证三项通过。模板原有的 `lint-staged` 也会在提交时自动执行格式化与检查。

**Q19：接口数据类型为什么要用 string 接金额和 ID？**
后端 Long/BigDecimal 序列化为字符串，避免 JavaScript `Number` 的精度丢失。展示时统一格式化，提交表单时再转成数字，既保证精度又保证交互。

**Q20：如果要做接口自动化测试或前端单测，怎么补？**
后端已有 JUnit + Postman 用例；前端可以在现有 Vite 工程中接 Vitest，优先覆盖金额格式化、状态映射、错误码判断等纯函数和组件事件，模板本身没有内置测试框架，需要额外引入并补充 `test` 脚本。

### 11.6 联调与排障

**Q21：登录后菜单没有“订单管理”、直接访问 404 怎么办？**
1. 确认访问的是 `http://localhost:3000`（本地 Vite 服务），不是中台演示环境地址；
2. 开发环境控制台执行 `window.generateResourceTree()` 上报资源；
3. 在平台资源管理中把订单资源授权给当前角色，刷新/重新登录；
4. 项目已设置 `usePermissionCheck: false`，资源未配置时直接访问 `/#/order-management/list` 也可以打开页面。

**Q22：页面里请求出现 500 怎么排查？**
先看 F12 Network 里红的是哪个 URL：
- `/api/consumer/*` → 服务 B（8082）未启动，或 B 调用 A 失败（A 未注册、Nacos 异常），按 Nacos → A → B 顺序检查；
- `/api/orders` → 服务 A（8081）未启动或正在启动（Vite 代理连不上会返回 500），启动服务 A 后重试；
- 平台接口（`/api/uc/*`、`/api/ca/*` 等）→ 中台开发环境问题，或本地 dev server 长时间运行、切换网络后代理连接异常，重启 `yarn dev`；
- 页面模块资源 500（Vite 错误覆盖层）→ 看终端里 `Internal server error` 的具体文件，一般是编译/依赖缓存问题，删除 `node_modules/.vite` 后重启。

**Q23：如何新增一个订单字段（比如备注）？**
按顺序改：后端 DTO/VO/数据库字段 → `src/service/order/types.ts` 类型 → `use-config.ts` 的表格列、查询项、表单字段 → 语言包（中英文）→ 详情弹框展示。前端各层已经按字段配置组织，改动集中在配置和类型两处。

---

## 12. 设计取舍与可改进点

| 现状 | 取舍 | 后续可改进 |
| --- | --- | --- |
| 详情/编辑用弹框 | 交互轻量，与列表上下文连续 | 字段多或需分享时改为路由页 |
| 表格查询按钮触发 | 与中台列表规范一致，避免频繁请求 | 若改输入即查询，可加 `lodash-es` 防抖并在卸载时取消 |
| 关闭权限校验 | 方便开发阶段没有菜单资源时联调 | 生产前按资源打通后打开 `usePermissionCheck` 并补 `permission` 权限码 |
| 查询走 B、写入走 A | 满足 A/B 调用链路要求，B 不重复定义写接口 | 如果后续所有流量都要经过 B，可为 A 的写接口补 B 的转发接口 |
| 字段配置用 `computed` | 切换语言即时更新 | 复杂表单可进一步拆成字段工厂函数复用 |
| 未引入前端单测 | 模板未内置测试框架，避免改造依赖 | 关键工具函数与组件事件补 Vitest 用例 |
