package com.cyberway.mp.demo.backenddemo.domain.demo.service;

import com.cyberway.mp.bc.common.api.Page;
import com.cyberway.mp.bc.i18n.api.exception.BaseI18nException;
import com.cyberway.mp.demo.backenddemo.api.constant.BackendDemoErrorCode;
import com.cyberway.mp.demo.backenddemo.api.demo.dto.DemoPageDto;
import com.cyberway.mp.demo.backenddemo.api.demo.dto.DemoHelloDto;
import com.cyberway.mp.demo.backenddemo.api.demo.enums.DemoStatus;
import com.cyberway.mp.demo.backenddemo.api.demo.vo.DemoListVo;
import com.cyberway.mp.demo.backenddemo.api.demo.vo.DemoHelloVo;
import com.cyberway.mp.demo.backenddemo.domain.demo.entity.Demo;
import com.cyberway.mp.demo.backenddemo.domain.demo.repository.DemoRepository;
import com.cyberway.mp.demo.backenddemo.domain.demo.repository.query.DemoQuery;

import java.util.Collections;
import java.util.List;
import java.time.OffsetDateTime;
import java.util.stream.Collectors;

public class DemoService {

    private final DemoRepository demoRepository;

    public DemoService(DemoRepository demoRepository) {
        this.demoRepository = demoRepository;
    }

    public DemoHelloVo hello(DemoHelloDto dto) {
        String name = dto.getName();
        if (name == null || name.isBlank()) {
            name = "World";
        }
        return new DemoHelloVo("Hello, " + name.trim() + "!", OffsetDateTime.now());
    }

    public void createDemo(Demo demo) {
        if (demoRepository.countByCode(demo.getCode()) > 0) {
            throw new BaseI18nException(BackendDemoErrorCode.DEMO_CODE_EXISTED);
        }
        demoRepository.save(demo);
    }

    public void modifyDemo(Demo demo) {
        demoRepository.updateById(demo, demo.getId());
    }

    public void deleteDemo(long id) {
        demoRepository.delete(id);
    }

    public Demo getDemo(long id) {
        return demoRepository.findOne(id);
    }

    public Page<DemoListVo> pageQueryDemo(DemoPageDto dto) {
        DemoQuery query = DemoQuery.fromDemoPageDto(dto);
        long total = demoRepository.countDemo(query);
        if (total < 1) {
            return new Page<>(dto.getCurrent(), dto.getPageSize(), 0, Collections.emptyList());
        }
        List<Demo> demos = demoRepository.searchDemo(query, dto);
        return new Page<>(dto.getCurrent(), dto.getPageSize(), total, demos.stream()
                .map(Demo::toDemoListVo).collect(Collectors.toList()));
    }

    public void changeStatus(long id, DemoStatus status) {
        demoRepository.changeStatus(id, status);
    }
}
