package io.eventuate.customerservice.customermanagement;

import io.eventuate.common.testcontainers.EventuateVanillaPostgresContainer;
import io.eventuate.customerservice.customermanagement.api.messaging.commands.ReserveCreditCommand;
import io.eventuate.customerservice.customermanagement.sagas.proxies.CustomerServiceProxy;
import io.eventuate.examples.springauthorizationserver.testcontainers.AuthorizationServerContainerForServiceContainers;
import io.eventuate.messaging.kafka.testcontainers.EventuateKafkaNativeCluster;
import io.eventuate.messaging.kafka.testcontainers.EventuateKafkaNativeContainer;
import io.eventuate.testcontainers.service.BuildArgsResolver;
import io.eventuate.testcontainers.service.ServiceContainer;
import io.eventuate.tram.spring.testing.outbox.commands.CommandOutboxTestSupport;
import io.eventuate.tram.spring.testing.outbox.commands.EnableCommandOutboxTestSupport;
import io.eventuate.tram.testing.producer.kafka.replies.EnableDirectToKafkaCommandReplyProducer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.lifecycle.Startables;

import java.nio.file.Paths;

import static io.restassured.RestAssured.given;

@SpringBootTest(classes = CustomerServiceComponentTest.TestConfiguration.class)
public class CustomerServiceComponentTest {

    @Configuration
    @EnableAutoConfiguration
    @EnableCommandOutboxTestSupport
    @EnableDirectToKafkaCommandReplyProducer
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
                    .dependsOn(kafka, database)
                    .withEnv(authorizationServer.resourceServerEnv())
                    .withLogConsumer(outputFrame -> System.out.print(outputFrame.getUtf8String()))
                    .withReuse(false)
            ;

    @Autowired
    private CommandOutboxTestSupport commandOutboxTestSupport;

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
    public void shouldGetCustomers() {
        given()
            .when()
            .get("/customers")
            .then()
            .log().ifValidationFails()
            .statusCode(200);
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

        Number createdCustomerId = given()
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
        commandOutboxTestSupport.assertThatCommandMessageSent(
                ReserveCreditCommand.class, CustomerServiceProxy.CHANNEL,
                cmd -> cmd.getCustomerId() == createdCustomerId.longValue());
    }
}
