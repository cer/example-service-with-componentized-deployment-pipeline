package io.eventuate.architecture.rules;

import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

// The rules under "Rules implemented by ArchUnit" in docs/design/architecture-rules-enforcement.md
public final class ArchitectureRuleDefinitions {

    private static final String CONFIGURATION_ANNOTATION = "org.springframework.context.annotation.Configuration";
    private static final String BEAN_ANNOTATION = "org.springframework.context.annotation.Bean";

    private ArchitectureRuleDefinitions() {
    }

    public static ArchRule hex6EachProjectWiresItself(RoleContext context) {
        String description = "HEX-6 (each project wires itself)";
        ArchRule beansAreOwnOrThirdPartyTypes = methods().that().areAnnotatedWith(BEAN_ANNOTATION)
                .should(returnATypeInTheProjectOrOutsideTheRootPackage(context))
                .allowEmptyShould(true);
        if (!context.isApi()) {
            return beansAreOwnOrThirdPartyTypes.as(description);
        }
        ArchRule noConfigurationClasses = noClasses().should().beMetaAnnotatedWith(CONFIGURATION_ANNOTATION)
                .because("an API project is wired by the projects that implement it");
        return CompositeArchRule.of(noConfigurationClasses).and(beansAreOwnOrThirdPartyTypes).as(description);
    }

    private static ArchCondition<JavaMethod> returnATypeInTheProjectOrOutsideTheRootPackage(RoleContext context) {
        return new ArchCondition<>("return a type in the project's package, a sub-package of it, or outside the root package") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                String returnedPackage = method.getRawReturnType().getPackageName();
                boolean satisfied = RoleContext.isInOrUnder(returnedPackage, context.projectPackage())
                        || !RoleContext.isInOrUnder(returnedPackage, context.rootPackage());
                events.add(new SimpleConditionEvent(method, satisfied,
                        "@Bean method " + method.getFullName() + " returns " + method.getRawReturnType().getName()
                                + ", which belongs to another project under " + context.rootPackage()));
            }
        };
    }
}
