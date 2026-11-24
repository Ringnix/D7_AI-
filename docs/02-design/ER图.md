# ER 图

```mermaid
erDiagram
    BACKEND_DEMO_PURCHASE_REQUEST ||--o{ BACKEND_DEMO_PURCHASE_REQUEST_ITEM : contains
    BACKEND_DEMO_PURCHASE_REQUEST {
        bigint id PK
        varchar(32) request_no UK
        varchar(100) title
        varchar(100) applicant_name
        varchar(100) department
        decimal(14,2) total_amount
        varchar(32) status
        varchar(500) remark
        int version
        varchar(500) cancel_reason
        datetime cancelled_time
        bigint approval_user_id
        varchar(100) approval_user_name
        datetime approval_time
        varchar(500) approval_opinion
        bigint created_user_id
        varchar(100) created_user_name
        datetime created_time
        bigint last_modified_user_id
        varchar(100) last_modified_user_name
        datetime last_modified_time
        bigint tenant_id
        bigint business_zone_id
        bigint app_instance_id
    }
    BACKEND_DEMO_PURCHASE_REQUEST_ITEM {
        bigint id PK
        bigint request_id FK
        varchar(100) item_name
        varchar(100) spec
        varchar(20) unit
        decimal(12,2) quantity
        decimal(12,2) unit_price
        decimal(14,2) amount
        int sort_no
    }
```

## 关系说明

- 一张采购申请单对应多条采购明细，`backend_demo_purchase_request_item.request_id` 关联主表 `id`。
- 主表保存总金额，子表保存每条明细数量、单价与金额。
- 主表 `version` 用于乐观锁；`status` 控制状态机。
- 审批信息（审批人/时间/意见）与取消信息（原因/时间）均落在主表，满足“仅记录状态变更”的需求。

## 截图位置说明

本图为 Mermaid 源码，可在支持 Mermaid 的 Markdown 渲染器中查看；如导出截图，放在 `02-design/screenshots/er.png`（本次未强制导出）。
