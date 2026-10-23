package io.eventuate.architecture.rules;

import io.eventuate.architecture.rules.fixtures.hex6.orders.api.OrdersApiConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.testkit.engine.EngineTestKit;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;

// Runs ArchitectureRulesTest the way checkArchitectureRules does: on the JUnit Platform, configured by system properties
class ArchitectureRulesTestTest {

    private static final String HEX6_ROOT_PACKAGE = "io.eventuate.architecture.rules.fixtures.hex6";
    private static final String HEX4_ROOT_PACKAGE = "io.eventuate.architecture.rules.fixtures.hex4";
    private static final String HEX4 = "HEX-4 (domain projects stay free of infrastructure technology)";

    private Map<String, String> properties = Map.of();

    @AfterEach
    void clearSystemProperties() {
        properties.keySet().forEach(System::clearProperty);
    }

    @Test
    void apiProjectWithConfigurationClassFailsHex6() {
        List<String> failures = runArchitectureRulesTest("api", HEX6_ROOT_PACKAGE, "orders", "orders.api");

        assertThat(failures).anySatisfy(failure -> assertThat(failure)
                .contains("HEX-6 (each project wires itself)")
                .contains(OrdersApiConfiguration.class.getName()));
    }

    @Test
    void domainProjectWithRestClientFieldFailsHex4() {
        List<String> failures = runArchitectureRulesTest("domain", HEX4_ROOT_PACKAGE, "customers", "customers.domain");

        assertThat(failures).anySatisfy(failure -> assertThat(failure)
                .contains(HEX4)
                .contains(HEX4_ROOT_PACKAGE + ".customers.domain.CustomerCreditCheck")
                .contains("org.springframework.web.client.RestClient"));
    }

    @Test
    void adapterProjectIsNotCheckedAgainstHex4() {
        List<String> failures = runArchitectureRulesTest("rest-api", HEX4_ROOT_PACKAGE, "customers", "customers.restapi");

        assertThat(failures).noneSatisfy(failure -> assertThat(failure).contains(HEX4));
    }

    private List<String> runArchitectureRulesTest(String role, String group, String subdomain, String packageUnderRoot) {
        properties = Map.of(
                "architectureRules.role", role,
                "architectureRules.group", group,
                "architectureRules.subdomain", subdomain,
                "architectureRules.package", group + "." + packageUnderRoot,
                "architectureRules.classesDir", fixtureClassesDir());
        properties.forEach(System::setProperty);

        return EngineTestKit.engine("archunit")
                .selectors(selectClass(ArchitectureRulesTest.class))
                .execute()
                .testEvents()
                .failed().stream()
                .map(event -> event.getRequiredPayload(TestExecutionResult.class))
                .map(result -> result.getThrowable().orElseThrow().getMessage())
                .toList();
    }

    private static String fixtureClassesDir() {
        try {
            return Path.of(OrdersApiConfiguration.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toString();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }
}
