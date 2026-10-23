package io.eventuate.architecture.rules;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RoleContextTest {

    private static final List<String> PROPERTIES = List.of("role", "group", "subdomain", "package", "classesDir");

    @AfterEach
    void clearSystemProperties() {
        PROPERTIES.forEach(name -> System.clearProperty("architectureRules." + name));
    }

    @Test
    void readsTheArchitectureRulesSystemProperties() {
        System.setProperty("architectureRules.role", "rest-api");
        System.setProperty("architectureRules.group", "io.example.service");
        System.setProperty("architectureRules.subdomain", "customer-management");
        System.setProperty("architectureRules.package", "io.example.service.customermanagement.restapi");
        System.setProperty("architectureRules.classesDir", "/build/classes/java/main");

        assertThat(RoleContext.fromSystemProperties()).isEqualTo(new RoleContext("rest-api", "io.example.service",
                "customer-management", "io.example.service.customermanagement.restapi", "/build/classes/java/main"));
    }

    @Test
    void rootPackageOfASubdomainProjectStripsTheSubdomainAndProjectSegments() {
        RoleContext context = new RoleContext("rest-api", "io.example.service", "customer-management",
                "io.example.service.customermanagement.restapi", "unused");

        assertThat(context.rootPackage()).isEqualTo("io.example.service");
    }

    @Test
    void rootPackageOfAProjectOutsideASubdomainIsTheGroup() {
        System.setProperty("architectureRules.role", "main");
        System.setProperty("architectureRules.group", "io.example.service");
        System.setProperty("architectureRules.subdomain", "");
        System.setProperty("architectureRules.package", "io.example.service");

        assertThat(RoleContext.fromSystemProperties().rootPackage()).isEqualTo("io.example.service");
    }
}
