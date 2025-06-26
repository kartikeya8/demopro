FROM kartikeyasoft/jdk17_onlineexam:v1.0.2 AS builder_image
RUN ls
RUN git clone https://github.com/ksrepo9/ksk8spro.git
RUN cd /ksapp/ksk8spro/service2 && mvn install 

FROM kartikeyasoft/jdk17:latest
COPY --from=builder_image /ksapp/ksk8spro/service2/target/*.jar ks.jar
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-jar", "ks.jar"]