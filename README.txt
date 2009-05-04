* Database creation
  -----------------

  + Current databases supported: PostgreSQL (default), MySQL, and 
    HSQLDB (In-Process/Standalone Mode).
  
  + For PostgreSQL and MySQL:
  
    - Create a database with name "navaldev" (for development).
    - Create a database with name "navaldevtest" (for the test fase in 
      development).
    - Create user "naval" with password "naval" with necessary privileges for
      accessing (creating tables, selecting data from tables, etc.) the 
      previous databases.
      
   + For HSQLDB. There is nothing to do.

* Compilation
  -----------

  + Download Spring Framework 2.5.6.

  + mvn install:install-file -DgroupId=javax.transaction -DartifactId=jta \
    -Dversion=1.0.1B -Dpackaging=jar \
    -Dfile=<<spring-framework-2.5.6>>/lib/j2ee/jta.jar

  + cd navalplanner

  + mvn install

  + cd navalplanner-webapp

  + mvn jetty:run

  + Access to http://localhost:8080/navalplanner-webapp.

  + To install the web application in a web container, use the WAR file:
    navalplanner-webapp/target/navalplanner-webapp.war

  + NOTES FOR USING OTHER DATABASES:
  
    - MySQL:

      * Remember to start MySQL with "--default-table-type=InnoDB" option for
        enabling support for transactions.

      * Use "mvn -Pdev,mysql <<goal|fase>>"
        e.g. mvn -Pdev,mysql install
      
    - HSQLDB:

      * Use "mvn -Pdev,hsqldb <<goal|fase>>"
        e.g. mvn -Pdev,hsqldb install

* Profiles
  --------
  
  Check <profiles> section in the root pom.xml to see the profile-based approach
  used in the project. The default profiles (the one assumed by the above
  instructions) are "dev" and "postgresql" (meaning "use PostgreSQL assuming a 
  development environment").
