# Practice: Using Kafka Connect for Data Extraction

That repo is used to practice Kafka Streams & Kafka Connect. Here I would 
expose working configuration of an environment, instruction how to run
simple test on it, suggestion of practice tasks and example of the solution. 

<!-- TOC -->
* [Practice: Using Kafka Connect for Data Extraction](#practice-using-kafka-connect-for-data-extraction)
  * [Exercises](#exercises)
  * [Description of the Example](#description-of-the-example)
  * [How to start](#how-to-start)
    * [Prerequisites](#prerequisites)
    * [Prepare environment](#prepare-environment)
      * [1. Download plugins for Kafka connect](#1-download-plugins-for-kafka-connect)
      * [2. Install Python Environment](#2-install-python-environment)
      * [3. Run environment](#3-run-environment)
      * [4. Configure Kafka Connect](#4-configure-kafka-connect)
      * [5. Check Event Publishing](#5-check-event-publishing-)
      * [6. Run Kafka Streams](#6-run-kafka-streams)
  * [References](#references)
<!-- TOC -->

## Exercises

Here are several ideas of task that could be used for practice. 

1. Join data from client table
2. Create event of status change
3. Configure backups of events
4. Configure live synchronization with another database
5. Configure reindexing of events

## Description of the Example

> TBD: Here should be the database description and diagram of the initial
> stream processing.

## How to start

### Prerequisites

1. Docker & Docker Compose
2. Java 17+
3. Python 3 + PIP

### Prepare environment

#### 1. Download plugins for Kafka connect

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

#### 2. Install Python Environment

For that demo, python used to simplify data generation. Because it 
allows executing requests to database with less amount of code. 

> TBD
> Generally you need python 3 with mysql connector. I will describe how 
> to install python venv with it later. 

#### 3. Run environment

To run environment, you should run docker compose, from the repository's 
root directory.

```bash
docker compose up -d
```

It could take several minutes to download all images and start them all. 

#### 4. Configure Kafka Connect

I prepared a list of requests that could be helpful to work with 
Kafka Connect and Schema Registry. It could be found in the file 
`platform.http`.

To start, you need to create connectors that extract data from the MySQL. To do 
that, execute the following requests:

* `### [Connect] Create connector for invoices`
* `### [Connect] Create connector for invoice items`

Then you may check the connector status.

* `### [Connect] Connector status - invoices-source`
* `### [Connect] Connector status - invoice-items-source`

You should get something like:

```json
{
  "name": "invoices-source",
  "connector": {
    "state": "RUNNING",
    "worker_id": "connect:8083"
  },
  "tasks": [
    {
      "id": 0,
      "state": "RUNNING",
      "worker_id": "connect:8083"
    }
  ],
  "type": "source"
}
```

#### 5. Check Event Publishing 

To check that events are published, we need to generate some data. It 
could be done using the python script.

```bash
python data-generator/data-generator.py 
```

Then you could use console consumer from schema-registry container to 
select check that some events are published.

```bash
docker exec -it schema-registry /bin/bash
kafka-avro-console-consumer --bootstrap-server broker:29092 --topic source-mysql-invoice-item --from-beginning
```

You also should be available to get event schema from the schema registry.

* `### [Schema Registry] Get invoice schema`
* `### [Schema Registry] Get invoice item schema`

#### 6. Run Kafka Streams

We will use Specific Avro Records in our Kafka Streams application. It requires
to generate Java classes from Avro schemas. To do that, you need to do the following:

1. Download Avro schemas from the Schema Registry (you also may do that manually using 
`platform.http` file)
```bash
wget http://localhost:8081/subjects/source-mysql-invoice-value/versions/-1/schema -O stream/src/main/avro/Invoice.avsc
wget http://localhost:8081/subjects/source-mysql-invoice-item-value/versions/-1/schema -O stream/src/main/avro/InvoiceItem.avsc
```

Then you should be able to generate Java classes using the following command:

```bash
./gradlew generateAvroJava
```

Classes will be generated in the `stream/build/generated-main-avro-java`.

Finally, you could run Kafka Streams application using the following command:

```bash
./gradlew bootRun
```

In logs, you should see the following:

```text

```

## References

* https://developer.confluent.io/courses/spring/hands-on-cloud-schema-registry/