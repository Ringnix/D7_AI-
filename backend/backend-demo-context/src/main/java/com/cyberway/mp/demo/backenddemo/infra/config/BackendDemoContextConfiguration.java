package com.cyberway.mp.demo.backenddemo.infra.config;

import com.cyberway.mp.bc.dal.config.EnableDAL;
import com.cyberway.mp.demo.backenddemo.api.demo.DemoApi;
import com.cyberway.mp.demo.backenddemo.api.order.OrderApi;
import com.cyberway.mp.demo.backenddemo.api.purchase.PurchaseRequestApi;
import com.cyberway.mp.demo.backenddemo.application.service.DemoApplicationService;
import com.cyberway.mp.demo.backenddemo.application.service.OrderApplicationService;
import com.cyberway.mp.demo.backenddemo.application.service.PurchaseRequestApplicationService;
import com.cyberway.mp.demo.backenddemo.domain.demo.repository.DemoRepository;
import com.cyberway.mp.demo.backenddemo.domain.demo.service.DemoService;
import com.cyberway.mp.demo.backenddemo.domain.order.repository.OrderRepository;
import com.cyberway.mp.demo.backenddemo.domain.order.service.OrderDomainService;
import com.cyberway.mp.demo.backenddemo.domain.purchase.repository.PurchaseRequestItemRepository;
import com.cyberway.mp.demo.backenddemo.domain.purchase.repository.PurchaseRequestRepository;
import com.cyberway.mp.demo.backenddemo.domain.purchase.service.PurchaseRequestDomainService;
import javax.sql.DataSource;
import liquibase.integration.spring.SpringLiquibase;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ResourceLoader;

@MapperScan(basePackages = "com.cyberway.mp.demo.backenddemo.domain")
@EnableDAL
@Configuration
public class BackendDemoContextConfiguration {

	@ConditionalOnProperty(prefix = "spring.liquibase", name = "enabled", havingValue = "true", matchIfMissing = true)
	@Bean(name = "backendDemoLiquibase")
	public SpringLiquibase backendDemoLiquibase(DataSource dataSource, ResourceLoader resourceLoader) {
		SpringLiquibase liquibase = new SpringLiquibase();
		liquibase.setDataSource(dataSource);
		liquibase.setResourceLoader(resourceLoader);
		liquibase.setChangeLog("classpath:backend-demo-liquibase-master.xml");
		liquibase.setDatabaseChangeLogTable("backend_demo__db_log");
		liquibase.setDatabaseChangeLogLockTable("backend_demo__db_lock");
		return liquibase;
	}

	@Bean
	public DemoService demoService(DemoRepository demoRepository) {
		return new DemoService(demoRepository);
	}

	@Bean
	public DemoApi demoApplicationService(DemoService demoService) {
		return new DemoApplicationService(demoService);
	}

	@Bean
	public OrderDomainService orderDomainService(OrderRepository orderRepository) {
		return new OrderDomainService(orderRepository);
	}

	@Bean
	public OrderApi orderApplicationService(OrderDomainService orderDomainService) {
		return new OrderApplicationService(orderDomainService);
	}

	@Bean
	public PurchaseRequestDomainService purchaseRequestDomainService(
			PurchaseRequestRepository purchaseRequestRepository,
			PurchaseRequestItemRepository purchaseRequestItemRepository) {
		return new PurchaseRequestDomainService(
				purchaseRequestRepository, purchaseRequestItemRepository);
	}

	@Bean
	public PurchaseRequestApi purchaseRequestApplicationService(
			PurchaseRequestDomainService purchaseRequestDomainService) {
		return new PurchaseRequestApplicationService(purchaseRequestDomainService);
	}

}
