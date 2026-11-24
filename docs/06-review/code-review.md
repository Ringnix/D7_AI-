# Code Review 记录

## 评审范围

- 后端：新增 api/context/starter 代码、Liquibase changeSet、单元测试
- 前端：service、页面、路由、国际化、代理配置

## 评审项与结论

| 编号 | 维度 | 发现 | 级别 | 结论 |
| --- | --- | --- | --- | --- |
| R1 | 前端渲染 | 列表状态/操作列未设置 `slotName`，CTable 不会渲染自定义插槽 | 中 | 已修复：use-config 补 `slotName: status/action/totalAmount` |
| R2 | 前端联调 | Vite 代理未包含 `/api/purchase-requests`，请求会落到中台接口 | 高 | 已修复：新增代理到 8081，并放在 `/api` 兜底规则之前 |
| R3 | 单测 | 并发冲突用例缺明细 stub，触发 UnnecessaryStubbing | 低 | 已修复：补 `findByRequestId` 非空 stub |
| R4 | 后端 | 审批人固定默认“审批人”，`approvalUserId=0` | 低 | 接受：本次不接中台登录态，DTO 可传 approverName；后续接 `req.userId` |
| R5 | 后端 | 删除接口未带版本号，理论上存在并发删除 | 低 | 接受：删除仅草稿且主子表同事务；版本号冲突保护成本高，当前场景可接受 |
| R6 | 后端 | `requestNo` 使用时间+随机生成，极端并发可能重复 | 低 | 接受：唯一约束兜底 + DuplicateKeyException 转 90000201 |
| R7 | 权限/租户 | 未接权限码与数据权限过滤 | 中 | 待人工确认：项目现有模块同样未启用；需求 Q1 已列待确认 |
| R8 | 前端 | UI 测试中浏览器控制台出现 2 条 `Uncaught (in promise)` 框架告警 | 低 | 未定位到业务代码路径；功能验证均通过，记录为已知项（见 UI 测试结果） |
| R9 | 事务 | 修改时先更新主表再删/插明细 | 无问题 | 同事务 + rollbackFor=Exception，失败整体回滚 |
| R10 | 分页 | 分页参数 current 1 起，后端转 PageParam 时减 1 | 无问题 | 与订单模块一致 |
| R11 | 日志脱敏 | 日志仅打印 requestId/operation/单据 ID/耗时/异常类型 | 无问题 | 不打印金额、申请人等业务明细 |

## 最小 diff 检查

- 未改动订单模块、消费者服务、公共配置。
- 未删除/改写历史 Liquibase changeSet；仅新增 v1.3.0.xml。
- 前端仅在 router/index 与 locale/index 做必要导出修改。

## 待人工确认

1. R7 权限/租户接入方式。
2. R8 框架告警是否在真实登录环境仍出现（本次 mock 平台环境）。
