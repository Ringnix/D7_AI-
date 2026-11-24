package com.cyberway.mp.demo.backenddemo.feign.service;

import com.cyberway.mp.bc.common.api.Page;
import com.cyberway.mp.demo.backenddemo.api.demo.DemoApi;
import com.cyberway.mp.demo.backenddemo.api.demo.dto.DemoCreateDto;
import com.cyberway.mp.demo.backenddemo.api.demo.dto.DemoHelloDto;
import com.cyberway.mp.demo.backenddemo.api.demo.dto.DemoModifyDto;
import com.cyberway.mp.demo.backenddemo.api.demo.dto.DemoPageDto;
import com.cyberway.mp.demo.backenddemo.api.demo.enums.DemoStatus;
import com.cyberway.mp.demo.backenddemo.api.demo.vo.DemoListVo;
import com.cyberway.mp.demo.backenddemo.api.demo.vo.DemoHelloVo;
import com.cyberway.mp.demo.backenddemo.api.demo.vo.DemoVo;
import com.cyberway.mp.demo.backenddemo.feign.client.DemoClient;
import com.cyberway.mp.demo.backenddemo.feign.client.HelloClient;

public class DemoFeignService implements DemoApi {

    private final DemoClient demoClient;

    private final HelloClient helloClient;

    public DemoFeignService(DemoClient demoClient, HelloClient helloClient) {
        this.demoClient = demoClient;
        this.helloClient = helloClient;
    }

    @Override
    public DemoHelloVo hello(DemoHelloDto dto) {
        return helloClient.hello(dto).getData();
    }

    @Override
    public long createDemo(DemoCreateDto dto) {
        return demoClient.createDemo(dto).getData();
    }

    @Override
    public void modifyDemo(DemoModifyDto dto) {
        demoClient.modifyDemo(dto.getId(), dto);
    }

    @Override
    public void deleteDemo(long id) {
        demoClient.deleteDemo(id);
    }

    @Override
    public Page<DemoListVo> queryDemo(DemoPageDto dto) {
        return demoClient.queryDemo(dto).getData();
    }

    @Override
    public DemoVo getDemo(long id) {
        return demoClient.getDemo(id).getData();
    }

    @Override
    public void changeStatus(long id, DemoStatus status) {
        demoClient.changeStatus(id, status);
    }
}
