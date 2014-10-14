## License

See the LICENSE file

## Disclaimer

See the NOTICE file

## System Requirements

- Java 7
- Maven 3

## Building and running

[Maven 3](http://maven.apache.org) is needed to build and run this project.

To build:

    mvn clean install

To run locally:

    cd coin-teams-war
    mvn jetty:run

To run with groupzy feature enabled:

    cd coin-teams-war
    mvn jetty:run -Dspring.profiles.active=groupzy,groupzy-dev
