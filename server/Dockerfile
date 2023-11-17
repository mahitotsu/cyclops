FROM public.ecr.aws/docker/library/maven:3-amazoncorretto-17 as builder

WORKDIR /workdir
COPY pom.xml .
RUN mvn dependency:resolve-plugins dependency:resolve
COPY src/ ./src/
RUN mvn package -DfinalName=app

# -----
FROM public.ecr.aws/amazoncorretto/amazoncorretto:17

COPY --from=builder /workdir/target/app.jar .
ENTRYPOINT [ "java", "-jar", "app.jar" ]