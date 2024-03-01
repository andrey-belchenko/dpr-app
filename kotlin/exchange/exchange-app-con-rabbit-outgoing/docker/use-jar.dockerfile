FROM openjdk:11-jre
COPY ./exchange-app-con-rabbit-outgoing/build/libs /bin
WORKDIR /bin
CMD ["java","-jar","exchange-app-con-rabbit-outgoing.jar"]