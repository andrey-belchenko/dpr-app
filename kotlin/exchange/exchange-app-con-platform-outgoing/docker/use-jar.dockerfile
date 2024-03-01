FROM openjdk:11-jre
COPY ./exchange-app-con-platform-outgoing/build/libs /bin
WORKDIR /bin
CMD ["java","-jar","exchange-app-con-platform-outgoing.jar"]