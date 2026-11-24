package com.cyberway.mp.demo.backenddemo.api.demo;

import com.cyberway.mp.bc.common.api.Page;
import com.cyberway.mp.demo.backenddemo.api.demo.dto.DemoCreateDto;
import com.cyberway.mp.demo.backenddemo.api.demo.dto.DemoHelloDto;
import com.cyberway.mp.demo.backenddemo.api.demo.dto.DemoModifyDto;
import com.cyberway.mp.demo.backenddemo.api.demo.dto.DemoPageDto;
import com.cyberway.mp.demo.backenddemo.api.demo.enums.DemoStatus;
import com.cyberway.mp.demo.backenddemo.api.demo.vo.DemoHelloVo;
import com.cyberway.mp.demo.backenddemo.api.demo.vo.DemoListVo;
import com.cyberway.mp.demo.backenddemo.api.demo.vo.DemoVo;

import jakarta.validation.Valid;

public interface DemoApi {

    DemoHelloVo hello(@Valid DemoHelloDto dto);

    /**
     * 添加数据
     *
     * @param dto 添加数据参数
     * @return 添加的数据ID
     */
    long createDemo(@Valid DemoCreateDto dto);

    /**
     * 修改数据
     *
     * @param dto 修改数据参数
     */
    void modifyDemo(@Valid DemoModifyDto dto);

    /**
     * 删除数据
     *
     * @param id 数据ID
     */
    void deleteDemo(long id);

    /**
     * 分页查询数据
     *
     * @param dto 查询参数
     * @return 查询结果
     */
    Page<DemoListVo> queryDemo(@Valid DemoPageDto dto);

    /**
     * 获取数据
     *
     * @param id 数据ID
     * @return 数据详情
     */
    DemoVo getDemo(long id);

    /**
     * 更改数据状态
     * @param id 数据ID
     * @param status 状态
     */
    void changeStatus(long id, DemoStatus status);
}
