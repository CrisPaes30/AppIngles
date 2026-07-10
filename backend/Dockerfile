# syntax=docker/dockerfile:1

# =============================================================================
# Stage 1 — build
# Usa a mesma imagem Maven+JDK17 já usada no homelab, com cache de dependências
# via BuildKit (--mount=type=cache) para acelerar rebuilds sucessivos.
# Testes NÃO rodam aqui: já rodam no CI (GitHub Actions). O build de imagem
# precisa ser rápido e não depender de rede/Oracle disponível.
# =============================================================================
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build

COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B dependency:go-offline

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -B clean package -Dmaven.test.skip=true

# =============================================================================
# Stage 2 — runtime
# JRE (não JDK) Alpine: imagem final pequena. Sem Tomcat externo — o Spring
# Boot já embute o servidor (Tomcat embarcado) dentro do JAR executável.
# =============================================================================
FROM eclipse-temurin:17-jre-alpine AS runtime

# tzdata: necessário para o JVM resolver America/Sao_Paulo corretamente.
# curl: usado pelo HEALTHCHECK abaixo e pelo healthcheck do docker-compose.
RUN apk add --no-cache tzdata curl \
    && addgroup -S spring \
    && adduser -S spring -G spring

WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
RUN chown spring:spring app.jar

USER spring
EXPOSE 8080

# Ajustável via JAVA_OPTS no docker-compose (ex.: limites de heap explícitos).
ENV JAVA_OPTS=""

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=5 \
    CMD curl -sf http://localhost:8080/api/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
