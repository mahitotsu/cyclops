FROM public.ecr.aws/docker/library/maven:3.9.6-amazoncorretto-21-al2023

ARG user 
USER ${user}

ENV MAVEN_HOME=/var/maven

VOLUME [ "/var/maven", "/workdir" ]
WORKDIR /workdir

EXPOSE 9080
EXPOSE 9443

CMD [ "mvn", "liberty:run", "-Duser.home=/var/maven" ]