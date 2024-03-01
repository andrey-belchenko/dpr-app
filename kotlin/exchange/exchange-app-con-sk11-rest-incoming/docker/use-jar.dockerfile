FROM openjdk:11-jre
COPY ./exchange-app-con-sk11-rest-incoming/build/libs /bin
WORKDIR /bin
CMD ["java","-jar","exchange-app-con-sk11-rest-incoming.jar"]