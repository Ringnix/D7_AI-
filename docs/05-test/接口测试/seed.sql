-- 采购申请单接口测试造数（仅 INSERT，唯一标识，执行后保留审计）
-- 主表
INSERT INTO backend_demo_purchase_request
    (id, created_user_id, created_user_name, created_time,
     last_modified_user_id, last_modified_user_name, last_modified_time,
     tenant_id, business_zone_id, app_instance_id,
     request_no, title, applicant_name, department, total_amount, status,
     remark, version, cancel_reason, cancelled_time,
     approval_user_id, approval_user_name, approval_time, approval_opinion)
VALUES
    (880000000000000001, 0, 'system', current_timestamp,
     0, 'system', current_timestamp,
     0, 0, 0,
     'PR20261124SEED0001', '接口测试-种子申请单', '测试员种子', '测试部', 150.00, 'SUBMITTED',
     'Liquibase 之外由测试脚本唯一 INSERT 的造数记录', 1, NULL, NULL,
     NULL, NULL, NULL, NULL);

-- 子表
INSERT INTO backend_demo_purchase_request_item
    (id, request_id, item_name, spec, unit, quantity, unit_price, amount, sort_no)
VALUES
    (880000000000000002, 880000000000000001, '测试物料A', '规格A', '个', 2.00, 50.00, 100.00, 1),
    (880000000000000003, 880000000000000001, '测试物料B', '规格B', '箱', 1.00, 50.00, 50.00, 2);

-- 造数校验
SELECT id, request_no, title, total_amount, status, version
FROM backend_demo_purchase_request
WHERE id = 880000000000000001;

SELECT id, request_id, item_name, quantity, unit_price, amount, sort_no
FROM backend_demo_purchase_request_item
WHERE request_id = 880000000000000001
ORDER BY sort_no;
