FROM openjdk:11-jre
COPY ./exchange-app-con-rabbit-incoming/build/libs /bin
WORKDIR /bin
CMD ["java","-jar","exchange-app-con-rabbit-incoming.jar"]