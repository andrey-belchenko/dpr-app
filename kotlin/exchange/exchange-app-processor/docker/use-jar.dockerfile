FROM timbru31/java-node:11-jre-fermium

COPY ./exchange-app-processor/build/libs /bin
WORKDIR /bin
CMD ["java","-jar","exchange-app-processor.jar"]