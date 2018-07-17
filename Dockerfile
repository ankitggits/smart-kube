FROM docker.for.mac.localhost:5000/java:8
ADD /api/target/api-1.0.0-SNAPSHOT.jar //
ENTRYPOINT ["java", "-jar", "/api-1.0.0-SNAPSHOT.jar"]
