package com.cyberway.mp.demo.backenddemo.feign.client;

import com.cyberway.mp.bc.common.controller.ResponseResult;
import com.cyberway.mp.demo.backenddemo.api.demo.dto.DemoHelloDto;
import com.cyberway.mp.demo.backenddemo.api.demo.vo.DemoHelloVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(
        contextId = "HelloClient",
        value = "${backend-demo.feign.server-name:backend-demo-server}",
        path = "/demo")
public interface HelloClient {

    @GetMapping("/hello")
    ResponseResult<DemoHelloVo> hello(@SpringQueryMap DemoHelloDto dto);
}
