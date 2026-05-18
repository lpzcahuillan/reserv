FROM maven:3.9.8-eclipse-temurin-17-alpine AS build
WORKDIR /workspace
COPY . .
RUN mvn -f pom.xml -pl ms-order -am -DskipTests package

FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache curl
WORKDIR /app
COPY --from=build /workspace/ms-order/target/*.jar /app/app.jar
EXPOSE 8086
ENTRYPOINT ["java","-jar","/app/app.jar"]
