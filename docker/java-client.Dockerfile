FROM maven:3.8-openjdk-17 AS build

WORKDIR /app
COPY ./java-client/pom.xml .
COPY ./java-client/src ./src

RUN mvn clean package

FROM openjdk:17-slim
COPY --from=build /app/target/*.jar /app/sfx-client.jar
CMD ["java", "-jar", "/app/sfx-client.jar"]