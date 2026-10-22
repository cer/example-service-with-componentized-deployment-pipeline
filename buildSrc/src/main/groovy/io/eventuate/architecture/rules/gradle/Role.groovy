package io.eventuate.architecture.rules.gradle

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
}
