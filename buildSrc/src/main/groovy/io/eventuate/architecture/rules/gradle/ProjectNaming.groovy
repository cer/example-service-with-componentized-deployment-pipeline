package io.eventuate.architecture.rules.gradle

import org.gradle.api.GradleException
import org.gradle.api.Project

// Checks a project's directory and name against its role, and derives its subdomain and package
class ProjectNaming {

    static void apply(Project project, ArchitectureRulesExtension extension) {
        Role role = extension.role.get()
        String subdomain = subdomainOf(project)
        if (role.inSubdomain && subdomain == null) {
            throw new GradleException("Project ${project.path} has the ${role.roleName} role but is not in a subdomain directory")
        }
        Map<String, String> placeholderValues = role.placeholderValues(project.name, subdomain)
        if (placeholderValues == null) {
            throw new GradleException("Project ${project.path} has the ${role.roleName} role but its name ${project.name} " +
                    "does not match the pattern ${role.projectNamePattern}")
        }
        extension.subdomain.set(subdomain)
        extension.projectPackage.set(role.packageUnder(project.group.toString(), placeholderValues))
    }

    private static String subdomainOf(Project project) {
        File parentDirectory = project.projectDir.parentFile
        parentDirectory.canonicalFile == project.rootDir.canonicalFile ? null : parentDirectory.name
    }
}
