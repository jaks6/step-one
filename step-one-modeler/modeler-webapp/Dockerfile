FROM adoptopenjdk/openjdk8:alpine-slim
RUN mkdir /opt/app
ADD https://github.com/jaks6/step-one/releases/download/v0.7/flowable-modeler.war /opt/app

RUN apk add --update ttf-dejavu

COPY wait-for-something.sh .
RUN chmod +x wait-for-something.sh
ENTRYPOINT ["java", "-jar", "/opt/app/flowable-modeler.war"]
