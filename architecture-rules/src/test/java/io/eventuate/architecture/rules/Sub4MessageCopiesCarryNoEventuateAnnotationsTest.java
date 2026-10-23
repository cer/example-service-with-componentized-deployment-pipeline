package io.eventuate.architecture.rules;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.EvaluationResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Sub4MessageCopiesCarryNoEventuateAnnotationsTest {

    private static final String ROOT_PACKAGE = "io.eventuate.architecture.rules.fixtures.sub4";
    private static final String PROJECT_PACKAGE = ROOT_PACKAGE + ".orders.eventsubscribers";
    private static final String SUB_4 = "SUB-4 (messages are copied, not shared)";

    @Test
    void copyClassWithAnEventuateAnnotatedMethodFails() {
        EvaluationResult result = evaluate(ROOT_PACKAGE + ".customers.domain");

        assertThat(result.hasViolation()).isTrue();
        assertThat(result.getFailureReport().toString())
                .contains(SUB_4)
                .contains(ROOT_PACKAGE + ".customers.domain.CustomerCreditReserved")
                .doesNotContain("OrderEventConsumer");
    }

    @Test
    void copyClassWithNoAnnotationsPasses() {
        EvaluationResult result = evaluate(ROOT_PACKAGE + ".accounts.domain");

        assertThat(result.hasViolation()).as(result.getFailureReport().toString()).isFalse();
    }

    private static EvaluationResult evaluate(String copyPackage) {
        RoleContext context = new RoleContext("event-subscribers", ROOT_PACKAGE, "orders", PROJECT_PACKAGE, "unused");
        JavaClasses classes = new ClassFileImporter().importPackages(PROJECT_PACKAGE, copyPackage);
        return ArchitectureRuleDefinitions.sub4MessageCopiesCarryNoEventuateAnnotations(context).evaluate(classes);
    }
}
