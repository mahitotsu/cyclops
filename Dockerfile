FROM public.ecr.aws/docker/library/maven:3.9.6-amazoncorretto-21-al2023

RUN yum -y update &&\
    yum -y upgrade &&\
    yum -y install npm

VOLUME [ "/var/maven/.m2", "/.npm", "/workdir" ]
ENV MAVEN_CONFIG=/var/maven/.m2

USER 1001
WORKDIR /workdir

EXPOSE 9080
EXPOSE 9443

CMD ["mvn", "clean", "package", "liberty:run", "-Duser.home=/var/maven"]