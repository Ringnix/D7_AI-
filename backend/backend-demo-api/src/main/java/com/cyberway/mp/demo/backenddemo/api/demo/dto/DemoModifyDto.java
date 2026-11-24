package com.cyberway.mp.demo.backenddemo.api.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class DemoModifyDto {

    @NotNull
    @Schema(title = "ID")
    private Long id;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
