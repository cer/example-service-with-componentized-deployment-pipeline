package io.eventuate.customerservice.customermanagement.otherserviceproxy;

import io.eventuate.customerservice.customermanagement.domain.OtherDetails;
import io.eventuate.customerservice.customermanagement.domain.OtherService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.restclient.test.autoconfigure.RestClientTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.client.MockRestServiceServer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@RestClientTest
@Import(OtherServiceProxyConfiguration.class)
@TestPropertySource(properties = "otherservice.url=http://other-service")
public class OtherServiceProxyTest {

    @SpringBootApplication
    static class TestApp {
    }

    @Autowired
    private MockRestServiceServer server;

    @Autowired
    private OtherService otherService;

    @Test
    public void shouldFindOther() {
        server.expect(requestTo("http://other-service/others/other-1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"otherId\":\"other-1\",\"name\":\"Fred\"}", MediaType.APPLICATION_JSON));

        assertThat(otherService.findOther("other-1")).contains(new OtherDetails("other-1", "Fred"));
    }

    @Test
    public void shouldReturnEmptyWhenOtherNotFound() {
        server.expect(requestTo("http://other-service/others/unknown"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withResourceNotFound());

        assertThat(otherService.findOther("unknown")).isEmpty();
    }
}
