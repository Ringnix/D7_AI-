# 后端 Demo 架构与 Postman 联调指南

本文按当前工程的实际代码和运行结果编写。目标不是只解释“分层名词”，而是回答三个问题：

1. A、B 分别负责什么。
2. B 为什么能调用 A，以及它如何知道服务名、实例地址和接口路径。
3. 如何用 Postman 从零完成一轮可验证的订单接口测试。

## 1. 先记住一句话

A 是订单数据提供者，B 是远程调用示例。B 不直接连接 MySQL，它只保存调用 A 的接口契约，通过 Nacos 找到 A 的实例，再发出 HTTP 请求。

```text
Postman
  |
  | GET /consumer/orders/{id}
  v
B 服务 backend-demo-consumer-server:8082
  |
  v
OrderConsumerController
  |
  v
OrderApi 接口
  |
  v
OrderFeignService
  |
  v
OrderClient Feign 代理
  |
  | 按服务名 backend-demo-server 向 Nacos 查询实例
  v
Nacos 命名空间 local
  |
  | 返回 A 的实例 IP:8081
  v
A 服务 backend-demo-server:8081
  |
  | GET /orders/{id}
  v
OrderController
  |
  v
OrderApplicationService
  |
  v
OrderDomainService
  |
  v
OrderRepository / DAL
  |
  v
MySQL
```

直接访问 A 的请求没有经过 B：

```text
Postman -> A:8081 /orders/{id} -> Controller -> Service -> MySQL
```

访问 B 的请求才会经过远程调用：

```text
Postman -> B:8082 /consumer/orders/{id} -> Feign -> Nacos -> A:8081 /orders/{id}
```

## 2. 模块之间是什么关系

| 模块 | 作用 | 是否包含 HTTP Controller | 是否访问数据库 |
| --- | --- | --- | --- |
| `backend-demo-api` | 放 `OrderApi`、DTO、VO、枚举、错误码等公共契约 | 否 | 否 |
| `backend-demo-context` | 订单业务实现、Repository、Liquibase、数据库配置 | 否 | 是 |
| `backend-demo-starter` | 装配 Context，并暴露 A 的 `OrderController` | 是 | 通过 Context |
| `backend-demo-feign-starter` | 安装 Feign Client，并用 `OrderFeignService` 调用 A | 否 | 否 |
| `backend-demo-server` | A 的启动模块，依赖本地 Starter | 通过 Starter | 是 |
| `backend-demo-consumer-server` | B 的启动模块，依赖 Feign Starter | 有自己的调用 Controller | 否 |

### 2.1 为什么 API 和实现要拆开

`OrderApi` 只描述能力：

```java
long createOrder(OrderCreateDto dto);
OrderVo getOrder(long id);
Page<OrderListVo> queryOrders(OrderPageDto dto);
void modifyOrder(long id, OrderModifyDto dto);
void deleteOrder(long id);
```

它不关心实现是“本地 Service”还是“远程 Feign 调用”。因此同一个接口有两个实现：

- A 使用 `OrderApplicationService` 在本地执行业务。
- B 使用 `OrderFeignService` 把调用转给 A。

具体文件：

- `backend-demo-api/src/main/java/com/cyberway/mp/demo/backenddemo/api/order/OrderApi.java`
- `backend-demo-context/src/main/java/com/cyberway/mp/demo/backenddemo/application/service/OrderApplicationService.java`
- `backend-demo-feign-starter/src/main/java/com/cyberway/mp/demo/backenddemo/feign/service/OrderFeignService.java`

### 2.2 为什么依赖不同 Starter 就会得到不同实现

A 的 `backend-demo-starter/pom.xml` 依赖 `backend-demo-context`，所以 A 会加载 Context 配置，创建：

- `OrderDomainService`
- `OrderApplicationService`

B 的 `backend-demo-consumer-server/pom.xml` 依赖 `backend-demo-feign-starter`，但不会引入 A 的 Context 和 MySQL。Feign Starter 的自动配置会创建：

- `OrderFeignService`

两个 Starter 都通过 Spring Boot 的自动配置入口生效：

```text
src/main/resources/META-INF/spring/
org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

这就是中台脚手架“开发时只依赖 API，部署时选择本地 Starter 或 Feign Starter”的实际含义。

## 3. 为什么由 B 调用 A

订单数据属于 A。创建、查询、修改、删除以及数据库事务都应在 A 内完成。B 没有订单数据源，也不应该绕过 A 直接操作订单表。

B 的作用是：

1. 模拟另一个微服务或上层业务服务。
2. 证明服务拆分后能够通过服务发现调用 A。
3. 证明调用方不需要写死 A 的 IP 和端口。
4. 证明 A 的业务错误能够跨服务传递。

因此“B 调 A”不是核心订单功能必须经过两次网络请求，而是作业要求的跨服务拆分部署演示。

## 4. B 如何知道要调用哪个服务

关键代码在 `OrderClient`：

```java
@FeignClient(
        contextId = "OrderClient",
        value = "${backend-demo.feign.server-name:backend-demo-server}",
        path = "/orders")
public interface OrderClient {
}
```

这里没有写：

```text
http://127.0.0.1:8081
```

它写的是服务名：

```text
backend-demo-server
```

B 的 `bootstrap.yml` 把这个名称配置为：

```yaml
backend-demo:
  feign:
    server-name: backend-demo-server
```

创建过程如下：

1. `BackendDemoFeignAutoConfiguration` 扫描 Feign Client。
2. Spring 为 `OrderClient` 创建动态代理。
3. B 调用代理方法时，Spring Cloud LoadBalancer 使用服务名查询 Nacos。
4. Nacos 返回 `backend-demo-server` 在当前命名空间中的健康实例。
5. LoadBalancer 选择一个实例，例如 `192.168.x.x:8081`。
6. Feign 把代理方法转换成普通 HTTP 请求。

所以 B 知道的是“逻辑服务名”，不是固定 IP。

## 5. B 如何知道调用哪个接口路径

接口由 Controller 映射和 Feign Client 注解共同确定。

用户访问 B 的入口：

```java
@RequestMapping("/consumer/orders")
public class OrderConsumerController {
    @GetMapping("/{id}")
    public ResponseResult<OrderVo> getOrder(long id) {
        return ResponseResult.successResult(orderApi.getOrder(id));
    }
}
```

因此对 B 的请求是：

```http
GET /consumer/orders/5
```

B 内部调用 A 的契约：

```java
@FeignClient(path = "/orders")
public interface OrderClient {
    @GetMapping("/{id}")
    ResponseResult<OrderVo> getOrder(@PathVariable("id") long id);
}
```

这两段路径拼接后，B 实际向 A 发送：

```http
GET /orders/5
```

最终目标可以理解为：

```text
http://{Nacos返回的A实例IP}:{端口}/orders/5
```

对应 A 的 Controller：

```java
@RequestMapping("/orders")
public class OrderController {
    @GetMapping("/{id}")
    public ResponseResult<OrderVo> getOrder(long id) {
    }
}
```

因此“调用哪个接口”由 Feign 方法上的 HTTP 注解决定：

- `@GetMapping("/{id}")` 决定方法和路径。
- `@PathVariable` 决定路径参数。
- `@SpringQueryMap` 决定查询参数。
- `@RequestBody` 决定 JSON 请求体。
- `ResponseResult<OrderVo>` 决定返回 JSON 的反序列化类型。

## 6. `OrderApi` 在 B 的运行时为什么变成远程调用

`OrderConsumerController` 注入的是接口：

```java
private final OrderApi orderApi;
```

由于 B 只引入了 Feign Starter，运行时容器中的实现是：

```text
OrderApi -> OrderFeignService -> OrderClient -> HTTP -> A
```

在 A 中，同样的 `OrderApi` 实现是：

```text
OrderApi -> OrderApplicationService -> OrderDomainService -> MySQL
```

相同接口、不同实现，由部署模块选择的 Starter 决定。这就是当前项目架构最核心的设计。

## 7. Nacos 中到底注册了什么

两个服务启动配置分别来自：

- `backend-demo-server/src/main/resources/bootstrap.yml`
- `backend-demo-consumer-server/src/main/resources/bootstrap.yml`

关键配置：

```yaml
spring:
  application:
    name: backend-demo-server
  cloud:
    nacos:
      discovery:
        enabled: true
        namespace: ${NACOS_NAMESPACE:local}
      server-addr: ${NACOS_ADDR:127.0.0.1:8848}
```

B 的配置同理，但 `spring.application.name` 是：

```text
backend-demo-consumer-server
```

启动成功后，Nacos 中的逻辑结构是：

```text
命名空间：local
分组：DEFAULT_GROUP

服务 backend-demo-server
  实例：<本机局域网IP>:8081
  健康：true

服务 backend-demo-consumer-server
  实例：<本机局域网IP>:8082
  健康：true
```

当前实测结果：

```text
local 命名空间服务数：2
服务名：backend-demo-consumer-server、backend-demo-server
```

默认 `public` 命名空间的运行实例数为 0。

## 8. 为什么控制台里可能看不到服务

最常见原因是控制台当前选中的是 `public`，而应用运行在 `local`。

### 8.1 从页面检查

1. 打开 `http://127.0.0.1:8848/nacos`。
2. 使用本机 Nacos 账号登录。
3. 在页面右上角或命名空间选择区域选择 `local`。
4. 打开“服务管理”中的“服务列表”。
5. 搜索 `backend-demo-server` 和 `backend-demo-consumer-server`。

如果页面仍在 `public`，看到 0 个服务并不代表应用没有注册。

### 8.2 名称和命名空间 ID 必须一致

应用配置使用的是命名空间 ID：

```text
local
```

如果控制台里创建了一个“显示名称为 local、ID 为随机 UUID”的命名空间，应用默认不会自动进入那个 UUID 命名空间。

本工程要确认：

```text
命名空间 ID：local
```

不只是显示名称叫 `local`。

### 8.3 使用接口直接检查

下面命令只从环境变量读取账号和密码，不把密码写入 URL 或源码：

```powershell
$nacosAddress = if ($env:NACOS_ADDR) { $env:NACOS_ADDR } else { "127.0.0.1:8848" }
$nacosNamespace = if ($env:NACOS_NAMESPACE) { $env:NACOS_NAMESPACE } else { "local" }

$login = Invoke-RestMethod `
  -Method Post `
  -Uri "http://$nacosAddress/nacos/v1/auth/login" `
  -Body @{
    username = $env:NACOS_USERNAME
    password = $env:NACOS_PASSWORD
  }

$headers = @{
  Authorization = "Bearer $($login.accessToken)"
}

Invoke-RestMethod `
  -Uri "http://$nacosAddress/nacos/v1/ns/service/list?pageNo=1&pageSize=100&namespaceId=$nacosNamespace" `
  -Headers $headers
```

正常结果应包含：

```text
count: 2
doms:
  backend-demo-consumer-server
  backend-demo-server
```

继续检查 A 的实例：

```powershell
Invoke-RestMethod `
  -Uri "http://$nacosAddress/nacos/v1/ns/instance/list?serviceName=backend-demo-server&namespaceId=$nacosNamespace" `
  -Headers $headers
```

重点看：

- `ip`
- `port`
- `healthy`
- `enabled`
- `ephemeral`

本工程应当看到 `port=8081`、`healthy=true`。

Nacos 默认注册网卡的局域网 IP，因此不一定是 `127.0.0.1`。只要 B 和 A 能互通，局域网 IP 是正常现象。

### 8.4 仍看不到时的排查顺序

| 检查项 | 正常表现 | 异常时说明 |
| --- | --- | --- |
| A 进程 | 8081 正在监听 | 应用启动失败，尚未注册 |
| B 进程 | 8082 正在监听 | 应用启动失败，尚未注册 |
| Nacos 进程 | 8848 和 9848 正在监听 | Nacos 2.x 客户端还需要 gRPC 端口 |
| `NACOS_ADDR` | `127.0.0.1:8848` | 地址错误会注册到别的 Nacos |
| `NACOS_NAMESPACE` | `local` | 两个服务必须使用同一命名空间 |
| Nacos 账号密码 | 登录和 API 查询成功 | 鉴权失败会阻止注册 |
| 控制台命名空间 | 已选中 `local` | 选 `public` 会看到 0 个实例 |
| 应用日志 | 无 Nacos 连接或鉴权异常 | 先解决启动日志中的第一个错误 |

临时实例在进程退出后会从 Nacos 消失，这是正常行为。

## 9. 当前服务的启动方法

### 9.1 检查端口

```powershell
Get-NetTCPConnection -State Listen |
  Where-Object { $_.LocalPort -in 3306,8848,9848,8081,8082 } |
  Select-Object LocalAddress,LocalPort,OwningProcess
```

应当能看到：

- MySQL：`3306`
- Nacos HTTP：`8848`
- Nacos gRPC：`9848`
- A：`8081`
- B：`8082`

### 9.2 环境变量

应用配置只读取变量，不保存账号密码：

```text
NACOS_ADDR
NACOS_NAMESPACE
NACOS_USERNAME
NACOS_PASSWORD
DB_URL
DB_USERNAME
DB_PASSWORD
```

按 Windows“系统变量”的要求，应在管理员权限下打开：

```text
系统属性 -> 高级 -> 环境变量 -> 系统变量
```

设置完成后必须新开终端或重启 IDE。已经启动的进程不会自动读取后来新增的系统变量。

不要在系统变量中设置唯一的 `SERVER_PORT` 再启动两个服务。A、B 需要不同端口，启动命令应分别显式指定 `8081` 和 `8082`。

### 9.3 构建

先停止旧的 A、B Java 进程，避免 Windows 锁住 JAR：

```powershell
mvn -ntp test
mvn -q -ntp -DskipTests package
```

### 9.4 启动 A

在第一个 PowerShell 窗口：

```powershell
java -jar backend-demo-server/target/backend-demo-server-0.0.1-SNAPSHOT.jar `
  --server.port=8081
```

A 启动时会：

1. 连接 Nacos 并注册 `backend-demo-server:8081`。
2. 连接 MySQL。
3. 执行尚未执行的 Liquibase changeSet。
4. 启动 `OrderController` 和 `DemoController` 等接口。

### 9.5 启动 B

在第二个 PowerShell 窗口：

```powershell
java -jar backend-demo-consumer-server/target/backend-demo-consumer-server-0.0.1-SNAPSHOT.jar `
  --server.port=8082
```

B 启动时会注册 `backend-demo-consumer-server:8082`。B 没有订单数据库连接。

### 9.6 快速检查

```powershell
Invoke-RestMethod "http://127.0.0.1:8081/demo/hello?name=World"
Invoke-RestMethod "http://127.0.0.1:8081/orders/910000000000000002"
Invoke-RestMethod "http://127.0.0.1:8082/consumer/orders/910000000000000002"
```

第三条成功，说明 B 已经通过 Nacos/Feign 找到 A。

## 10. Postman 手把手测试

### 10.1 导入集合

1. 打开 Postman。
2. 点击左上角 `Import`。
3. 选择：

```text
postman/order-management.postman_collection.json
```

4. 导入后打开集合 `订单管理接口回归`。

集合使用集合变量，不需要先创建环境。

### 10.2 检查变量

点击集合名称，再打开 `Variables`：

| 变量 | 初始值 | 用途 |
| --- | --- | --- |
| `aBaseUrl` | `http://127.0.0.1:8081` | A 服务 |
| `bBaseUrl` | `http://127.0.0.1:8082` | B 服务 |
| `seedOrderId` | `910000000000000002` | Liquibase 初始化订单 |
| `orderNo` | 空 | 新建订单号 |
| `orderId` | 空 | 新建订单返回的 ID |

如果上一次测试中途失败，先把 `orderNo` 和 `orderId` 清空。

`orderNo` 的前置脚本会生成类似：

```text
ORDER-1794980000000
```

创建请求的测试脚本会把响应中的 `data` 保存到 `orderId`。后续详情、修改和删除请求都使用这个变量。

### 10.3 单步执行顺序

不要一开始只发“查询详情”。详情依赖 `orderId`，首次测试应先创建订单。

#### 第一步：创建订单

选择：

```text
A - 创建订单
```

请求：

```http
POST http://127.0.0.1:8081/orders
Content-Type: application/json
```

请求体：

```json
{
  "orderNo": "{{orderNo}}",
  "customerId": 2001,
  "amount": 199.90,
  "status": "CREATED"
}
```

点击 `Send`。预期：

- HTTP `200`
- `success=true`
- `data` 是数字形式的订单 ID
- Postman 的 `orderId` 变量被自动写入

打开 Postman 控制台可以看到测试脚本执行结果。

#### 第二步：查询刚创建的订单

选择 `A - 查询订单详情`，发送。

预期：

- 订单号等于当前 `orderNo`
- 金额是 `199.90`
- 状态是 `CREATED`

这一步的业务链路：

```text
GET /orders/{id}
  -> OrderController
  -> OrderApplicationService
  -> OrderDomainService
  -> OrderRepository
  -> MySQL
```

#### 第三步：分页查询

选择 `A - 分页查询`，发送：

```http
GET http://127.0.0.1:8081/orders?current=1&pageSize=20
```

重点检查：

- `data.current=1`
- `data.pageSize=20`
- `data.total`
- `data.datas`
- 返回条数不超过 `pageSize`
- 按 `createdTime` 倒序，创建时间相同时按 `id` 倒序

#### 第四步：修改订单

选择 `A - 修改订单`，发送。

请求体把金额改成 `299.90`，状态改成 `PAID`。

再发送 `A - 修改后查询`，确认：

```text
amount = 299.90
status = PAID
```

如果只看到修改接口返回成功，却看不到数据库变化，应以“修改后查询”结果为准。

#### 第五步：重复订单号

选择 `A - 重复订单号冲突`，发送。

预期：

- HTTP `409`
- `success=false`
- `errorCode=90000101`

数据库还有唯一约束兜底。应用层预查询用于改善提示，数据库约束用于防止并发重复。

#### 第六步：参数校验反例

选择 `A - 参数校验反例`，发送。

请求体故意包含：

```json
{
  "orderNo": "",
  "customerId": 0,
  "amount": -1,
  "status": "CREATED"
}
```

预期 HTTP `400`。这种错误在 Controller 参数绑定或 Bean Validation 阶段发生，不应进入正常写入逻辑。

#### 第七步：删除和删除后查询

1. 发送 `A - 删除订单`，预期 HTTP `200`。
2. 发送 `A - 删除后查询`，预期 HTTP `404`、错误码 `90000102`。

本工程采用物理删除。

#### 第八步：验证 Controller 到 Service

选择 `A - Hello 请求链路`：

```http
GET http://127.0.0.1:8081/demo/hello?name=World
```

预期：

```json
{
  "message": "Hello, World!"
}
```

完整链路：

```text
HelloController -> DemoApi -> DemoApplicationService
  -> DemoService -> DemoHelloVo -> ResponseResult
```

#### 第九步：B 调用 A 查询详情

选择：

```text
B - Feign 查询详情
```

请求：

```http
GET http://127.0.0.1:8082/consumer/orders/910000000000000002
```

预期：

- HTTP `200`
- 订单号是 `DEMO-ORDER-2026111802`
- 返回内容与直接访问 A 的同一订单一致

注意两个路径的区别：

```text
Postman -> B：/consumer/orders/{id}
B -> A：      /orders/{id}
```

#### 第十步：B 调用 A 的分页接口

选择 `B - Feign 分页查询`：

```http
GET http://127.0.0.1:8082/consumer/orders?current=1&pageSize=20
```

预期 HTTP `200`，`data.current=1`。

#### 第十一步：B 调 A 的失败语义

选择 `B - Feign 订单不存在`：

```http
GET http://127.0.0.1:8082/consumer/orders/999999999999999999
```

预期：

- HTTP `404`
- `errorCode=90000102`

这说明 B 没有吞掉 A 的业务错误，`OrderFeignService` 将远端失败转换为 B 的 `BaseException`，`ConsumerExceptionHandler` 再映射成统一响应。

### 10.4 一次运行全部请求

确认 A、B、Nacos、MySQL 正在运行后：

1. 右键集合 `订单管理接口回归`。
2. 选择 `Run collection`。
3. 保持请求顺序。
4. 点击 `Run`。

当前集合预期：

```text
请求：13
断言：13
失败：0
```

请求有先后依赖，不能改成随机顺序。

### 10.5 使用 Newman 运行同一集合

如果已安装 Newman：

```powershell
newman run postman/order-management.postman_collection.json `
  --reporters cli,json `
  --reporter-json-export docs/evidence/newman-report.json
```

机器可读结果在：

```text
docs/evidence/newman-report.json
```

## 11. 如何确认 B 真的不是直连数据库

可以从四个证据同时确认：

1. B 的 `pom.xml` 只依赖 Feign Starter，没有 Context、DAL 或 MySQL 驱动。
2. B 的配置没有 `spring.datasource`。
3. B 的 `OrderApi` 运行时实现是 `OrderFeignService`。
4. Nacos 中 A 的实例是 `backend-demo-server:8081`，B 成功请求后 A 的 Controller 收到 `/orders/{id}`。

直接发送 B 请求时，B 只是入口；真正的订单查询发生在 A。

## 12. 当前验收清单

| 验收项 | 状态 | 仓库中的证据 |
| --- | --- | --- |
| 源码与配置说明 | 已齐 | 六个 Maven 模块、`bootstrap.yml`、`application-local.properties`、本指南 |
| Liquibase 变更脚本 | 已齐 | `v1.0.0.xml`、`v1.1.0.xml`、`v1.2.0.xml`、master changelog |
| JUnit 测试结果 | 已齐 | 测试源码、`docs/evidence/junit-test-results.md`，17 项通过 |
| 接口文档与 Postman 集合 | 已齐 | `docs/order-api.md`、Swagger、Postman 13 个请求 |
| B 到 A 调用证据 | 已齐 | 成功详情、成功分页、失败 404，以及 Newman JSON 报告 |
| README 启动步骤 | 已齐 | 环境变量、启动顺序、Liquibase、接口和联调入口 |

从代码仓库交付物角度，以上内容已经齐备。

需要单独区分的是 Windows 系统变量作用域：源码和 README 已按环境变量方式配置，但当前用户如果没有管理员权限，就无法写入 Machine 级变量。该问题属于本机环境配置，不是仓库源码缺失。

## 13. 一次完整验收建议

```text
1. 检查 3306、8848、9848、8081、8082 正在监听
2. 打开 Nacos，选择命名空间 local
3. 确认 A、B 两个服务及健康实例存在
4. 打开 A 的 Swagger，确认接口文档可访问
5. Postman 单步完成 Hello、创建、详情、分页、修改、冲突、校验、删除
6. Postman 完成 B 的成功详情、成功分页、失败 404
7. 运行 mvn test，确认 17 项通过
8. 运行全部 Postman 请求或 Newman，确认 13 个请求和断言通过
9. 检查 Git 工作区，确认没有账号、密码或 Token
```

## 14. 常见错误对照

| 现象 | 优先检查 |
| --- | --- |
| Nacos 服务列表为空 | 是否选中 `local`，命名空间 ID 是否真的是 `local` |
| 服务只在 `public` 出现 | 启动终端中的 `NACOS_NAMESPACE` 被改成了 `public` |
| B 返回连接失败 | A 是否在 8081 运行，A、B 是否在同命名空间 |
| B 返回 404 | 订单 ID 是否真实存在；查看 `errorCode=90000102` |
| 创建返回 409 | `orderNo` 是否已被数据库占用；清空集合变量后重试 |
| 参数请求返回 400 | 查看字段长度、金额小数位、分页范围和枚举值 |
| 打包提示无法重命名 JAR | 旧的 A/B 进程仍在占用 JAR，先停止进程 |
| 重启终端后变量又为空 | 环境变量只设在临时进程，没有设置到系统变量 |
| 页面看到局域网 IP | Spring Cloud Nacos 默认注册可用网卡地址，不一定注册 127.0.0.1 |
