package io.eventuate.customerservice.customermanagement.otherserviceproxy;

import io.eventuate.customerservice.customermanagement.domain.OtherDetails;

public record GetOtherResponse(String otherId, String name) {

    public OtherDetails toOtherDetails() {
        return new OtherDetails(otherId, name);
    }
}
