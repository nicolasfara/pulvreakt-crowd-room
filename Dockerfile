FROM gradle:8.2.1-jdk17-jammy AS build

WORKDIR /build-gradle/
COPY . .
RUN ./gradlew generateJars

FROM eclipse-temurin:17.0.7_7-jre

WORKDIR /root/
COPY --from=build /build-gradle/build/libs/* ./

RUN apt-get update && apt-get install -y netcat

COPY .docker/* ./
RUN chmod +x *.sh

ENTRYPOINT ["./entrypoint.sh"]
