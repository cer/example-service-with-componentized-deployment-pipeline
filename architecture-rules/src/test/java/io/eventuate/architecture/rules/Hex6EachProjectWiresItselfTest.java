package io.eventuate.architecture.rules;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.EvaluationResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Hex6EachProjectWiresItselfTest {

    private static final String ROOT_PACKAGE = "io.eventuate.architecture.rules.fixtures.hex6";

    @Test
    void apiProjectWithConfigurationClassFails() {
        EvaluationResult result = evaluate(roleContext("api", "orders", "orders.api"));

        assertThat(result.hasViolation()).isTrue();
        assertThat(result.getFailureReport().toString())
                .contains("HEX-6 (each project wires itself)")
                .contains(ROOT_PACKAGE + ".orders.api.OrdersApiConfiguration");
    }

    @Test
    void beanReturningSiblingProjectTypeFails() {
        EvaluationResult result = evaluate(roleContext("domain", "orders", "orders.domain"));

        assertThat(result.hasViolation()).isTrue();
        assertThat(result.getFailureReport().toString())
                .contains("HEX-6 (each project wires itself)")
                .contains(ROOT_PACKAGE + ".orders.domain.OrdersDomainConfiguration.orderRepositoryAdapter()")
                .contains(ROOT_PACKAGE + ".orders.persistence.OrderRepositoryAdapter");
    }

    @Test
    void beansReturningOwnSubPackageAndThirdPartyTypesPass() {
        EvaluationResult result = evaluate(roleContext("domain", "customers", "customers.domain"));

        assertThat(result.hasViolation()).as(result.getFailureReport().toString()).isFalse();
    }

    @Test
    void rootPackageIsThePackageBeforeTheSubdomain() {
        assertThat(roleContext("domain", "customers", "customers.domain").rootPackage()).isEqualTo(ROOT_PACKAGE);
    }

    private static RoleContext roleContext(String role, String subdomain, String packageUnderRoot) {
        return new RoleContext(role, "domain", subdomain, ROOT_PACKAGE + "." + packageUnderRoot, "unused");
    }

    private static EvaluationResult evaluate(RoleContext context) {
        JavaClasses classes = new ClassFileImporter().importPackages(context.projectPackage());
        return ArchitectureRuleDefinitions.hex6EachProjectWiresItself(context).evaluate(classes);
    }
}
