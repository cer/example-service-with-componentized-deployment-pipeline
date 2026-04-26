package io.eventuate.customerservice.customermanagement;


import io.eventuate.common.testcontainers.EventuateVanillaPostgresContainer;
import io.eventuate.examples.springauthorizationserver.testcontainers.AuthorizationServerContainerForServiceContainers;
import io.eventuate.messaging.kafka.testcontainers.EventuateKafkaNativeCluster;
import io.eventuate.messaging.kafka.testcontainers.EventuateKafkaNativeContainer;
import io.eventuate.testcontainers.service.BuildArgsResolver;
import io.eventuate.testcontainers.service.ServiceContainer;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.lifecycle.Startables;

import java.nio.file.Paths;

import static io.restassured.RestAssured.given;
import static io.restassured.RestAssured.oauth2;

public class CustomerServiceComponentTest {

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
                    .withReuse(false) // should rebuild
            ;

    @BeforeAll
    public static void startContainers() {
        Startables.deepStart(service, authorizationServer).join();
    }

    @BeforeEach
    public void setup() {
        RestAssured.port = service.getFirstMappedPort();
        RestAssured.authentication = oauth2(authorizationServer.getJwt());
    }

    @Test
    public void shouldStart() {
        // HTTP
        // Messaging
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
}
