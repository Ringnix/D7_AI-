package com.cyberway.mp.demo.backenddemo.feign.client;

import com.cyberway.mp.bc.common.api.Page;
import com.cyberway.mp.bc.common.controller.ResponseResult;
import com.cyberway.mp.demo.backenddemo.api.demo.dto.DemoCreateDto;
import com.cyberway.mp.demo.backenddemo.api.demo.dto.DemoModifyDto;
import com.cyberway.mp.demo.backenddemo.api.demo.dto.DemoPageDto;
import com.cyberway.mp.demo.backenddemo.api.demo.enums.DemoStatus;
import com.cyberway.mp.demo.backenddemo.api.demo.vo.DemoListVo;
import com.cyberway.mp.demo.backenddemo.api.demo.vo.DemoVo;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(contextId = "DemoClient", value = "${backend-demo.feign.server-name:backend-demo-server}", path = "/api/demo")
public interface DemoClient {

    /**
     * 添加数据
     *
     * @param dto 添加数据参数
     * @return 添加的数据ID
     */
    @PostMapping
    ResponseResult<Long> createDemo(@RequestBody DemoCreateDto dto);

    /**
     * 修改数据
     *
     * @param dto 修改数据参数
     * @return 无
     */
    @PutMapping("{id}")
    ResponseResult<Void> modifyDemo(@PathVariable("id") long id, @RequestBody DemoModifyDto dto);

    /**
     * 删除数据
     *
     * @param id 数据ID
     * @return 无
     */
    @DeleteMapping("{id}")
    ResponseResult<Void> deleteDemo(@PathVariable("id") long id);

    /**
     * 分页查询数据
     *
     * @param dto 查询参数
     * @return 查询结果
     */
    @PostMapping("query")
    ResponseResult<Page<DemoListVo>> queryDemo(@RequestBody DemoPageDto dto);

    /**
     * 获取数据
     *
     * @param id 数据ID
     * @return 数据详情
     */
    @GetMapping("{id}")
    ResponseResult<DemoVo> getDemo(@PathVariable("id") long id);

    /**
     * 更改数据状态
     *
     * @param id     数据ID
     * @param status 状态
     * @return 无
     */
    @PutMapping("{id}/{status}")
    ResponseResult<Void> changeStatus(@PathVariable("id") long id, @PathVariable("status") DemoStatus status);
}
