package com.aiskov.jointables.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Component;

import static org.apache.kafka.streams.KafkaStreams.State.ERROR;

@Slf4j
@Component
public class StreamStateListener {

    @Autowired
    void configure(StreamsBuilderFactoryBean factoryBean) {
        factoryBean.setKafkaStreamsCustomizer(kafkaStreams -> {
            kafkaStreams.setStateListener((newState, oldState) -> {
                log.info("Kafka Streams state changed from {} to {}", oldState, newState);

                if (newState == ERROR) {
                    log.error("Kafka Streams state is {} - application will be terminated", newState);
                    System.exit(1);
                }
            });
        });
    }
}
