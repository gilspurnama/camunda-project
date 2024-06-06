package com.example.workflow.model;

import lombok.Data;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "error_log")
public class ErrorLog {

    @Id
    @GeneratedValue(generator = "uuid")
    @GenericGenerator(name = "uuid", strategy = "org.hibernate.id.UUIDGenerator")
    private String id;

    @Column(name = "tenant_id")
    private String tenantId;

    @Column(name = "event_name")
    private String evetName;

    @Column(name = "definition_id")
    private String definitionId;

    @Column(name = "instance_id")
    private String instanceId;

    @Column(name = "activity_id")
    private String activityId;

    @Column(name = "activity_name")
    private String activityName;

    @Column(name = "error_code")
    private String errorCode;

    @Column(name = "code")
    private Integer code;

    @Column(name = "message")
    private String message;

    @Column(name = "type",columnDefinition = "text")
    private String type;

    @Column(name = "task_id")
    private String taskId;

    @Column(name = "request_method")
    private String requestMethod;

    @Column(name = "request_body", columnDefinition = "text")
    private String requestBody;

    @Column(name = "request_path")
    private String requestPath;

    @Column(name = "request_header", columnDefinition = "text")
    private String requestHeader;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

}
