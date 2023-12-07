FROM public.ecr.aws/amazoncorretto/amazoncorretto:21

COPY ./target/*.jar /app.jar
ENTRYPOINT [ "java", "-jar", "/app.jar" ]