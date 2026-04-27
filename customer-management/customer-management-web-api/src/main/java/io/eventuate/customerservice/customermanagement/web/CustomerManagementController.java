package io.eventuate.customerservice.customermanagement.web;

import io.eventuate.customerservice.customermanagement.api.web.ReserveCreditRequest;
import io.eventuate.customerservice.customermanagement.api.web.ReserveCreditResponse;
import io.eventuate.customerservice.customermanagement.api.web.CreateCustomerRequest;
import io.eventuate.customerservice.customermanagement.api.web.CreateCustomerResponse;
import io.eventuate.customerservice.customermanagement.api.web.GetCustomerResponse;
import io.eventuate.customerservice.customermanagement.api.web.GetCustomersResponse;
import io.eventuate.customerservice.customermanagement.domain.Customer;
import io.eventuate.customerservice.customermanagement.domain.CustomerId;
import io.eventuate.customerservice.customermanagement.domain.CustomerManagementService;
import io.eventuate.customerservice.customermanagement.sagas.CustomerManagementSagaService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@SecurityRequirement(name = "Bearer Authentication")
public class CustomerManagementController {

  private final CustomerManagementService customerManagementService;
  private final CustomerManagementSagaService customerManagementSagaService;

  @Autowired
  public CustomerManagementController(CustomerManagementService customerManagementService,
                                      CustomerManagementSagaService customerManagementSagaService) {
    this.customerManagementService = customerManagementService;
    this.customerManagementSagaService = customerManagementSagaService;
  }

  @RequestMapping(value = "/customers", method = RequestMethod.POST)
  @PreAuthorize("hasRole('USER')")
  public CreateCustomerResponse createCustomer(@RequestBody CreateCustomerRequest createCustomerRequest) {
    Customer customer = customerManagementService.createCustomer(createCustomerRequest.getName(), createCustomerRequest.getCreditLimit());
    return new CreateCustomerResponse(customer.getId().toString());
  }

  @RequestMapping(value="/customers", method= RequestMethod.GET)
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<GetCustomersResponse> getAll() {
    return ResponseEntity.ok(new GetCustomersResponse(customerManagementService.findAll().stream()
            .map(c -> new GetCustomerResponse(c.getId().toString(), c.getName(), c.getCreditLimit())).collect(Collectors.toList())));
  }

  @RequestMapping(value = "/customers/{customerId}/creditreservations", method = RequestMethod.POST)
  @PreAuthorize("hasRole('USER')")
  public ReserveCreditResponse createCreditReservation(@PathVariable String customerId,
                                                                  @RequestBody ReserveCreditRequest request) {
    customerManagementSagaService.reserveCredit(new CustomerId(UUID.fromString(customerId)), request.getOrderId(), request.getOrderTotal());
    return new ReserveCreditResponse("PENDING");
  }

  @RequestMapping(value="/customers/{customerId}", method= RequestMethod.GET)
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<GetCustomerResponse> getCustomer(@PathVariable String customerId) {
    return customerManagementService
            .findById(new CustomerId(UUID.fromString(customerId)))
            .map(c -> new ResponseEntity<>(new GetCustomerResponse(c.getId().toString(), c.getName(), c.getCreditLimit()), HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }
}
