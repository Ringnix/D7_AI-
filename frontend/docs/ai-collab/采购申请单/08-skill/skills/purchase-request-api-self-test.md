---
name: purchase-request-api-self-test
description: 对本地 Spring Boot 接口做全自动接口测试闭环：解析环境→唯一 INSERT 造数→curl.exe 逐条执行→记录 PASS/FAIL→读日志→修复回归。触发：需要验证采购申请单或同类 CRUD/状态流转接口时使用。
---

# 接口测试与本地自测闭环 Skill

## 触发条件

- 后端可在本地启动（MySQL/Redis 等依赖就绪）。
- 已有接口清单或 Controller。

## 输入

- 端口、数据库连接、日志路径（从 application-local/bootstrap 解析）
- 用例清单（正向 + 负向 + 事务/并发）

## 核心判断

1. DB 默认只读，造数只允许唯一 INSERT；禁止 DELETE/UPDATE/TRUNCATE/DROP 清理。
2. Windows 用 `curl.exe`；中文 JSON 写 UTF-8 无 BOM 文件 + `--data-binary @file`。
3. 分页字段先读 DTO（本项目 current 从 1 起、pageSize 默认 20）。
4. FAIL 先读服务日志（api-error/server）定位根因，禁止盲改。
5. 负向用例触发的业务异常 WARN 不等于服务故障。

## 工作流程

1. 解析环境与日志路径；2. 写 seed.sql（唯一 INSERT + SELECT 校验）；3. 生成 case JSON；4. curl 逐条执行并记录 HTTP/body/结果；5. 失败读日志→修复→重跑失败用例 + 相邻正向；6. 生成报告与造数审计。

## 产出

- `05-test/接口测试/*-manual-test.md`
- `seed.sql`、`case-*.json`、`responses/`
- `接口测试报告.md`、`日志排查记录.md`、`seed-audit.md`

## 验证方法

- 每个用例有 HTTP/body 摘要/PASS/FAIL/证据；
- 造数有唯一 ID 与 SELECT 审计；
- 修复后有回归与日志复查记录。

## 不适用范围

- 需要共享环境/真实客户数据的测试；
- 无法本地启动、无日志可读的环境（应标记 BLOCKED 而非猜测）。
