# 造数审计（seed.sql）

## 执行方式

1. 将 `seed.sql` 复制到 ASCII 临时路径（规避 MySQL CLI 中文路径 `source` 失败）；
2. `mysql --default-character-set=utf8mb4 --execute="source <临时路径>"` 执行；
3. 执行后 SELECT 校验。

## 造数内容（仅 INSERT）

| 表 | 主键 | 关键字段 |
| --- | --- | --- |
| backend_demo_purchase_request | 880000000000000001 | request_no=PR20261124SEED0001，title=接口测试-种子申请单，status=SUBMITTED，version=1，total=150.00 |
| backend_demo_purchase_request_item | 880000000000000002 | request_id=...0001，测试物料A，2.00×50.00=100.00 |
| backend_demo_purchase_request_item | 880000000000000003 | request_id=...0001，测试物料B，1.00×50.00=50.00 |

## 校验输出（实际）

```text
id                  request_no           title          total_amount  status     version
880000000000000001  PR20261124SEED0001  接口测试-种子申请单  150.00       SUBMITTED  1

id                  request_id          item_name  quantity  unit_price  amount  sort_no
880000000000000002  880000000000000001  测试物料A   2.00      50.00       100.00  1
880000000000000003  880000000000000001  测试物料B   1.00      50.00       50.00   2
```

## 审计声明

- 仅执行 INSERT，无 DELETE/UPDATE/TRUNCATE/DROP。
- 主键、单号全局唯一，明确属于本次测试数据。
- 执行后保留数据与 SELECT 证据，未清理共享状态。
- 业务用例产生的数据均由 API 正常创建，未做清理。
