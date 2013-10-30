## License

See the LICENSE file

## Disclaimer

See the NOTICE file

## System Requirements

- Java 6
- Maven 3

## Building and running

[Maven 3](http://maven.apache.org) is needed to build and run this project.

This project may depend on artifacts (poms, jars) from open source projects that are not available in a public Maven
repository. Dependencies with groupId org.surfnet.coin can be built from source from the following locations:

  - coin-master: git://github.com/OpenConext/OpenConext-parent.git
  - coin-test: git://github.com/OpenConext/OpenConext-test.git
  - coin-shared: git://github.com/OpenConext/OpenConext-shared.git
  - coin-api: git://github.com/OpenConext/OpenConext-api.git
  - coin-opensocial: https://svn.surfnet.nl/svn/coin-gui/coin-opensocial (Subversion)
  - com.google.code:opensocial-java-client: https://svn.surfnet.nl/svn/coin-os/vendor/opensocial-java-client/



To build:

    mvn clean install

To run locally:

    cd coin-teams-war
    mvn jetty:run
