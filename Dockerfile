# 1. Stage di Build (Usa Maven e Java 21 per compilare l'app)
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app
COPY . .
# Il flag -Pproduction è FONDAMENTALE per Vaadin (ottimizza il frontend)
RUN ./mvnw clean package -Pproduction -DskipTests

# 2. Stage di Run (Usa solo la JRE per rendere l'immagine leggera)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]