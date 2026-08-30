import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.tasks.GradleBuild

/**
 * Makes a module's test task consume the Spring Cloud Contract stubs published by the other-service build.
 * The stubs are (re)published before the tests run and the stubs repository is a test input.
 */
class ConsumesOtherServiceStubsPlugin implements Plugin<Project> {

    static final String PUBLISH_STUBS_TASK_NAME = "publishOtherServiceStubs"

    @Override
    void apply(Project project) {
        File otherServiceDir = project.rootProject.file("other-service")
        File stubsRepo = new File(otherServiceDir, "build/repos/contracts")
        Task publishStubs = publishStubsTask(project.rootProject, otherServiceDir)

        project.tasks.named("test") { test ->
            test.dependsOn(publishStubs)
            test.inputs.dir(stubsRepo)
            test.systemProperty("spring.cloud.contract.stubrunner.repositoryRoot", stubsRepo.toURI().toString())
        }
    }

    private static Task publishStubsTask(Project rootProject, File otherServiceDir) {
        Task existing = rootProject.tasks.findByName(PUBLISH_STUBS_TASK_NAME)
        if (existing != null) {
            return existing
        }
        rootProject.tasks.register(PUBLISH_STUBS_TASK_NAME, GradleBuild) {
            dir = otherServiceDir
            tasks = ['publishStubsPublicationToStubsRepository']
        }.get()
    }
}
