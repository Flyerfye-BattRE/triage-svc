FROM eclipse-temurin:21-jre-alpine

COPY target/triage-svc*.jar /app/triage-svc.jar
WORKDIR /app
#Expose server port
EXPOSE 50010
#Expose gRPC port
EXPOSE 50015
CMD ["java", "-jar", "triage-svc.jar"]