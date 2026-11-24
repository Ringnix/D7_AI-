package com.cyberway.mp.demo.backenddemo.application.service;

import com.cyberway.mp.bc.common.api.Page;
import com.cyberway.mp.bc.i18n.api.exception.BaseI18nException;
import com.cyberway.mp.demo.backenddemo.api.constant.BackendDemoErrorCode;
import com.cyberway.mp.demo.backenddemo.api.demo.DemoApi;
import com.cyberway.mp.demo.backenddemo.api.demo.dto.DemoCreateDto;
import com.cyberway.mp.demo.backenddemo.api.demo.dto.DemoHelloDto;
import com.cyberway.mp.demo.backenddemo.api.demo.dto.DemoModifyDto;
import com.cyberway.mp.demo.backenddemo.api.demo.dto.DemoPageDto;
import com.cyberway.mp.demo.backenddemo.api.demo.enums.DemoStatus;
import com.cyberway.mp.demo.backenddemo.api.demo.vo.DemoListVo;
import com.cyberway.mp.demo.backenddemo.api.demo.vo.DemoHelloVo;
import com.cyberway.mp.demo.backenddemo.api.demo.vo.DemoVo;
import com.cyberway.mp.demo.backenddemo.domain.demo.entity.Demo;
import com.cyberway.mp.demo.backenddemo.domain.demo.service.DemoService;
import org.springframework.validation.annotation.Validated;

@Validated
public class DemoApplicationService implements DemoApi {

    private final DemoService demoService;

    public DemoApplicationService(DemoService demoService) {
        this.demoService = demoService;
    }

    @Override
    public DemoHelloVo hello(DemoHelloDto dto) {
        return demoService.hello(dto);
    }

    @Override
    public long createDemo(DemoCreateDto dto) {
        Demo demo = Demo.fromDemoCreateDto(dto);
        demoService.createDemo(demo);
        return demo.getId();
    }

    @Override
    public void modifyDemo(DemoModifyDto dto) {
        Demo demo = demoService.getDemo(dto.getId());
        if (demo == null) {
            throw new BaseI18nException(BackendDemoErrorCode.DEMO_ID_NOT_EXISTED);
        }
        demo.update(dto);
        demoService.modifyDemo(demo);
    }

    @Override
    public void deleteDemo(long id) {
        demoService.deleteDemo(id);
    }

    @Override
    public Page<DemoListVo> queryDemo(DemoPageDto dto) {
        return demoService.pageQueryDemo(dto);
    }

    @Override
    public DemoVo getDemo(long id) {
        Demo demo = demoService.getDemo(id);
        if (demo == null) {
            throw new BaseI18nException(BackendDemoErrorCode.DEMO_ID_NOT_EXISTED);
        }
        return demo.toDemoVo();
    }

    @Override
    public void changeStatus(long id, DemoStatus status) {
        demoService.changeStatus(id, status);
    }
}
