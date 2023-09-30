package com.aiskov.jointables.config;


import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.ConstructorBinding;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;

@Getter
@Validated
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {
    @NotBlank
    private String appName;

    @NotBlank
    private String bootstrapServers;

    @NotBlank
    private String schemaRegistryUrl;

    private Map<String, String> properties = new HashMap<>();
}
