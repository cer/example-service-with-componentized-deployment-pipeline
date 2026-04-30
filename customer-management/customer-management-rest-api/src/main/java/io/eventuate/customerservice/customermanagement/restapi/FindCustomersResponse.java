package io.eventuate.customerservice.customermanagement.restapi;

import java.util.List;

public record FindCustomersResponse(List<FindCustomerResponse> customers) {
}
