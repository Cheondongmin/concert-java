FROM khipu/openjdk17-alpine:latest AS builder

WORKDIR /usr/src/app

FROM khipu/openjdk17-alpine:latest
COPY ../build/libs/concert-0.0.1-SNAPSHOT.jar ./concert-api.jar
EXPOSE 8080
RUN ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime
CMD ["java","-jar","concert-api.jar"]

# hi!
#docker build -t obt-api .
#docker save obt-api > obt-api.tar