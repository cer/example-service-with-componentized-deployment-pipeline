package io.eventuate.customerservice.customermanagement.domain;

public record CustomerCreditReservedEvent(Long orderId) implements CustomerEvent {}
