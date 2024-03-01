FROM openjdk:11-jdk as BUILD

COPY . /src
WORKDIR /src
# CMD ls
RUN ./gradlew --no-daemon exchange-app-con-rabbit-outgoing:jar

FROM openjdk:11-jre
COPY --from=BUILD /src/exchange-app-con-rabbit-outgoing/build/libs /bin
WORKDIR /bin
CMD ["java","-jar","exchange-app-con-rabbit-outgoing.jar"]



