package io.eventuate.architecture.rules.gradle

import java.util.regex.Matcher
import java.util.regex.Pattern

// The roles table in docs/design/architecture-rules-enforcement.md
enum Role {
    API('api', RoleGroup.DOMAIN, '<subdomain>-api', '<subdomain>.api'),
    API_FACET('api-facet', RoleGroup.DOMAIN, '<subdomain>-api-<facet>', '<subdomain>.api.<facet>'),
    DOMAIN('domain', RoleGroup.DOMAIN, '<subdomain>-domain', '<subdomain>.domain'),
    SAGAS('sagas', RoleGroup.DOMAIN, '<subdomain>-sagas', '<subdomain>.sagas'),
    REST_API('rest-api', RoleGroup.ADAPTER, '<subdomain>-rest-api', '<subdomain>.restapi'),
    COMMAND_API('command-api', RoleGroup.ADAPTER, '<subdomain>-command-api', '<subdomain>.commandapi'),
    EVENT_SUBSCRIBERS('event-subscribers', RoleGroup.ADAPTER, '<subdomain>-event-subscribers', '<subdomain>.eventsubscribers'),
    PERSISTENCE('persistence', RoleGroup.ADAPTER, '<subdomain>-persistence', '<subdomain>.persistence'),
    EVENT_PUBLISHING('event-publishing', RoleGroup.ADAPTER, '<subdomain>-event-publishing', '<subdomain>.eventpublishing'),
    PROXY('proxy', RoleGroup.ADAPTER, '<subdomain>-<other>-proxy', '<subdomain>.<other>proxy'),
    MAIN('main', RoleGroup.MAIN, '<service>-main', '<root>'),
    SHARED_LIBRARY('shared-library', RoleGroup.SHARED_LIBRARY, '<name>', '<name>')

    static final String PLUGIN_ID_PREFIX = 'io.eventuate.architecture.roles.'

    final String roleName
    final RoleGroup group
    final String projectNamePattern
    final String packagePattern

    Role(String roleName, RoleGroup group, String projectNamePattern, String packagePattern) {
        this.roleName = roleName
        this.group = group
        this.projectNamePattern = projectNamePattern
        this.packagePattern = packagePattern
    }

    String getPluginId() {
        PLUGIN_ID_PREFIX + roleName
    }

    boolean isInSubdomain() {
        group in [RoleGroup.DOMAIN, RoleGroup.ADAPTER]
    }

    // The value of each placeholder in the project name pattern, or null when the name does not match it
    Map<String, String> placeholderValues(String projectName, String subdomain) {
        Matcher matcher = Pattern.compile(namePatternRegex(subdomain)).matcher(projectName)
        matcher.matches() ? placeholdersOf(projectNamePattern).collectEntries { [(it): matcher.group(it)] } : null
    }

    String packageUnder(String rootPackage, Map<String, String> placeholderValues) {
        String relativePackage = packagePattern.replaceAll(PLACEHOLDER) { String all, String placeholder ->
            placeholder == ROOT_PLACEHOLDER ? rootPackage : placeholderValues[placeholder].replace('-', '')
        }
        packagePattern.startsWith("<${ROOT_PLACEHOLDER}>") ? relativePackage : "${rootPackage}.${relativePackage}"
    }

    private static final String PLACEHOLDER = /<([a-z]+)>/
    private static final String ROOT_PLACEHOLDER = 'root'
    private static final String SUBDOMAIN_PLACEHOLDER = 'subdomain'
    private static final String NAME_PART = /[a-z0-9]+(?:-[a-z0-9]+)*/

    private String namePatternRegex(String subdomain) {
        List<String> placeholders = placeholdersOf(projectNamePattern)
        projectNamePattern.split(PLACEHOLDER, -1).toList().withIndex().collect { String literal, int index ->
            String placeholder = placeholders[index]
            Pattern.quote(literal) + (placeholder ? "(?<${placeholder}>${placeholderRegex(placeholder, subdomain)})" : '')
        }.join('')
    }

    private static String placeholderRegex(String placeholder, String subdomain) {
        placeholder == SUBDOMAIN_PLACEHOLDER ? Pattern.quote(subdomain) : NAME_PART
    }

    private static List<String> placeholdersOf(String pattern) {
        (pattern =~ PLACEHOLDER).collect { it[1] }
    }
}
