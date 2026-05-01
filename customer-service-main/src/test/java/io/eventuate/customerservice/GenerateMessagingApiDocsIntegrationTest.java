package io.eventuate.customerservice;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.eventuate.tram.spring.inmemory.EnableTramInMemory;
import io.restassured.RestAssured;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = GenerateMessagingApiDocsIntegrationTest.Config.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class GenerateMessagingApiDocsIntegrationTest {

    @Configuration
    @EnableAutoConfiguration(exclude = {FlywayAutoConfiguration.class})
    @EnableTramInMemory
    @ComponentScan
    public static class Config {
    }

    @LocalServerPort
    private int port;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void shouldGenerateAsyncApiDocs() throws IOException {
        String body = given()
            .when()
                .get("/springwolf/docs")
            .then()
                .statusCode(200)
                .extract().asString();

        JsonNode channels = objectMapper.readTree(body).path("channels");
        assertThat(channels).isNotEmpty();

        Path outputDir = Path.of("build/api-docs");
        Files.createDirectories(outputDir);
        Files.writeString(outputDir.resolve("asyncapi.json"), body);
    }
}
