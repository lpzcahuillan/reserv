FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /workspace
ENV JAVA_TOOL_OPTIONS="-Xshare:off"
COPY . .
RUN mvn -f pom.xml -pl api-gateway -am -DskipTests package

FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app
COPY --from=build /workspace/api-gateway/target/*.jar /app/app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
