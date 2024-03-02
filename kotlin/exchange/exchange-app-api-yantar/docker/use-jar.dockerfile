FROM registry.access.redhat.com/ubi8/openjdk-11:1.11

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en'

# Install python
USER root
RUN microdnf install python3 -y && microdnf clean all

# Install pip
RUN microdnf install python3-pip -y && microdnf clean all
# Update pip
RUN pip3 install --upgrade pip
# Install libs
RUN pip3 install pymongo
RUN pip3 install psycopg2-binary

# Install MongoDB Shell (mongosh)
RUN curl -o mongodb-shell.rpm https://downloads.mongodb.com/compass/mongodb-mongosh-2.1.1.x86_64.rpm && \
    rpm -ivh mongodb-shell.rpm && \
    rm mongodb-shell.rpm

# We make four distinct layers so if there are application changes the library layers can be re-used
COPY --chown=185 ./exchange-app-api-yantar/build/quarkus-app/lib/ /deployments/lib/
COPY --chown=185 ./exchange-app-api-yantar/build/quarkus-app/*.jar /deployments/
COPY --chown=185 ./exchange-app-api-yantar/build/quarkus-app/app/ /deployments/app/
COPY --chown=185 ./exchange-app-api-yantar/build/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185
ENV AB_JOLOKIA_OFF=""
ENV JAVA_OPTS="-Dfile.encoding=UTF-8 -Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager"
ENV JAVA_APP_JAR="/deployments/quarkus-run.jar"
