## Download plugins

You need to download the following plugins:

- [confluent.io/hub/confluentinc/kafka-connect-jdbc](https://www.confluent.io/hub/confluentinc/kafka-connect-jdbc)

Also for JDBC connector, you need to download JDBC driver for your database. For example, for MySQL you can download
it from here: [dev.mysql.com/downloads/connector/j/](https://dev.mysql.com/downloads/connector/j/)

Finally, you should get the following files:

```
connect-plugins/
├── confluentinc-kafka-connect-jdbc-10.7.4/
connect-libs/
├── mysql-connector-java-8.0.26.jar
```

## Run 

```bash
docker exec -it schema-registry /bin/bash
kafka-avro-console-consumer --bootstrap-server broker:29092 --topic source-mysql-invoice-item --from-beginning
```