package com.cyberway.mp.demo.backenddemo.controller;

import com.cyberway.mp.bc.common.controller.ResponseResult;
import com.cyberway.mp.demo.backenddemo.api.demo.DemoApi;
import com.cyberway.mp.demo.backenddemo.api.demo.dto.DemoHelloDto;
import com.cyberway.mp.demo.backenddemo.api.demo.vo.DemoHelloVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/demo")
@Tag(name = "Demo 请求链路")
public class HelloController {

    private final DemoApi demoApi;

    public HelloController(DemoApi demoApi) {
        this.demoApi = demoApi;
    }

    @GetMapping("/hello")
    @Operation(summary = "Hello 请求链路验证")
    public ResponseResult<DemoHelloVo> hello(@Valid @ModelAttribute DemoHelloDto dto) {
        return ResponseResult.successResult(demoApi.hello(dto));
    }
}
