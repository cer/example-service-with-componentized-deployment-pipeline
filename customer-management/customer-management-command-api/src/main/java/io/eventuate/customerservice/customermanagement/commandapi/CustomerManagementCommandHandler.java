package io.eventuate.customerservice.customermanagement.commandapi;

import io.eventuate.customerservice.customermanagement.domain.CustomerCreditLimitExceededException;
import io.eventuate.customerservice.customermanagement.domain.CustomerId;
import io.eventuate.customerservice.customermanagement.domain.CustomerNotFoundException;
import io.eventuate.customerservice.customermanagement.domain.CustomerManagementService;
import io.eventuate.tram.commands.consumer.CommandMessage;
import io.eventuate.tram.commands.consumer.annotations.EventuateCommandHandler;
import io.eventuate.tram.messaging.common.Message;

import static io.eventuate.tram.commands.consumer.CommandHandlerReplyBuilder.withFailure;
import static io.eventuate.tram.commands.consumer.CommandHandlerReplyBuilder.withSuccess;

public class CustomerManagementCommandHandler {

  private CustomerManagementService customerManagementService;

  public CustomerManagementCommandHandler(CustomerManagementService customerManagementService) {
    this.customerManagementService = customerManagementService;
  }


  @EventuateCommandHandler(subscriberId="customerCommandDispatcher", channel="customerService")
  public Message reserveCredit(CommandMessage<ReserveCreditCommand> cm) {
    ReserveCreditCommand cmd = cm.getCommand();
    try {
      customerManagementService.reserveCredit(new CustomerId(cmd.customerId()), cmd.orderId(), cmd.orderTotal());
      return withSuccess(new CustomerCreditReserved());
    } catch (CustomerNotFoundException e) {
      return withFailure(new CustomerNotFound());
    } catch (CustomerCreditLimitExceededException e) {
      return withFailure(new CustomerCreditLimitExceeded());
    }
  }

}
