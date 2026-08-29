package io.eventuate.otherservice.othersubdomain.eventpublishing;

import io.eventuate.otherservice.othersubdomain.domain.OtherEvent;
import io.eventuate.tram.events.publisher.DomainEventPublisher;
import io.eventuate.tram.spring.testing.cloudcontract.EnableEventuateTramContractVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, classes = {BaseClass.TestConfig.class})
public abstract class BaseClass {

    @Autowired
    private DomainEventPublisher domainEventPublisher;

    public void otherEvent() {
        domainEventPublisher.publish("io.eventuate.otherservice.othersubdomain.domain.OtherAggregate", "99", List.of(new OtherEvent(101L)));
    }

    @Configuration
    @EnableAutoConfiguration
    @EnableEventuateTramContractVerifier
    public static class TestConfig {

    }


}
