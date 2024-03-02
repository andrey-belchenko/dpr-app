FROM openjdk:11-jdk as BUILD

COPY . /src
WORKDIR /src
# CMD ls
RUN ./gradlew --no-daemon exchange-sandbox:jar

FROM openjdk:11-jre
COPY --from=BUILD /src/exchange-sandbox/build/libs /bin
WORKDIR /bin
CMD ["java","-jar","exchange-sandbox.jar"]



