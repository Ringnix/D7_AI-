package com.cyberway.mp.demo.backenddemo.api.demo.vo;

import io.swagger.v3.oas.annotations.media.Schema;

public class DemoVo extends DemoListVo {

    @Schema(title = "描述")
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
