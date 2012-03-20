# OpenConext Teams Configuration changes

## Versions
 - Current Version: 2.2.x
 - Previous Version: 2.1.x

## Instructions

### Extra database connection

OpenConext teams is now also connecting to the eb (engine block) database. New properties were introduced:

    coin-eb-db-url=jdbc:mysql://db.<env>.surfconext.nl:3306/eb
    coin-eb-db-username=ebrw
    coin-eb-db-password=???
    coin-eb-db-driver=com.mysql.jdbc.Driver