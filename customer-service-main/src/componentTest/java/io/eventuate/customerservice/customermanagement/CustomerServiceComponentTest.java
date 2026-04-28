package io.eventuate.customerservice.customermanagement;

import io.eventuate.common.testcontainers.EventuateVanillaPostgresContainer;
import io.eventuate.customerservice.customermanagement.commandapi.ReserveCreditCommand;
import io.eventuate.customerservice.customermanagement.domain.CustomerCreatedEvent;
import io.eventuate.customerservice.customermanagement.domain.CustomerCreditReservedEvent;
import io.eventuate.customerservice.customermanagement.sagas.proxies.CustomerServiceProxy;
import io.eventuate.examples.common.money.Money;
import io.eventuate.examples.springauthorizationserver.testcontainers.AuthorizationServerContainerForServiceContainers;
import io.eventuate.messaging.kafka.testcontainers.EventuateKafkaNativeCluster;
import io.eventuate.messaging.kafka.testcontainers.EventuateKafkaNativeContainer;
import io.eventuate.testcontainers.service.BuildArgsResolver;
import io.eventuate.testcontainers.service.ServiceContainer;
import io.eventuate.tram.spring.testing.outbox.commands.CommandOutboxTestSupport;
import io.eventuate.tram.spring.testing.outbox.commands.EnableCommandOutboxTestSupport;
import io.eventuate.tram.spring.testing.outbox.events.DomainEventOutboxTestSupport;
import io.eventuate.tram.spring.testing.outbox.events.EnableDomainEventOutboxTestSupport;
import io.eventuate.tram.testing.producer.kafka.commands.DirectToKafkaCommandProducer;
import io.eventuate.tram.testing.producer.kafka.commands.EnableDirectToKafkaCommandProducer;
import io.eventuate.tram.testing.producer.kafka.events.DirectToKafkaDomainEventPublisher;
import io.eventuate.tram.testing.producer.kafka.events.EnableDirectToKafkaDomainEventPublisher;
import io.eventuate.tram.testing.producer.kafka.replies.EnableDirectToKafkaCommandReplyProducer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.lifecycle.Startables;

import java.nio.file.Paths;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(classes = CustomerServiceComponentTest.TestConfiguration.class)
public class CustomerServiceComponentTest {

    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(CustomerServiceComponentTest.class);

    @Configuration
    @EnableAutoConfiguration
    @EnableCommandOutboxTestSupport
    @EnableDirectToKafkaCommandReplyProducer
    @EnableDirectToKafkaCommandProducer
    @EnableDirectToKafkaDomainEventPublisher
    @EnableDomainEventOutboxTestSupport
    static class TestConfiguration {
    }

    public static EventuateKafkaNativeCluster eventuateKafkaCluster = new EventuateKafkaNativeCluster("customer-service-tests");

    public static EventuateKafkaNativeContainer kafka = eventuateKafkaCluster.kafka
        .withNetworkAliases("kafka")
        .withReuse(true);

    public static EventuateVanillaPostgresContainer database =
            new EventuateVanillaPostgresContainer()
                    .withNetwork(eventuateKafkaCluster.network)
                    .withNetworkAliases("customer-service-db")
                    .withReuse(true);

    public static AuthorizationServerContainerForServiceContainers authorizationServer = new AuthorizationServerContainerForServiceContainers()
            .withUserDb()
            .withNetwork(eventuateKafkaCluster.network)
            .withNetworkAliases("authorization-server")
            .withEnv("USERS_INITIAL_0_USERNAME", "user")
            .withEnv("USERS_INITIAL_0_PASSWORD", "password")
            .withEnv("USERS_INITIAL_0_ROLES_0_", "USER")
            .withEnv("USERS_INITIAL_0_ENABLED", "true")
            .withReuse(true);

    public static ServiceContainer service =
            new ServiceContainer(new ImageFromDockerfile()
                    .withFileFromPath(".", Paths.get(".").toAbsolutePath())
                    .withDockerfilePath("Dockerfile")
                    .withBuildArgs(BuildArgsResolver.buildArgs()))
                    .withNetwork(eventuateKafkaCluster.network)
                    .withDatabase(database)
                    .withKafka(kafka)
                    .withEnv(authorizationServer.resourceServerEnv())
                    .withLogConsumer(new Slf4jLogConsumer(logger).withPrefix("SVC security-system-service:"))
                    .withReuse(false)
            ;

    @Autowired
    private CommandOutboxTestSupport commandOutboxTestSupport;

    @Autowired
    private DirectToKafkaCommandProducer commandProducer;

    @Autowired
    private DirectToKafkaDomainEventPublisher domainEventPublisher;

    @Autowired
    private DomainEventOutboxTestSupport domainEventOutboxTestSupport;

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        Startables.deepStart(service, authorizationServer).join();

        kafka.registerProperties(registry::add);
        database.registerProperties(registry::add);
    }

    @BeforeEach
    public void setup() {
        RestAssured.port = service.getFirstMappedPort();
        RestAssured.authentication = RestAssured.oauth2(authorizationServer.getJwt());
    }

    @Test
    public void shouldStart() {
    }

    @Test
    public void shouldCreateAndGetCustomer() {
        String name = "Component Test Customer";
        String creditLimit = "500.00";

        String createCustomerJson = """
            {
                "name": "%s",
                "creditLimit": {"amount": "%s"}
            }
            """.formatted(name, creditLimit);

        String createdCustomerId = given()
                .contentType(ContentType.JSON)
                .body(createCustomerJson)
                .when()
                .post("/customers")
                .then()
                .statusCode(200)
                .extract()
                .path("customerId");

        // Verify CustomerCreatedEvent published to outbox
        domainEventOutboxTestSupport.assertDomainEventInOutbox(
                "io.eventuate.customerservice.customermanagement.domain.Customer",
                createdCustomerId,
                CustomerCreatedEvent.class.getName());

        // Verify GET /customers returns the created customer
        given()
            .when()
            .get("/customers")
            .then()
            .log().ifValidationFails()
            .statusCode(200)
            .body("customers.find { it.customerId == '%s' }.name".formatted(createdCustomerId), org.hamcrest.Matchers.equalTo(name));
    }

    @Test
    public void shouldGetActuatorHealth() {
        given().
            when()
            .auth().none()
            .get("/actuator/health")
            .then()
            .statusCode(200);
    }

    @Test
    public void shouldReserveCreditViaSaga() {
        long customerId = System.currentTimeMillis();
        long orderId = 102L;
        String orderTotal = "12.34";

        // First create the customer
        String createCustomerJson = """
            {
                "name": "Test Customer",
                "creditLimit": {"amount": %s}
            }
            """.formatted("1000.00");

        String createdCustomerId = given()
                .contentType(ContentType.JSON)
                .body(createCustomerJson)
                .when()
                .post("/customers")
                .then()
                .statusCode(200)
                .extract()
                .path("customerId");

        // POST to trigger the saga
        String reservationJson = """
            {
                "orderId": %d,
                "orderTotal": {"amount": %s}
            }
            """.formatted(orderId, orderTotal);

        given()
                .contentType(ContentType.JSON)
                .body(reservationJson)
                .when()
                .post("/customers/{customerId}/creditreservations", createdCustomerId)
                .then()
                .log().ifValidationFails()
                .statusCode(200);

        // Verify the saga sent a ReserveCreditCommand
        UUID expectedCustomerId = UUID.fromString(createdCustomerId);
        commandOutboxTestSupport.assertThatCommandMessageSent(
                ReserveCreditCommand.class, CustomerServiceProxy.CHANNEL,
                cmd -> cmd.customerId().equals(expectedCustomerId));
    }

    @Test
    public void shouldGetSwaggerUi() {
        given()
            .when()
            .auth().none()
            .get("/swagger-ui/index.html")
            .then()
            .log().ifValidationFails()
            .statusCode(200);
    }

    @Test
    public void shouldGetOpenApiDocs() {
        assertUnauthenticatedEndpointContains("/v3/api-docs", "/customers");
    }

    @Test
    public void shouldGetAsyncApiDocs() {
        assertUnauthenticatedEndpointContains("/springwolf/docs",
                "io.eventuate.customerservice.customermanagement.domain.Customer");
    }

    private void assertUnauthenticatedEndpointContains(String path, String expectedContent) {
        String body = given()
            .when()
            .auth().none()
            .get(path)
            .then()
            .statusCode(200)
            .extract().asString();

        assertThat(body).contains(expectedContent);
    }

    @Test
    public void shouldHandleReserveCreditCommand() {
        // Create a customer via REST API
        String createCustomerJson = """
            {
                "name": "Command Test Customer",
                "creditLimit": {"amount": "1000.00"}
            }
            """;

        String createdCustomerId = given()
                .contentType(ContentType.JSON)
                .body(createCustomerJson)
                .when()
                .post("/customers")
                .then()
                .statusCode(200)
                .extract()
                .path("customerId");

        UUID customerId = UUID.fromString(createdCustomerId);
        long orderId = System.currentTimeMillis();
        Money orderTotal = new Money("12.34");
        String replyTo = "test-reply-channel-" + System.currentTimeMillis();

        // Send a ReserveCreditCommand directly to Kafka
        commandProducer.send("customerService",
                new ReserveCreditCommand(customerId, orderId, orderTotal),
                replyTo, Collections.emptyMap());

        // Verify the command handler processed it and sent a reply
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() ->
                commandOutboxTestSupport.assertCommandReplyMessageSent(replyTo)
        );

        // Verify CustomerCreditReservedEvent was published
        domainEventOutboxTestSupport.assertDomainEventInOutbox(
                "io.eventuate.customerservice.customermanagement.domain.Customer",
                createdCustomerId,
                CustomerCreditReservedEvent.class.getName());
    }

    @Test
    public void shouldConsumeCustomerCreditReservedEvent() {
        CustomerCreditReservedEvent event = new CustomerCreditReservedEvent(99L);

        domainEventPublisher.publish(
                "io.eventuate.customerservice.customermanagement.domain.Customer",
                "1",
                event);

        // TODO Need an effective way to validate
    }
}
