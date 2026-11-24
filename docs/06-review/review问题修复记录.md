# Review 问题修复记录

| 编号 | 问题 | 修复内容 | 再验证 | 状态 |
| --- | --- | --- | --- | --- |
| R1 | CTable 自定义状态/操作列不渲染 | `use-config.ts` 为 status/action/totalAmount 列补 `slotName` | UI 测试列表行状态显示“待审批/已驳回”，操作按钮按状态出现 | 已修 |
| R2 | 采购申请单请求被代理到中台 | `vite.config.ts` 新增 `/api/purchase-requests` → 8081 | 重启 dev server 后列表 5 条数据正常加载 | 已修 |
| R3 | 单测 UnnecessaryStubbing | 补 `findByRequestId` 返回非空明细 | `mvn test` 30/30 通过 | 已修 |
| R4/R5/R6 | 默认审批人、删除无版本、单号随机生成 | 评估后接受为最小实现；单号唯一约束兜底 | 接口测试覆盖正常/异常路径 | 已确认 |
| R7 | 权限/租户未接入 | 不在本次范围，需求待确认项 Q1 | - | 待人工确认 |
| R8 | 浏览器 Uncaught (in promise) 告警 | 未定位到业务代码；记录已知项 | UI 功能正常，接口无异常 | 待人工确认（非阻塞） |
| R9~R11 | 事务/分页/日志 | 无问题 | 接口测试 + 单测通过 | 已确认 |

## 修复后回归

- 后端：`mvn test`（30 通过），重新打包启动成功；
- 接口：32/32 PASS（含版本冲突、非法流转、并发提交）；
- 前端：ESLint 0 error、Stylelint 0 error、生产构建成功；
- UI：列表/详情/新增表单/取消原因弹框截图与断言通过。
