package com.cyberway.mp.demo.backenddemo.api.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DemoCreateDto {

    /**
     * 编码
     */
    @NotBlank
    @Schema(title = "编码")
    private String code;

    /**
     * 名称
     */
    @NotBlank
    @Schema(title = "名称")
    private String name;

    /**
     * 描述
     */
    @NotNull
    @Schema(title = "描述")
    private String description;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
