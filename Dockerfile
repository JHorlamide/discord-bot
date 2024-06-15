# Use an official OpenJDK runtime as a parent image
FROM openjdk:11

# Set the working directory in the container
WORKDIR /app

# Copy the build.gradle and settings.gradle files
COPY build.gradle settings.gradle /app/

# Copy the gradle directory
COPY  gradle /app/gradle

# Copy the gradlew files
COPY gradlew /app/
COPY gradlew.bat /app/

# Copy the source code
COPY src /app/src

# Install dependencies and build the application
RUN ./gradlew clean build

# Copy the built jar file to the container
COPY build/libs/*.jar /app/app.jar

# Run the application
ENTRYPOINT ["java", "-jar", "/app/app.jar"]