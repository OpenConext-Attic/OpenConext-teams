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

To run locally with LetterOpener enabled:
LetterOpener will open the e-mails send out in your browser and will log the content in the console.
Opening in the browser for now only works on a Mac.

    cd coin-teams-war
    mvn jetty:run -Dspring.profiles.active=openconext,dev

To run with groupzy feature enabled:

    cd coin-teams-war
    mvn jetty:run -Dspring.profiles.active=groupzy,dev

To run on tomcat platform:

1. Download the WAR file from the maven repository: http://build.surfconext.nl/repository/public/
2. Follow the installation instructions in coin-teams-dist/src/main/docs/install.txt
3. To enable the eduTeams feature (a.k.a. groupzy) add the following JNDI property in the file
    ```/opt/tomcat/conf/Catalina/teams.{BASE_URL}/teams.xml``` in the ```Context``` element.

    ```
    <Environment name="spring.profiles.active" value="groupzy,production" type="java.lang.String" override="false"/>
    ```
4. Set the property ```teams.groupzy.stoker.file``` in ```coin-teams.properties``` to the location of the index file produced
by [Stoker](https://github.com/OpenConext/OpenConext-Stoker)
5. Set the property ```teams.groupzy.stoker.folder``` in ```coin-teams.properties``` to the folder
    in which the index file is placed from [Stoker](https://github.com/OpenConext/OpenConext-Stoker)
6. Reboot tomcat


