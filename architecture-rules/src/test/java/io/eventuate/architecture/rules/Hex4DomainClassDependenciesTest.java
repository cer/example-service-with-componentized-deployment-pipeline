package io.eventuate.architecture.rules;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.EvaluationResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Hex4DomainClassDependenciesTest {

    private static final String ROOT_PACKAGE = "io.eventuate.architecture.rules.fixtures.hex4";
    private static final String COMMAND_WITH_DESTINATION = "io.eventuate.tram.commands.consumer.CommandWithDestination";

    @Test
    void domainClassWithRestClientFieldFails() {
        EvaluationResult result = evaluate(roleContext("domain", "customers", "customers.domain"));

        assertThat(result.hasViolation()).isTrue();
        assertThat(result.getFailureReport().toString())
                .contains("HEX-4 (domain projects stay free of infrastructure technology)")
                .contains(ROOT_PACKAGE + ".customers.domain.CustomerCreditCheck")
                .contains("org.springframework.web.client.RestClient");
    }

    @Test
    void sagasClassDependingOnCommandWithDestinationPasses() {
        EvaluationResult result = evaluate(roleContext("sagas", "orders", "orders.sagas"));

        assertThat(result.hasViolation()).as(result.getFailureReport().toString()).isFalse();
    }

    @Test
    void domainClassDependingOnCommandWithDestinationFails() {
        EvaluationResult result = evaluate(roleContext("domain", "orders", "orders.domain"));

        assertThat(result.hasViolation()).isTrue();
        assertThat(result.getFailureReport().toString())
                .contains("HEX-4 (domain projects stay free of infrastructure technology)")
                .contains(ROOT_PACKAGE + ".orders.domain.CreateOrderSagaStep")
                .contains(COMMAND_WITH_DESTINATION);
    }

    private static RoleContext roleContext(String role, String subdomain, String packageUnderRoot) {
        return new RoleContext(role, ROOT_PACKAGE, subdomain, ROOT_PACKAGE + "." + packageUnderRoot, "unused");
    }

    private static EvaluationResult evaluate(RoleContext context) {
        JavaClasses classes = new ClassFileImporter().importPackages(context.projectPackage());
        return ArchitectureRuleDefinitions.hex4DomainClassDependencies(context).evaluate(classes);
    }
}
