package com.example.workflow.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

public class ProcessInstanceVariableDto {

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Response{
        private final String executionId;
        private final String activityId;
        private final String activityName;
        private final List<Variable> variables;
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class Variable{
        private final String name;
        private final Object value;
    }
}
