# 订单分页索引验证

验证日期：2026-11-18

## 默认分页

```sql
SELECT id, order_no, customer_id, amount, status, created_time
FROM backend_demo_order
ORDER BY created_time DESC, id DESC
LIMIT 20 OFFSET 0;
```

当前只有 6 条测试数据，优化器选择全表扫描和 filesort：

```text
type=ALL
rows=6
Extra=Using filesort
```

小表选择全表扫描是优化器基于成本的合理选择。清空或增加数据量后，
`idx_backend_demo_order_created(created_time, id)` 可用于反向扫描，
避免额外排序。

## 客户和状态过滤

```sql
SELECT id, order_no, customer_id, amount, status, created_time
FROM backend_demo_order
WHERE customer_id = 1001
  AND status = 'PAID'
ORDER BY created_time DESC, id DESC
LIMIT 20 OFFSET 0;
```

索引：

```text
idx_backend_demo_order_filter_page
(customer_id, status, created_time, id)
```

该联合索引覆盖两个等值过滤列，并按分页顺序排列后两列，可用于过滤和稳定排序。
订单号唯一约束和分页索引分别保证并发唯一性与翻页顺序。

实际执行计划：

```text
key=idx_backend_demo_order_filter_page
type=ref
rows=1
Extra=Backward index scan
```

该查询命中联合索引并按索引反向读取，不再出现 filesort。
