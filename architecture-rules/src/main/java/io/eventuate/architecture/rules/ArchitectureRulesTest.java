package io.eventuate.architecture.rules;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;

// The ArchUnit test that checkArchitectureRules runs against the classes of the project in RoleContext
@AnalyzeClasses(locations = ClassesDirLocationProvider.class)
public class ArchitectureRulesTest {

    @ArchTest
    void hex6EachProjectWiresItself(JavaClasses classes) {
        ArchitectureRuleDefinitions.hex6EachProjectWiresItself(RoleContext.fromSystemProperties()).check(classes);
    }
}
