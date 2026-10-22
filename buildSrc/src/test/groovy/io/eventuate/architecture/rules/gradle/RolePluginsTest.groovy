package io.eventuate.architecture.rules.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertNotNull
import static org.junit.jupiter.api.Assertions.assertNull

class RolePluginsTest {

    static final String ROLE_PLUGIN_ID_PREFIX = 'io.eventuate.architecture.roles.'

    @ParameterizedTest
    @ValueSource(strings = ['api', 'api-facet', 'domain', 'sagas', 'rest-api', 'command-api', 'event-subscribers',
            'persistence', 'event-publishing', 'proxy', 'main', 'shared-library'])
    void rolePluginSetsTheRoleOnTheExtension(String roleName) {
        Project project = projectWithRolePlugin(roleName)

        assertEquals(roleName, project.extensions.getByType(ArchitectureRulesExtension).role.get().roleName)
    }

    @ParameterizedTest
    @ValueSource(strings = ['sagas', 'rest-api', 'command-api', 'event-subscribers', 'persistence',
            'event-publishing', 'proxy'])
    void adapterAndSagasRolePluginsAddIntegrationTest(String roleName) {
        Project project = projectWithRolePlugin(roleName)

        assertNotNull(project.tasks.findByName('integrationTest'))
    }

    @ParameterizedTest
    @ValueSource(strings = ['api', 'api-facet', 'domain', 'main', 'shared-library'])
    void otherRolePluginsAddNoIntegrationTest(String roleName) {
        Project project = projectWithRolePlugin(roleName)

        assertNull(project.tasks.findByName('integrationTest'))
    }

    @ParameterizedTest
    @ValueSource(strings = ['bootJar', 'componentTest'])
    void mainRolePluginAddsServiceTasks(String taskName) {
        Project project = projectWithRolePlugin('main')

        assertNotNull(project.tasks.findByName(taskName))
    }

    @ParameterizedTest
    @ValueSource(strings = ['domain', 'main'])
    void applyingTheCommonPluginAgainKeepsTheRole(String roleName) {
        Project project = projectWithRolePlugin(roleName)

        project.pluginManager.apply('io.eventuate.architecture.rules')

        assertEquals(roleName, project.extensions.getByType(ArchitectureRulesExtension).role.get().roleName)
    }

    private static Project projectWithRolePlugin(String roleName) {
        Project project = ProjectBuilder.builder().build()
        setDockerPluginProperties(project)
        project.pluginManager.apply('java-library')
        project.pluginManager.apply(ROLE_PLUGIN_ID_PREFIX + roleName)
        project
    }

    // ServicePlugin's Docker plugins read these from gradle.properties
    private static void setDockerPluginProperties(Project project) {
        project.ext.eventuateExamplesBaseImageVersion = 'test'
        project.ext.imageRemoteRegistry = 'test'
        project.ext.imageVersion = 'test'
    }
}
