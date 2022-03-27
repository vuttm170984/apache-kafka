# apache-kafka
$ ./mvnw clean
$ ./mvnw package -DskipTests=true
$ java -jar -Dserver.port=8082 target/lib-events-consumer-0.0.1-SNAPSHOT.jar
