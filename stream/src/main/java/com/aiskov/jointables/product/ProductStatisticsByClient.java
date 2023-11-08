package com.aiskov.jointables.product;

import com.aiskov.InvoiceAggregate;
import com.aiskov.StatisticsByClientAndProduct;
import com.aiskov.jointables.config.KafkaConfig.SerdeProvider;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductStatisticsByClient {
    private final String AGGREGATE_INVOICE_TOPIC = "invoices-event-log";
    private final String STATISTICS_TOPIC = "statistics-by-client-and-product";

    private final SerdeProvider serdeProvider;

    //@Autowired
    void topology(StreamsBuilder topology) {
        SpecificAvroSerde<InvoiceAggregate> invoiceSerde = this.serdeProvider.get();

        topology.table(AGGREGATE_INVOICE_TOPIC, Consumed.with(Serdes.String(), invoiceSerde))
                .toStream()
                .peek((key, value) -> log.info("Processing statistic for invoice: invoice event {}: {}.", key, value))
                .flatMap((key, value) -> this.convertToStatistics(value))
                .toTable()
                .toStream((key, value) -> KeyValue.pair(value.getClientId() + "-" + value.getProductName(), value))
                .groupByKey()
                .reduce((aggValue, newValue) -> {
                    aggValue.setQuantity(aggValue.getQuantity() + newValue.getQuantity());
                    aggValue.setTotal(aggValue.getTotal() + newValue.getTotal());
                    return aggValue;
                })
                .toStream()
                .peek((key, value) -> log.info("Statistics updated for {}: {}.", key, value))
                .to(STATISTICS_TOPIC);
    }

    private List<KeyValue<String, StatisticsByClientAndProduct>> convertToStatistics(InvoiceAggregate invoice) {
        return invoice.getItems().stream()
                .map(item -> KeyValue.pair(
                        item.getId(),
                        StatisticsByClientAndProduct.newBuilder()
                                .setClientId(invoice.getClient().getId())
                                .setClientName(invoice.getClient().getName())
                                .setProductName(item.getName())
                                .setQuantity(item.getQuantity())
                                .setTotal(item.getTotal())
                                .build()
                ))
                .toList();
    }
}
