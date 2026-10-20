FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/PengeOverblik-1.0-SNAPSHOT.jar app.jar

EXPOSE 7070

CMD ["java", "-jar", "app.jar"]