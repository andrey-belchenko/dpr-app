FROM openjdk:11-jre
COPY ./exchange-app-mail/build/libs /bin
WORKDIR /bin
CMD ["java","-jar","exchange-app-mail.jar"]