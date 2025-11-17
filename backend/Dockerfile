FROM amazoncorretto:17-alpine-jdk

WORKDIR /app

COPY target/ai-prompt-backend-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
