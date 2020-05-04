FROM maven:latest

MAINTAINER Tanner Purves <tannerpurves@u.boisestate.edu>

EXPOSE 1099
EXPOSE 5005

RUN mkdir /cs455-p3
COPY ./mysecurity.policy /cs455-p3
COPY ./target /cs455-p3
COPY ./resources /cs455-p3/resources
WORKDIR /cs455-p3
ENTRYPOINT ["java","-cp", "p3-1.0-SNAPSHOT-jar-with-dependencies.jar","-Djava.security.policy=./mysecurity.policy","IdServer","--numport","1099","--verbose"]