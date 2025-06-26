FROM kartikeyasoft/jdk17_onlineexam:v1.0.2 AS builder_image
RUN git clone https://github.com/kartikeya8/demopro.git
RUN cd /ksapp/demopro/server && mvn install

FROM kartikeyasoft/jdk17:latest
COPY --from=builder_image /ksapp/demopro/server/target/*.jar ks.jar
EXPOSE 8761
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "ks.jar"]