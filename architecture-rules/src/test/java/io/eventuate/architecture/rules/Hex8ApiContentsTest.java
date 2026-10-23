package io.eventuate.architecture.rules;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.EvaluationResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Hex8ApiContentsTest {

    private static final String ROOT_PACKAGE = "io.eventuate.architecture.rules.fixtures.hex8";
    private static final String HEX_8 = "HEX-8 (API projects contain only interfaces and value types)";

    @Test
    void plainClassFails() {
        EvaluationResult result = evaluate(roleContext("orders"));

        assertThat(result.hasViolation()).isTrue();
        assertThat(result.getFailureReport().toString())
                .contains(HEX_8)
                .contains(ROOT_PACKAGE + ".orders.api.OrderTotals");
    }

    @Test
    void recordAnnotatedEmbeddableFails() {
        EvaluationResult result = evaluate(roleContext("orders"));

        assertThat(result.hasViolation()).isTrue();
        assertThat(result.getFailureReport().toString())
                .contains(HEX_8)
                .contains(ROOT_PACKAGE + ".orders.api.OrderLine")
                .contains("jakarta.persistence.Embeddable");
    }

    @Test
    void interfaceRecordAndEnumWithoutAnnotationsPass() {
        EvaluationResult result = evaluate(roleContext("customers"));

        assertThat(result.hasViolation()).as(result.getFailureReport().toString()).isFalse();
    }

    private static RoleContext roleContext(String subdomain) {
        return new RoleContext("api", ROOT_PACKAGE, subdomain, ROOT_PACKAGE + "." + subdomain + ".api", "unused");
    }

    private static EvaluationResult evaluate(RoleContext context) {
        JavaClasses classes = new ClassFileImporter().importPackages(context.projectPackage());
        return ArchitectureRuleDefinitions.hex8ApiContents().evaluate(classes);
    }
}
