# 采购申请单接口测试用例（manual-test）

## 环境

- API：`http://127.0.0.1:8081`
- DB：`127.0.0.1/java_simulation_project`（MySQL 8.0.44）
- 工具：PowerShell + `curl.exe`；含中文 JSON 统一写 UTF-8 无 BOM 文件后 `--data-binary @file`
- 造数：`seed.sql`（唯一 INSERT，主表 id=880000000000000001，子表 id=...0002/0003）
- 执行脚本：`run-interface-tests.ps1`；结果 CSV/JSON：`interface-test-results-20261124-145257.*`

## 用例清单

| 用例 | 方法/路径 | 预期 |
| --- | --- | --- |
| C01 分页查询默认 | GET /purchase-requests?current=1&pageSize=10 | 200，total≥1，datas 非空 |
| C02 按申请单号模糊查询 | GET ...requestNoLike=SEED | 200，结果包含 SEED 单号 |
| C03 种子详情 | GET /purchase-requests/880000000000000001 | 200，items=2，status=SUBMITTED |
| C04 新增草稿 | POST /purchase-requests | 200，返回数字 ID |
| C05 新建草稿详情 | GET /purchase-requests/{id} | DRAFT、version=0、total=259.80、items=2 |
| C06 修改草稿（version=0） | PUT /purchase-requests/{id} | 200 |
| C07 修改后详情 | GET /purchase-requests/{id} | version=1、total=30.00、items=1、标题更新 |
| C08 旧版本修改 | PUT /purchase-requests/{id}（version=0） | 409 / 90000206 |
| C09 提交草稿 | POST .../submit {version:1} | 200 |
| C10 提交后详情 | GET .../ | SUBMITTED、version=2 |
| C11 重复提交 | POST .../submit {version:2} | 409 / 90000203 |
| C12 撤回 | POST .../withdraw {version:2} | 200 |
| C13 取消（带原因） | POST .../cancel {version:3, reason} | 200 |
| C14 取消后详情 | GET .../ | CANCELLED、cancelReason、cancelledAt |
| C15 已取消再次取消 | POST .../cancel {version:4} | 409 / 90000203 |
| C16 新增申请 B | POST /purchase-requests | 200 |
| C17 提交申请 B | POST .../submit {version:0} | 200 |
| C18 审批通过 | POST .../approve {version:1, approverName, opinion} | 200 |
| C19 审批后详情 | GET .../ | APPROVED、审批人/意见 |
| C20 已通过重复审批 | POST .../approve {version:2} | 409 / 90000203 |
| C21 新增申请 C | POST /purchase-requests | 200 |
| C22 提交申请 C | POST .../submit {version:0} | 200 |
| C23 驳回原因必填 | POST .../reject {version:1, reason:''} | 400 / 90000205 |
| C24 驳回（带原因） | POST .../reject {version:1, reason} | 200 |
| C25 驳回后详情 | GET .../ | REJECTED、审批意见=驳回原因 |
| C26 参数校验 | POST /purchase-requests（标题空、明细空） | 400 |
| C27 新增申请 D | POST /purchase-requests | 200 |
| C28 删除草稿 D | DELETE /purchase-requests/{idD} | 200 |
| C29 删除后详情 | GET /purchase-requests/{idD} | 404 / 90000202 |
| C30 删除非草稿 | DELETE /purchase-requests/{idB} | 409 / 90000203 |
| C31 新增申请 E | POST /purchase-requests | 200 |
| C32 并发提交同一版本 | 两个 POST .../submit {version:0} 并行 | HTTP 组合 200+409（乐观锁） |

## 本地自测报告（2026-11-24 第 1 轮）

**环境**：`http://127.0.0.1:8081` · MySQL `127.0.0.1/java_simulation_project` · 日志 `backend-demo/logs/api-server.log`、`api-error.log`
**结论**：**32/32 PASS**；代码缺陷：无；行为符合预期。负向用例的 409/400 与 90000203/205/206/202 均与设计一致。

| 用例 | 结果 | 备注 |
| --- | --- | --- |
| C01~C03 | PASS | 种子分页/筛选/主子表详情 |
| C04~C08 | PASS | 新增、修改、金额重算、版本冲突 |
| C09~C15 | PASS | 提交/撤回/取消/非法流转 |
| C16~C20 | PASS | 审批通过、重复审批 |
| C21~C25 | PASS | 驳回原因必填与记录 |
| C26 | PASS | 参数校验 400 |
| C27~C30 | PASS | 删除草稿/不存在/非草稿拦截 |
| C31~C32 | PASS | 并发提交 200+409 |

**修复记录**：无代码缺陷；执行脚本修正 2 处（requestId 插值、按钮文案空格匹配，均属测试脚本问题）。
**本轮踩坑**：MySQL CLI `source` 中文路径无法打开，已改为复制到 ASCII 临时路径执行；含中文 JSON 均使用 UTF-8 无 BOM 文件。

## 造数与审计

- `seed.sql` 仅含 2 条明细 INSERT + 1 条主表 INSERT + 校验 SELECT，均为唯一 ID；执行后保留，不清理、不更新。
- 业务测试数据通过 API 正常创建，未执行 DELETE/UPDATE/TRUNCATE 清理。
- 审计校验 SELECT 输出见 `seed-audit.md`。
