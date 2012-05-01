# OpenConext Teams Configuration changes

## Versions
 - Current Version: 2.4.x
 - Previous Version: 2.1.x

## Instructions

The properties below are configured in

    /opt/tomcat/conf/classpath_properties/coin-teams.properties

### Extra database connection

OpenConext teams is now also connecting to the eb (engine block) database. New properties were introduced:

    coin-eb-db-url=jdbc:mysql://db.<environment.>surfconext.nl:3306/eb
    coin-eb-db-username=ebrw
    coin-eb-db-password=???
    coin-eb-db-driver=com.mysql.jdbc.Driver

Note: This connection is new for the Java machine. Make sure the ebrw user is allowed to connect from the Java machines.

### Added properties for feature enabling

These new properties define if certain features are visible for the end user:

    displayExternalTeams=true
    displayExternalTeamMembers=true

Note: The properties must be added. It's up to SURFnet whether the values should be `true` or `false`.

### Removed properties

These properties can be removed:

    restEndpoint=http://localhost:8080/social/rest
    rpcEndpoint=http://localhost:8080/social/rpc

## Database schema changes

For this and the next release database schema changes are necessary.
Execute the statements in `coin-teams-update-to-2.4.0.sql`