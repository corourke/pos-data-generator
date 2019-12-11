FROM openjdk:8

WORKDIR /usr/local/src

COPY ./target/pos-data-generator-1.0.jar ./

ENTRYPOINT exec java -jar /usr/local/src/pos-data-generator-1.0.jar /
    -Dcom.sun.management.jmxremote.port=9999 \
    -Dcom.sun.management.jmxremote.authenticate=false \
    -Dcom.sun.management.jmxremote.ssl=false \
    ScanDataGenerator
