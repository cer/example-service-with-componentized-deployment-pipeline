package io.eventuate.architecture.rules;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.EvaluationResult;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Sub8SharedLibraryContentsTest {

    private static final String ROOT_PACKAGE = "io.eventuate.architecture.rules.fixtures.sub8";
    private static final String SUB_8 = "SUB-8 (shared libraries hold only value types)";

    @Test
    void plainClassFails() {
        EvaluationResult result = evaluate("money");

        assertThat(result.hasViolation()).isTrue();
        assertThat(result.getFailureReport().toString())
                .contains(SUB_8)
                .contains(ROOT_PACKAGE + ".money.MoneyCalculator");
    }

    @Test
    void recordPasses() {
        EvaluationResult result = evaluate("units");

        assertThat(result.hasViolation()).as(result.getFailureReport().toString()).isFalse();
    }

    private static EvaluationResult evaluate(String sharedLibrary) {
        JavaClasses classes = new ClassFileImporter().importPackages(ROOT_PACKAGE + "." + sharedLibrary);
        return ArchitectureRuleDefinitions.sub8SharedLibraryContents().evaluate(classes);
    }
}
