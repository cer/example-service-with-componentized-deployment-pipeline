package io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain;

public record CustomerCreditReservedEvent(Long orderId) implements CustomerEvent {}
