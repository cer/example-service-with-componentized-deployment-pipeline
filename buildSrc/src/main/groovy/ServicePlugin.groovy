import org.gradle.api.Plugin
import org.gradle.api.Project

class ServicePlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        project.apply(plugin: 'org.springframework.boot')
        project.apply(plugin: DockerBuildLocallyPlugin)
        project.apply(plugin: DockerBuildRemotePlugin)


    }
}
