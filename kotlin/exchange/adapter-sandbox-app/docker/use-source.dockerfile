FROM openjdk:11-jdk as BUILD

COPY . /src
WORKDIR /src
# CMD ls
RUN ./gradlew --no-daemon adapter-sandbox-app:jar

FROM openjdk:11-jre
COPY --from=BUILD /src/adapter-sandbox-app/build/libs /bin
WORKDIR /bin
CMD ["java","-jar","adapter-sandbox-app.jar"]



