package io.eventuate.customerservice.customermanagement.domain;

import java.io.Serializable;
import java.util.UUID;

public record CustomerId(UUID id) implements Serializable {

    public CustomerId {
    }

    public static CustomerId generate() {
        return new CustomerId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return id.toString();
    }
}
