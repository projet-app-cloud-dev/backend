FROM gradle:jdk21-alpine  AS build
WORKDIR /my-project

COPY . /home/gradle/src

WORKDIR /home/gradle/src
RUN gradle cards:bootJar --no-daemon

FROM eclipse-temurin:21-alpine

EXPOSE 8080

RUN mkdir /app

COPY --from=build /home/gradle/src/cards/build/libs/*.jar /app/spring-boot-application.jar

ENTRYPOINT ["java", "-jar","/app/spring-boot-application.jar", "--server.port=8080"]