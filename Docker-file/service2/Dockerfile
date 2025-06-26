FROM kartikeyasoft/jdk17_onlineexam:v1.0.2 AS builder_image
RUN git clone https://github.com/kartikeya8/demopro.git
RUN cd /ksapp/demopro/service2 && mvn install 

FROM kartikeyasoft/jdk17:latest
COPY --from=builder_image /ksapp/demopro/service2/target/*.jar ks.jar
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "ks.jar"]