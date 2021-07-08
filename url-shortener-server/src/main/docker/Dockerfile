FROM openjdk:8-alpine
RUN apk add --update curl && rm -rf /var/cache/apk/*
ADD url-shortener-server.jar url-shortener-server.jar
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom", "-jar","/url-shortener-server.jar"]
