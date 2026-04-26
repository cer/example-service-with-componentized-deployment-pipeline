package io.eventuate.customerservice.customermanagement.messaging;

import io.eventuate.customerservice.customermanagement.domain.CustomerService;
import io.eventuate.tram.commands.common.CommandNameMapping;
import io.eventuate.tram.commands.common.DefaultCommandNameMapping;
import io.eventuate.tram.messaging.common.ChannelMapping;
import io.eventuate.tram.messaging.common.DefaultChannelMapping;
import io.eventuate.tram.spring.cloudcontractsupport.EventuateContractVerifierConfiguration;
import io.eventuate.tram.spring.inmemory.EnableTramInMemory;
import io.eventuate.tram.spring.messaging.producer.jdbc.TramMessageProducerJdbcConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.data.jdbc.AutoConfigureDataJdbc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cloud.contract.verifier.messaging.boot.AutoConfigureMessageVerifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@SpringBootTest(classes = OrderserviceBase.TestConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
@AutoConfigureMessageVerifier
@AutoConfigureDataJdbc
public abstract class OrderserviceBase {

  @Configuration
  //@EnableAutoConfiguration // (exclude = {DataSourceAutoConfiguration.class, EventuateTramKafkaMessageConsumerAutoConfiguration.class})
  @EnableTramInMemory
  @Import({CustomerCommandHandlerConfiguration.class,
          EventuateContractVerifierConfiguration.class,
          TramMessageProducerJdbcConfiguration.class})
  public static class TestConfiguration {

    @Bean
    public ChannelMapping channelMapping() {
      return DefaultChannelMapping.builder().build();
    }

    @Bean
    public CommandNameMapping commandNameMapping() {
      return new DefaultCommandNameMapping();
    }


  }

  @MockBean
  private CustomerService customerService;

  @BeforeEach
  public void setUp() {

  }

}
