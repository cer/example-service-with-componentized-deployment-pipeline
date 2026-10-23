package io.eventuate.architecture.rules;

import io.eventuate.architecture.rules.fixtures.hex6.orders.api.OrdersApiConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineTestKit;
import org.junit.platform.testkit.engine.Events;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

// Runs ArchitectureRulesTest the way checkArchitectureRules does: on the JUnit Platform, configured by system properties
class ArchitectureRulesTestTest {

    private static final String ROOT_PACKAGE = "io.eventuate.architecture.rules.fixtures.hex6";

    private static final Map<String, String> API_PROJECT_PROPERTIES = Map.of(
            "architectureRules.role", "api",
            "architectureRules.group", ROOT_PACKAGE,
            "architectureRules.subdomain", "orders",
            "architectureRules.package", ROOT_PACKAGE + ".orders.api",
            "architectureRules.classesDir", fixtureClassesDir());

    @AfterEach
    void clearSystemProperties() {
        API_PROJECT_PROPERTIES.keySet().forEach(System::clearProperty);
    }

    @Test
    void apiProjectWithConfigurationClassFailsHex6() {
        API_PROJECT_PROPERTIES.forEach(System::setProperty);

        Events tests = EngineTestKit.engine("archunit")
                .selectors(selectClass(ArchitectureRulesTest.class))
                .execute()
                .testEvents();

        assertThat(tests.failed().count()).isEqualTo(1);
        assertThat(onlyFailureMessage(tests))
                .contains("HEX-6 (each project wires itself)")
                .contains(OrdersApiConfiguration.class.getName());
    }

    private static String onlyFailureMessage(Events tests) {
        return tests.failed().stream()
                .map(event -> event.getRequiredPayload(TestExecutionResult.class))
                .map(result -> result.getThrowable().orElseThrow().getMessage())
                .findFirst().orElseThrow();
    }

    private static String fixtureClassesDir() {
        try {
            return Path.of(OrdersApiConfiguration.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
