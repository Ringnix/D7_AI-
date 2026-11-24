# 订单管理接口验证记录

验证日期：2026-11-18

## 环境

- JDK 17.0.12
- Spring Boot 3.4.8
- MySQL 8.0.44
- Nacos 2.5.3，命名空间 `local`
- A：`backend-demo-server:8081`
- B：`backend-demo-consumer-server:8082`

## 数据库

Liquibase 成功执行：

```text
backend-demo_change_log/v1.0.0.xml
backend-demo_change_log/v1.1.0.xml
backend-demo_change_log/v1.2.0.xml
```

并创建订单表、唯一约束、两个索引和 3 条合成订单数据。

## A 服务验证

| 场景 | 结果 |
| --- | --- |
| 创建有效订单 | HTTP `200`，`success=true`，返回订单 ID |
| 查询刚创建订单 | HTTP `200`，订单号、金额、状态一致 |
| 分页查询 | HTTP `200`，`total` 与 `datas` 数量一致 |
| 修改订单 | HTTP `200`，金额更新为 `299.90`，状态更新为 `PAID` |
| 重复订单号 | HTTP `409`，错误码 `90000101` |
| 空订单号、非法数值 | HTTP `400` |
| 删除订单 | HTTP `200` |
| 删除后查询 | HTTP `404`，错误码 `90000102` |
| Hello 请求链路 | HTTP `200`，返回 `Hello, World!` |
| Hello 名称超长 | HTTP `400`，请求在参数绑定阶段被拒绝 |

## B 服务验证

Nacos 中注册成功：

```text
backend-demo-server          <host>:8081
backend-demo-consumer-server <host>:8082
```

Feign 使用服务名 `backend-demo-server` 和 Spring Cloud LoadBalancer。

| 场景 | 结果 |
| --- | --- |
| B 查询 A 的订单详情 | HTTP `200`，返回 `DEMO-ORDER-2026111802` |
| B 分页查询 A | HTTP `200`，条数与 A 一致 |
| B 查询不存在的订单 | HTTP `404`，错误码 `90000102`，已纳入 Postman 集合 |

## 自动化回归

`mvn test`：

- 单元、校验和 Feign 测试共 `17` 项通过。
- 测试覆盖创建、重复、数据库唯一冲突、缺失、修改缺失、删除、分页空页和正常页。

`newman run postman/order-management.postman_collection.json`：

- 请求 `13` 个。
- 断言 `13` 项。
- 失败 `0` 项。

机器可读报告见 `docs/evidence/newman-report.json`。

验证使用的订单数据均为重新编写的合成数据，不包含客户真实信息。

## JAR 部署验证

执行 `scripts/build-deploy.ps1` 后成功生成：

```text
deploy/backend-demo-server.jar
deploy/backend-demo-consumer-server.jar
```

通过 `start-a.bat` 和 `start-b.bat` 启动两个 JAR 后：

- A 在 `8081` 正常监听。
- B 在 `8082` 正常监听。
- Nacos `local` 命名空间中存在两个健康服务。
- `/demo/hello?name=Deploy` 返回 HTTP `200`。
- B 查询 A 的初始化订单返回 HTTP `200`。
- Newman 13 个请求、13 个断言全部通过。
