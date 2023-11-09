FROM public.ecr.aws/amazonlinux/amazonlinux:2023 as builder
RUN yum -y update
RUN yum -y install maven npm

WORKDIR /workdir
COPY pom.xml .
RUN mvn dependency:resolve-plugins dependency:resolve
COPY src/ ./src/
RUN mvn package -DfinalName=app

# -----
FROM public.ecr.aws/amazoncorretto/amazoncorretto:17-al2023-headless

COPY --from=builder /workdir/target/app.jar /app.jar
ENTRYPOINT [ "java", "-jar", "/app.jar" ]