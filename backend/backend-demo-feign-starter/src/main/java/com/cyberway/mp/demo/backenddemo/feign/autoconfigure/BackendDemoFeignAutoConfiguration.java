package com.cyberway.mp.demo.backenddemo.feign.autoconfigure;

import com.cyberway.mp.bc.common.annotation.EnableCommonFeignConfig;
import com.cyberway.mp.demo.backenddemo.feign.client.DemoClient;
import com.cyberway.mp.demo.backenddemo.feign.client.HelloClient;
import com.cyberway.mp.demo.backenddemo.feign.client.OrderClient;
import com.cyberway.mp.demo.backenddemo.feign.service.DemoFeignService;
import com.cyberway.mp.demo.backenddemo.feign.service.OrderFeignService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;

@ConditionalOnProperty(prefix = "backend-demo-feign", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableFeignClients(basePackages = "com.cyberway.mp.demo.backenddemo.feign.client")
@EnableCommonFeignConfig
public class BackendDemoFeignAutoConfiguration {

	@Bean
	public DemoFeignService demoFeignService(DemoClient demoClient, HelloClient helloClient) {
		return new DemoFeignService(demoClient, helloClient);
	}

	@Bean
	public OrderFeignService orderFeignService(OrderClient orderClient) {
		return new OrderFeignService(orderClient);
	}

}
