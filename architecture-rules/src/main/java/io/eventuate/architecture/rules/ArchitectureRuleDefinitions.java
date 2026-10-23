package io.eventuate.architecture.rules;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAnnotation;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.CompositeArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import static com.tngtech.archunit.base.DescribedPredicate.not;
import static com.tngtech.archunit.core.domain.JavaClass.Predicates.resideInAnyPackage;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

// The rules under "Rules implemented by ArchUnit" in docs/design/architecture-rules-enforcement.md
public final class ArchitectureRuleDefinitions {

    private static final String CONFIGURATION_ANNOTATION = "org.springframework.context.annotation.Configuration";
    private static final String BEAN_ANNOTATION = "org.springframework.context.annotation.Bean";

    private static final String[] FORBIDDEN_INFRASTRUCTURE_PACKAGES = {
            "java.sql..",
            "javax.sql..",
            "org.springframework.web..",
            "org.springframework.jdbc..",
            "org.flywaydb..",
            "jakarta.servlet..",
            "org.apache.kafka..",
            "io.eventuate.tram.messaging.producer..",
            "io.eventuate.tram.messaging.consumer..",
            "io.eventuate.tram.commands..",
    };

    // The orchestration DSL's Command and CommandWithDestination types are in the command common and consumer packages
    private static final String[] PERMITTED_SAGAS_PACKAGES = {
            "io.eventuate.tram.commands.common..",
            "io.eventuate.tram.commands.consumer..",
            "io.eventuate.tram.sagas..",
    };

    private static final String[] FRAMEWORK_ANNOTATION_PACKAGES = {
            "org.springframework..",
            "jakarta.persistence..",
            "io.eventuate..",
    };

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

    public static ArchRule hex4DomainClassDependencies(RoleContext context) {
        return noClasses().should().dependOnClassesThat(resideInAForbiddenInfrastructurePackage(context))
                .as("HEX-4 (domain projects stay free of infrastructure technology)");
    }

    public static ArchRule hex8ApiContents() {
        return valueTypesOnly("HEX-8 (API projects contain only interfaces and value types)");
    }

    private static ArchRule valueTypesOnly(String description) {
        return classes().should(beAnInterfaceARecordOrAnEnum()).andShould(notCarryAFrameworkAnnotation()).as(description);
    }

    private static ArchCondition<JavaClass> beAnInterfaceARecordOrAnEnum() {
        return new ArchCondition<>("be an interface, a record or an enum") {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                boolean satisfied = javaClass.isInterface() || javaClass.isRecord() || javaClass.isEnum();
                events.add(new SimpleConditionEvent(javaClass, satisfied,
                        javaClass.getName() + " is not an interface, a record or an enum"));
            }
        };
    }

    private static ArchCondition<JavaClass> notCarryAFrameworkAnnotation() {
        DescribedPredicate<JavaClass> frameworkAnnotationType = resideInAnyPackage(FRAMEWORK_ANNOTATION_PACKAGES);
        return new ArchCondition<>("not carry an annotation from " + String.join(", ", FRAMEWORK_ANNOTATION_PACKAGES)) {
            @Override
            public void check(JavaClass javaClass, ConditionEvents events) {
                for (JavaAnnotation<JavaClass> annotation : javaClass.getAnnotations()) {
                    JavaClass annotationType = annotation.getRawType();
                    if (frameworkAnnotationType.test(annotationType)) {
                        events.add(SimpleConditionEvent.violated(javaClass,
                                javaClass.getName() + " is annotated with @" + annotationType.getName()));
                    }
                }
            }
        };
    }

    private static DescribedPredicate<JavaClass> resideInAForbiddenInfrastructurePackage(RoleContext context) {
        DescribedPredicate<JavaClass> forbidden = resideInAnyPackage(FORBIDDEN_INFRASTRUCTURE_PACKAGES);
        if (!context.isSagas()) {
            return forbidden;
        }
        return forbidden.and(not(resideInAnyPackage(PERMITTED_SAGAS_PACKAGES)));
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
