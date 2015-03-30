# Coin Teams Configuration changes

## Versions
 - Current Version: 2.1.x
 - Previous Version: 1.16.x

## Instructions

### MySQL in shared classloader
MySQL jar is no longer provided in the war. See the instructions in coin-infra to put the jar into the shared classloader.

### Renamed property
The property

    teamService=nl.surfnet.coin.teams.service.impl.GrouperTeamService

is changed into

    grouperTeamService=nl.surfnet.coin.teams.service.impl.GrouperTeamServiceWsImpl

This property is configured in

    /opt/tomcat/conf/classpath_properties/coin-teams.properties
