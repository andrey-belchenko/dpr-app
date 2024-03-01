FROM openjdk:11-jre
COPY ./exchange-app-con-meter-incoming/build/libs /bin
WORKDIR /bin
CMD ["java","-jar","exchange-app-con-meter-incoming.jar"]