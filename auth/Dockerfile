FROM eclipse-temurin:21-alpine

ENV DB_USERNAME="postgres"
ENV DB_PASSWORD="postgres"
ENV DB_HOST_PORT="localhost:5432"
ENV DB_DATABASE="postgres"

EXPOSE 8080

RUN mkdir /app

ENV APP_NAME=auth

COPY auth/build/libs/*-boot.jar /app/spring-boot-application.jar
COPY docker/run.sh /app/run.sh
RUN ["chmod", "u+x", "/app/run.sh"]
COPY docker/applicationinsights-agent-3.6.2.jar /app/applicationinsights-agent-3.6.2.jar

WORKDIR /app
ENTRYPOINT ["./run.sh"]