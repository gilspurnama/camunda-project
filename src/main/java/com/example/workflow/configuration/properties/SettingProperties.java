package com.example.workflow.configuration.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "setting")
public class SettingProperties {

    private Email email;

    @Data
    public static class Email{

        private String apiKey;

    }
}
