package com.cyberway.mp.demo.backenddemo.controller;

import com.cyberway.mp.bc.common.api.Page;
import com.cyberway.mp.bc.common.controller.ResponseResult;
import com.cyberway.mp.demo.backenddemo.api.demo.DemoApi;
import com.cyberway.mp.demo.backenddemo.api.demo.dto.DemoCreateDto;
import com.cyberway.mp.demo.backenddemo.api.demo.dto.DemoModifyDto;
import com.cyberway.mp.demo.backenddemo.api.demo.dto.DemoPageDto;
import com.cyberway.mp.demo.backenddemo.api.demo.enums.DemoStatus;
import com.cyberway.mp.demo.backenddemo.api.demo.vo.DemoListVo;
import com.cyberway.mp.demo.backenddemo.api.demo.vo.DemoVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/api/demo")
@Tag(name = "demo")
public class DemoController {

    private final DemoApi demoApi;

    public DemoController(DemoApi demoApi) {
        this.demoApi = demoApi;
    }

    @Operation(summary = "添加数据")
    @PostMapping
    public ResponseResult<Long> createDemo(@RequestBody DemoCreateDto dto) {
        return ResponseResult.successResult(demoApi.createDemo(dto));
    }

    @Operation(summary = "修改数据")
    @PutMapping("{id}")
    public ResponseResult<Void> modifyDemo(@PathVariable("id") long id, @RequestBody DemoModifyDto dto) {
        dto.setId(id);
        demoApi.modifyDemo(dto);
        return ResponseResult.successResult();
    }

    @Operation(summary = "删除数据")
    @DeleteMapping("{id}")
    public ResponseResult<Void> deleteDemo(@PathVariable("id") long id) {
        demoApi.deleteDemo(id);
        return ResponseResult.successResult();
    }

    @Operation(summary = "分页查询数据")
    @PostMapping("query")
    public ResponseResult<Page<DemoListVo>> queryDemo(@RequestBody DemoPageDto dto) {
        return ResponseResult.successResult(demoApi.queryDemo(dto));
    }

    @Operation(summary = "获取数据")
    @GetMapping("{id}")
    public ResponseResult<DemoVo> getDemo(@PathVariable("id") long id) {
        return ResponseResult.successResult(demoApi.getDemo(id));
    }

    @Operation(summary = "更改数据状态")
    @PutMapping("{id}/{status}")
    public ResponseResult<Void> changeStatus(@PathVariable("id") long id,
            @PathVariable("status") DemoStatus status) {
        demoApi.changeStatus(id, status);
        return ResponseResult.successResult();
    }
}
