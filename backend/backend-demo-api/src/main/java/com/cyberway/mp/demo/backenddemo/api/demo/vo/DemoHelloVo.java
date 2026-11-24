package com.cyberway.mp.demo.backenddemo.api.demo.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.OffsetDateTime;

@Schema(description = "Hello 响应")
public class DemoHelloVo {

    @Schema(title = "响应消息", example = "Hello, World!")
    private String message;

    @Schema(title = "响应时间")
    private OffsetDateTime timestamp;

    public DemoHelloVo() {
    }

    public DemoHelloVo(String message, OffsetDateTime timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(OffsetDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
