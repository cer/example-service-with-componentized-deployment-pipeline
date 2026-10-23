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

    @ArchTest
    void hex4DomainClassDependencies(JavaClasses classes) {
        RoleContext context = RoleContext.fromSystemProperties();
        if (context.isInDomainGroup()) {
            ArchitectureRuleDefinitions.hex4DomainClassDependencies(context).check(classes);
        }
    }

    @ArchTest
    void hex8ApiContents(JavaClasses classes) {
        if (RoleContext.fromSystemProperties().isApi()) {
            ArchitectureRuleDefinitions.hex8ApiContents().check(classes);
        }
    }

    @ArchTest
    void sub8SharedLibraryContents(JavaClasses classes) {
        if (RoleContext.fromSystemProperties().isSharedLibrary()) {
            ArchitectureRuleDefinitions.sub8SharedLibraryContents().check(classes);
        }
    }
}
