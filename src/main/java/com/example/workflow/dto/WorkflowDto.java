package com.example.workflow.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Date;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class WorkflowDto {
    private String id;
    private String key;
    private String name;
    private String description;
    private String versionTag;
    private Integer version;
    private String category;
    private String deploymentId;
    private String diagramResourceName;
    private Integer historyTimeToLive;
    private String resourceName;
    private String tenantId;

}
