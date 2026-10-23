package io.eventuate.architecture.rules.fixtures.hex8.orders.api;

import jakarta.persistence.Embeddable;

@Embeddable
public record OrderLine(String productId, int quantity) {
}
