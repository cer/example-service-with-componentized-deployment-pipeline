package io.eventuate.customerservice.customers.domain;

public record CustomerCreditReservedEvent(Long orderId) implements CustomerEvent {}
