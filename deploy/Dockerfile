FROM openjdk:21-buster

RUN sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt buster-pgdg main" > /etc/apt/sources.list.d/pgdg.list' \
    && wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | apt-key add - \
    && apt update \
    && apt install postgresql-client-14 -y

RUN mkdir /mongotools
COPY mongotools/* /mongotools
ENV PATH="/mongotools:${PATH}"

COPY build/db-backup-service-*.jar /db-backup-service.jar

ENTRYPOINT ["java", "-jar", "db-backup-service.jar"]