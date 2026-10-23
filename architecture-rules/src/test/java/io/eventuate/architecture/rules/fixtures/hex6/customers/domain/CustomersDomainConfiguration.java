package io.eventuate.architecture.rules.fixtures.hex6.customers.domain;

import io.eventuate.architecture.rules.fixtures.hex6.customers.domain.internal.CreditLimitPolicy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class CustomersDomainConfiguration {

    @Bean
    public CustomerService customerService() {
        return new CustomerService();
    }

    @Bean
    public CreditLimitPolicy creditLimitPolicy() {
        return new CreditLimitPolicy();
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
