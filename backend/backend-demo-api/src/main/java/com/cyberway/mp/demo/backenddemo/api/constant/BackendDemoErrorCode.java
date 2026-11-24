package com.cyberway.mp.demo.backenddemo.api.constant;

import com.cyberway.mp.bc.common.api.exception.ErrorCode;

public class BackendDemoErrorCode implements ErrorCode {

    private static final String BASE_CODE = "9000";

    public static final BackendDemoErrorCode DEMO_CODE_EXISTED = buildCode("0001", "指定Demo编码已存在");

    public static final BackendDemoErrorCode DEMO_ID_NOT_EXISTED = buildCode("0002", "指定Demo数据不存在");

    public static final BackendDemoErrorCode ORDER_NO_EXISTED = buildCode("0101", "订单号已存在");

    public static final BackendDemoErrorCode ORDER_ID_NOT_EXISTED = buildCode("0102", "订单不存在");

    public static final BackendDemoErrorCode PURCHASE_REQUEST_NO_EXISTED = buildCode("0201", "采购申请单号已存在");

    public static final BackendDemoErrorCode PURCHASE_REQUEST_ID_NOT_EXISTED = buildCode("0202", "采购申请单不存在");

    public static final BackendDemoErrorCode PURCHASE_REQUEST_STATUS_INVALID = buildCode("0203", "当前状态不允许该操作");

    public static final BackendDemoErrorCode PURCHASE_REQUEST_CANCEL_REASON_REQUIRED = buildCode("0204", "取消原因不能为空");

    public static final BackendDemoErrorCode PURCHASE_REQUEST_REJECT_REASON_REQUIRED = buildCode("0205", "驳回原因不能为空");

    public static final BackendDemoErrorCode PURCHASE_REQUEST_VERSION_CONFLICT = buildCode("0206", "数据已被他人修改，请刷新后重试");

    private final String code;

    private final String message;

    private BackendDemoErrorCode(String code, String message) {
        this.code = code;
        this.message = message;
    }

    private static BackendDemoErrorCode buildCode(String subCode, String msg) {
        return new BackendDemoErrorCode(BASE_CODE + subCode, msg);
    }

    @Override
    public String getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
