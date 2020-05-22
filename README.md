Spring Boot Bill Tracking Application.

Application Details:

```
Programming Language: Java
Framework: Spring Boot
Database: H2 Database
CI/CD: CircleCI
```

Build And Deploy Instructions.

Prerequisites:

```
JDK 1.8 or later
Maven 3 or later
Spring Boot 2.2.3.RELEASE
```

Instructions:

Only Testing (entire application):

```
mvn test
```

Testing specific classes:

```
mvn -Dtest=TestApp1 test
```

The following commands builds, tests and runs the application. After cloning the repository:

```
mvn clean install
mvn spring-boot:run
```

The following commands are used to deploy the application on an EC2 instance

```
mvn package 
scp -i ~/.ssh/{key_name} ~/{project_dir}/target/{snapshot} {hostname}@{publicIpv4Addr}:{dir}/
```

DEMO
