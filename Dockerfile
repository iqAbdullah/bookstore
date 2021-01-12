FROM openjdk:8-jre-alpine

RUN rm -rf /src
WORKDIR /src
COPY . /src
EXPOSE 8080
	
CMD ["java", "-jar", "-Dserver.port=8080", "-Dspring.profiles.active=docker" , "target/book-store-0.0.1-SNAPSHOT.jar"]