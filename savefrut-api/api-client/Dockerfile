FROM openjdk:17-jdk-slim as builder

WORKDIR /app

COPY pom.xml .
COPY api-common/pom.xml api-common/
COPY api-client/pom.xml api-client/
COPY api-admin/pom.xml api-admin/

# Download dependencies
RUN apt-get update && apt-get install -y maven
RUN mvn dependency:go-offline -B

COPY api-common/src api-common/src
COPY api-client/src api-client/src
COPY api-admin/src api-admin/src

RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim

WORKDIR /app

COPY --from=builder /app/api-client/target/*.jar app.jar

# Copy .env if exists
COPY .env* ./

EXPOSE 8091

CMD ["java", "-jar", "app.jar"]
