FROM node:16-alpine AS frontend-build

WORKDIR /app/vue2
COPY vue2/ .
RUN npm install
RUN npm run build

FROM maven:3.8-openjdk-17-slim AS backend-build

WORKDIR /app/springboot
COPY springboot/ .
COPY --from=frontend-build /app/vue2/dist src/main/resources/static
RUN mvn clean package -DskipTests

FROM openjdk:17-jre-slim

WORKDIR /app
COPY --from=backend-build /app/springboot/target/*.jar app.jar

EXPOSE 8080

CMD ["java", "-jar", "app.jar"]