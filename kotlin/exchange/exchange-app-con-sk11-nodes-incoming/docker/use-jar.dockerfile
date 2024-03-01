FROM openjdk:11-jre
COPY ./exchange-app-con-sk11-nodes-incoming/build/libs /bin
WORKDIR /bin
CMD ["java","-jar","exchange-app-con-sk11-nodes-incoming.jar"]