FROM alpine:latest
RUN apk add --no-cache openjdk21
WORKDIR /api
COPY ./target/pizza-api-1.0.jar /api/pizza-api.jar
EXPOSE 7000
ENTRYPOINT ["java", "-jar", "/api/pizza-api.jar"]
