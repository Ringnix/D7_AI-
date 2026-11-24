package com.cyberway.mp.demo.backenddemo.api.demo.enums;

import io.swagger.v3.oas.annotations.media.Schema;

public enum DemoStatus {

    /**
     * 草稿
     */
    @Schema(title = "草稿")
    DRAFT,

    /**
     * 正常
     */
    @Schema(title = "正常")
    NORMAL,

    /**
     * 下线
     */
    @Schema(title = "下线")
    OFFLINE
}
