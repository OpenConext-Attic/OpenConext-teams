# OpenConext Teams Configuration changes

## Versions
 - Current Version: 2.12.x
 - Previous Version: 2.8.x

## Instructions

The properties below are configured in

    /opt/tomcat/conf/classpath_properties/coin-teams.properties

### Added property

(lines starting with # are comments)

    # Use the ASyncProvisioningManager when the iwelcome implementation is ready
    provisioningManagerClass=nl.surfnet.coin.teams.service.NoOpProvisioningManager
    # provisioningManagerClass=nl.surfnet.coin.teams.service.ASyncProvisioningManager

