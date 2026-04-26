package io.eventuate.customerservice.customermanagement.web;

import io.eventuate.customerservice.customermanagement.api.web.CreateCustomerRequest;
import io.eventuate.customerservice.customermanagement.api.web.CreateCustomerResponse;
import io.eventuate.customerservice.customermanagement.api.web.GetCustomerResponse;
import io.eventuate.customerservice.customermanagement.api.web.GetCustomersResponse;
import io.eventuate.customerservice.customermanagement.domain.Customer;
import io.eventuate.customerservice.customermanagement.domain.CustomerService;
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

import java.util.stream.Collectors;

@RestController
@SecurityRequirement(name = "Bearer Authentication")
public class CustomerController {

  private final CustomerService customerService;

  @Autowired
  public CustomerController(CustomerService customerService) {
    this.customerService = customerService;
  }

  @RequestMapping(value = "/customers", method = RequestMethod.POST)
  @PreAuthorize("hasRole('USER')")
  public CreateCustomerResponse createCustomer(@RequestBody CreateCustomerRequest createCustomerRequest) {
    Customer customer = customerService.createCustomer(createCustomerRequest.getName(), createCustomerRequest.getCreditLimit());
    return new CreateCustomerResponse(customer.getId());
  }

  @RequestMapping(value="/customers", method= RequestMethod.GET)
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<GetCustomersResponse> getAll() {
    return ResponseEntity.ok(new GetCustomersResponse(customerService.findAll().stream()
            .map(c -> new GetCustomerResponse(c.getId(), c.getName(), c.getCreditLimit())).collect(Collectors.toList())));
  }

  @RequestMapping(value="/customers/{customerId}", method= RequestMethod.GET)
  @PreAuthorize("hasRole('USER')")
  public ResponseEntity<GetCustomerResponse> getCustomer(@PathVariable Long customerId) {
    return customerService
            .findById(customerId)
            .map(c -> new ResponseEntity<>(new GetCustomerResponse(c.getId(), c.getName(), c.getCreditLimit()), HttpStatus.OK))
            .orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
  }
}
