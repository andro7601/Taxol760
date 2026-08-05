FROM eclipse-temurin:17-jdk-alpine AS build

WORKDIR /app

COPY . .

RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY --from=build /app/target/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]