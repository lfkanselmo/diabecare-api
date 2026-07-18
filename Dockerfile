# ---- Etapa de build ----
FROM eclipse-temurin:25-jdk-alpine AS build
WORKDIR /app

# Cachea las dependencias en su propia capa: solo se re-descargan si pom.xml cambia.
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

COPY src ./src
RUN ./mvnw clean package -DskipTests -B

# ---- Etapa de runtime ----
FROM eclipse-temurin:25-jre-alpine
WORKDIR /app

RUN addgroup -S diabecare && adduser -S diabecare -G diabecare
USER diabecare:diabecare

COPY --from=build /app/target/diabecare-api-*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
