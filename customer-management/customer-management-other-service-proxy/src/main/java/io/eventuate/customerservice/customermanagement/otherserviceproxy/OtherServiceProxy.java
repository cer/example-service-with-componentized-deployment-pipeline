package io.eventuate.customerservice.customermanagement.otherserviceproxy;

import io.eventuate.customerservice.customermanagement.domain.OtherDetails;
import io.eventuate.customerservice.customermanagement.domain.OtherService;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Optional;

public class OtherServiceProxy implements OtherService {

    private final RestClient restClient;

    public OtherServiceProxy(RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public Optional<OtherDetails> findOther(String otherId) {
        try {
            GetOtherResponse response = restClient.get()
                    .uri("/others/{otherId}", otherId)
                    .retrieve()
                    .body(GetOtherResponse.class);
            return Optional.of(response.toOtherDetails());
        } catch (HttpClientErrorException.NotFound e) {
            return Optional.empty();
        }
    }
}
