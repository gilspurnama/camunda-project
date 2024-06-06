package com.example.workflow.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "acls")
public class AclsProperties {

    private String server;

    private String apiKey;

}
