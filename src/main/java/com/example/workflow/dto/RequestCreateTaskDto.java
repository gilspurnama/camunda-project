package com.example.workflow.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestCreateTaskDto {
    private String taskName;
    private String taskTenantId;
    private String taskDescription;
    private String taskAssignee;
}
