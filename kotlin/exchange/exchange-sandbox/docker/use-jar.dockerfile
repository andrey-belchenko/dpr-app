FROM openjdk:11-jre
COPY ./exchange-sandbox/build/libs /bin
WORKDIR /bin
CMD ["java","-jar","exchange-sandbox.jar"]