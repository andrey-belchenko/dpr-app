FROM openjdk:11-jre
COPY ./adapter-sandbox-app-sk-ping/build/libs /bin
WORKDIR /bin
CMD ["java","-jar","adapter-sandbox-app-sk-ping.jar"]