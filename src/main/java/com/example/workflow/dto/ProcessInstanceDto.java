package com.example.workflow.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;

public class ProcessInstanceDto {

    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Response{
        private final String id;
        private final String processDefinitionKey;
        private final String processDefinitionName;
        private final String status;
        private final String businessKey;
        private final Date startTime;
        private final Date endTime;
        private final String userId;
        private final String taskId;
        private final String processDefinitionId;
    }

}
