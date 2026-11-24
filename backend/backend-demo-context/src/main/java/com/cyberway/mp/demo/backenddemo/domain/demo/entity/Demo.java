package com.cyberway.mp.demo.backenddemo.domain.demo.entity;

import com.cyberway.mp.bc.dal.annotation.Table;
import com.cyberway.mp.bc.dal.model.BusinessEntityWithAppInstance;
import com.cyberway.mp.demo.backenddemo.api.demo.dto.DemoCreateDto;
import com.cyberway.mp.demo.backenddemo.api.demo.dto.DemoModifyDto;
import com.cyberway.mp.demo.backenddemo.api.demo.enums.DemoStatus;
import com.cyberway.mp.demo.backenddemo.api.demo.vo.DemoListVo;
import com.cyberway.mp.demo.backenddemo.api.demo.vo.DemoVo;

@Table(name = "backend_demo_demo")
public class Demo extends BusinessEntityWithAppInstance {

    /**
     * 编码
     */
    private String code;

    /**
     * 名称
     */
    private String name;

    /**
     * 描述
     */
    private String description;

    /**
     * 状态
     */
    private DemoStatus status;

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

    public DemoStatus getStatus() {
        return status;
    }

    public void setStatus(DemoStatus status) {
        this.status = status;
    }

    public void update(DemoModifyDto dto) {
        setName(dto.getName());
        setDescription(dto.getDescription());
    }

    public DemoListVo toDemoListVo() {
        DemoListVo vo = new DemoListVo();
        initDemoListVo(vo);
        return vo;
    }

    public DemoVo toDemoVo() {
        DemoVo vo = new DemoVo();
        initDemoListVo(vo);
        vo.setDescription(getDescription());
        return vo;
    }

    private void initDemoListVo(DemoListVo vo) {
        vo.setId(getId());
        vo.setCode(getCode());
        vo.setName(getName());
        vo.setStatus(getStatus());
    }

    public static Demo fromDemoCreateDto(DemoCreateDto dto) {
        Demo demo = new Demo();
        demo.setCode(dto.getCode());
        demo.setName(dto.getName());
        demo.setDescription(dto.getDescription());
        demo.setStatus(DemoStatus.DRAFT);
        return demo;
    }
}
