package com.aiskov.jointables.invoice;

import com.aiskov.Invoice;
import com.aiskov.jointables.config.KafkaConfig;
import com.aiskov.jointables.config.KafkaConfig.SerdeProvider;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JoinInvoiceData {
    private final String SOURCE_INVOICE_TOPIC = "source-mysql-invoice";
    private final String SOURCE_INVOICE_ITEM_TOPIC = "source-mysql-invoice-item";

    private final String OUTPUT_TOPIC = "invoices-event-log";

//    @Value(DEFAULT_DISCO_EVENTS_INPUT_TOPIC_REF)
//    private final String discoEventsInputTopic;
//    @Value(DEFAULT_DISCO_EVENTS_OUTPUT_TOPIC_REF)
//    private final String discoEventsOutputTopic;

    private final SerdeProvider serdeProvider;

    @Autowired
    void topology(StreamsBuilder topology) {
        Consumed<String, Invoice> inputSerde = Consumed.with(
                Serdes.String(),
                this.serdeProvider.get(Invoice.class)
        );

        Consumed<String, InvoiceItem> inputItemSerde = Consumed.with(
                Serdes.String(),
                this.serdeProvider.get(Invoice.class)
        );

        Consumed<String, Invoice> inputItemSerde = Consumed.with(
                Serdes.String(),
                this.serdeProvider.get(Invoice.class)
        );



        //Produced<String, > outputSerde = Produced.with(Serdes.String(), this.discoEventsValueOutputSerde);

        topology.stream(SOURCE_INVOICE_TOPIC, inputSerde)
                .to(this.discoEventsOutputTopic, outputSerde);
    }
//    private @NonNull List<KeyValue<String, SampleSourceChangeEventPayload>> split(@Nullable String keyIgnored, @NonNull BulkSampleSourceChangeEvent value) {
//        log.info("Splitting event {} to {}.", value.getEventId(), value.getPayload().size());
//        return value.getPayload().stream()
//                .map(payload -> KeyValue.pair(payload.getAnimalId(), payload))
//                .collect(toList());
//    }
}
