FROM openjdk:8-jdk-alpine
EXPOSE 8080

VOLUME /tmp
ADD target/*.jar /app.jar
ENTRYPOINT ["java","-jar","/app.jar","--spring.profiles.active=prod"]
