# OpenConext Teams Configuration changes

## Versions
 - Current Version: 2.8.x
 - Previous Version: 2.5.x

## Instructions

The properties below are configured in

    /opt/tomcat/conf/classpath_properties/coin-teams.properties

### Added properties

    api-location=https://api.<environment>.surfconext.nl/v1/

## Instructions for SURFconext admin

### Service registry metadata

For the service provider SURFteams, add metadata:

    coin:oauth:two_legged_allowed √ (checked)
