package io.eventuate.customerservice.customers.messaging;

import io.eventuate.customerservice.customers.api.messaging.commands.ReserveCreditCommand;
import io.eventuate.customerservice.customers.api.messaging.replies.CustomerCreditLimitExceeded;
import io.eventuate.customerservice.customers.api.messaging.replies.CustomerCreditReserved;
import io.eventuate.customerservice.customers.api.messaging.replies.CustomerNotFound;
import io.eventuate.customerservice.customers.domain.CustomerCreditLimitExceededException;
import io.eventuate.customerservice.customers.domain.CustomerNotFoundException;
import io.eventuate.customerservice.customers.domain.CustomerService;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.commands.consumer.annotations.EventuateCommandHandler;
import io.eventuate.tram.messaging.common.Message;

import static io.eventuate.tram.commands.consumer.CommandHandlerReplyBuilder.withFailure;
import static io.eventuate.tram.commands.consumer.CommandHandlerReplyBuilder.withSuccess;

public class CustomerCommandHandler {

  private CustomerService customerService;

  public CustomerCommandHandler(CustomerService customerService) {
    this.customerService = customerService;
  }


  @EventuateCommandHandler(subscriberId="customerCommandDispatcher", channel="customerService")
  public Message reserveCredit(CommandMessage<ReserveCreditCommand> cm) {
    ReserveCreditCommand cmd = cm.getCommand();
    try {
      customerService.reserveCredit(cmd.getCustomerId(), cmd.getOrderId(), cmd.getOrderTotal());
      return withSuccess(new CustomerCreditReserved());
    } catch (CustomerNotFoundException e) {
      return withFailure(new CustomerNotFound());
    } catch (CustomerCreditLimitExceededException e) {
      return withFailure(new CustomerCreditLimitExceeded());
    }
  }

}
