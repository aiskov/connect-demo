package com.aiskov.jointables.invoice;

import com.aiskov.Invoice;
import com.aiskov.InvoiceAggregate;
import com.aiskov.InvoiceAggregateItem;
import com.aiskov.InvoiceItem;
import com.aiskov.jointables.config.KafkaConfig.SerdeProvider;
import io.confluent.kafka.streams.serdes.avro.SpecificAvroSerde;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.JoinWindows;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.StreamJoined;
import org.apache.kafka.streams.kstream.TimeWindows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@RequiredArgsConstructor
public class JoinInvoiceData {
    private final String SOURCE_INVOICE_TOPIC = "source-mysql-invoice";
    private final String SOURCE_INVOICE_ITEM_TOPIC = "source-mysql-invoice-item";
    private final String OUTPUT_TOPIC = "invoices-event-log";

    private final SerdeProvider serdeProvider;

    @Autowired
    void topology(StreamsBuilder topology) {
        SpecificAvroSerde<Invoice> invoiceSerde = this.serdeProvider.get();
        SpecificAvroSerde<InvoiceItem> invoiceItemSerde = this.serdeProvider.get();

        SpecificAvroSerde<InvoiceAggregate> resultSerde = this.serdeProvider.get();

        topology.stream(SOURCE_INVOICE_TOPIC, Consumed.with(Serdes.String(), invoiceSerde))
                .peek((key, value) -> log.info("Processing start: invoice event {}: {}.", key, value))
                .mapValues(this::convertToAggregate)
                .leftJoin(
                        topology.stream(SOURCE_INVOICE_ITEM_TOPIC, Consumed.with(Serdes.String(), invoiceItemSerde))
                                .peek((key, value) -> log.info("Invoice item event {}: {}.", key, value)),
                        this::addItemToInvoice,
                        JoinWindows.ofTimeDifferenceWithNoGrace(Duration.ofSeconds(1)),
                        StreamJoined.with(Serdes.String(), resultSerde, invoiceItemSerde)
                )
                .peek((key, value) -> log.info("Invoice item match to invoice {}: {}.", key, value))
                .groupByKey()
                .windowedBy(TimeWindows.ofSizeWithNoGrace(Duration.ofSeconds(1)))
                .reduce(
                        (left, right) -> {
                            if (left.getItems() == null || left.getItems().isEmpty()) return right;
                            if (right.getItems() == null || right.getItems().isEmpty()) return left;

                            left.getItems().add(right.getItems().get(0));
                            return left;
                        },
                        Materialized.with(Serdes.String(), resultSerde)
                )
                .toStream()
                .selectKey((key, value) -> key.key())
                .peek((key, value) -> log.info("Processing finished: invoice event {}: {}.", key, value))
                .to(OUTPUT_TOPIC, Produced.with(Serdes.String(), resultSerde));
    }

    private InvoiceAggregate addItemToInvoice(InvoiceAggregate invoice, InvoiceItem invoiceItem) {
        if (invoiceItem == null) return invoice;

        invoice.getItems().add(
                InvoiceAggregateItem.newBuilder()
                        .setId(invoice.getId())
                        .setName(invoiceItem.getName())
                        .build()
        );

        return invoice;
    }

    private InvoiceAggregate convertToAggregate(Invoice value) {
        return InvoiceAggregate.newBuilder()
                .setId(value.getId())
                .setCode(value.getCode())
                .setCreatedAt(value.getCreatedAt())
                .setLastUpdatedAt(value.getLastUpdatedAt())
                .build();
    }
}
