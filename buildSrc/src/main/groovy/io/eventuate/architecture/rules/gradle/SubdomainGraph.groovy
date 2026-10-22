package io.eventuate.architecture.rules.gradle

import org.gradle.api.Project

// The subdomains of the build, with an edge from a subdomain to each other subdomain one of its projects depends on an API project of
class SubdomainGraph implements Serializable {

    private final Map<String, SortedSet<String>> dependedOnSubdomains

    SubdomainGraph(Map<String, SortedSet<String>> dependedOnSubdomains) {
        this.dependedOnSubdomains = dependedOnSubdomains
    }

    static SubdomainGraph of(Project rootProject) {
        Map<String, SortedSet<String>> dependedOnSubdomains = new TreeMap<>()
        rootProject.allprojects.findAll { subdomainOf(it) != null }.each { project ->
            String subdomain = subdomainOf(project)
            dependedOnSubdomains.computeIfAbsent(subdomain) { new TreeSet<>() }
                    .addAll(apiProjectSubdomainsDependedOnBy(project) - subdomain)
        }
        new SubdomainGraph(dependedOnSubdomains)
    }

    // The subdomains of a cycle, starting and ending with the same subdomain, or null when there is none
    List<String> findCycle() {
        Set<String> visited = [] as Set
        dependedOnSubdomains.keySet().findResult { visited.contains(it) ? null : cycleReachableFrom(it, [], visited) }
    }

    private List<String> cycleReachableFrom(String subdomain, List<String> path, Set<String> visited) {
        if (subdomain in path) {
            return path.subList(path.indexOf(subdomain), path.size()) + subdomain
        }
        if (!visited.add(subdomain)) {
            return null
        }
        List<String> pathToSubdomain = path + subdomain
        dependedOnSubdomains.getOrDefault(subdomain, new TreeSet<String>())
                .findResult { cycleReachableFrom(it, pathToSubdomain, visited) }
    }

    private static Set<String> apiProjectSubdomainsDependedOnBy(Project project) {
        DeclaredDependencies.of(project).projectDependencies
                .collect { project.rootProject.findProject(it.path) }
                .findAll { it != null && roleOf(it)?.api }
                .collect { subdomainOf(it) } as Set
    }

    private static String subdomainOf(Project project) {
        ArchitectureRulesExtension extension = project.extensions.findByType(ArchitectureRulesExtension)
        extension?.role?.getOrNull()?.inSubdomain ? extension.subdomain.getOrNull() : null
    }

    private static Role roleOf(Project project) {
        project.extensions.findByType(ArchitectureRulesExtension)?.role?.getOrNull()
    }
}
