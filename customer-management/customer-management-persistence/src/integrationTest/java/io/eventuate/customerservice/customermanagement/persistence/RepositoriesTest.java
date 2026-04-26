package io.eventuate.customerservice.customermanagement.persistence;

import io.eventuate.common.testcontainers.EventuateVanillaPostgresContainer;
import io.eventuate.common.testcontainers.PropertyProvidingContainer;
import io.eventuate.examples.common.money.Money;
import io.eventuate.customerservice.customermanagement.domain.Customer;
import io.eventuate.customerservice.customermanagement.domain.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ContextConfiguration(classes=RepositoriesTest.Config.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Transactional(propagation = Propagation.NEVER)
@Testcontainers
public class RepositoriesTest {


  @Container
  public static EventuateVanillaPostgresContainer database = new EventuateVanillaPostgresContainer();

  @DynamicPropertySource
  static void registerDbProperties(DynamicPropertyRegistry registry) {
    PropertyProvidingContainer.startAndProvideProperties(registry, database);
  }

  public static final String customerName = "Chris";

  @Configuration
  @Import(CustomerPersistenceConfiguration.class)
  static public class Config {
  }

  @Autowired
  private CustomerRepository customerRepository;

  @Autowired
  private TransactionTemplate transactionTemplate;

  @Test
  public void shouldSaveAndLoadCustomer() {
    Money creditLimit = new Money("12.34");
    Money amount = new Money("10");
    Money expectedAvailableCredit = creditLimit.subtract(amount);

    Customer c = new Customer(customerName, creditLimit);

    transactionTemplate.executeWithoutResult( ts -> customerRepository.save(c) );

    transactionTemplate.executeWithoutResult(ts -> {
      Customer c2 = customerRepository.findById(c.getId()).get();
      assertThat(c2.getName()).isEqualTo(customerName);
      assertThat(c2.getCreditLimit()).isEqualTo(creditLimit);
      assertThat(c2.availableCredit()).isEqualTo(creditLimit);

      c2.reserveCredit(1234L, amount);
    });

    transactionTemplate.executeWithoutResult(ts -> {
      Customer c2 = customerRepository.findById(c.getId()).get();
      assertThat(c2.availableCredit()).isEqualTo(expectedAvailableCredit);

    });


  }
}
