package com.cyberway.mp.demo.backenddemo.api.demo.dto;

import com.cyberway.mp.bc.common.api.OrderPageParam;
import com.cyberway.mp.demo.backenddemo.api.demo.enums.DemoStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public class DemoPageDto extends OrderPageParam {

    @Schema(title = "编码, 模糊查找")
    private String codeLike;

    @Schema(title = "名称, 模糊查找")
    private String nameLike;

    @Schema(title = "状态, 精确查找")
    private DemoStatus status;

    public String getCodeLike() {
        return codeLike;
    }

    public void setCodeLike(String codeLike) {
        this.codeLike = codeLike;
    }

    public String getNameLike() {
        return nameLike;
    }

    public void setNameLike(String nameLike) {
        this.nameLike = nameLike;
    }

    public DemoStatus getStatus() {
        return status;
    }

    public void setStatus(DemoStatus status) {
        this.status = status;
    }
}
