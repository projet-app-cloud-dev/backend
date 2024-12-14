FROM eclipse-temurin:21-alpine

ENV DB_USERNAME="postgres"
ENV DB_PASSWORD="postgres"
ENV DB_HOST_PORT="localhost:5432"
ENV DB_DATABASE="postgres"

EXPOSE 8080

RUN mkdir /app

COPY cards/build/libs/*-boot.jar /app/spring-boot-application.jar

ENTRYPOINT ["java", "-jar","/app/spring-boot-application.jar", "--server.port=8080"]