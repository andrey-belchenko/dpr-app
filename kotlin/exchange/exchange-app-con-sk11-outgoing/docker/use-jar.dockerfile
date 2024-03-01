FROM openjdk:11-jre
COPY ./exchange-app-con-sk11-outgoing/build/libs /bin
WORKDIR /bin
CMD ["java","-jar","exchange-app-con-sk11-outgoing.jar"]