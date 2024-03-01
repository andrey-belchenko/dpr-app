FROM openjdk:11-jre
COPY ./adapter-sandbox-app/build/libs /bin
WORKDIR /bin
CMD ["java","-jar","adapter-sandbox-app.jar"]