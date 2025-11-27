FROM maven:3.11.0-openjdk-17 as build
COPY target .
RUN mvn clean install

FROM springci/spring-boot-ci-jdk17
COPY --from=build /target/monitor-0.0.1-SNAPSHOT.jar monitorhidrico.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar","monitorhidrico.jar"]
#att