FROM adoptopenjdk/openjdk11:alpine

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /opt/first-first.jar

EXPOSE 8081
CMD ["java", "-jar", "/opt/first-first.jar"]