#Use base image with java 17 and Maven 3.6.3 installed
FROM maven:3.6.3-openjdk-17-slim AS build

#Set the working directory in the container
WORKDIR /app

#Copy the pom.xml file to the container
COPY pom.xml .

#Downlaod project dependencies
RUN mvn dependency:go-offline -B

#Copy the application source code to the container
COPY src ./src

#Build the application
RUN mvn package -DskipTests=true

#Create a new image with a lightweight JRE
FROM openjdk:17-jdk-slim

#Set the working directory in the container
WORKDIR /app

#Copy the JAR file from the build stage to container
COPY --from=build /app/target/RestMarketPlaceApp-0.0.1-SNAPSHOT.jar testapp.jar

#Set the command to run the application
CMD ["java", "-jar", "testapp.jar"]