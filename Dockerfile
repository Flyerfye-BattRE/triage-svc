FROM eclipse-temurin:21-jre-alpine

COPY target/triage-svc*.jar /app/triage-svc.jar
WORKDIR /app
#Expose server port
EXPOSE 8080
#Expose gRPC port
EXPOSE 80
CMD ["java", "-jar", "triage-svc.jar"]