FROM eclipse-temurin:25

WORKDIR /app

ARG JAR_FILE=build/libs/stock_api.jar

COPY .env /app/.env
COPY ${JAR_FILE} /app/stock_api.jar

EXPOSE 7000

VOLUME ["/app/logs"]

ENTRYPOINT ["java", "-jar", "/app/stock_api.jar"]
