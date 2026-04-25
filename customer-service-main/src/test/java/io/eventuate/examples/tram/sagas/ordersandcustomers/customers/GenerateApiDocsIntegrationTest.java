package io.eventuate.examples.tram.sagas.ordersandcustomers.customers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.eventuate.tram.spring.inmemory.TramInMemoryConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration;
import org.springframework.boot.actuate.autoconfigure.security.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.oauth2.resource.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = GenerateApiDocsIntegrationTest.Config.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class GenerateApiDocsIntegrationTest {

    @Configuration
    @EnableAutoConfiguration(exclude = {
        FlywayAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        SecurityFilterAutoConfiguration.class,
        ManagementWebSecurityAutoConfiguration.class,
        OAuth2ResourceServerAutoConfiguration.class
    })
    @Import(TramInMemoryConfiguration.class)
    @ComponentScan(basePackages = "io.eventuate.examples.tram.sagas.ordersandcustomers.customers",
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX,
            pattern = ".*(SecurityConfig|CustomerWebConfiguration|CustomerServiceMain)"))
    public static class Config {
    }

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void shouldGenerateOpenApiDocs() throws IOException {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/v3/api-docs", String.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotBlank();

        JsonNode paths = objectMapper.readTree(response.getBody()).path("paths");
        assertThat(paths.has("/customers")).isTrue();

        Path outputDir = Path.of("build/api-docs");
        Files.createDirectories(outputDir);
        Files.writeString(outputDir.resolve("openapi.json"), response.getBody());
    }

    @Test
    void shouldGenerateAsyncApiDocs() throws IOException {
        ResponseEntity<String> response = restTemplate.getForEntity(
            "http://localhost:" + port + "/springwolf/docs", String.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(response.getBody()).isNotBlank();

        JsonNode root = objectMapper.readTree(response.getBody());
        assertThat(root.path("channels").has(
            "io.eventuate.examples.tram.sagas.ordersandcustomers.customers.domain.Customer")).isTrue();

        Path outputDir = Path.of("build/api-docs");
        Files.createDirectories(outputDir);
        Files.writeString(outputDir.resolve("asyncapi.json"), response.getBody());
    }
}
