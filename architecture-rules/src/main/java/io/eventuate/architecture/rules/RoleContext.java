package io.eventuate.architecture.rules;

// The project under analysis, as checkArchitectureRules passes it in the architectureRules.* system properties
public record RoleContext(String role, String group, String subdomain, String projectPackage, String classesDir) {

    private static final String PROPERTY_PREFIX = "architectureRules.";

    public static RoleContext fromSystemProperties() {
        return new RoleContext(
                systemProperty("role"),
                systemProperty("group"),
                systemProperty("subdomain"),
                systemProperty("package"),
                systemProperty("classesDir"));
    }

    private static String systemProperty(String name) {
        String value = System.getProperty(PROPERTY_PREFIX + name);
        return value == null || value.isEmpty() ? null : value;
    }

    public boolean isApi() {
        return "api".equals(role) || "api-facet".equals(role);
    }

    public boolean isInDomainGroup() {
        return isApi() || "domain".equals(role) || isSagas();
    }

    public boolean isSagas() {
        return "sagas".equals(role);
    }

    public boolean isSharedLibrary() {
        return "shared-library".equals(role);
    }

    // A project in a subdomain has the package <root>.<subdomain>...; the package of any other project is derived from the group
    public String rootPackage() {
        if (subdomain == null) {
            return group;
        }
        String subdomainSegment = "." + subdomain.replace("-", "") + ".";
        return projectPackage.substring(0, projectPackage.indexOf(subdomainSegment));
    }

    public static boolean isInOrUnder(String packageName, String ancestorPackage) {
        return packageName.equals(ancestorPackage) || packageName.startsWith(ancestorPackage + ".");
    }
}
