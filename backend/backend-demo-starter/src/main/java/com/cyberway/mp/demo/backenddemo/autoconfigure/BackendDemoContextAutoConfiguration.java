package com.cyberway.mp.demo.backenddemo.autoconfigure;

import com.cyberway.mp.bc.common.annotation.EnableCommonControllerConfig;
import com.cyberway.mp.bc.common.doc.DefaultOpenAPI;
import com.cyberway.mp.demo.backenddemo.infra.config.BackendDemoContextConfiguration;
import io.swagger.v3.oas.models.OpenAPI;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@ConditionalOnProperty(prefix = "backend-demo", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableCommonControllerConfig
@Configuration
@ComponentScan(basePackages = "com.cyberway.mp.demo.backenddemo.controller")
@Import(BackendDemoContextConfiguration.class)
public class BackendDemoContextAutoConfiguration {

	@Bean
	public GroupedOpenApi backendDemoGroupedOpenApi() {
		return GroupedOpenApi.builder().group("backend-demo").packagesToScan("com.cyberway.mp.demo.backenddemo.controller").build();
	}

	@Bean
	@ConditionalOnMissingBean
	public OpenAPI backendDemoOpenApi() {
		return new DefaultOpenAPI();
	}

}
