FROM openjdk:11-jdk as BUILD

COPY . /src
WORKDIR /src
# CMD ls
RUN ./gradlew --no-daemon exchange-app-con-platform-outgoing:jar

FROM openjdk:11-jre
COPY --from=BUILD /src/exchange-app-con-platform-outgoing/build/libs /bin
WORKDIR /bin
CMD ["java","-jar","exchange-app-con-platform-outgoing.jar"]



