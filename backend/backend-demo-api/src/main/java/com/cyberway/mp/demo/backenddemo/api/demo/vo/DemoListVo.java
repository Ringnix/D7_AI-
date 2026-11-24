package com.cyberway.mp.demo.backenddemo.api.demo.vo;

import com.cyberway.mp.demo.backenddemo.api.demo.enums.DemoStatus;
import io.swagger.v3.oas.annotations.media.Schema;

public class DemoListVo {

    @Schema(title = "ID")
    private Long id;

    /**
     * 编码
     */
    @Schema(title = "编码")
    private String code;

    /**
     * 名称
     */
    @Schema(title = "名称")
    private String name;

    /**
     * 状态
     */
    @Schema(title = "状态")
    private DemoStatus status;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DemoStatus getStatus() {
        return status;
    }

    public void setStatus(DemoStatus status) {
        this.status = status;
    }
}
