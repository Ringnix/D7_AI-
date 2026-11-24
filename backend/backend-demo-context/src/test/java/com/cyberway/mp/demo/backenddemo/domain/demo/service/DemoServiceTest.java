package com.cyberway.mp.demo.backenddemo.domain.demo.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

import com.cyberway.mp.demo.backenddemo.api.demo.dto.DemoHelloDto;
import com.cyberway.mp.demo.backenddemo.api.demo.vo.DemoHelloVo;
import com.cyberway.mp.demo.backenddemo.domain.demo.repository.DemoRepository;
import org.junit.jupiter.api.Test;

class DemoServiceTest {

    private final DemoService demoService = new DemoService(mock(DemoRepository.class));

    @Test
    void returnsDefaultHelloMessage() {
        DemoHelloVo result = demoService.hello(new DemoHelloDto());

        assertEquals("Hello, World!", result.getMessage());
        assertNotNull(result.getTimestamp());
    }

    @Test
    void returnsMessageForProvidedName() {
        DemoHelloDto dto = new DemoHelloDto();
        dto.setName("  Backend  ");

        DemoHelloVo result = demoService.hello(dto);

        assertEquals("Hello, Backend!", result.getMessage());
    }
}
