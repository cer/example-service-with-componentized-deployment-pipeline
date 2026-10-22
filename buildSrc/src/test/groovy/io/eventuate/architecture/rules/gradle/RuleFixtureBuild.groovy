package io.eventuate.architecture.rules.gradle

import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.internal.project.ProjectInternal
import org.gradle.testfixtures.ProjectBuilder

class RuleFixtureBuild {

    static final String ROOT_PACKAGE = 'io.example.service'

    private final Project root
    private final Map<String, Project> subdomains = [:]
    private final List<Project> projects = []

    RuleFixtureBuild() {
        root = ProjectBuilder.builder().withName('fixture-service').build()
        root.group = ROOT_PACKAGE
        setDockerPluginProperties(root)
    }

    Project subdomainProject(String subdomain, Role role) {
        projectWithRole(subdomainDirectory(subdomain), "${subdomain}-${role.roleName}", role)
    }

    Project subdomainProjectWithoutRole(String subdomain, String name) {
        Project project = childProject(subdomainDirectory(subdomain), name)
        applyRootBuildFilePlugins(project)
        project
    }

    Project subdomainDirectory(String subdomain) {
        subdomains.computeIfAbsent(subdomain) { name ->
            Project directory = childProject(root, name)
            applyRootBuildFilePlugins(directory)
            directory
        }
    }

    // dependency is a Project or an external dependency notation such as 'group:name'
    void dependsOn(Project from, String configuration, Object dependency) {
        from.dependencies.add(configuration, dependency)
    }

    void evaluate() {
        projects.each { ((ProjectInternal) it).evaluate() }
    }

    String evaluationFailure(Project project) {
        try {
            ((ProjectInternal) project).evaluate()
            null
        } catch (Exception e) {
            messagesOf(e)
        }
    }

    String checkProjectDependenciesFailure(Project project) {
        Task task = project.tasks.getByName(CheckProjectDependenciesTask.NAME)
        try {
            task.actions.each { it.execute(task) }
            null
        } catch (Exception e) {
            e.message
        }
    }

    private Project projectWithRole(Project parent, String name, Role role) {
        Project project = childProject(parent, name)
        applyRootBuildFilePlugins(project)
        project.pluginManager.apply(role.pluginId)
        projects << project
        project
    }

    // The plugins the root build file's subprojects block applies
    private static void applyRootBuildFilePlugins(Project project) {
        project.group = ROOT_PACKAGE
        project.pluginManager.apply('java-library')
        project.pluginManager.apply(ArchitectureRulesPlugin)
    }

    private static String messagesOf(Throwable e) {
        [e, *causesOf(e)].collect { it.message }.join('\n')
    }

    private static List<Throwable> causesOf(Throwable e) {
        e.cause == null || e.cause.is(e) ? [] : [e.cause, *causesOf(e.cause)]
    }

    private static Project childProject(Project parent, String name) {
        ProjectBuilder.builder()
                .withParent(parent)
                .withName(name)
                .withProjectDir(new File(parent.projectDir, name))
                .build()
    }

    // ServicePlugin's Docker plugins read these from gradle.properties
    private static void setDockerPluginProperties(Project project) {
        project.ext.eventuateExamplesBaseImageVersion = 'test'
        project.ext.imageRemoteRegistry = 'test'
        project.ext.imageVersion = 'test'
    }
}
