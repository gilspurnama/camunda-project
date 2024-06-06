package com.example.workflow.dto;

import lombok.*;

import java.util.Date;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class DeploymentDto {
    private String id;
    private String name;
    private Date deploymentTime;
    private String source;
    private String tenantId;

}
