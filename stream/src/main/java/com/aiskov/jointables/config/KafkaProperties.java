package com.aiskov.jointables.config;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.Map;

@Data
@Validated
@ConfigurationProperties(prefix = "kafka")
public class KafkaProperties {
    @NotBlank
    private String appName;

    @NotBlank
    private String bootstrapServers;

    @NotBlank
    private String schemaRegistryUrl;

    @NotNull
    private Boolean alwaysResetOffset;

    private Map<String, String> properties = new HashMap<>();
}
