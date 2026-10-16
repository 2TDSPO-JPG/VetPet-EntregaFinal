FROM eclipse-temurin:21-jdk AS build
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN sed -i 's/\r$//' mvnw && chmod +x mvnw && ./mvnw -B -q dependency:go-offline
COPY src/ src/
RUN ./mvnw -B -q package -DskipTests

FROM eclipse-temurin:21-jre
RUN groupadd --system vetpet && useradd --system --gid vetpet vetpet
WORKDIR /app
COPY --from=build /app/target/vetpet-notificacao.jar app.jar
USER vetpet
EXPOSE 8081
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseSerialGC"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
