package io.eventuate.architecture.rules.fixtures.hex6.orders.domain;

import io.eventuate.architecture.rules.fixtures.hex6.orders.persistence.OrderRepositoryAdapter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OrdersDomainConfiguration {

    @Bean
    public OrderRepositoryAdapter orderRepositoryAdapter() {
        return new OrderRepositoryAdapter();
    }
}
