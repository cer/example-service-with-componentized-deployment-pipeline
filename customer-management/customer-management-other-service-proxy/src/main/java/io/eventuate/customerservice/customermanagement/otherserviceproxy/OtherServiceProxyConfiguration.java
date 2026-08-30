package io.eventuate.customerservice.customermanagement.otherserviceproxy;

import io.eventuate.customerservice.customermanagement.domain.OtherService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class OtherServiceProxyConfiguration {

    @Bean
    public OtherService otherService(RestClient.Builder restClientBuilder, @Value("${otherservice.url}") String otherServiceUrl) {
        return new OtherServiceProxy(restClientBuilder.baseUrl(otherServiceUrl).build());
    }
}
