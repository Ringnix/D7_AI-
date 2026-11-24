---
name: purchase-request-backend-master-detail
description: 在 Spring Boot + 中台 DAL 项目中生成主子表单据后端代码（实体、仓储、领域服务、应用服务、Controller、Liquibase、状态机、乐观锁）。触发：需要新增带主子表与审批状态的业务模块时使用。
---

# 主子表 + 状态流转后端代码生成 Skill

## 触发条件

- 项目已有 `GenericMapper`、`@Table`、`BaseI18nException`、Liquibase 自定义记录表。
- 需求包含主表 + 明细、状态流转、原因必填、并发控制。

## 输入

- 表结构/接口清单/状态机设计
- 现有订单模块代码（作为分层与命名模板）

## 核心判断

1. 实体继承项目现有基类（审计/租户字段），不手写审计列。
2. 主表状态/原因/审批字段全部落库；子表按主表 ID 查询/删除。
3. 主子表保存、更新、删除必须同事务。
4. 乐观锁：`version` 字段 + `where id=? and version=?` 条件更新，0 行抛业务异常。
5. 原因校验放在领域服务，错误码与 HTTP 映射在 Controller 异常处理统一维护。

## 工作流程

1. 建 api（枚举/DTO/VO/Api）；2. 建实体与仓储 XML；3. 领域服务（状态机 + 事务 + 金额）；4. 应用服务 + 日志；5. 配置 Bean；6. Liquibase changeSet；7. Controller + 异常映射；8. 单测（状态机/版本/原因/分页）。

## 产出

- 后端源码与 `v1.x.0.xml` changeSet
- 单元测试与 `mvn test` 结果

## 验证方法

- 单测覆盖合法/非法流转、原因必填、版本冲突、主子表；
- 应用启动后 Liquibase `EXECUTED`、表结构正确；
- 接口测试覆盖全路径。

## 不适用范围

- 需要分库分表、分布式事务、复杂工作流；
- 需要迁移历史 changeSet 的场景（禁止改写历史记录）。
