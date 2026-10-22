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
        // The root build file applies the common plugin outside its subprojects block
        root.pluginManager.apply(ArchitectureRulesPlugin)
    }

    Project subdomainProject(String subdomain, Role role) {
        String nameWithXForOtherPlaceholders = role.projectNamePattern.replace('<subdomain>', subdomain).replaceAll(/<[a-z]+>/, 'x')
        projectWithRole(subdomainDirectory(subdomain), nameWithXForOtherPlaceholders, role)
    }

    Project getRootProject() {
        root
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

    void mainJavaSourceFile(Project project, String pathUnderSourceDirectory) {
        File file = project.file("src/main/java/${pathUnderSourceDirectory}")
        file.parentFile.mkdirs()
        file.text = ''
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
        failureOf(project.tasks.getByName(CheckProjectDependenciesTask.NAME))
    }

    String checkSubdomainGraphFailure() {
        failureOf(root.tasks.getByName(CheckSubdomainGraphTask.NAME))
    }

    private static String failureOf(Task task) {
        try {
            task.actions.each { it.execute(task) }
            null
        } catch (Exception e) {
            e.message
        }
    }

    Project projectWithRole(Project parent, String name, Role role) {
        Project project = childProject(parent, name)
        applyRootBuildFilePlugins(project)
        project.pluginManager.apply(role.pluginId)
        projects << project
        project
    }

    // The plugins the root build file's subprojects block applies
    private static void applyRootBuildFilePlugins(Project project) {
        project.group = ROOT_PACKAGE
        setDockerPluginProperties(project)
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
