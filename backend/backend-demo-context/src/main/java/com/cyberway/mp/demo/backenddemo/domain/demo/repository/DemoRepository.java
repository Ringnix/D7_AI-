package com.cyberway.mp.demo.backenddemo.domain.demo.repository;

import com.cyberway.mp.bc.common.api.PageParam;
import com.cyberway.mp.bc.dal.GenericMapper;
import com.cyberway.mp.bc.dal.annotation.IgnoreMethod;
import com.cyberway.mp.demo.backenddemo.api.demo.enums.DemoStatus;
import com.cyberway.mp.demo.backenddemo.domain.demo.entity.Demo;
import com.cyberway.mp.demo.backenddemo.domain.demo.repository.query.DemoQuery;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DemoRepository extends GenericMapper<Demo, Long> {

    long countDemo(DemoQuery query);

    List<Demo> searchDemo(DemoQuery query, PageParam pageParam);

    long countByCode(@Param("code") String code);

    @IgnoreMethod
    void changeStatus(@Param("id") long id, @Param("status")DemoStatus demoStatus);
}
