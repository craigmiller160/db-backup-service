FROM openjdk:18.0.2-jdk-buster

RUN sh -c 'echo "deb http://apt.postgresql.org/pub/repos/apt buster-pgdg main" > /etc/apt/sources.list.d/pgdg.list'
RUN wget --quiet -O - https://www.postgresql.org/media/keys/ACCC4CF8.asc | apt-key add -
RUN apt update
RUN apt install postgresql-client-12 -y

RUN mkdir /mongotools
COPY mongotools/mongodump /mongotools

COPY build/db-backup-service-*.jar /db-backup-service.jar

ENTRYPOINT ["java", "-jar", "db-backup-service.jar"]