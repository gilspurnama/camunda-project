package com.example.workflow.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import javax.validation.constraints.NotBlank;
import java.util.Map;

public class WebhookDto {

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Response{
        private String id;
        private String name;
        private String url;
        private Map<String,String> header;
        private String content;
        private String contentChecksum;
        private String tenantId;
        private String method;
        private String processDefinitionId;
    }

    @Data
    public static class Request{
        @NotBlank(message = "name is required")
        @Schema(description = "use to easily remember by human")
        private String name;
        @NotBlank(message = "url is required")
        @Schema(description = "url which will be called")
        private String url;
        @NotBlank(message = "method is required")
        @Schema(description = "request method of targeted url")
        private String method;
        @Schema(description = "http header which will be populated when calling url")
        private Map<String,String> header;
        @Schema(description = "request payload, using mustache features")
        @NotBlank(message = "content is required")
        private String content;
        @Schema(description = "use to filter webhook, only interested on a specific tenantId")
        private String tenantId;
        @Schema(description = "use to filter webhook, only interested on a specific processDefinitionId")
        private String processDefinitionId;
    }
}
