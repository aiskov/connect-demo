package com.aiskov.jointables.config;

import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;

import java.util.HashMap;
import java.util.Map;

import static io.confluent.kafka.serializers.AbstractKafkaSchemaSerDeConfig.SCHEMA_REGISTRY_URL_CONFIG;
import static java.util.Map.entry;
import static org.apache.kafka.streams.StreamsConfig.APPLICATION_ID_CONFIG;
import static org.apache.kafka.streams.StreamsConfig.BOOTSTRAP_SERVERS_CONFIG;

@Slf4j
@EnableKafka
@Configuration
@EnableKafkaStreams
public class KafkaConfig {

    @Bean(name = KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    KafkaStreamsConfiguration kafkaStreamsConfig(KafkaProperties kafkaProps) {
        Map<String, Object> props = new HashMap<>(Map.ofEntries(
                entry(APPLICATION_ID_CONFIG, "streams-app"),
                entry(BOOTSTRAP_SERVERS_CONFIG, kafkaProps.getBootstrapServers())
        ));
        props.putAll(kafkaProps.getProperties());

        return new KafkaStreamsConfiguration(props);
    }

    @Autowired
    void configure(StreamsBuilderFactoryBean factoryBean) {
        factoryBean.setKafkaStreamsCustomizer(kafkaStreams -> {
            kafkaStreams.setStateListener((newState, oldState) -> {
                log.info("Kafka Streams state changed from {} to {}", oldState, newState);

                if (newState == org.apache.kafka.streams.KafkaStreams.State.ERROR) {
                    log.error("Kafka Streams state is {} - application will be terminated", newState);
                    System.exit(1);
                }
            });
        });
    }

    @Bean
    SerdeProvider serdeProvider(KafkaProperties kafkaProps) {
        Map<String, Object> props = new HashMap<>();
        props.put(SCHEMA_REGISTRY_URL_CONFIG, kafkaProps.getSchemaRegistryUrl());
        props.putAll(kafkaProps.getProperties());

        return SerdeProvider.of(kafkaProps.getProperties());
    }

    @RequiredArgsConstructor(staticName = "of")
    public static class SerdeProvider {
        private final Map<String, String> properties;

        public <T extends org.apache.avro.specific.SpecificRecord> SpecificAvroSerde<T> get(Class<T> clazz) {
            SpecificAvroSerde<T> serde = new SpecificAvroSerde<>();
            serde.configure(properties, false);
            return serde;
        }
    }

}
