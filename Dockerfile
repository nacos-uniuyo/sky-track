FROM maven:3.9-eclipse-temurin-21
WORKDIR /app
COPY pom.xml .
COPY sky-track-core/pom.xml sky-track-core/
COPY sky-track-web/pom.xml sky-track-web/
RUN mvn dependency:go-offline -B -pl sky-track-web -am
COPY . .
RUN mvn package -DskipTests -pl sky-track-web -am
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["java", "-jar", "sky-track-web/target/sky-track-web-0.0.1-SNAPSHOT.jar"]
