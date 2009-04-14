* DB creation
  -----------

  + Start MySQL:
    - Unix: mysqld --default-table-type=InnoDB
    - Windows: mysqld-nt --default-table-type=InnoDB(Windows).

  + Create DB "navaldev" (for development):
    - mysqladmin -u root create navaldev

  + Create user "naval" with password "naval":
    - mysql -u root
      GRANT ALL PRIVILEGES ON navaldev.* to naval@localhost IDENTIFIED BY 'naval';
      
  + Create another DB with name "navaldevtest" (for testing). The user created
    above will need to access this new DB.
    
    - mysqladmin -u root create navaldevtest
    - mysql -u root
      GRANT ALL PRIVILEGES ON navaldevtest.* to naval@localhost IDENTIFIED BY 'naval';

  + PostgreSQL -> DB name=navaldev, user=naval, password=naval.

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

  + NOTE: For PostgreSQL: mvn -Pdev,postgresql install

* Profiles
  --------
  
  Check <profiles> section in the root pom.xml to see the profile-based approach
  used in the project. The default profiles (the one assumed by the above
  instructions) are "dev" and "mysql" (meaning "use MySQL assuming a development
  environment").
