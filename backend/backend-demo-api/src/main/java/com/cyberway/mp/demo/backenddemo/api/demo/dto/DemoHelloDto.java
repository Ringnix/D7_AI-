package com.cyberway.mp.demo.backenddemo.api.demo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

@Schema(description = "Hello 请求参数")
public class DemoHelloDto {

    @Size(max = 50, message = "名称长度不能超过50")
    @Schema(title = "名称", example = "World", maxLength = 50)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
