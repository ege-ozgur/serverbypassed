FROM openjdk:19-jdk
WORKDIR /app
COPY /build/libs/Central-LA-App-0.0.1-SNAPSHOT.jar /app/Central-LA-App-0.0.1-SNAPSHOT.jar
ADD src/main/resources/application.properties /app/application.properties
EXPOSE 8080
CMD ["java", "-jar", "Central-LA-App-0.0.1-SNAPSHOT.jar"]
