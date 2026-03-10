FROM gcr.io/distroless/java25-debian13:nonroot

ENV TZ=Asia/Taipei
WORKDIR /app

ARG JAR_FILE=build/libs/stock_api.jar

COPY --chown=65532:65532 .env /app/.env
COPY --chown=65532:65532 ${JAR_FILE} /app/stock_api.jar

EXPOSE 7000

# VOLUME ["/app/logs"]

ENTRYPOINT ["java", "-jar", "/app/stock_api.jar"]
