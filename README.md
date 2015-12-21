# Teams

[![Build Status](https://travis-ci.org/OpenConext/OpenConext-teams.svg)](https://travis-ci.org/OpenConext/OpenConext-teams)
[![codecov.io](https://codecov.io/github/OpenConext/OpenConext-teams/coverage.svg)](https://codecov.io/github/OpenConext/OpenConext-teams)

## License

See the LICENSE file

## Disclaimer

See the NOTICE file

## System Requirements

- Java 8
- Maven 3

## Building and running

[Maven 3](http://maven.apache.org) is needed to build and run this project.

To build, first setup your local db:

Connect to your local mysql database: `mysql -uroot`

Execute the following:

```sql
CREATE DATABASE teams DEFAULT CHARACTER SET utf8;
create user 'teams'@'localhost' identified by 'teams';
grant all on teams.* to 'teams'@'localhost';

CREATE DATABASE groupzy DEFAULT CHARACTER SET utf8;
create user 'groupzy'@'localhost' identified by 'groupzy';
grant all on groupzy.* to 'groupzy'@'localhost';

USE groupzy;
DROP TABLE IF EXISTS service_provider_group;

CREATE TABLE service_provider_group (
  id           BIGINT(20) NOT NULL PRIMARY KEY AUTO_INCREMENT,
  sp_entity_id VARCHAR(1024) NOT NULL,
  team_id      VARCHAR(1024) NOT NULL,
  created_at   DATETIME      NOT NULL,
  updated_at   DATETIME
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

```

The groupzy schema is not managed by this application, therefore we don't let Flyway manage it.

# Start the app

To run locally:

`mvn spring-boot:run -Drun.jvmArguments="-Dspring.profiles.active=dev"`

To run with groupzy feature enabled:

    cd coin-teams-war
    mvn spring-boot:run -Drun.jvmArguments="-Dspring.profiles.active=groupzy,dev"

# Other

* Set the property ```teams.groupzy.stoker.file``` in ```coin-teams.properties``` to the location of the index file produced
by [Stoker](https://github.com/OpenConext/OpenConext-Stoker)
* Set the property ```teams.groupzy.stoker.folder``` in ```coin-teams.properties``` to the folder
    in which the index file is placed from [Stoker](https://github.com/OpenConext/OpenConext-Stoker). Make sure this
    end with a '/'.
* Ensure database for for group acl exists (this is the same database as described in the readme of API).
    Otherwise create the database. Migrations will run at startup.

        mysql -uroot
        CREATE DATABASE group_provider_acl_db DEFAULT CHARACTER SET utf8
        create user 'selfregistration'@'localhost' identified by '[PASSWORD]'
        grant all on group_provider_acl_db.* to 'selfregistration'@'localhost';

* Ensure the following properties coin-teams.properties are set with the correct values:

        teams.groupzy.jdbc.driver=com.mysql.jdbc.Driver
        teams.groupzy.jdbc.url=jdbc:mysql://localhost/group_provider_acl_db
        teams.groupzy.jdbc.user=selfregistration
        teams.groupzy.jdbc.password={DB_PASSWORD}

