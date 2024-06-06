package com.example.workflow.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;

public class TaskDto {

    @Getter
    public static class CompleteRequest {
        @Schema(description = "key value mapping, e.g {\"deposit\":3000000,\"accounts\":[\"4398000182\",\"490222222\"]}")
        private Map<String,Object> variables;
    }

    @Getter
    public static class SetAssigneeRequest {
        private String userId;
    }

    @Getter
    public static class SetVariableRequest {
        @Schema(description = "key value mapping, e.g {\"deposit\":3000000,\"accounts\":[\"4398000182\",\"490222222\"]}")
        private Map<String,Object> variablesLocal;
        @Schema(description = "key value mapping, e.g {\"deposit\":3000000,\"accounts\":[\"4398000182\",\"490222222\"]}")
        private Map<String,Object> variables;
    }


    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Response{
        private String id;
        private String assignedUser;
        private boolean isSuspended;
        private String processDefinitionId;
        private String processInstanceId;
        private List<Activity> activities;
        private Map<String,Object> variables;
        private Map<String,Object> localVariables;
    }


    @Getter
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Activity{
        private String id;
        private String name;
    }

}
