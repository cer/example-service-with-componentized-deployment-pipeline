package io.eventuate.customerservice.customermanagement.otherserviceproxy;

import io.eventuate.customerservice.customermanagement.domain.OtherDetails;
import io.eventuate.customerservice.customermanagement.domain.OtherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.stubrunner.spring.AutoConfigureStubRunner;
import org.springframework.cloud.contract.stubrunner.spring.StubRunnerProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE,
        properties = "otherservice.url=http://localhost:${spring.cloud.contract.stubrunner.runningstubs.other-subdomain-rest-api.port}")
@AutoConfigureStubRunner(ids = "io.eventuate.otherservice:other-subdomain-rest-api",
        stubsMode = StubRunnerProperties.StubsMode.REMOTE)
public class OtherServiceProxyContractTest {

    @Configuration
    @EnableAutoConfiguration(excludeName = "org.springframework.boot.jdbc.autoconfigure.DataSourceAutoConfiguration")
    @Import(OtherServiceProxyConfiguration.class)
    public static class TestConfiguration {
    }

    @Autowired
    private OtherService otherService;

    @Test
    public void shouldFindOther() {
        assertThat(otherService.findOther("other-1")).contains(new OtherDetails("other-1", "Fred"));
    }

    @Test
    public void shouldReturnEmptyWhenOtherNotFound() {
        assertThat(otherService.findOther("unknown")).isEmpty();
    }
}
